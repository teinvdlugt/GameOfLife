package com.teinproductions.tein.gameoflife;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private ViewOfLife viewOfLife;
    private ImageButton playPauseButton, autoZoomButton, clearButton;
    private ImageButton pencilButton, eraseButton, zoomInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewOfLife = (ViewOfLife) findViewById(R.id.view_of_life);
        playPauseButton = (ImageButton) findViewById(R.id.playPause_button);
        autoZoomButton = (ImageButton) findViewById(R.id.auto_zoom_button);
        clearButton = (ImageButton) findViewById(R.id.clear_button);
        pencilButton = (ImageButton) findViewById(R.id.pencilButton);
        eraseButton = (ImageButton) findViewById(R.id.eraseButton);
        zoomInButton = (ImageButton) findViewById(R.id.zoomInButton);

        clearButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clear();
                return true;
            }
        });

        resetSpeed();
        viewOfLife.setEditMode(ViewOfLife.EditMode.ADD);
        pencilButton.setSelected(true);
        viewOfLife.setAutoZoom(true);
        autoZoomButton.setSelected(true);
    }

    public void onClickPencil(View view) {
        viewOfLife.setEditMode(ViewOfLife.EditMode.ADD);
        pencilButton.setSelected(true);
        eraseButton.setSelected(false);
        zoomInButton.setSelected(false);
    }

    public void onClickErase(View view) {
        viewOfLife.setEditMode(ViewOfLife.EditMode.REMOVE);
        pencilButton.setSelected(false);
        eraseButton.setSelected(true);
        zoomInButton.setSelected(false);
    }

    public void onClickZoomIn(View view) {
        viewOfLife.setEditMode(ViewOfLife.EditMode.ZOOM_IN);
        pencilButton.setSelected(false);
        eraseButton.setSelected(false);
        zoomInButton.setSelected(true);
    }

    public void clear() {
        viewOfLife.stop();
        viewOfLife.clear();
        playPauseButton.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
    }

    public void onClickClear(View view) {
        Toast.makeText(this, getString(R.string.short_click_clear_message), Toast.LENGTH_SHORT).show();
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

    public void onClickZoomFit(View view) {
        viewOfLife.zoomFit();
    }

    public void onClickZoomOut(View view) {
        viewOfLife.zoomOut();
        Log.d("sizes", "height: " + viewOfLife.getFieldHeight() + ", width: " + viewOfLife.getFieldWidth());
    }

    public void onClickAutoZoom(View view) {
        if (viewOfLife.isAutoZoom()) {
            viewOfLife.setAutoZoom(false);
            autoZoomButton.setSelected(false);
        } else {
            viewOfLife.setAutoZoom(true);
            autoZoomButton.setSelected(true);
        }
    }

    public void onClickSettings(View view) {
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
        playPauseButton.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetSpeed();
        resetStrokeWidth();
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

    private void resetStrokeWidth() {
        String speed = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.stroke_width_key), "1");

        try {
            viewOfLife.setStrokeWidth(Integer.parseInt(speed));
        } catch (NumberFormatException e) {
            viewOfLife.setStrokeWidth(1);
        }
    }
}