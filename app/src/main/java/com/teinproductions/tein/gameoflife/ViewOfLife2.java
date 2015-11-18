package com.teinproductions.tein.gameoflife;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ViewOfLife2 extends View {

    private int cellWidth = 30;
    private float startX = 0, startY = 0;
    /**
     * 0: x position
     * 1: y position
     * 2: alive [0|1]
     */
    private List<short[]> cells = new ArrayList<>();
    private Paint gridPaint, cellPaint;

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

        short cellX = (short) (roundAwayFromZero(startX) - (startX >= 0 ? 1 : 0));
        short cellYStart = (short) (roundAwayFromZero(startY) - (startY >= 0 ? 1 : 0));
        int pixelXStart = firstVerLine - cellWidth;
        int pixelYStart = firstHorLine - cellWidth;

        for (int x = pixelXStart + 1; x < width; x += cellWidth) {
            short cellY = cellYStart;
            for (int y = pixelYStart + 1; y < height; y += cellWidth) {
                if (isAlive(cellX, cellY)) {
                    canvas.drawRect(x, y, x + cellWidth, y + cellWidth, cellPaint);
                }
                cellY++;
            }
            cellX++;
        }
    }

    private boolean isAlive(short x, short y) {
        for (short[] cell : cells)
            if (cell[0] == x && cell[1] == y) {
                return cell[2] == 1;
            }
        return false;
    }

    private static short roundAwayFromZero(float i) {
        if (i > 0) return (short) Math.ceil(i);
        else return (short) Math.floor(i);
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

        cellPaint = new Paint();
        cellPaint.setColor(getColor(R.color.block_color));
        cellPaint.setStyle(Paint.Style.FILL);

        cells.add(new short[]{0, 0, 1});
        cells.add(new short[]{1, 1, 1});
        cells.add(new short[]{2, 2, 1});
        cells.add(new short[]{2, 3, 1});
        cells.add(new short[]{2, 4, 1});
        cells.add(new short[]{2, 5, 1});
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


    private int getColor(@ColorRes int colorId) {
        if (Build.VERSION.SDK_INT >= 23) return getContext().getColor(colorId);
        else return getContext().getResources().getColor(colorId);
    }
}
