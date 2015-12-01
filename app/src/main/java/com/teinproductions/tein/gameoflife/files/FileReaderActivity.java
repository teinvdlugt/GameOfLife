package com.teinproductions.tein.gameoflife.files;


import android.content.Intent;
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

    public void onClickButton(View view) {
        if (et.length() == 0) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.invalid_input_title))
                    .setMessage(getString(R.string.invalid_input_message))
                    .setPositiveButton(R.string.ok, null)
                    .create().show();
            return;
        }

        Life life = LifeOneOFiveInterpreter.parse(et.getText().toString());

        Intent data = new Intent();
        data.putExtra(LIFE_MODEL_EXTRA, life);
        setResult(RESULT_OK, data);
        finish();
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
