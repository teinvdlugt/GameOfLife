package com.teinproductions.tein.gameoflife;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;


public class MainActivity extends AppCompatActivity {

    private ViewOfLife viewOfLife;
    private ImageButton playPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewOfLife = (ViewOfLife) findViewById(R.id.view_of_life);
        playPauseButton = (ImageButton) findViewById(R.id.playPause_button);
    }

    public void onClickPencil(View view) {
        viewOfLife.setEditMode(ViewOfLife.EditMode.ADD);
    }

    public void onClickErase(View view) {
        viewOfLife.setEditMode(ViewOfLife.EditMode.REMOVE);
    }

    public void onClickClear(View view) {
        viewOfLife.clear();
        viewOfLife.stop();
        playPauseButton.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
    }

    public void onClickPlayPause(View view) {
        ImageButton button = (ImageButton) findViewById(R.id.playPause_button);

        if (viewOfLife.isRunning()) {
            viewOfLife.stop();
            button.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
        } else {
            viewOfLife.start();
            button.setImageResource(R.mipmap.ic_pause_black_36dp);
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


}
