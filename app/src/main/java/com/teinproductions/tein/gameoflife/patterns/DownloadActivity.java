package com.teinproductions.tein.gameoflife.patterns;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teinproductions.tein.gameoflife.R;
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


public class DownloadActivity extends AppCompatActivity implements PatternAdapter.OnPatternClickListener {

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] names = {"Gosper Glider Gun", "Snail", "Zweiback",
                "10-engine Cordership", "Seal", "Scrubber"};
        String[] files = {"gosperglidergun_105.lif", "snail_105.lif", "zweiback_105.lif",
                "10enginecordership_105.lif", "seal_105.lif", "scrubber_105.lif"};

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PatternAdapter adapter = new PatternAdapter(names, files, this, this);
        recyclerView.setAdapter(adapter);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }
}

class PatternAdapter extends RecyclerView.Adapter<PatternAdapter.ViewHolder> {

    private String[] names, files;
    private Context context;
    private OnPatternClickListener listener;

    public interface OnPatternClickListener {
        void onClickPattern(String file);
    }

    public PatternAdapter(String[] names, String[] files, Context context, OnPatternClickListener listener) {
        this.names = names;
        this.files = files;
        this.context = context;
        this.listener = listener;
    }

    public void setLives(String[] names, String[] files) {
        if (names.length != files.length)
            throw new IllegalArgumentException("names and files are not of the same size");

        this.names = names;
        this.files = files;
    }

    public void setListener(OnPatternClickListener listener) {
        this.listener = listener;
    }

    @Override
    public PatternAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_pattern, parent, false));
    }

    @Override
    public void onBindViewHolder(PatternAdapter.ViewHolder holder, int position) {
        holder.bind(names[position], files[position]);
    }

    @Override
    public int getItemCount() {
        return names.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTV;
        private ViewGroup itemRoot;
        private String file;

        public ViewHolder(View itemView) {
            super(itemView);

            nameTV = (TextView) itemView.findViewById(R.id.name_textView);
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
            this.file = file;
        }
    }
}