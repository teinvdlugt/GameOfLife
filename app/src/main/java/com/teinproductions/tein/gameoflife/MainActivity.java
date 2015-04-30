package com.teinproductions.tein.gameoflife;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    private ViewOfLife viewOfLife;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewOfLife = (ViewOfLife) findViewById(R.id.view_of_life);
    }

    public void onClickNextStep(View view) {
        viewOfLife.nextGeneration();
    }
}
