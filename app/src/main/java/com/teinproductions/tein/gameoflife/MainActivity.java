package com.teinproductions.tein.gameoflife;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.teinproductions.tein.gameoflife.files.FileReaderActivity;
import com.teinproductions.tein.gameoflife.files.Life;
import com.teinproductions.tein.gameoflife.patterns.DownloadActivity;


public class MainActivity extends AppCompatActivity {

    private ViewOfLife viewOfLife;
    private ImageButton playPauseButton;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        viewOfLife = (ViewOfLife) findViewById(R.id.view_of_life);
        playPauseButton = (ImageButton) findViewById(R.id.playPause_button);
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
        viewOfLife.setTouchMode(ViewOfLife.TOUCH_MODE_ADD);
    }

    public void onClickErase(View view) {
        viewOfLife.setTouchMode(ViewOfLife.TOUCH_MODE_REMOVE);
    }

    public void onClickMove(View view) {
        viewOfLife.setTouchMode(ViewOfLife.TOUCH_MODE_MOVE);
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
        // CELL COLOR
        boolean cellColorNotSet = false;
        String hex = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.cell_color_key), null);
        if (hex == null) {
            cellColorNotSet = true;
        } else {
            try {
                viewOfLife.setCellColor(Color.parseColor(hex));
            } catch (IllegalArgumentException e) {
                Snackbar.make(coordinatorLayout, "Provide a valid hexadecimal grid color", Snackbar.LENGTH_SHORT).show();
                cellColorNotSet = true;
            }
        }

        if (cellColorNotSet) {
            viewOfLife.setCellColor(getColor(R.color.default_cell_color));
        }


        // GRID COLOR
        boolean gridColorNotSet = false;
        String hex2 = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.grid_color_key), null);
        if (hex2 == null) {
            gridColorNotSet = true;
        } else {
            try {
                viewOfLife.setGridColor(Color.parseColor(hex2));
            } catch (IllegalArgumentException e) {
                if (!cellColorNotSet) {
                    Snackbar.make(coordinatorLayout, "Provide a valid hexadecimal grid color", Snackbar.LENGTH_SHORT).show();
                }
                gridColorNotSet = true;
            }
        }

        if (gridColorNotSet) {
            viewOfLife.setCellColor(getColor(R.color.default_grid_color));
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

    private static final int FILE_READER_ACTIVITY_REQUEST_CODE = 1;
    private static final int DOWNLOAD_ACTIVITY_REQUEST_CODE = 2;

    public void onClickCreateFromFile(View view) {
        // Intent intent = new Intent(this, FileReaderActivity.class);
        // startActivityForResult(intent, FILE_READER_ACTIVITY_REQUEST_CODE);
        Intent intent = new Intent(this, DownloadActivity.class);
        startActivityForResult(intent, DOWNLOAD_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Life result;
        if (resultCode == RESULT_OK && data != null
                && (result = (Life) data.getSerializableExtra(FileReaderActivity.LIFE_MODEL_EXTRA)) != null) {
            if (requestCode == FILE_READER_ACTIVITY_REQUEST_CODE || requestCode == DOWNLOAD_ACTIVITY_REQUEST_CODE) {
                viewOfLife.load(result);
            }
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
