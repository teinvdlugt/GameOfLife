package com.teinproductions.tein.gameoflife.files;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.teinproductions.tein.gameoflife.R;

import java.util.Arrays;

public class SaveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
    }

    public void onClickSave(View view) {
        EditText nameET = (EditText) findViewById(R.id.patternName_editText);
        EditText creatorET = (EditText) findViewById(R.id.creator_editText);
        EditText commentsET = (EditText) findViewById(R.id.comments_editText);
        String name = nameET.getText().toString().trim();
        String creator = creatorET.getText().toString().trim();
        String comments = commentsET.getText().toString().trim();

        if (name.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_pattern_name_given_message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }

        if (!isValidName(name)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.invalid_filename_message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }

        Life life = new Life();
        life.setName(name);
        life.setCreator(creator.isEmpty() ? null : creator);
        life.setComments(comments.isEmpty() ? null : Arrays.asList(comments.split("\n")));

        // TODO: 22-6-17 Check if a file of that name already exists
        setResult(RESULT_OK, new Intent().putExtra(ChoosePatternActivity.LIFE_INFO_EXTRA, life));
        finish();
    }

    private static boolean isValidName(String name) {
        // Checks whether the string only consists of letters, numbers, spaces and dashes.
        for (char ch : name.toCharArray()) {
            if (!(Character.isLetter(ch) || Character.isDigit(ch) || (ch == '-') || (ch == '_') || (ch == ' '))) {
                return false;
            }
        }
        return true;
    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
