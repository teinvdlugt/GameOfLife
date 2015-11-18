package com.teinproductions.tein.gameoflife;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ViewOfLife2 extends View {

    private int cellWidth = 30;
    private float startX = 0, startY = 0;
    private List<short[]> cells = new ArrayList<>();
    private Paint gridPaint;

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth(), height = getHeight();
        int firstVerLine = (int) (cellWidth * (Math.ceil(startX) - startX));
        int firstHorLine = (int) (cellWidth * (Math.ceil(startY) - startY));

        // Vertical grid lines
        for (float x = firstVerLine; x < width; x += cellWidth) {
            canvas.drawLine(x, 0, x, height, gridPaint);
        }
        // Horizontal grid lines
        for (float y = firstHorLine; y < height; y += cellWidth) {
            canvas.drawLine(0, y, width, y, gridPaint);
        }

        // TODO: 18-11-2015 Draw alive cells
    }

    private float prevXDrag, prevYDrag;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevXDrag = event.getX() / cellWidth + startX;
                prevYDrag = event.getY() / cellWidth + startY;
                return true;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX() / cellWidth + startX;
                float y = event.getY() / cellWidth + startY;
                startX -= x - prevXDrag;
                // TODO: 18-11-2015 Does startX += x + prevXDrag also work
                startY -= y - prevYDrag;
                prevXDrag = x;
                prevYDrag = y;
                invalidate();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    public void init() {
        gridPaint = new Paint();
        gridPaint.setStyle(Paint.Style.STROKE);
    }

    public ViewOfLife2(Context context) {
        super(context);
        init();
    }

    public ViewOfLife2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewOfLife2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
}
