package com.teinproductions.tein.gameoflife.files;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.teinproductions.tein.gameoflife.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ChoosePatternActivity extends AppCompatActivity implements PatternAdapter.OnClickPatternListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pattern);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
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
}
