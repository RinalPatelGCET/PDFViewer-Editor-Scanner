package com.smartpdfsuite.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;

public class DocumentScanner {

    /**
     * Safe crop method – prevents width/height <= 0 crash
     */
    public static Bitmap fourPointTransform(
            Bitmap src,
            PointF tl,
            PointF tr,
            PointF br,
            PointF bl
    ) {
        if (src == null) return null;

        float widthA = distance(br, bl);
        float widthB = distance(tr, tl);
        float maxWidth = Math.max(widthA, widthB);

        float heightA = distance(tr, br);
        float heightB = distance(tl, bl);
        float maxHeight = Math.max(heightA, heightB);

        // 🔴 CRASH FIX (very important)
        if (maxWidth < 1 || maxHeight < 1) {
            return src; // return original instead of crash
        }

        Bitmap output = Bitmap.createBitmap(
                (int) maxWidth,
                (int) maxHeight,
                Bitmap.Config.ARGB_8888
        );

        Matrix matrix = new Matrix();

        float[] srcPts = {
                tl.x, tl.y,
                tr.x, tr.y,
                br.x, br.y,
                bl.x, bl.y
        };

        float[] dstPts = {
                0, 0,
                maxWidth, 0,
                maxWidth, maxHeight,
                0, maxHeight
        };

        matrix.setPolyToPoly(srcPts, 0, dstPts, 0, 4);

        android.graphics.Canvas canvas = new android.graphics.Canvas(output);
        canvas.drawBitmap(src, matrix, null);

        return output;
    }

    private static float distance(PointF a, PointF b) {
        return (float) Math.hypot(a.x - b.x, a.y - b.y);
    }
}
