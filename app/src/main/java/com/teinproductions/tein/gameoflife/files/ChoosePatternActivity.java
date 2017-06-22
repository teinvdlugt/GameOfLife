package com.teinproductions.tein.gameoflife.files;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.teinproductions.tein.gameoflife.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ChoosePatternActivity extends AppCompatActivity implements PatternAdapter.OnClickPatternListener {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pattern);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Remove the 'save'-FAB when there is nothing to save
        findViewById(R.id.save_fab).setVisibility(
                getIntent().getExtras().containsKey(CELLS_ARRAY_EXTRA) ? View.VISIBLE : View.GONE);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PatternAdapter adapter = new PatternAdapter(this, this, RLEPattern.getList(getResources()));
        recyclerView.setAdapter(adapter);
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

        new AsyncTask<String, Void, Life>() {
            @Override
            protected Life doInBackground(String... values) {
                try {
                    InputStream in = getResources().getAssets().open("patterns/" + values[0]);
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
                        setResult(RESULT_OK, new Intent().putExtra(FileReaderActivity.LIFE_MODEL_EXTRA, life));
                        finish();
                    }
                } else {
                    // Reset member variable
                    cancelled = false;
                }
            }
        }.execute(pattern.getFilename());
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
    public static final int SAVE_ACTIVITY_RQ = 42;

    public void onClickSave(View view) {
        if (getIntent().getExtras().containsKey(CELLS_ARRAY_EXTRA)) {
            Snackbar.make(recyclerView, R.string.cells_array_save_error, Snackbar.LENGTH_LONG).show();
            return;
        }

        startActivityForResult(new Intent(this, SaveActivity.class), SAVE_ACTIVITY_RQ);

        // TODO: 21-6-17 Check if there is such an Extra at all
        List<short[]> cells = (List<short[]>) (getIntent().getSerializableExtra(CELLS_ARRAY_EXTRA));
        // FOR DEBUG
        new AlertDialog.Builder(this)
                .setMessage("Encoded file: \n" + RLEEncoder.encode(cells))
                .setPositiveButton(R.string.ok, null)
                .show();
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

    private void saveLife(final Life info) {
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

        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    String file = RLEEncoder.constructFile(info,
                            (List<short[]>) getIntent().getSerializableExtra(CELLS_ARRAY_EXTRA));

                } catch (Exception e) {
                    return 0;
                }

                return 0;
            }

            @Override
            protected void onPostExecute(Integer resultCode) {
                // resultCode: 0 means failed, 1 means succeeded.

            }
        }.execute();
    }
}
