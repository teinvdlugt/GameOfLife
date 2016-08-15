package com.teinproductions.tein.gameoflife.patterns;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teinproductions.tein.gameoflife.R;
import com.teinproductions.tein.gameoflife.db.DefaultPatternContract;
import com.teinproductions.tein.gameoflife.db.DefaultPatternDbHelper;
import com.teinproductions.tein.gameoflife.files.FileReaderActivity;
import com.teinproductions.tein.gameoflife.files.Life;
import com.teinproductions.tein.gameoflife.files.LifeOneOFiveInterpreter;
import com.teinproductions.tein.gameoflife.files.LifeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class DownloadActivity extends AppCompatActivity implements PatternAdapter.OnPatternClickListener,
        NoPatternsAdapter.OnDownloadClickListener {

    private List<String> fileNames = new ArrayList<>();
    private List<String> patternNames = new ArrayList<>();

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getPatternFileNames();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (fileNames.size() > 0) {
            PatternAdapter adapter = new PatternAdapter(patternNames, fileNames, this, this);
            recyclerView.setAdapter(adapter);
        } else {
            NoPatternsAdapter adapter = new NoPatternsAdapter(this, this);
            recyclerView.setAdapter(adapter);
        }
    }

    private void getPatternFileNames() {
        try {
            fileNames.clear();
            patternNames.clear();

            SQLiteDatabase db = new DefaultPatternDbHelper(this).getReadableDatabase();
            Cursor c = db.query(
                    DefaultPatternContract.DefaultPatternEntry.TABLE_NAME,
                    new String[]{DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_URL,
                            DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_NAME},
                    null, null, null, null, null);

            if (c.moveToFirst()) {
                int urlColumnIndex = c.getColumnIndex(DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_URL);
                int nameColumnIndex = c.getColumnIndex(DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_NAME);
                do {
                    String url = c.getString(urlColumnIndex);
                    String name = c.getString(nameColumnIndex);
                    fileNames.add(url);
                    patternNames.add(name == null ? url : name);
                } while (c.moveToNext());
            }

            c.close();
        } catch (SQLiteException | IndexOutOfBoundsException e) {
            fileNames.clear();
            patternNames.clear();
            e.printStackTrace();
        }
    }

    @Override
    public void onClickPattern(final String file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.downloading_progress_dialog_message));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        new AsyncTask<Void, Void, Life>() {
            @Override
            protected Life doInBackground(Void... params) {
                final String url = "http://gameoflife.netau.net/patterns/" + file;
                String file;
                try {
                    file = downloadFile(url);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

                publishProgress();

                Life life = LifeOneOFiveInterpreter.parse(file);
                LifeUtils.documentCells(life.getCells());
                return life;
            }

            private String downloadFile(String urlStr) throws IOException {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(20000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int responseCode = conn.getResponseCode();

                if (responseCode < 200 || responseCode >= 400)
                    throw new IOException("response code was " + responseCode);

                InputStream is = conn.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                BufferedReader bufferedReader = new BufferedReader(reader);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                is.close();
                conn.disconnect();
                return sb.toString();
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                ((TextView) dialog.findViewById(android.R.id.message)).setText(R.string.parsing_progress_dialog_message);
            }

            @Override
            protected void onPostExecute(Life life) {
                try {
                    dialog.dismiss();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                Intent data = new Intent();
                data.putExtra(FileReaderActivity.LIFE_MODEL_EXTRA, life);
                setResult(RESULT_OK, data);
                finish();
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.reload_index:
                startService(new Intent(this, IndexDownloadIntentService.class));
                return true;
            case R.id.download_names:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.download_names_dialog_title)
                        .setMessage(getString(R.string.download_names_dialog_message))
                        .setPositiveButton(R.string.begin, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startService(new Intent(DownloadActivity.this, NamesDownloadService.class));
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create().show();
            default:
                return false;
        }
    }

    @Override
    public void onClickDownload() {
        Intent intent = new Intent(this, IndexDownloadIntentService.class);
        startService(intent);
    }
}

class PatternAdapter extends RecyclerView.Adapter<PatternAdapter.ViewHolder> {
    private List<String> names, files;
    private Context context;
    private OnPatternClickListener listener;

    public interface OnPatternClickListener {
        void onClickPattern(String file);
    }

    public PatternAdapter(List<String> names, List<String> files, Context context, OnPatternClickListener listener) {
        if (names.size() != files.size())
            throw new IllegalArgumentException("names and files are not of the same size");
        this.names = names;
        this.files = files;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public PatternAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_pattern, parent, false));
    }

    @Override
    public void onBindViewHolder(PatternAdapter.ViewHolder holder, int position) {
        holder.bind(names.get(position), files.get(position));
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTV, urlTV;
        private ViewGroup itemRoot;
        private String file;

        public ViewHolder(View itemView) {
            super(itemView);

            nameTV = (TextView) itemView.findViewById(R.id.name_textView);
            urlTV = (TextView) itemView.findViewById(R.id.url_textView);
            itemRoot = (ViewGroup) itemView.findViewById(R.id.itemRoot);
            itemRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null && file != null) listener.onClickPattern(file);
                }
            });
        }

        public void bind(String name, String file) {
            nameTV.setText(name);
            urlTV.setText(file);
            this.file = file;
        }
    }
}

class NoPatternsAdapter extends RecyclerView.Adapter<NoPatternsAdapter.ViewHolder> {
    private Context context;
    private OnDownloadClickListener listener;

    public NoPatternsAdapter(Context context, OnDownloadClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_no_patterns, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {}

    @Override
    public int getItemCount() {
        return 1;
    }

    interface OnDownloadClickListener {
        void onClickDownload();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.findViewById(R.id.download_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onClickDownload();
                }
            });
        }
    }
}
