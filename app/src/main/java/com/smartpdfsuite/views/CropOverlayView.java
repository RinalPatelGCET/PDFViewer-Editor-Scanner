package com.smartpdfsuite.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class CropOverlayView extends View {

    private static final float CORNER_RADIUS = 20f;
    private static final float TOUCH_RADIUS = 60f;
    private static final float DEFAULT_PADDING = 60f;

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final PointF[] corners = new PointF[4];

    private int activeCorner = -1;
    private boolean isInitialized = false;

    public CropOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        borderPaint.setColor(Color.GREEN);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setStyle(Paint.Style.STROKE);

        cornerPaint.setColor(Color.WHITE);
        cornerPaint.setStyle(Paint.Style.FILL);

        shadePaint.setColor(Color.parseColor("#88000000"));
        shadePaint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 4; i++) {
            corners[i] = new PointF();
        }
    }

    /* ---------------------------------------------------
       AUTO INIT WHEN VIEW SIZE IS READY
       --------------------------------------------------- */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w == 0 || h == 0) return;

        // Reserve space for bottom button (56dp)
        int bottomPadding = dpToPx(56);
        setPadding(0, 0, 0, bottomPadding);

        if (!isInitialized) {
            initDefaultCorners();
            isInitialized = true;
        }
    }

    /* --------------------------------------------------- */
    public void initDefaultCorners() {
        float left = DEFAULT_PADDING;
        float top = DEFAULT_PADDING;
        float right = getWidth() - DEFAULT_PADDING;
        float bottom = getHeight() - DEFAULT_PADDING - getPaddingBottom();

        corners[0].set(left, top);       // Top-left
        corners[1].set(right, top);      // Top-right
        corners[2].set(right, bottom);   // Bottom-right
        corners[3].set(left, bottom);    // Bottom-left

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isInitialized) return;

        // Draw crop rectangle
        Path path = new Path();
        path.moveTo(corners[0].x, corners[0].y);
        for (int i = 1; i < 4; i++) {
            path.lineTo(corners[i].x, corners[i].y);
        }
        path.close();

        canvas.drawPath(path, borderPaint);

        // Draw corners
        for (PointF corner : corners) {
            canvas.drawCircle(corner.x, corner.y, CORNER_RADIUS, cornerPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                activeCorner = findTouchedCorner(x, y);
                return activeCorner != -1;

            case MotionEvent.ACTION_MOVE:
                if (activeCorner != -1) {
                    corners[activeCorner].set(
                            clamp(x, 0, getWidth()),
                            clamp(y, 0, getHeight() - getPaddingBottom())
                    );
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activeCorner = -1;
                break;
        }
        return super.onTouchEvent(event);
    }

    private int findTouchedCorner(float x, float y) {
        for (int i = 0; i < 4; i++) {
            if (distance(corners[i].x, corners[i].y, x, y) <= TOUCH_RADIUS) {
                return i;
            }
        }
        return -1;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x1 - x2, y1 - y2);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    /* ---------------------------------------------------
       USED BY EdgeCropFragment
       --------------------------------------------------- */
    public PointF[] getOrderedPoints() {
        return new PointF[]{
                new PointF(corners[0].x, corners[0].y),
                new PointF(corners[1].x, corners[1].y),
                new PointF(corners[2].x, corners[2].y),
                new PointF(corners[3].x, corners[3].y)
        };
    }
}



/*
package com.smartpdfsuite.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class CropOverlayView extends View {

    private static final float CORNER_RADIUS = 20f;
    private static final float TOUCH_RADIUS = 60f;

    private final Paint borderPaint = new Paint();
    private final Paint cornerPaint = new Paint();
    private final Paint shadePaint = new Paint();

    private final PointF[] corners = new PointF[4];
    private int bitmapWidth = 0;
    private int bitmapHeight = 0;

    private int activeCorner = -1;

    public CropOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        borderPaint.setColor(Color.GREEN);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);

        cornerPaint.setColor(Color.WHITE);
        cornerPaint.setStyle(Paint.Style.FILL);
        cornerPaint.setAntiAlias(true);

        shadePaint.setColor(Color.parseColor("#88000000"));
        shadePaint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 4; i++) {
            corners[i] = new PointF();
        }
    }

    */
/* ---------------------------------------------------
       ✅ THIS METHOD FIXES YOUR ERROR
       --------------------------------------------------- *//*

    public void setBitmapSize(int width, int height) {
        if (width <= 0 || height <= 0) return;

        this.bitmapWidth = width;
        this.bitmapHeight = height;
    }

    */
/* --------------------------------------------------- *//*

    public void initDefaultCorners() {
        if (bitmapWidth == 0 || bitmapHeight == 0) return;

        float padding = 60f;

        corners[0].set(padding, padding); // top-left
        corners[1].set(bitmapWidth - padding, padding); // top-right
        corners[2].set(bitmapWidth - padding, bitmapHeight - padding); // bottom-right
        corners[3].set(padding, bitmapHeight - padding); // bottom-left

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (bitmapWidth == 0 || bitmapHeight == 0) return;

        Path path = new Path();
        path.moveTo(corners[0].x, corners[0].y);
        for (int i = 1; i < 4; i++) {
            path.lineTo(corners[i].x, corners[i].y);
        }
        path.close();

        canvas.drawPath(path, borderPaint);

        for (PointF corner : corners) {
            canvas.drawCircle(corner.x, corner.y, CORNER_RADIUS, cornerPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activeCorner = findTouchedCorner(x, y);
                return activeCorner != -1;

            case MotionEvent.ACTION_MOVE:
                if (activeCorner != -1) {
                    corners[activeCorner].set(
                            clamp(x, 0, bitmapWidth),
                            clamp(y, 0, bitmapHeight)
                    );
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activeCorner = -1;
                break;
        }
        return super.onTouchEvent(event);
    }

    private int findTouchedCorner(float x, float y) {
        for (int i = 0; i < 4; i++) {
            if (distance(corners[i].x, corners[i].y, x, y) < TOUCH_RADIUS) {
                return i;
            }
        }
        return -1;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x1 - x2, y1 - y2);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    */
/* ---------------------------------------------------
       USED BY EdgeCropFragment
       --------------------------------------------------- *//*

    public PointF[] getOrderedPoints() {
        return new PointF[]{
                new PointF(corners[0].x, corners[0].y),
                new PointF(corners[1].x, corners[1].y),
                new PointF(corners[2].x, corners[2].y),
                new PointF(corners[3].x, corners[3].y)
        };
    }
}


*/
/*

package com.smartpdfsuite.views;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;

public class CropOverlayView extends View {

    private Paint linePaint, pointPaint;
    private PointF[] points = new PointF[4];
    private int activePoint = -1;

    public CropOverlayView(Context context) {
        super(context);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(4);
        linePaint.setStyle(Paint.Style.STROKE);

        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.FILL);
    }

    public void initDefaultCorners(float w, float h) {
        points[0] = new PointF(50, 50);
        points[1] = new PointF(w - 50, 50);
        points[2] = new PointF(w - 50, h - 50);
        points[3] = new PointF(50, h - 50);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (points[0] == null) return;

        Path path = new Path();
        path.moveTo(points[0].x, points[0].y);
        for (int i = 1; i < 4; i++) path.lineTo(points[i].x, points[i].y);
        path.close();

        canvas.drawPath(path, linePaint);

        for (PointF p : points) {
            canvas.drawCircle(p.x, p.y, 15, pointPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < 4; i++) {
                if (distance(points[i], x, y) < 40) {
                    activePoint = i;
                    return true;
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && activePoint != -1) {
            points[activePoint].set(x, y);
            invalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            activePoint = -1;
        }
        return true;
    }

    private float distance(PointF p, float x, float y) {
        return (float) Math.hypot(p.x - x, p.y - y);
    }

    public PointF[] getOrderedPoints() {
        return points;
    }
}
*/

