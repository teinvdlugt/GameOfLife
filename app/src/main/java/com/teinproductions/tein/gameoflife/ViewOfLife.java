package com.teinproductions.tein.gameoflife;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ViewOfLife extends View {

    private int pixelsPerCell = 30;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(getResources().getColor(android.R.color.black));


        int verCells = (int) Math.floor(getHeight() / 30);
        int horCells = (int) Math.floor(getWidth() / 30);

        for (int x = 0; x < getWidth(); x += pixelsPerCell) {
            int stopY = verCells * pixelsPerCell;
            canvas.drawLine(x, 0, x, stopY, paint);
        }

        for (int y = 0; y < getHeight(); y += pixelsPerCell) {
            int stopX = horCells * pixelsPerCell;
            canvas.drawLine(0, y, stopX, y, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
