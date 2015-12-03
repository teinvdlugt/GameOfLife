package com.teinproductions.tein.gameoflife;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.teinproductions.tein.gameoflife.files.Life;

import java.util.ArrayList;
import java.util.List;

public class ViewOfLife extends View {

    private float cellWidth = 50;
    private float startX = 0, startY = 0;
    private int minGridCellWidth = 15;
    private long speed = 50;

    private float defaultCellWidth = 50;
    /**
     * 0: x position
     * 1: y position
     * 2: alive [0|1]
     * 3: num of neighbours
     */
    private List<short[]> cells = new ArrayList<>();
    private final Object lock = new Object();
    private Paint gridPaint, cellPaint;

    private List<short[]> gen0 = new ArrayList<>();

    private boolean running;

    private int touchMode = TOUCH_MODE_MOVE;
    public static final int TOUCH_MODE_ADD = 0;
    public static final int TOUCH_MODE_REMOVE = 1;
    public static final int TOUCH_MODE_MOVE = 2;

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth(), height = getHeight();
        int firstVerLine = (int) (cellWidth * (Math.ceil(startX) - startX));
        int firstHorLine = (int) (cellWidth * (Math.ceil(startY) - startY));

        // DRAW CELLS
        short xStart = (short) Math.floor(startX);
        short xEnd = (short) (xStart + width / cellWidth + 1);
        short yStart = (short) Math.floor(startY);
        short yEnd = (short) (yStart + height / cellWidth + 1);

        synchronized (lock) {
            for (short[] cell : cells) {
                if (cell[2] == 1 && cell[0] >= xStart && cell[0] <= xEnd &&
                        cell[1] >= yStart && cell[1] <= yEnd) {
                    float left = (cell[0] - startX) * cellWidth;
                    float top = (cell[1] - startY) * cellWidth;
                    canvas.drawRect(left, top, left + cellWidth, top + cellWidth, cellPaint);
                }
            }
        }

        // DRAW GRID
        if (cellWidth >= minGridCellWidth) {
            // Vertical grid lines
            for (float x = firstVerLine; x < width; x += cellWidth) {
                canvas.drawLine(x, 0, x, height, gridPaint);
            }
            // Horizontal grid lines
            for (float y = firstHorLine; y < height; y += cellWidth) {
                canvas.drawLine(0, y, width, y, gridPaint);
            }
        }
    }

    private boolean isAlive(short x, short y) {
        for (short[] cell : cells)
            if (cell[0] == x && cell[1] == y) {
                return cell[2] == 1;
            }
        return false;
    }

    private float prevXDrag1 = -1, prevYDrag1 = -1;
    private float prevXDrag2 = -1, prevYDrag2 = -1;
    private int zoomPointerId1 = -1, zoomPointerId2 = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() / cellWidth + startX;
        float y = event.getY() / cellWidth + startY;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                switch (touchMode) {
                    case TOUCH_MODE_ADD:
                        synchronized (lock) {
                            makeAlive((short) Math.floor(x), (short) Math.floor(y));
                        }
                        invalidate();
                        return true;
                    case TOUCH_MODE_REMOVE:
                        synchronized (lock) {
                            makeDead((short) Math.floor(x), (short) Math.floor(y));
                        }
                        invalidate();
                        return true;
                    case TOUCH_MODE_MOVE:
                        prevXDrag1 = event.getX();
                        prevYDrag1 = event.getY();
                        zoomPointerId1 = event.getPointerId(0);
                        invalidate();
                        return true;
                    default:
                        return false;
                }
            case MotionEvent.ACTION_POINTER_DOWN:
                if (touchMode != TOUCH_MODE_MOVE) return false;
                if (zoomPointerId2 != -1) return false;
                zoomPointerId2 = event.getPointerId(event.getActionIndex());
                int index = event.getActionIndex();
                prevXDrag2 = event.getX(index);
                prevYDrag2 = event.getY(index);
            case MotionEvent.ACTION_MOVE:
                switch (touchMode) {
                    case TOUCH_MODE_ADD:
                        synchronized (lock) {
                            makeAlive((short) Math.floor(x), (short) Math.floor(y));
                        }
                        invalidate();
                        return true;
                    case TOUCH_MODE_REMOVE:
                        synchronized (lock) {
                            makeDead((short) Math.floor(x), (short) Math.floor(y));
                        }
                        invalidate();
                        return true;
                    case TOUCH_MODE_MOVE:
                        if (event.getPointerCount() == 1) {
                            startX -= x - (prevXDrag1 / cellWidth + startX);
                            startY -= y - (prevYDrag1 / cellWidth + startY);
                            prevXDrag1 = event.getX();
                            prevYDrag1 = event.getY();
                        } else {
                            int indexCurrent = event.getActionIndex();
                            int index1 = event.findPointerIndex(zoomPointerId1);
                            int index2 = event.findPointerIndex(zoomPointerId2);
                            if ((indexCurrent != index1 && indexCurrent != index2) || zoomPointerId1 == -1 ||
                                    zoomPointerId2 == -1 || prevXDrag1 == -1 || prevYDrag1 == -1 ||
                                    prevXDrag2 == -1 || prevYDrag2 == -1) return false;


                            double xDist1 = prevXDrag2 - prevXDrag1;
                            double yDist1 = prevYDrag2 - prevYDrag1;
                            double dist1Sqr = xDist1 * xDist1 + yDist1 * yDist1;

                            double centerPointX1 = (prevXDrag1 + prevXDrag2) / 2d;
                            double centerPointY1 = (prevYDrag1 + prevYDrag2) / 2d;
                            double centerPointX1Cell = centerPointX1 / cellWidth + startX;
                            double centerPointY1Cell = centerPointY1 / cellWidth + startY;

                            prevXDrag1 = event.getX(index1);
                            prevYDrag1 = event.getY(index1);
                            prevXDrag2 = event.getX(index2);
                            prevYDrag2 = event.getY(index2);


                            // Change cellWidth
                            double xDist2 = prevXDrag2 - prevXDrag1;
                            double yDist2 = prevYDrag2 - prevYDrag1;
                            double dist2Sqr = xDist2 * xDist2 + yDist2 * yDist2;
                            cellWidth *= Math.sqrt(dist2Sqr / dist1Sqr);


                            // Change startX and startY
                            double centerPointX2 = (prevXDrag1 + prevXDrag2) / 2d;
                            double centerPointY2 = (prevYDrag1 + prevYDrag2) / 2d;
                            // The cell at grid-position (centerPointX1Cell, centerPointY1Cell) has to
                            // move to pixel-position (centerPointX2, centerPointY2)
                            //
                            // Written out version:
                            // double cellsLeft = centerPointX2 / cellWidth;
                            // startX = (float) (centerPointX1Cell - cellsLeft);
                            startX = (float) (centerPointX1Cell - centerPointX2 / cellWidth);
                            startY = (float) (centerPointY1Cell - centerPointY2 / cellWidth);
                        }

                        invalidate();
                        return true;
                    default:
                        return false;
                }
            case MotionEvent.ACTION_UP:
                switch (touchMode) {
                    case TOUCH_MODE_ADD:
                        synchronized (lock) {
                            makeAlive((short) Math.floor(x), (short) Math.floor(y));
                            updateGen0();
                        }
                        invalidate();
                        return true;
                    case TOUCH_MODE_REMOVE:
                        synchronized (lock) {
                            makeDead((short) Math.floor(x), (short) Math.floor(y));
                            updateGen0();
                        }
                        invalidate();
                        return true;
                    case TOUCH_MODE_MOVE:
                        prevXDrag1 = prevXDrag2 = prevYDrag1 = prevYDrag2 =
                                zoomPointerId2 = zoomPointerId1 = -1;
                    default:
                        return false;
                }
            case MotionEvent.ACTION_POINTER_UP:
                if (touchMode != TOUCH_MODE_MOVE) return false;
                int pointerIndex = event.getActionIndex();
                if (pointerIndex == event.findPointerIndex(zoomPointerId1)) {
                    // Transfer the data of pointer 2 to pointer 1,
                    zoomPointerId1 = zoomPointerId2;
                    prevXDrag1 = prevXDrag2;
                    prevYDrag1 = prevYDrag2;

                    // Get rid of pointer 2
                    zoomPointerId2 = -1;
                    prevXDrag2 = prevYDrag2 = -1;
                    Log.d("hi", "onTouchEvent: pointerId 1");
                } else if (pointerIndex == event.findPointerIndex(zoomPointerId2)) {
                    zoomPointerId2 = -1;
                    prevXDrag2 = prevYDrag2 = -1;
                    Log.d("hi", "onTouchEvent: pointerId 2");
                } else return false;
                return true;
            default:
                return false;
        }
    }

    /**
     * Make the cell at the specified location alive, if it
     * isn't already, and increment the number of neighbours of its
     * neighbours.
     *
     * @param x X position of the cell in the grid
     * @param y Y position of the cell in the grid
     */
    private void makeAlive(short x, short y) {
        for (short[] cell : cells) {
            if (cell[0] == x && cell[1] == y) {
                if (cell[2] == 0) {
                    cell[2] = 1;
                    notifyNeighbours(x, y, (byte) 1);
                }
                return;
            }
        }

        // Cell not yet in array
        cells.add(new short[]{x, y, 1, neighbours(x, y)});
        notifyNeighbours(x, y, (byte) 1);
    }

    private void makeDead(short x, short y) {
        for (int i = 0; i < cells.size(); i++) {
            short[] cell = cells.get(i);
            if (cell[0] == x && cell[1] == y) {
                if (cell[2] == 1) {
                    cell[2] = 0;
                    if (cell[3] == 0) {
                        cells.remove(i);
                    }
                    notifyNeighbours(x, y, (byte) -1);
                }
                return;
            }
        }
    }

    /**
     * Example: if the cell at position x, y became alive,
     * its neighbours have to be notified (position 3 in their short[]
     * has to be incremented by one).
     *
     * @param x     X position of cell whose neighbours have to be notified
     * @param y     Y position of cell whose neighbours have to be notified
     * @param toAdd Should be either 1 of -1; dependant on whether the cell at x, y
     *              became alive of died.
     */
    private void notifyNeighbours(short x, short y, byte toAdd) {
        incrementNeighbours((short) (x - 1), (short) (y - 1), toAdd);
        incrementNeighbours(x, (short) (y - 1), toAdd);
        incrementNeighbours((short) (x + 1), (short) (y - 1), toAdd);
        incrementNeighbours((short) (x - 1), y, toAdd);
        incrementNeighbours((short) (x + 1), y, toAdd);
        incrementNeighbours((short) (x - 1), (short) (y + 1), toAdd);
        incrementNeighbours(x, (short) (y + 1), toAdd);
        incrementNeighbours((short) (x + 1), (short) (y + 1), toAdd);
    }

    /**
     * Increment the number of neighbours of the cell on a specified location
     * in the grid by a specified amount. If the cell isn't present yet in the
     * {@code List<short[]> cells}, it will be added.
     *
     * @param x     X position of cell in Game of Life grid
     * @param y     Y position of cell in Game of Life grid
     * @param toAdd Amount of neighbours to add
     */
    private void incrementNeighbours(short x, short y, byte toAdd) {
        for (int i = 0; i < cells.size(); i++) {
            short[] cell = cells.get(i);
            if (cell[0] == x && cell[1] == y) {
                cell[3] += toAdd;
                if (cell[3] == 0 && cell[2] == 0)
                    cells.remove(i);
                return;
            }
        }

        // Cell not yet in array
        cells.add(new short[]{x, y, 0, toAdd > 0 ? toAdd : 0});
    }

    private byte neighbours(short x, short y) {
        byte neighbours = 0;
        for (short[] cell : cells) {
            if (cell[2] == 1 && (cell[0] == x - 1 || cell[0] == x || cell[0] == x + 1) &&
                    (cell[1] == y - 1 || cell[1] == y | cell[1] == y + 1) &&
                    !(cell[0] == x && cell[1] == y))
                neighbours++;
        }
        return neighbours;
    }


    public void nextGeneration() {
        synchronized (lock) {
            List<short[]> cellsBackup = clone(cells);
            for (int i = 0; i < cellsBackup.size(); i++) {
                short[] cell = cellsBackup.get(i);
                if (cell[2] == 0 && cell[3] == 3) {
                    makeAlive(cell[0], cell[1]);
                } else if (cell[2] == 1 && (cell[3] < 2 || cell[3] > 3)) {
                    makeDead(cell[0], cell[1]);
                }
            }
        }
    }

    private static List<short[]> clone(List<short[]> array) {
        List<short[]> clone = new ArrayList<>();
        for (short[] cell : array) {
            clone.add(new short[]{
                    cell[0], cell[1], cell[2], cell[3]});
        }
        return clone;
    }

    public void start() {
        running = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    long millis = System.currentTimeMillis();
                    nextGeneration();
                    postInvalidate();
                    long sleepTime = speed - System.currentTimeMillis() + millis;
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public int getTouchMode() {
        return touchMode;
    }

    public void setTouchMode(int touchMode) {
        if (!(touchMode == TOUCH_MODE_ADD || touchMode == TOUCH_MODE_REMOVE || touchMode == TOUCH_MODE_MOVE))
            throw new IllegalArgumentException(
                    "Parameter touchMode must be one of TOUCH_MODE_ADD, TOUCH_MODE_REMOVE and TOUCH_MODE_MOVE");
        this.touchMode = touchMode;
    }

    public int getMinGridCellWidth() {
        return minGridCellWidth;
    }

    public void setMinGridCellWidth(int minGridCellWidth) {
        this.minGridCellWidth = minGridCellWidth;
    }

    public float getDefaultCellWidth() {
        return defaultCellWidth;
    }

    public void setDefaultCellWidth(float defaultCellWidth) {
        this.defaultCellWidth = defaultCellWidth;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public void clear() {
        synchronized (lock) {
            cells.clear();
            gen0.clear();
            startX = startY = 0;
            cellWidth = defaultCellWidth;
            invalidate();
        }
    }

    public void updateGen0() {
        synchronized (lock) {
            gen0 = clone(cells);
        }
    }

    public void restoreGen0() {
        if (gen0 != null) {
            synchronized (lock) {
                cells = clone(gen0);
                invalidate();
            }
        }
    }

    public void load(Life life) {
        running = false;
        cells = life.getCells();
        /*checkCellsDocumented();
        recountNeighbours();*/
        updateGen0();
        startX = startY = 0;
        invalidate();
    }

    private void checkCellsDocumented() {
        int numOfCells = cells.size();
        for (int i = 0; i < numOfCells; i++) {
            checkNeighboursDocumented(cells.get(i)[0], cells.get(i)[1]);
        }
    }

    private void checkNeighboursDocumented(short x, short y) {
        for (short[] neighbour : new short[][]{{(short) (x - 1), (short) (y - 1)}, {x, (short) (y - 1)},
                {(short) (x + 1), (short) (y - 1)}, {(short) (x - 1), y}, {(short) (x + 1), y},
                {(short) (x - 1), (short) (y + 1)}, {x, (short) (y + 1)}, {(short) (x + 1), (short) (y + 1)}}) {
            checkCellDocumented(neighbour[0], neighbour[1]);
        }
    }

    private void checkCellDocumented(short x, short y) {
        for (short[] cell : cells) {
            if (cell[0] == x && cell[1] == y) {
                return;
            }
        }

        cells.add(new short[]{x, y, 0, 0});
    }

    private void recountNeighbours() {
        for (int i = 0; i < cells.size(); i++) {
            short[] cell = cells.get(i);
            cells.set(i, new short[]{cell[0], cell[1], cell[2], neighbours(cell[0], cell[1])});
        }
    }

    public void setCellColor(@ColorInt int color) {
        cellPaint.setColor(color);
    }

    public void setGridColor(@ColorInt int color) {
        gridPaint.setColor(color);
    }


    public void init() {
        gridPaint = new Paint();
        gridPaint.setStyle(Paint.Style.STROKE);

        cellPaint = new Paint();
        cellPaint.setColor(MainActivity.getColor(getContext(), R.color.default_cell_color));
        cellPaint.setStyle(Paint.Style.FILL);
    }

    public ViewOfLife(Context context) {
        super(context);
        init();
    }

    public ViewOfLife(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewOfLife(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
}
