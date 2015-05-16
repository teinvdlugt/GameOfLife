package com.teinproductions.tein.gameoflife;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ViewOfLife extends View {

    private float pixelsPerCell;

    private boolean[][] field;

    private boolean running = false;

    public enum EditMode {ADD, REMOVE}

    private EditMode editMode = EditMode.ADD;

    private float previousMovePositionX = -1;
    private float previousMovePositionY = -1;

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
        if (field == null) initField();

        drawBlocks(canvas);
        drawGrid(canvas);
    }

    private void initField() {
        pixelsPerCell = 30;
        int verCells = (int) Math.floor(getHeight() / pixelsPerCell);
        int horCells = (int) Math.floor(getWidth() / pixelsPerCell);

        // Otherwise, the last lines from the grid won't be drawn:
        if (getHeight() % pixelsPerCell == 0) verCells--;
        if (getWidth() % pixelsPerCell == 0) horCells--;

        field = new boolean[verCells][horCells];
    }

    private void drawBlocks(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.block_color));

        for (int y = 0; y < field.length; y++) {
            for (int x = 0; x < field[0].length; x++) {
                if (field[y][x]) {
                    float left = x * pixelsPerCell;
                    float right = (x + 1) * pixelsPerCell;
                    float top = y * pixelsPerCell;
                    float bottom = (y + 1) * pixelsPerCell;

                    canvas.drawRect(left, top, right, bottom, paint);
                }
            }
        }
    }

    private void drawGrid(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(android.R.color.black));

        // Vertical lines
        for (int x = 0; x < field[0].length; x++) {
            canvas.drawLine(x * pixelsPerCell, 0, x * pixelsPerCell, field.length * pixelsPerCell, paint);
        }
        // Last vertical line:
        // TODO last line not drawn
        canvas.drawLine(field[0].length * pixelsPerCell, 0,
                field[0].length * pixelsPerCell, field.length * pixelsPerCell, paint);

        // Horizontal lines
        for (int y = 0; y < field.length; y++) {
            canvas.drawLine(0, y * pixelsPerCell, field[0].length * pixelsPerCell, y * pixelsPerCell, paint);
        }
        // Last horizontal line:
        canvas.drawLine(0, field.length * pixelsPerCell,
                field[0].length * pixelsPerCell, field.length * pixelsPerCell, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        initField();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            try {
                int x = (int) Math.floor(event.getX() / pixelsPerCell);
                int y = (int) Math.floor(event.getY() / pixelsPerCell);

                previousMovePositionX = event.getX();
                previousMovePositionY = event.getY();

                switch (editMode) {
                    case ADD:
                        field[y][x] = true;
                        break;
                    case REMOVE:
                        field[y][x] = false;
                }

                invalidate();
                return true;
            } catch (ArrayIndexOutOfBoundsException e) {
                return super.onTouchEvent(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            try {
                int x = (int) Math.floor(event.getX() / pixelsPerCell);
                int y = (int) Math.floor(event.getY() / pixelsPerCell);

                allCellsOnLine(event);

                previousMovePositionX = event.getX();
                previousMovePositionY = event.getY();

                switch (editMode) {
                    case ADD:
                        field[y][x] = true;
                        break;
                    case REMOVE:
                        field[y][x] = false;
                }

                invalidate();
                return true;
            } catch (ArrayIndexOutOfBoundsException e) {
                return super.onTouchEvent(event);
            }
        } else return super.onTouchEvent(event);
    }

    // TODO needs enhancement!
    private void allCellsOnLine(MotionEvent event) {
        float xDiff = event.getX() - previousMovePositionX;
        float yDiff = event.getY() - previousMovePositionY;

        double lengthOfLine = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
        int amountOfCells = (int) Math.ceil(lengthOfLine / pixelsPerCell);

        if (amountOfCells < 1) return;

        float xSpacing = xDiff / amountOfCells;
        float ySpacing = yDiff / amountOfCells;

        float currX = previousMovePositionX, currY = previousMovePositionY;
        while (currX <= event.getX() && currY <= event.getY()) {
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis() + 100;
            int metaState = 0;
            MotionEvent motionEvent = MotionEvent.obtain(
                    downTime, eventTime,
                    MotionEvent.ACTION_DOWN,
                    currX, currY, metaState);

            dispatchTouchEvent(motionEvent);

            currX += xSpacing;
            currY += ySpacing;
        }
    }


    public void start() {
        Log.d("START", "start() begin");
        if (running) return;
        running = true;
        Log.d("START", "start()");

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (1 == 1) {
                    if (!running) return;

                    post(new Runnable() {
                        @Override
                        public void run() {
                            nextGeneration();
                        }
                    });

                    try {
                        Thread.sleep(100);
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

    public void clear() {
        field = new boolean[field.length][field[0].length];
        invalidate();
    }

    /**
     * This method is called zoomOutY because you only specify the rows to add at the top and bottom.
     * The columns to add to the left and right are then calculated automatically from the new
     * pixelsPerCell and the width of the view.
     *
     * @param addTop    The rows to add to the top of the existing field
     * @param addBottom The rows to add to the bottom of the existing field
     */
    public void zoomOutY(int addTop, int addBottom) {
        expandFieldY(addTop, addBottom);
        invalidate();
    }

    private void expandFieldY(int addTop, int addBottom) {
        boolean[][] newField = new boolean[field.length + addTop + addBottom][field[0].length];

        pixelsPerCell = getHeight() / (newField.length);

        for (int i = 0; i < field.length; i++) {
            if (!(addTop - i > 0)) {
                // This is not a new, empty row
                newField[i + addTop] = field[i];
            }
        }

        // Zoomed out over y, now adapt amount of x cells to the width and the new pixelsPerCell
        int newHorCells = (int) Math.floor(getWidth() / pixelsPerCell);
        int diffX = newHorCells - field[0].length;
        boolean[][] newNewField = new boolean[newField.length][newHorCells];

        for (int i = 0; i < newField.length; i++) {
            newNewField[i] = expandRow(newField[i], diffX / 2, diffX / 2 + diffX % 2);
        }


        field = newNewField;

        Log.d("draw problems", "new width: " + field[0].length);
        Log.d("draw problems", "new height: " + field.length);
    }

    private boolean[] expandRow(boolean[] row, int addLeft, int addRight) {
        boolean[] newRow = new boolean[row.length + addLeft + addRight];

        for (int i = 0; i < row.length; i++) {
            if (addLeft - i > 0) {
                // This is a new cell, leave it empty
            } else {
                newRow[i + addLeft] = row[i];
            }
        }

        return newRow;
    }


    public float getPixelsPerCell() {
        return pixelsPerCell;
    }

    public void setPixelsPerCell(float pixelsPerCell) {
        this.pixelsPerCell = pixelsPerCell;
        invalidate();
    }

    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

    public EditMode getEditMode() {
        return editMode;
    }

    public boolean isRunning() {
        return running;
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
