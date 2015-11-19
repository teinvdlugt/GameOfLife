package com.teinproductions.tein.gameoflife;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements ViewOfLife.ActivityInterface {

    private ViewOfLife2 viewOfLife;
    private ImageButton playPauseButton, clearButton;
    private ImageButton pencilButton, eraseButton;
    private ImageButton nextGenButton, initialStateButton;
    private TextView generationTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewOfLife = (ViewOfLife2) findViewById(R.id.view_of_life);
        playPauseButton = (ImageButton) findViewById(R.id.playPause_button);
        clearButton = (ImageButton) findViewById(R.id.clear_button);
        pencilButton = (ImageButton) findViewById(R.id.pencilButton);
        eraseButton = (ImageButton) findViewById(R.id.eraseButton);
        nextGenButton = (ImageButton) findViewById(R.id.nextGenButton);
        initialStateButton = (ImageButton) findViewById(R.id.initialStateButton);
        generationTV = (TextView) findViewById(R.id.generation_textView);

        clearButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clear();
                return true;
            }
        });

        /*viewOfLife.setActivityInterface(this);
        playPauseButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (zoomOutButton.getVisibility() == View.VISIBLE
                        && nextGenButton.getVisibility() == View.GONE)
                    showGenerationButtonSection();
                else showZoomButtonSection();
                return true;
            }
        });

        resetSpeed();

        viewOfLife.setEditMode(ViewOfLife.EditMode.ADD);
        pencilButton.setSelected(true);
        viewOfLife.setAutoZoom(true);
        autoZoomButton.setSelected(true);
        showZoomButtonSection();*/
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
        //viewOfLife.stop();
        viewOfLife.clear();
        playPauseButton.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
        lockScreenOrientation(false);
    }

    public void onClickClear(View view) {
        Snackbar.make(viewOfLife, getString(R.string.short_click_clear_message), Snackbar.LENGTH_SHORT).show();
    }

    public void onClickPlayPause(View view) {
        /*if (viewOfLifer.isRunning()) {
            viewOfLife.stop();
            playPauseButton.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
        } else {
            viewOfLife.start();
            playPauseButton.setImageResource(R.mipmap.ic_pause_black_36dp);
        }*/
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
        //viewOfLife.stop();
        //playPauseButton.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //resetSpeed();
        //resetStrokeWidth();
    }

    private void resetSpeed() {
        /*String speed = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.speed_key), "100");
        try {
            viewOfLife.setSpeed(Integer.parseInt(speed));
        } catch (NumberFormatException e) {
            viewOfLife.setSpeed(100);
        }*/
    }

    private void resetStrokeWidth() {
        /*String speed = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.stroke_width_key), "1");

        try {
            viewOfLife.setStrokeWidth(Integer.parseInt(speed));
        } catch (NumberFormatException e) {
            viewOfLife.setStrokeWidth(1);
        }*/
    }

    public void onClickNextGen(View view) {
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                viewOfLife.nextGeneration();
                viewOfLife.postInvalidate();
            }
        }).start();*/
    }

    public void onClickInitialState(View view) {
        /*viewOfLife.restoreInitialState();*/
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
