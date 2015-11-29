package com.teinproductions.tein.gameoflife;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;


public class FABBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

    // https://lab.getbase.com/introduction-to-coordinator-layout-on-android/

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }

    public FABBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
