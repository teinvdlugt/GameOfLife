package com.teinproductions.tein.gameoflife;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements ViewOfLife.ActivityInterface {

    private ViewOfLife2 viewOfLife;
    private ImageButton playPauseButton;
    private TextView generationTV;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        viewOfLife = (ViewOfLife2) findViewById(R.id.view_of_life);
        playPauseButton = (ImageButton) findViewById(R.id.playPause_button);
        generationTV = (TextView) findViewById(R.id.generation_textView);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        findViewById(R.id.clear_button).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clear();
                return true;
            }
        });

        reloadPreferenceValues();
    }

    public void onClickPencil(View view) {
        viewOfLife.setTouchMode(ViewOfLife2.TOUCH_MODE_ADD);
    }

    public void onClickErase(View view) {
        viewOfLife.setTouchMode(ViewOfLife2.TOUCH_MODE_REMOVE);
    }

    public void onClickMove(View view) {
        viewOfLife.setTouchMode(ViewOfLife2.TOUCH_MODE_MOVE);
    }

    public void clear() {
        viewOfLife.stop();
        viewOfLife.clear();
        playPauseButton.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
        lockScreenOrientation(false);
    }

    public void onClickClear(View view) {
        Snackbar.make(coordinatorLayout, getString(R.string.short_click_clear_message), Snackbar.LENGTH_SHORT).show();
    }

    public void onClickPlayPause(View view) {
        if (viewOfLife.isRunning()) {
            viewOfLife.stop();
            playPauseButton.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
        } else {
            viewOfLife.start();
            playPauseButton.setImageResource(R.mipmap.ic_pause_black_36dp);
        }
    }

    public void onClickSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void lockScreenOrientation(boolean lock) {
        if (lock) {
            int orientation = getResources().getConfiguration().orientation;
            switch (orientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
            }
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
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
        playPauseButton.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
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
    }

    private void resetSpeed() {
        String speed = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.speed_key), "100");
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

    public void onClickNextGen(View view) {
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
    }

    @Override
    public void onGenerationChanged(long newGen) {
        //String gen = "" + newGen;
        //float textSize = generationTV.getWidth() / newGen;
        generationTV.setText("" + newGen);
    }

    @Override
    public void onEdited() {
        lockScreenOrientation(true);
    }
}
