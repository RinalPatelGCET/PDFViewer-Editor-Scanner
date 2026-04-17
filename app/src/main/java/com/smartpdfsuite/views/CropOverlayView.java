package com.smartpdfsuite.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class CropOverlayView extends View {

    private static final float HANDLE_RADIUS = 18f;
    private static final float TOUCH_RADIUS = 50f;

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 0 TL, 1 TR, 2 BR, 3 BL
    // 4 TopMid, 5 RightMid, 6 BottomMid, 7 LeftMid
    private final PointF[] points = new PointF[8];

    private RectF imageRect;
    private int activePoint = -1;

    public CropOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        borderPaint.setColor(Color.GREEN);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setStyle(Paint.Style.STROKE);

        handlePaint.setColor(Color.WHITE);
        handlePaint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 8; i++) {
            points[i] = new PointF();
        }
    }

    public void setImageRect(RectF rect) {
        this.imageRect = rect;

        // 🔥 Set default crop inside image (NOT full screen)
        float padding = 60f;
// 🔥 Set crop area INSIDE IMAGE (like your screenshot)
        points[0].set(rect.left + padding, rect.top + padding);           // TL
        points[1].set(rect.right - padding, rect.top + padding);          // TR
        points[2].set(rect.right - padding, rect.bottom - padding);       // BR
        points[3].set(rect.left + padding, rect.bottom - padding);        // BL

        updateMidPoints();

       // initDefaultPoints();
        invalidate();
    }

    private void initDefaultPoints() {
        float l = imageRect.left;
        float t = imageRect.top;
        float r = imageRect.right;
        float b = imageRect.bottom;

        points[0].set(l, t);
        points[1].set(r, t);
        points[2].set(r, b);
        points[3].set(l, b);

        updateMidPoints();
    }

    private void updateMidPoints() {
        points[4].set(mid(points[0], points[1]));
        points[5].set(mid(points[1], points[2]));
        points[6].set(mid(points[2], points[3]));
        points[7].set(mid(points[3], points[0]));
    }

    private PointF mid(PointF a, PointF b) {
        return new PointF((a.x + b.x) / 2f, (a.y + b.y) / 2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (imageRect == null) return;

        Path path = new Path();
        path.moveTo(points[0].x, points[0].y);
        path.lineTo(points[1].x, points[1].y);
        path.lineTo(points[2].x, points[2].y);
        path.lineTo(points[3].x, points[3].y);
        path.close();

        canvas.drawPath(path, borderPaint);

        for (PointF p : points) {
            canvas.drawCircle(p.x, p.y, HANDLE_RADIUS, handlePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (imageRect == null) return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                activePoint = findTouchedPoint(x, y);
                return activePoint != -1;

            case MotionEvent.ACTION_MOVE:
                if (activePoint != -1) {

                    x = clamp(x, imageRect.left, imageRect.right);
                    y = clamp(y, imageRect.top, imageRect.bottom);

                    handlePointMove(activePoint, x, y);
                    updateMidPoints();
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                activePoint = -1;
                break;
        }

        return false;
    }

    private void handlePointMove(int index, float x, float y) {

        switch (index) {

            // 🔹 CORNERS (free move)
            case 0: case 1: case 2: case 3:
                points[index].set(x, y);
                break;

            // 🔹 TOP MID → move top edge vertically
            case 4:
                points[0].y = y;
                points[1].y = y;
                break;

            // 🔹 RIGHT MID → move right edge horizontally
            case 5:
                points[1].x = x;
                points[2].x = x;
                break;

            // 🔹 BOTTOM MID → move bottom edge vertically
            case 6:
                points[2].y = y;
                points[3].y = y;
                break;

            // 🔹 LEFT MID → move left edge horizontally
            case 7:
                points[0].x = x;
                points[3].x = x;
                break;
        }
    }

    private int findTouchedPoint(float x, float y) {
        for (int i = 0; i < points.length; i++) {
            if (distance(points[i], x, y) < TOUCH_RADIUS) {
                return i;
            }
        }
        return -1;
    }

    private float distance(PointF p, float x, float y) {
        return (float) Math.hypot(p.x - x, p.y - y);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    // Only 4 corners used for cropping
    public PointF[] getCornerPoints() {
        return new PointF[]{
                new PointF(points[0].x, points[0].y),
                new PointF(points[1].x, points[1].y),
                new PointF(points[2].x, points[2].y),
                new PointF(points[3].x, points[3].y)
        };
    }
}   



