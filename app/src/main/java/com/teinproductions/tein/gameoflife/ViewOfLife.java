package com.teinproductions.tein.gameoflife;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ViewOfLife extends View {

    private float pixelsPerCell;

    private boolean[][] field;

    private boolean running = false;

    private boolean autoZoom = true;
    private int speed = 100;

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

        boolean autoZoomOut = pixelsPerCell >= 2 && autoZoom;
        boolean zoomOutTop = false, zoomOutRight = false, zoomOutBottom = false, zoomOutLeft = false;

        for (int y = 0; y < fieldCache.length; y++) {
            for (int x = 0; x < fieldCache[0].length; x++) {
                int neighbours = livingNeighbours(fieldCache, x, y);
                if (neighbours > 3 || neighbours < 2) field[y][x] = false;
                if (neighbours == 3) field[y][x] = true;

                // Check if it is needed to zoom out
                if (autoZoomOut && field[y][x]) {
                    if (y < 2) zoomOutTop = true;
                    if (x > field[0].length - 3) zoomOutRight = true;
                    if (y > field.length - 3) zoomOutBottom = true;
                    if (x < 2) zoomOutLeft = true;
                }
            }
        }

        if (autoZoomOut) {
            if ((zoomOutTop || zoomOutBottom) && (zoomOutLeft || zoomOutRight)) {
                // Zoom out over both x and y
                if (getHeight() > getWidth()) {
                    // Zoom out over y
                    expandFieldY(zoomOutTop ? 2 : 0, zoomOutBottom ? 2 : 0, true, true);
                } else {
                    // Zoom out over x
                    expandFieldX(zoomOutLeft ? 2 : 0, zoomOutRight ? 2 : 0, true, true);
                }
            } else if (zoomOutTop || zoomOutBottom) {
                // Zoom out only over y
                expandFieldY(zoomOutTop ? 2 : 0, zoomOutBottom ? 2 : 0, true, true);
            } else if (zoomOutLeft || zoomOutRight) {
                // Zoom out only over x
                expandFieldX(zoomOutLeft ? 2 : 0, zoomOutRight ? 2 : 0, true, true);
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
        if (pixelsPerCell >= 12) drawGrid(canvas);
    }

    private void initField() {
        pixelsPerCell = 30;
        int verCells = (int) (getHeight() / pixelsPerCell);
        int horCells = (int) (getWidth() / pixelsPerCell);

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
        float x = field[0].length * pixelsPerCell;
        if (x == getWidth()) x--;
        canvas.drawLine(x, 0, x, field.length * pixelsPerCell, paint);

        // Horizontal lines
        for (int y = 0; y < field.length; y++) {
            canvas.drawLine(0, y * pixelsPerCell, field[0].length * pixelsPerCell, y * pixelsPerCell, paint);
        }
        // Last horizontal line:
        float y = field.length * pixelsPerCell;
        if (y == getHeight()) y--;
        canvas.drawLine(0, y, field[0].length * pixelsPerCell, y, paint);
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
                int x = (int) (event.getX() / pixelsPerCell);
                int y = (int) (event.getY() / pixelsPerCell);

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
                int x = (int) (event.getX() / pixelsPerCell);
                int y = (int) (event.getY() / pixelsPerCell);

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
        if (running) return;
        running = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (1 == 1) {
                    if (!running) return;

                    //long millis = System.currentTimeMillis();

                    post(new Runnable() {
                        @Override
                        public void run() {
                            //long millis = System.currentTimeMillis();
                            nextGeneration();
                            //Log.d("asdfasdf", "elapsed time: " + (System.currentTimeMillis() - millis) + " ms");
                        }
                    });

                    //Log.d("asdfasdf", "elapsed time in thread: " + (System.currentTimeMillis() - millis) + " ms");

                    try {
                        Thread.sleep(speed);
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


    public void zoomOutPixels(int amount) {
        if (pixelsPerCell < 2) return;

        pixelsPerCell -= amount;
        int newYCells = (int) (getHeight() / pixelsPerCell);
        int diffYCells = newYCells - field.length;

        boolean modulusTop = Math.floor(Math.random() * 2) == 1;
        int addTop = diffYCells / 2 + (modulusTop ? diffYCells % 2 : 0);
        int addBottom = diffYCells / 2 + (modulusTop ? 0 : diffYCells % 2);

        zoomOutY(addTop, addBottom, false, true);
    }

    /**
     * Accounts for this method and for expandFieldY():
     * This method is called zoomOutY because you only specify the rows to add at the top and bottom.
     * The columns to add to the left and right are then calculated automatically from the new
     * pixelsPerCell and the width of the view.
     *
     * @param addTop    The rows to add to the top of the existing field
     * @param addBottom The rows to add to the bottom of the existing field
     * @param changePPC Whether or not pixelsPerCell must be recalculated.
     *                  The third line in expandFieldY() caused trouble when
     *                  having a height of 720 px.
     * @param changeX   Whether or not the amount of horizontal cells has to be
     *                  recalculated.
     */
    public void zoomOutY(int addTop, int addBottom, boolean changePPC, boolean changeX) {
        if (pixelsPerCell < 2) return;

        expandFieldY(addTop, addBottom, changePPC, changeX);
        invalidate();
    }

    private void expandFieldY(int addTop, int addBottom, boolean changePPC, boolean changeX) {
        boolean[][] newField = new boolean[field.length + addTop + addBottom][field[0].length];

        if (changePPC) pixelsPerCell = getHeight() / (newField.length);

        for (int y = 0; y < field.length; y++) {
            // The y index of newField that corresponds to the y index in field:
            int corrY = y + addTop;
            newField[corrY] = field[y];
        }

        if (!changeX) {
            field = newField;
            return;
        }

        // Zoomed out over y, now adapt amount of x cells to the width and new pixelsPerCell
        int newHorCells = (int) (getWidth() / pixelsPerCell);
        int diffX = newHorCells - field[0].length;
        boolean[][] newNewField = new boolean[newField.length][newHorCells];

        boolean modulusLeft = ((int) Math.floor(Math.random() * 2)) == 1;
        for (int i = 0; i < newField.length; i++) {
            int addLeft = diffX / 2 + (modulusLeft ? diffX % 2 : 0);
            int addRight = diffX / 2 + (modulusLeft ? 0 : diffX % 2);

            newNewField[i] = expandRow(newField[i], addLeft, addRight);
        }


        field = newNewField;
    }

    private boolean[] expandRow(boolean[] row, int addLeft, int addRight) {
        boolean[] newRow = new boolean[row.length + addLeft + addRight];

        for (int i = 0; i < row.length; i++) {
            if (addLeft - i <= 1) {
                newRow[i + addLeft] = row[i];
            }
        }

        return newRow;
    }


    // TODO needs enhancement
    public void zoomFit() {
        int startX = lowestX();
        int stopX = highestX();
        int startY = lowestY();
        int stopY = highestY();

        if (startX == -1 || stopX == -1 || startY == -1 || stopY == -1) {
            field = null;
            invalidate();
            return;
        }

        // Add a 'padding' of two if possible:
        if (startX == 1) startX = 0;
        if (startX > 1) startX -= 2;
        if (stopX == field[0].length - 2) stopX = field[0].length - 1;
        if (stopX < field[0].length - 2) stopX += 2;
        if (startY == 1) startY = 0;
        if (startY > 1) startY -= 2;
        if (stopY == field.length - 2) stopY = field.length - 1;
        if (stopY < field.length - 2) stopY += 2;

        zoomIn(startX, stopX, startY, stopY);
        invalidate();
    }

    public int lowestX() {
        int lowestX = -1;

        for (boolean[] row : field) {
            for (int x = 0; x < field[0].length; x++) {
                if (x >= lowestX && lowestX != -1) break;
                if (row[x]) lowestX = x;
            }
        }

        return lowestX;
    }

    public int highestX() {
        int highestX = -1;

        for (boolean[] row : field) {
            for (int x = field[0].length - 1; x >= 0; x--) {
                if (x <= highestX) break;
                if (row[x]) highestX = x;
            }
        }

        return highestX;
    }

    public int lowestY() {
        for (int y = 0; y < field.length; y++) {
            // If this row contains a true, then immediately return that
            for (int x = 0; x < field[0].length; x++) {
                if (field[y][x]) return y;
            }
        }

        return -1;
    }

    public int highestY() {
        for (int y = field.length - 1; y >= 0; y--) {
            for (int x = 0; x < field[0].length; x++) {
                if (field[y][x]) return y;
            }
        }

        return -1;
    }

    public void zoomIn(int startX, int stopX, int startY, int stopY) {
        boolean[][] newField = new boolean[stopY - startY + 1][stopX - startX + 1];

        for (int y = 0; y < newField.length; y++) {
            // The y index in field that corresponds with the y index in newField
            int corrY = y + startY;
            for (int x = 0; x < newField[0].length; x++) {
                int corrX = x + startX;
                newField[y][x] = field[corrY][corrX];
            }
        }

        field = newField;

        // Now that the new field is constructed, adapt the pixelsPerCell:
        float pPCX = getWidth() / field[0].length;
        float pPCY = getHeight() / field.length;

        pixelsPerCell = pPCX < pPCY ? pPCX : pPCY;
        if (Math.floor(pPCX) == Math.floor(pPCY)) return;

        // Extend the field again to fill the remaining space:
        if (pixelsPerCell == pPCX) {
            // Then the y space has to be filled up
            int cellsY = (int) (getHeight() / pixelsPerCell);
            int diffY = cellsY - newField.length;

            boolean modulusTop = Math.floor(Math.random() * 2) == 1;
            int addTop = diffY / 2 + (modulusTop ? diffY % 2 : 0);
            int addBottom = diffY / 2 + (modulusTop ? 0 : diffY % 2);

            expandFieldY(addTop, addBottom, false, false);
        } else {
            // Then the x space has to be filled up
            int cellsX = (int) (getWidth() / pixelsPerCell);
            int diffX = cellsX - newField[0].length;

            boolean modulusLeft = Math.floor(Math.random() * 2) == 1;
            int addLeft = diffX / 2 + (modulusLeft ? diffX % 2 : 0);
            int addRight = diffX / 2 + (modulusLeft ? 0 : diffX % 2);

            expandFieldX(addLeft, addRight, false, false);
        }
    }

    private void expandFieldX(int addLeft, int addRight, boolean changePPC, boolean changeY) {
        boolean[][] newField = new boolean[field.length][field[0].length + addLeft + addRight];

        if (changePPC) pixelsPerCell = getWidth() / newField[0].length;

        for (int y = 0; y < field.length; y++) {
            for (int x = 0; x < field[0].length; x++) {
                // The x index in newField that corresponds to the x index in field:
                int corrX = x + addLeft;
                newField[y][corrX] = field[y][x];
            }
        }

        field = newField;
        if (!changeY) return;

        // Now fill the y space:
        int cellsY = (int) (getHeight() / pixelsPerCell);
        int diffY = cellsY - field.length;
        boolean modulusTop = randomBoolean();
        int addTop = diffY / 2 + (modulusTop ? diffY % 2 : 0);
        int addBottom = diffY / 2 + (modulusTop ? 0 : diffY % 2);

        expandFieldY(addTop, addBottom, false, false);
    }


    public void zoomOutPixels() {
        zoomOutPixels(1);
    }

    public float getPixelsPerCell() {
        return pixelsPerCell;
    }

    public void setPixelsPerCell(float pixelsPerCell) {
        this.pixelsPerCell = pixelsPerCell;
        invalidate();
    }

    public int getFieldWidth() {
        return field[0].length;
    }

    public int getFieldHeight() {
        return field.length;
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

    public boolean isAutoZoom() {
        return autoZoom;
    }

    public void setAutoZoom(boolean autoZoom) {
        this.autoZoom = autoZoom;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
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


    public static boolean randomBoolean() {
        return Math.floor(Math.random() * 2) == 1;
    }
}
