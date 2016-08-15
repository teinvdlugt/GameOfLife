package com.teinproductions.tein.gameoflife.files;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.teinproductions.tein.gameoflife.R;

public class FileReaderActivity extends AppCompatActivity {
    public static final String LIFE_MODEL_EXTRA = "life";

    EditText et;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_reader);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        et = (EditText) findViewById(R.id.editText);
    }

    private boolean cancelled = false;

    public void onClickButton(View view) {
        final String input = et.getText().toString();

        if (input.length() == 0) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.invalid_input_title))
                    .setMessage(getString(R.string.invalid_input_message))
                    .setPositiveButton(R.string.ok, null)
                    .create().show();
            return;
        }

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

        new AsyncTask<Void, Void, Life>() {
            @Override
            protected Life doInBackground(Void... params) {
                // Life life = LifeOneOFiveInterpreter.parse(input);
                try {
                    Life life = RLEReader.parse(input);
                    LifeUtils.documentCells(life.getCells());
                    return life;
                } catch (FileParseException e) {
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
                    Intent data = new Intent();
                    data.putExtra(LIFE_MODEL_EXTRA, life);
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    // Reset for next click on "Parse"
                    cancelled = false;
                }
            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }
}
