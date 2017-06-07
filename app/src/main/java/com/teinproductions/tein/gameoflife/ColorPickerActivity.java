package com.teinproductions.tein.gameoflife;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class ColorPickerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private static final String COLOR_SAVE_INSTANCE_STATE = "color"; // Key to save color in savedInstanceState

    private static final String PREF_KEY_EXTRA = "pref_key";
    private static final String DEFAULT_COLOR_EXTRA = "default_color";
    private String preferenceKey; // Preference where the color is stored; Passed as extra to this activity
    private int defaultColor; // @ColorInt; Passed as extra to this activity

    private TextView rTV, gTV, bTV;
    private SeekBar rSB, gSB, bSB;
    private View sampleView;

    private int red;
    private int green;
    private int blue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_picker);

        // Get the color
        preferenceKey = getIntent().getStringExtra(PREF_KEY_EXTRA);
        defaultColor = getIntent().getIntExtra(DEFAULT_COLOR_EXTRA, Color.WHITE);
        int color;
        if (savedInstanceState == null) {
            color = PreferenceManager.getDefaultSharedPreferences(this)
                    .getInt(preferenceKey, defaultColor);
        } else {
            color = savedInstanceState.getInt(COLOR_SAVE_INSTANCE_STATE);
        }
        red = Color.red(color);
        green = Color.green(color);
        blue = Color.blue(color);

        // Init the views
        sampleView = findViewById(R.id.color_sample_view);
        rTV = (TextView) findViewById(R.id.rv_tv);
        gTV = (TextView) findViewById(R.id.gv_tv);
        bTV = (TextView) findViewById(R.id.bv_tv);
        rSB = (SeekBar) findViewById(R.id.r_seekBar);
        gSB = (SeekBar) findViewById(R.id.g_seekBar);
        bSB = (SeekBar) findViewById(R.id.b_seekBar);

        // Set listeners
        rSB.setOnSeekBarChangeListener(this);
        gSB.setOnSeekBarChangeListener(this);
        bSB.setOnSeekBarChangeListener(this);

        // Load the color into the views
        rSB.setProgress(red);
        gSB.setProgress(green);
        bSB.setProgress(blue);
        sampleView.setBackgroundColor(Color.rgb(red, green, blue));
        rTV.setText(Integer.toString(red));
        gTV.setText(Integer.toString(green));
        bTV.setText(Integer.toString(blue));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.equals(rSB)) { red = progress; rTV.setText(Integer.toString(progress)); }
        if (seekBar.equals(gSB)) { green = progress; gTV.setText(Integer.toString(progress)); }
        if (seekBar.equals(bSB)) { blue = progress; bTV.setText(Integer.toString(progress)); }
        sampleView.setBackgroundColor(Color.rgb(red, green, blue));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { /* ignored */ }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { /* ignored */ }


    public void onClickOK(View view) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(preferenceKey, Color.rgb(red, green, blue))
                .apply();
        finish();
    }

    public void onClickCancel(View view) {
        finish();
    }

    public void onClickReset(View view) {
        red = Color.red(defaultColor);
        green = Color.green(defaultColor);
        blue = Color.blue(defaultColor);
        rSB.setProgress(red);
        gSB.setProgress(green);
        bSB.setProgress(blue);
    }

    public static void openActivity(Context context, @ColorInt int defaultColor, String preferenceKey) {
        Intent intent = new Intent(context, ColorPickerActivity.class);
        intent.putExtra(PREF_KEY_EXTRA, preferenceKey);
        intent.putExtra(DEFAULT_COLOR_EXTRA, defaultColor);
        context.startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(COLOR_SAVE_INSTANCE_STATE, Color.rgb(red, green, blue));
    }
}
