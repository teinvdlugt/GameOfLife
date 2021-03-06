package com.teinproductions.tein.gameoflife;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.teinproductions.tein.gameoflife.files.ChoosePatternActivity;
import com.teinproductions.tein.gameoflife.files.Life;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    // Parameters from ViewOfLife to save from orientation change:
    private static final String CELLS_ARRAY = "cells_array";
    private static final String START_X = "start_x";
    private static final String START_Y = "start_Y";
    private static final String CELL_WIDTH = "cell_width";

    private FirebaseAnalytics firebaseAnalytics;

    private ViewOfLife viewOfLife;
    private ImageButton playPauseButton;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setUserProperty("debug_status", BuildConfig.DEBUG ? "debugger" : "user");

        viewOfLife = (ViewOfLife) findViewById(R.id.view_of_life);
        playPauseButton = (ImageButton) findViewById(R.id.playPause_button);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        findViewById(R.id.clear_button).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clear();

                // Log Firebase Analytics event
                Bundle params = new Bundle();
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Click Clear");
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
                return true;
            }
        });

        reloadPreferenceValues();

        // Restore from orientation change
        if (savedInstanceState != null) {
            ArrayList<short[]> cells = (ArrayList<short[]>) savedInstanceState.getSerializable(CELLS_ARRAY);
            if (cells == null) return;
            float startX = savedInstanceState.getFloat(START_X, 0);
            float startY = savedInstanceState.getFloat(START_Y, 0);
            float cellWidth = savedInstanceState.getFloat(CELL_WIDTH, -1);
            Life life = new Life();
            life.setCells(cells);
            viewOfLife.load(life);
            viewOfLife.setStartX(startX);
            viewOfLife.setStartY(startY);
            if (cellWidth > 0) viewOfLife.setCellWidth(cellWidth);
            viewOfLife.invalidate();
        }
    }

    public void onClickPencil(View view) {
        viewOfLife.setTouchMode(ViewOfLife.TOUCH_MODE_ADD);

        // Log Firebase Analytics event
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Click Pencil");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
    }

    public void onClickErase(View view) {
        viewOfLife.setTouchMode(ViewOfLife.TOUCH_MODE_REMOVE);

        // Log Firebase Analytics event
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Click Eraser");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
    }

    public void onClickMove(View view) {
        viewOfLife.setTouchMode(ViewOfLife.TOUCH_MODE_MOVE);
    }

    public void clear() {
        viewOfLife.stop();
        viewOfLife.clear();
        playPauseButton.setImageResource(R.drawable.ic_play_arrow_black_36dp);
    }

    public void onClickClear(View view) {
        Toast.makeText(this, R.string.short_click_clear_message, Toast.LENGTH_SHORT).show();
    }

    public void onClickPlayPause(View view) {
        if (viewOfLife.isRunning()) {
            viewOfLife.stop();
            playPauseButton.setImageResource(R.drawable.ic_play_arrow_black_36dp);
        } else {
            // Log Firebase Analytics event
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Click Play");
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);

            viewOfLife.start();
            playPauseButton.setImageResource(R.drawable.ic_pause_black_36dp);
        }
    }

    public void onClickSettings(View view) {
        // Log Firebase Analytics event
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Click Settings");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);

        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            viewOfLife.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onPause() {
        viewOfLife.stop();
        playPauseButton.setImageResource(R.drawable.ic_play_arrow_black_36dp);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadPreferenceValues();
    }

    private void reloadPreferenceValues() {
        resetSpeed();
        resetMinCellWidthGrid();
        resetDefaultCellWidth();
        resetColors();
    }

    private void resetSpeed() {
        String speed = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.speed_key), "20");
        try {
            viewOfLife.setSpeed(Integer.parseInt(speed));
        } catch (NumberFormatException e) {
            viewOfLife.setSpeed(100);
        }
    }

    private void resetMinCellWidthGrid() {
        String width = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.min_cell_width_grid_key), "15");
        try {
            viewOfLife.setMinGridCellWidth(Integer.parseInt(width));
        } catch (NumberFormatException e) {
            viewOfLife.setMinGridCellWidth(15);
        }
    }

    private void resetDefaultCellWidth() {
        String width = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.default_cell_width_key), "50");
        try {
            viewOfLife.setDefaultCellWidth(Float.parseFloat(width));
        } catch (NumberFormatException e) {
            viewOfLife.setDefaultCellWidth(50f);
        }
    }

    private void resetColors() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // BACKGROUND COLOR
        int backgroundColor = pref.getInt(getString(R.string.background_color_key),
                getColor(this, R.color.default_background_color));
        viewOfLife.setBackgroundColor(backgroundColor);

        // CELL COLOR
        int cellColor = pref.getInt(getString(R.string.cell_color_key),
                getColor(this, R.color.default_cell_color));
        viewOfLife.setCellColor(cellColor);

        // GRID COLOR
        int gridColor = pref.getInt(getString(R.string.grid_color_key),
                getColor(this, R.color.default_grid_color));
        viewOfLife.setGridColor(gridColor);
    }

    public void onClickNextGen(View view) {
        // Log Firebase Analytics event
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Click Next Generation");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);

        new Thread(new Runnable() {
            @Override
            public void run() {
                viewOfLife.nextGeneration();
                viewOfLife.postInvalidate();
            }
        }).start();
    }

    public void onClickInitialState(View view) {
        viewOfLife.restoreGen0();

        // Log Firebase Analytics event
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Click Restore Generation 0");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
    }

    private static final int FILES_ACTIVITY_REQUEST_CODE = 1;

    public void onClickFiles(View view) {
        viewOfLife.stop(); // To stop the mutation of viewOfLife.getCells(). The play icon will be set in onPause.

        // Log Firebase Analytics event
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Click Open Pattern");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);

        synchronized (viewOfLife.lock) {
            ArrayList<short[]> cells = viewOfLife.getCells().isEmpty() ? null : viewOfLife.getCells();
            startActivityForResult(new Intent(this, ChoosePatternActivity.class)
                    .putExtra(ChoosePatternActivity.CELLS_ARRAY_EXTRA, cells), FILES_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Life result;
        if (requestCode == FILES_ACTIVITY_REQUEST_CODE &&
                resultCode == RESULT_OK &&
                data != null && (result = (Life) data.getSerializableExtra(ChoosePatternActivity.LIFE_MODEL_EXTRA)) != null) {
            viewOfLife.load(result);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // onSaveInstanceState may be called before or after onPause.
        viewOfLife.stop();
        synchronized (viewOfLife.lock) {
            outState.putSerializable(CELLS_ARRAY, viewOfLife.getCells());
            outState.putFloat(START_X, viewOfLife.getStartX());
            outState.putFloat(START_Y, viewOfLife.getStartY());
            outState.putFloat(CELL_WIDTH, viewOfLife.getCellWidth());
        }
    }

    /*@Override
    public void onEdited() {
        lockScreenOrientation(true);
    }*/

    public static int getColor(Context context, @ColorRes int colorId) {
        if (Build.VERSION.SDK_INT >= 23) return context.getColor(colorId);
        else return context.getResources().getColor(colorId);
    }
}
