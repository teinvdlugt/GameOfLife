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

    private int cellWidth = 50;
    private float startX = 0, startY = 0;
    /**
     * 0: x position
     * 1: y position
     * 2: alive [0|1]
     * 3: num of neighbours
     */
    private List<short[]> cells = new ArrayList<>();
    private final Object lock = new Object();
    private Paint gridPaint, cellPaint;
    //private Paint deadCellPaint;

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

        synchronized (lock) {
            for (int x = pixelXStart + 1; x < width; x += cellWidth) {
                short cellY = cellYStart;
                for (int y = pixelYStart + 1; y < height; y += cellWidth) {

                    // Check if the cell is alive
                    for (short[] cell : cells) {
                        if (cell[0] == cellX && cell[1] == cellY) {
                            if (cell[2] == 1) {
                                // Cell is alive
                                canvas.drawRect(x, y, x + cellWidth, y + cellWidth, cellPaint);
                            }/* else {
                                // Cell is dead, draw when debugging
                                canvas.drawRect(x, y, x + cellWidth, y + cellWidth, deadCellPaint);
                            }*/
                            break;
                        }
                    }

                    cellY++;
                }
                cellX++;
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

    private static short roundAwayFromZero(float i) {
        if (i > 0) return (short) Math.ceil(i);
        else return (short) Math.floor(i);
    }

    private float prevXDrag = -1, prevYDrag = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() / cellWidth + startX;
        float y = event.getY() / cellWidth + startY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (touchMode) {
                    case TOUCH_MODE_ADD:
                        synchronized (lock) {
                            makeAlive((short) x, (short) y);
                        }
                        invalidate();
                        return true;
                    case TOUCH_MODE_REMOVE:
                        synchronized (lock) {
                            makeDead((short) x, (short) y);
                        }
                        invalidate();
                        return true;
                    case TOUCH_MODE_MOVE:
                        prevXDrag = x;
                        prevYDrag = y;
                        invalidate();
                        return true;
                    default:
                        return super.onTouchEvent(event);
                }
            case MotionEvent.ACTION_MOVE:
                switch (touchMode) {
                    case TOUCH_MODE_ADD:
                        synchronized (lock) {
                            makeAlive((short) x, (short) y);
                        }
                        invalidate();
                        return true;
                    case TOUCH_MODE_REMOVE:
                        synchronized (lock) {
                            makeDead((short) x, (short) y);
                        }
                        invalidate();
                        return true;
                    case TOUCH_MODE_MOVE:
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
            case MotionEvent.ACTION_UP:
                switch (touchMode) {
                    case TOUCH_MODE_ADD:
                        synchronized (lock) {
                            makeAlive((short) x, (short) y);
                        }
                        invalidate();
                        return true;
                    case TOUCH_MODE_REMOVE:
                        synchronized (lock) {
                            makeDead((short) x, (short) y);
                        }
                        invalidate();
                        return true;
                    default:
                        return super.onTouchEvent(event);
                }
            default:
                return super.onTouchEvent(event);
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
        // TODO: 19-11-2015 More efficient way? One loop, which checks each neighbour
        byte neighbours = 0;
        if (isAlive((short) (x - 1), (short) (y - 1))) neighbours++;
        if (isAlive(x, (short) (y - 1))) neighbours++;
        if (isAlive((short) (x + 1), (short) (y - 1))) neighbours++;
        if (isAlive((short) (x - 1), y)) neighbours++;
        if (isAlive((short) (x + 1), y)) neighbours++;
        if (isAlive((short) (x - 1), (short) (y + 1))) neighbours++;
        if (isAlive(x, (short) (y + 1))) neighbours++;
        if (isAlive((short) (x + 1), (short) (y + 1))) neighbours++;
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
                    nextGeneration();
                    postInvalidate();
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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

    public void clear() {
        synchronized (lock) {
            cells.clear();
            startX = startY = 0;
            invalidate();
        }
    }

    public void init() {
        gridPaint = new Paint();
        gridPaint.setStyle(Paint.Style.STROKE);

        cellPaint = new Paint();
        cellPaint.setColor(getColor(R.color.block_color));
        cellPaint.setStyle(Paint.Style.FILL);

        /*// For debugging:
        deadCellPaint = new Paint();
        deadCellPaint.setColor(getColor(android.R.color.darker_gray));
        deadCellPaint.setStyle(Paint.Style.FILL);*/

        makeAlive((short) 0, (short) 0);
        makeAlive((short) 1, (short) 1);
        makeAlive((short) 2, (short) 2);
        makeAlive((short) 2, (short) 3);
        makeAlive((short) 2, (short) 4);
        makeAlive((short) 2, (short) 5);
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
