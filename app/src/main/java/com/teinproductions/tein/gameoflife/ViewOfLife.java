package com.teinproductions.tein.gameoflife;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ViewOfLife extends View {

    private int pixelsPerCell = 50;

    private int verCells = 10;
    private int horCells = 10;
    private boolean[][] field = new boolean[horCells][verCells];

    @Override
    protected void onDraw(Canvas canvas) {
        drawBlocks(canvas);
        drawGrid(canvas);
    }

    private void drawBlocks(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.block_color));

        for (int y = 0; y < field.length; y++) {
            for (int x = 0; x < field[0].length; x++) {
                if (field[y][x]) {
                    int left = y * pixelsPerCell;
                    int right = (y + 1) * pixelsPerCell;
                    int top = x * pixelsPerCell;
                    int bottom = (x + 1) * pixelsPerCell;

                    canvas.drawRect(left, top, right, bottom, paint);
                }
            }
        }
    }

    private void drawGrid(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(android.R.color.black));

        // Vertical lines
        for (int x = 0; x < getWidth(); x += pixelsPerCell) {
            int stopY = verCells * pixelsPerCell;
            canvas.drawLine(x, 0, x, stopY, paint);
        }

        // Horizontal lines
        for (int y = 0; y < getHeight(); y += pixelsPerCell) {
            int stopX = horCells * pixelsPerCell;
            canvas.drawLine(0, y, stopX, y, paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        verCells = (int) Math.floor(getHeight() / pixelsPerCell);
        horCells = (int) Math.floor(getWidth() / pixelsPerCell);

        // Otherwise, the last lines from the grid won't be drawn:
        if (getHeight() % pixelsPerCell == 0) verCells--;
        if (getWidth() % pixelsPerCell == 0) horCells--;

        field = new boolean[horCells][verCells];

        generateRandomField();

        Log.d("lollipop", "getWidth(): " + getWidth());
        Log.d("lollipop", "getHeight(): " + getHeight());
        Log.d("lollipop", "verCells: " + verCells);
        Log.d("lollipop", "horCells: " + horCells);
    }

    /**
     * For debugging
     */
    private void generateRandomField() {
        for (int y = 0; y < field.length; y++) {
            for (int x = 0; x < field[0].length; x++) {
                field[y][x] = (int) Math.floor(Math.random() * 2) == 0;
            }
        }
    }

    public int getPixelsPerCell() {
        return pixelsPerCell;
    }

    public void setPixelsPerCell(int pixelsPerCell) {
        this.pixelsPerCell = pixelsPerCell;
        invalidate();
    }


    public ViewOfLife(Context context) {
        super(context);
    }

    public ViewOfLife(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewOfLife(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
