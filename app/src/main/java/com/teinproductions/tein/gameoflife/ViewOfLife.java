package com.teinproductions.tein.gameoflife;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ViewOfLife extends View {

    private int pixelsPerCell = 70;

    private int verCells = 10;
    private int horCells = 10;
    private boolean[][] field = new boolean[horCells][verCells];

    /**
     * The rules of Game of Life:
     * - A dead cell with exactly three living neighbours, awakens.
     * - A living cell with two or three living neighbours, stay alive.
     * - A living cell with less than two living neighbours, dies.
     * - A living cell with more than three living neighbours, dies.
     */
    public void nextGeneration() {
        boolean[][] fieldCache = new boolean[field.length][field[0].length];
        for (int y = 0; y < field.length; y++) {
            System.arraycopy(field[y], 0, fieldCache[y], 0, field[0].length);
        }

        for (int y = 0; y < fieldCache.length; y++) {
            for (int x = 0; x < fieldCache[0].length; x++) {
                int neighbours = livingNeighbours(fieldCache, x, y);
                Log.d("neighbours", "(" + x + "," + y + ") " + neighbours);
                if (neighbours > 3 || neighbours < 2) field[y][x] = false;
                if (neighbours == 3) field[y][x] = true;
            }
        }

        invalidate();
    }

    public int livingNeighbours(boolean[][] field, int x, int y) {
        int livingNeighbours = 0;

        try {
            if (field[y - 1][x - 1]) livingNeighbours++;
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        try {
            if (field[y - 1][x]) livingNeighbours++;
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        try {
            if (field[y - 1][x + 1]) livingNeighbours++;
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        try {
            if (field[y][x - 1]) livingNeighbours++;
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        try {
            if (field[y][x + 1]) livingNeighbours++;
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        try {
            if (field[y + 1][x - 1]) livingNeighbours++;
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        try {
            if (field[y + 1][x]) livingNeighbours++;
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        try {
            if (field[y + 1][x + 1]) livingNeighbours++;
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        return livingNeighbours;
    }

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

        Log.d("lollipop", "getWidth(): " + getWidth());
        Log.d("lollipop", "getHeight(): " + getHeight());
        Log.d("lollipop", "verCells: " + verCells);
        Log.d("lollipop", "horCells: " + horCells);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            try {
                int x = (int) Math.floor(event.getX() / pixelsPerCell);
                int y = (int) Math.floor(event.getY() / pixelsPerCell);

                Log.d("lollipop", "event.getX(): " + event.getX());
                Log.d("lollipop", "event.getY(): " + event.getY());
                Log.d("lollipop", "x: " + x);
                Log.d("lollipop", "y: " + y);

                field[x][y] = !field[x][y];
                invalidate();
                return true;
            } catch (ArrayIndexOutOfBoundsException e) {
                return super.onTouchEvent(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            try {
                int x = (int) Math.floor(event.getX() / pixelsPerCell);
                int y = (int) Math.floor(event.getY() / pixelsPerCell);

                Log.d("lollipop", "event.getX(): " + event.getX());
                Log.d("lollipop", "event.getY(): " + event.getY());
                Log.d("lollipop", "x: " + x);
                Log.d("lollipop", "y: " + y);

                field[x][y] = true;
                invalidate();
                return true;
            } catch (ArrayIndexOutOfBoundsException e) {
                return super.onTouchEvent(event);
            }
        } else return super.onTouchEvent(event);
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
