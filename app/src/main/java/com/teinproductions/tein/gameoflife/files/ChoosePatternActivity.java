package com.teinproductions.tein.gameoflife.files;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.teinproductions.tein.gameoflife.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ChoosePatternActivity extends AppCompatActivity implements PatternAdapter.OnClickPatternListener {
    public static final String LIFE_MODEL_EXTRA = "life";

    private RecyclerView recyclerView;
    private PatternAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pattern);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Remove the 'save'-FAB when there is nothing to save
        findViewById(R.id.save_fab).setVisibility(
                getIntent().getSerializableExtra(CELLS_ARRAY_EXTRA) != null ? View.VISIBLE : View.GONE);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatternAdapter(this, this, null);
        loadAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void loadAdapter() {
        List<PatternListable> items = new ArrayList<>();
        List<RLEPattern> preloaded = RLEPattern.getList(getResources());
        List<RLEPattern> saved = getSavedPatternsList();
        if (saved != null && !saved.isEmpty()) {
            items.add(new Header(getString(R.string.saved_patterns)));
            items.addAll(saved);
        }
        if (preloaded != null && !preloaded.isEmpty()) {
            items.add(new Header(getString(R.string.preloaded_patterns)));
            items.addAll(preloaded);
        }
        adapter.setData(items);
    }

    private List<RLEPattern> getSavedPatternsList() {
        List<RLEPattern> result = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(ChoosePatternActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File dir = Environment.getExternalStoragePublicDirectory("GameOfLife");
            File[] savedFiles = dir.listFiles();
            if (savedFiles != null)
                for (File file : savedFiles) {
                    if (file.isFile() && (file.getName().endsWith(".rle") || file.getName().endsWith(".RLE")))
                        result.add(new RLEPattern(file.getName().substring(0, file.getName().length() - 4), file.getName(), false));
                }
        }
        return result;
    }

    private boolean cancelled = false;

    @Override
    public void onClick(RLEPattern pattern) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.parsing_progress_dialog_message));
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelled = true;
            }
        });
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        new AsyncTask<RLEPattern, Void, Life>() {
            @Override
            protected Life doInBackground(RLEPattern... values) {
                try {
                    InputStream in;
                    if (values[0].isPreloaded()) {
                        in = getResources().getAssets().open("patterns/" + values[0].getFilename());
                    } else {
                        if (ContextCompat.checkSelfPermission(ChoosePatternActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            File dir = Environment.getExternalStoragePublicDirectory("GameOfLife");
                            File file = new File(dir, values[0].getFilename());
                            in = new FileInputStream(file);
                        } else {
                            return null;
                        }
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        sb.append(line).append("\n");
                    Life life = RLEReader.parse(sb.toString()); // TODO: 22-8-2016 Listen to isCancelled() in RLEReader.parse()
                    LifeUtils.documentCells(life.getCells());

                    return life;
                } catch (IOException | FileParseException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Life life) {
                if (!cancelled) {
                    try {
                        dialog.dismiss();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                    if (life == null) new AlertDialog.Builder(ChoosePatternActivity.this)
                            .setMessage(R.string.something_went_wrong)
                            .setPositiveButton(R.string.ok, null)
                            .create().show();
                    else {
                        setResult(RESULT_OK, new Intent().putExtra(LIFE_MODEL_EXTRA, life));
                        finish();
                    }
                } else {
                    // Reset member variable
                    cancelled = false;
                }
            }
        }.execute(pattern);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Used to pass a List<short[]> from MainActivity to ChoosePatternActivity
     */
    public static final String CELLS_ARRAY_EXTRA = "cells_array";
    /**
     * Used to return a Life object from SaveActivity to ChoosePatternActivity
     */
    public static final String LIFE_INFO_EXTRA = "save_info";
    private static final int SAVE_ACTIVITY_RQ = 42;

    public void onClickSave(View view) {
        if (!getIntent().getExtras().containsKey(CELLS_ARRAY_EXTRA)) {
            Snackbar.make(recyclerView, R.string.cells_array_save_error, Snackbar.LENGTH_LONG).show();
            return;
        }

        startActivityForResult(new Intent(this, SaveActivity.class), SAVE_ACTIVITY_RQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAVE_ACTIVITY_RQ && resultCode == RESULT_OK) {
            Life info = (Life) data.getSerializableExtra(LIFE_INFO_EXTRA);
            if (info == null) {
                Snackbar.make(recyclerView, R.string.something_went_wrong, Snackbar.LENGTH_LONG);
                return;
            }
            saveLife(info);
        }
    }

    private static final int REQUEST_EXTERNAL_STORAGE_RQ = 43;
    /**
     * Temporary member variable to keep the String to save, whilst storage permission is requested.
     */
    private String tempFile;
    private String tempFilename;

    private void saveLife(final Life info) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.parsing_progress_dialog_message));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    return RLEEncoder.constructFile(info,
                            (List<short[]>) getIntent().getSerializableExtra(CELLS_ARRAY_EXTRA));
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String file) {
                // Dismiss dialog
                try {
                    dialog.dismiss();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                if (file == null) {
                    Snackbar.make(recyclerView, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                } else {
                    // Request permission to external storage, if necessary
                    if (ContextCompat.checkSelfPermission(ChoosePatternActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ChoosePatternActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_EXTERNAL_STORAGE_RQ);
                        tempFile = file;
                        tempFilename = info.getName() + ".rle";
                    } else {
                        saveFile(info.getName() + ".rle", file);
                    }
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE_RQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted!
                saveFile(tempFilename, tempFile);
            } else {
                // Permission denied
                Snackbar.make(recyclerView, R.string.permission_denied_save_error, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void saveFile(String filename, String contents) {
        try {
            File dir = Environment.getExternalStoragePublicDirectory("GameOfLife");
            boolean dirsMade = dir.mkdirs();
            File file = new File(dir, filename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(contents.getBytes());
            fos.close();
            Snackbar.make(recyclerView, R.string.pattern_saved, Snackbar.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.make(recyclerView, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
        }
    }
}
