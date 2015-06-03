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

    public enum EditMode {ADD, REMOVE, ZOOM_IN}

    private EditMode editMode = EditMode.ADD;

    private float previousMovePositionX = -1;
    private float previousMovePositionY = -1;
    private boolean zoomingIn = false;
    private float startZoomInPositionX = -1;
    private float startZoomInPositionY = -1;

    private int zoomInLeft, zoomInRight, zoomInTop, zoomInBottom;

    /**
     * Alert: invalidate() is not called in this method
     * The rules of Game of Life:
     * - A dead cell with exactly three living neighbours, awakens.
     * - A living cell with two or three living neighbours, stay alive.
     * - A living cell with less than two living neighbours, dies.
     * - A living cell with more than three living neighbours, dies.
     */
    private void nextGeneration() {
        boolean[][] fieldCache = new boolean[field.length][field[0].length];
        for (int y = 0; y < field.length; y++) {
            System.arraycopy(field[y], 0, fieldCache[y], 0, field[0].length);
        }

        boolean autoZoomOut = pixelsPerCell >= 2 && autoZoom;
        boolean expandLeft = false, expandRight = false, expandTop = false, expandBottom = false;

        for (int y = 0; y < fieldCache.length; y++) {
            for (int x = 0; x < fieldCache[0].length; x++) {
                int neighbours = livingNeighbours(fieldCache, x, y);
                if (neighbours > 3 || neighbours < 2) field[y][x] = false;
                if (neighbours == 3) {
                    field[y][x] = true;
                }

                // Check if it is needed to zoom out
                if (autoZoomOut && field[y][x]) {
                    if (y < 2) expandTop = true;
                    if (x > field[0].length - 3) expandRight = true;
                    if (y > field.length - 3) expandBottom = true;
                    if (x < 2) expandLeft = true;
                }
            }
        }

        if (autoZoomOut && (expandTop || expandBottom || expandLeft || expandRight)) {
            boolean zoomOut = zoomInLeft == 0 && zoomInRight == field[0].length - 1
                    && zoomInTop == 0 && zoomInBottom == field.length - 1;
            expandField(expandLeft ? 2 : 0, expandRight ? 2 : 0, expandTop ? 2 : 0, expandBottom ? 2 : 0);

            if (zoomOut) {
                // Zoom out
                zoomFitField();
            }
        }
    }

    private int livingNeighbours(boolean[][] field, int x, int y) {
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

        zoomInLeft = 0;
        zoomInTop = 0;
        zoomInRight = horCells - 1;
        zoomInBottom = verCells - 1;

        field = new boolean[verCells][horCells];
    }

    private void drawBlocks(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.block_color));

        for (int y = 0; y < visibleHeight(); y++) {
            for (int x = 0; x < visibleWidth(); x++) {
                if (field[y + zoomInTop][x + zoomInLeft]) {
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

        int visibleWidth = visibleWidth();
        int visibleHeight = visibleHeight();

        // Vertical lines
        for (int x = 0; x < visibleWidth; x++) {
            canvas.drawLine(x * pixelsPerCell, 0, x * pixelsPerCell, visibleHeight * pixelsPerCell, paint);
        }
        // Last vertical line:
        float x = visibleWidth * pixelsPerCell;
        if (x == getWidth()) x--;
        canvas.drawLine(x, 0, x, visibleHeight * pixelsPerCell, paint);

        // Horizontal lines
        for (int y = 0; y < visibleHeight; y++) {
            canvas.drawLine(0, y * pixelsPerCell, visibleWidth * pixelsPerCell, y * pixelsPerCell, paint);
        }
        // Last horizontal line:
        float y = visibleHeight * pixelsPerCell;
        if (y == getHeight()) y--;
        canvas.drawLine(0, y, visibleWidth * pixelsPerCell, y, paint);
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
                if (editMode == EditMode.ADD || editMode == EditMode.REMOVE) {
                    int x = (int) (event.getX() / pixelsPerCell) + zoomInLeft;
                    int y = (int) (event.getY() / pixelsPerCell) + zoomInTop;
                    previousMovePositionX = event.getX();
                    previousMovePositionY = event.getY();

                    field[y][x] = editMode == EditMode.ADD;

                    invalidate();
                } else if (editMode == EditMode.ZOOM_IN && !zoomingIn) {
                    zoomingIn = true;
                    startZoomInPositionX = event.getX();
                    startZoomInPositionY = event.getY();
                }

                return true;
            } catch (ArrayIndexOutOfBoundsException e) {
                return super.onTouchEvent(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE
                && (editMode == EditMode.ADD || editMode == EditMode.REMOVE)) {
            try {
                int x = (int) (event.getX() / pixelsPerCell) + zoomInLeft;
                int y = (int) (event.getY() / pixelsPerCell) + zoomInTop;

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
        } else if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                && editMode == EditMode.ZOOM_IN && zoomingIn) {
            int startX = (int) (startZoomInPositionX / pixelsPerCell);
            int startY = (int) (startZoomInPositionY / pixelsPerCell);
            int stopX = (int) (event.getX() / pixelsPerCell);
            int stopY = (int) (event.getY() / pixelsPerCell);

            zoomingIn = false;

            if (startX > stopX) {
                int temp = startX;
                startX = stopX;
                stopX = temp;
            } else if (startX == stopX) return true;

            if (startY > stopY) {
                int temp = startY;
                startY = stopY;
                stopY = temp;
            } else if (startY == stopY) return true;

            zoomIn(startX, stopX, startY, stopY);

            return true;
        } else return super.onTouchEvent(event);
    }

    // TODO needs enhancement!
    private void allCellsOnLine(MotionEvent event) {
        float xDiff = event.getX() - previousMovePositionX;
        float yDiff = event.getY() - previousMovePositionY;

        double lengthOfLine = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
        int amountOfCells = (int) Math.ceil(lengthOfLine / pixelsPerCell); // TODO is Math.ceil correct?!?

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
                while (1 + 1 == 2) {
                    if (!running) return;

                    long millis = System.currentTimeMillis();

                    nextGeneration();
                    postInvalidate();

                    long takenTime = System.currentTimeMillis() - millis;
                    long sleepTime;
                    if (takenTime > speed) sleepTime = takenTime;
                    else sleepTime = speed - takenTime;

                    try {
                        Thread.sleep(sleepTime);
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


    public void zoomOut() {
        if (pixelsPerCell < 2) return;

        int width = visibleWidth();
        int pixelsPerCell = (int) this.pixelsPerCell;
        pixelsPerCell--;

        do {
            width++;
        } while (getWidth() / width >= pixelsPerCell);

        pixelsPerCell = getWidth() / width;
        zoomOutPixels((int) this.pixelsPerCell - pixelsPerCell);
    }

    public void zoomOutPixels(int amount) {
        if (pixelsPerCell - amount < 1) return;

        pixelsPerCell -= amount;
        int newYCells = (int) (getHeight() / pixelsPerCell);
        int diffYCells = newYCells - visibleHeight();
        boolean modulusTop = Math.floor(Math.random() * 2) == 1;
        int addTop = diffYCells / 2 + (modulusTop ? diffYCells % 2 : 0);
        int addBottom = diffYCells / 2 + (modulusTop ? 0 : diffYCells % 2);

        zoomOut(0, 0, addTop, addBottom);
        fillXSpace();
        invalidate();
    }

    private void expandField(int expandLeft, int expandRight, int expandTop, int expandBottom) {
        boolean[][] newField = new boolean[field.length + expandTop + expandBottom]
                [field[0].length + expandLeft + expandRight];

        for (int y = 0; y < field.length; y++) {
            // The y index of newField that corresponds to the y index in field:
            int corrY = y + expandTop;
            for (int x = 0; x < field[0].length; x++) {
                // The x index of newField that corresponds to the x index in field:
                int corrX = x + expandLeft;
                newField[corrY][corrX] = field[y][x];
            }
        }

        field = newField;

        zoomInTop += expandTop;
        zoomInBottom += expandTop;
        zoomInLeft += expandLeft;
        zoomInRight += expandLeft;
    }

    private void zoomOut(int addLeft, int addRight, int addTop, int addBottom) {
        int expandLeft = addLeft - zoomInLeft;
        int expandRight = addRight - field[0].length + zoomInRight + 1;
        int expandTop = addTop - zoomInTop;
        int expandBottom = addBottom - field.length + zoomInBottom + 1;
        if (expandLeft < 0) expandLeft = 0;
        if (expandRight < 0) expandRight = 0;
        if (expandTop < 0) expandTop = 0;
        if (expandBottom < 0) expandBottom = 0;

        if (expandTop != 0 || expandBottom != 0) {
            expandField(expandLeft, expandRight, expandTop, expandBottom);
            //expandFieldY(expandTop, expandBottom, false, false);
        }

        zoomInLeft -= addLeft;
        zoomInRight += addRight;
        zoomInTop -= addTop;
        zoomInBottom += addBottom;
    }


    /**
     * Zooms so that all living cells are visible
     */
    public void zoomFit() {
        cutFit();
        zoomFitField();
        invalidate();
    }

    /**
     * Cuts the field to make it as small as possible
     */
    private void cutFit() {
        int startX = lowestX();
        int stopX = highestX();
        int startY = lowestY();
        int stopY = highestY();

        if (startX == -1 || stopX == -1 || startY == -1 || stopY == -1) {
            // There is no living cell in the field
            initField();
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

        cut(startX, stopX, startY, stopY);
    }

    private int lowestX() {
        int lowestX = -1;

        for (boolean[] row : field) {
            for (int x = 0; x < field[0].length; x++) {
                if (x >= lowestX && lowestX != -1) break;
                if (row[x]) lowestX = x;
            }
        }

        return lowestX;
    }

    private int highestX() {
        int highestX = -1;

        for (boolean[] row : field) {
            for (int x = field[0].length - 1; x >= 0; x--) {
                if (x <= highestX) break;
                if (row[x]) highestX = x;
            }
        }

        return highestX;
    }

    private int lowestY() {
        for (int y = 0; y < field.length; y++) {
            // If this row contains a true, then immediately return that
            for (int x = 0; x < field[0].length; x++) {
                if (field[y][x]) return y;
            }
        }

        return -1;
    }

    private int highestY() {
        for (int y = field.length - 1; y >= 0; y--) {
            for (int x = 0; x < field[0].length; x++) {
                if (field[y][x]) return y;
            }
        }

        return -1;
    }

    private void cut(int startX, int stopX, int startY, int stopY) {
        boolean[][] newField = new boolean[stopY - startY + 1][stopX - startX + 1];

        for (int y = 0; y < newField.length; y++) {
            // The y index in field that corresponds with the y index in newField
            int corrY = y + startY;
            for (int x = 0; x < newField[0].length; x++) {
                // The x index in field that corresponds with the x index in newField
                int corrX = x + startX;
                newField[y][x] = field[corrY][corrX];
            }
        }

        field = newField;
    }

    private void zoomFitField() {
        zoomInLeft = 0;
        zoomInRight = field[0].length - 1;
        zoomInTop = 0;
        zoomInBottom = field.length - 1;

        int pPCX = getWidth() / field[0].length;
        int pPCY = getHeight() / field.length;
        pixelsPerCell = pPCX < pPCY ? pPCX : pPCY;

        fillXSpace();
        fillYSpace();
    }

    private void zoomIn(int startX, int stopX, int startY, int stopY) {
        // The following order can't be changed, because when
        // zoomInLeft changes, visibleWidth() also changes
        zoomInRight -= visibleWidth() - stopX - 1;
        zoomInLeft += startX;
        zoomInBottom -= visibleHeight() - stopY - 1;
        zoomInTop += startY;

        int pPCX = getWidth() / visibleWidth();
        int pPCY = getHeight() / visibleHeight();
        pixelsPerCell = pPCX < pPCY ? pPCX : pPCY;

        fillXSpace();
        fillYSpace();

        invalidate();
    }


    private void fillXSpace() {
        int cellsX = (int) (getWidth() / pixelsPerCell);
        int diffX = cellsX - visibleWidth();

        if (diffX <= 0) return;

        boolean modulusLeft = randomBoolean();
        int addLeft = diffX / 2 + (modulusLeft ? diffX % 2 : 0);
        int addRight = diffX / 2 + (modulusLeft ? 0 : diffX % 2);

        // Expand the field if needed
        int expandLeft = addLeft - zoomInLeft;
        int expandRight = addRight - field[0].length + zoomInRight + 1;
        if (expandLeft < 0) expandLeft = 0;
        if (expandRight < 0) expandRight = 0;
        if (expandLeft != 0 || expandRight != 0) {
            expandField(expandLeft, expandRight, 0, 0);
            //expandFieldX(expandLeft, expandRight, false, false);
        }

        zoomInLeft -= addLeft;
        zoomInRight += addRight;
    }

    private void fillYSpace() {
        int cellsY = (int) (getHeight() / pixelsPerCell);
        int diffY = cellsY - visibleHeight();

        if (diffY <= 0) return;

        boolean modulusTop = randomBoolean();
        int addTop = diffY / 2 + (modulusTop ? diffY % 2 : 0);
        int addBottom = diffY / 2 + (modulusTop ? 0 : diffY % 2);

        // Expand the field if needed
        int expandTop = addTop - zoomInTop;
        int expandBottom = addBottom - field.length + zoomInBottom + 1;
        if (expandTop < 0) expandTop = 0;
        if (expandBottom < 0) expandBottom = 0;
        if (expandTop != 0 || expandBottom != 0) {
            expandField(0, 0, expandTop, expandBottom);
            //expandFieldY(expandTop, expandBottom, false, false);
        }

        zoomInTop -= addTop;
        zoomInBottom += addBottom;
    }


    public int visibleWidth() {
        return zoomInRight - zoomInLeft + 1;
    }

    public int visibleHeight() {
        return zoomInBottom - zoomInTop + 1;
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
