package com.smartpdfsuite.utils;

import android.content.ContentResolver;
import android.content.Context; // Added for MediaScannerConnection
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;
import android.media.MediaScannerConnection; // Added for MediaStore indexing

import java.io.File; // Added for MediaScannerConnection
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * Loads a Bitmap from a given URI, safely downsampling it to prevent OutOfMemoryError.
     *
     * @param contentResolver The ContentResolver to access the URI.
     * @param imageUri The URI of the image to load.
     * @param maxDimension The maximum dimension (width or height) the bitmap should have.
     * @return The loaded and possibly downsampled Bitmap, or null if loading fails.
     */
    public static Bitmap loadBitmapFromUri(ContentResolver contentResolver, Uri imageUri, int maxDimension) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }

            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            try {
                inputStream.close(); // Close the first input stream
            } catch (IOException e) {
                Log.e(TAG, "Error closing input stream after decodeBounds: " + e.getMessage());
            }

            int photoW = options.outWidth;
            int photoH = options.outHeight;

            // Determine how much to scale down the image
            // Only downsample if image is larger than maxDimension
            int scaleFactor = 1;
            if (photoW > maxDimension || photoH > maxDimension) {
                scaleFactor = Math.max(photoW / maxDimension, photoH / maxDimension);
            }

            // Decode the image file into a Bitmap sized to fill the View
            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleFactor;
            options.inPurgeable = true; // For older Android versions, indicates that the bitmap can be purged from memory

            inputStream = contentResolver.openInputStream(imageUri); // Re-open stream
            return BitmapFactory.decodeStream(inputStream, null, options);

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing final input stream: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Conceptual method for detecting the document and cropping it from the image.
     * This is highly complex and typically involves:
     * 1. Grayscale conversion, Gaussian blur.
     * 2. Edge detection (Canny, Sobel).
     * 3. Contour finding or line detection (Hough Transform).
     * 4. Identifying the largest quadrilateral that represents the document.
     * 5. Calculating the perspective transform points.
     *
     * @param originalBitmap The original captured image.
     * @return A cropped bitmap containing only the document, or null if detection fails significantly.
     *         **Requires a robust image processing library like OpenCV for Android for production use.**
     */
    public static Bitmap detectAndCropDocument(Bitmap originalBitmap) {
        Log.d(TAG, "Detecting and cropping document (conceptual)");
        // TODO: Implement actual document detection and cropping.
        // This is a placeholder. You would integrate a library like OpenCV here.
        // For now, it returns null to indicate that this complex step is not implemented,
        // which will cause the ViewModel to fallback to the original/cropped image.
        return null;
    }

    /**
     * Conceptual method for applying perspective correction (de-skewing).
     * This takes an image and transforms it as if viewed directly head-on.
     *
     * @param inputBitmap The image after cropping.
     * @return A de-skewed bitmap, or null if correction fails.
     *         **Requires a robust image processing library like OpenCV for Android for production use.**
     */
    public static Bitmap applyPerspectiveCorrection(Bitmap inputBitmap) {
        Log.d(TAG, "Applying perspective correction (conceptual)");
        // TODO: Implement actual perspective correction.
        // This is a placeholder. If you have the 4 corner points (from detectAndCropDocument),
        // you would use them to create a transformation matrix.
        // For now, it returns null to indicate that this complex step is not implemented,
        // which will cause the ViewModel to fallback to the previous bitmap.
        return null;
    }

    /**
     * Conceptual method for applying various image enhancements.
     *
     * @param inputBitmap The image after de-skewing.
     * @return An enhanced bitmap.
     */
    public static Bitmap applyEnhancements(Bitmap inputBitmap) {
        Log.d(TAG, "Applying image enhancements (conceptual)");
        if (inputBitmap == null) return null;

        // Create a mutable copy to apply changes
        Bitmap mutableBitmap = inputBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();

        // 1. Grayscale Conversion (if not already done)
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0); // 0 for grayscale
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(mutableBitmap, new Matrix(), paint); // Apply grayscale
        paint.setColorFilter(null); // Reset filter

        // 2. Adjust Brightness and Contrast (Example: simple adjustment)
        float contrast = 1.2f; // 1.0 is normal, >1 for more contrast
        float brightness = 10f; // 0 is normal, positive for brighter
        ColorMatrix cm = new ColorMatrix(new float[] {
                contrast, 0, 0, 0, brightness,
                0, contrast, 0, 0, brightness,
                0, 0, contrast, 0, brightness,
                0, 0, 0, 1, 0
        });
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(mutableBitmap, new Matrix(), paint);
        paint.setColorFilter(null);


        // 3. Adaptive Thresholding (Crucial for document clarity - make text black, background white)
        // This is typically done at the pixel level or using libraries like OpenCV.
        // Android's built-in APIs don't have a direct adaptive threshold.
        // You'd iterate pixels, or use OpenCV (e.g., Imgproc.adaptiveThreshold).
        Log.d(TAG, "Adaptive thresholding would be applied here for text clarity.");


        // 4. Sharpening (Optional)
        // Also typically a pixel-level convolution operation.
        Log.d(TAG, "Sharpening would be applied here (optional).");

        return mutableBitmap;
    }

    /**
     * Saves a Bitmap to a given URI with specified format and quality.
     *
     * @param contentResolver The ContentResolver to write to the URI.
     * @param bitmap The Bitmap to save.
     * @param outputUri The URI where the bitmap should be saved.
     * @param format The compression format (JPEG, PNG, WEBP).
     * @param quality The compression quality (0-100, ignored for PNG).
     * @throws IOException If an I/O error occurs during saving.
     */
    public static void saveBitmapToUri(ContentResolver contentResolver, Bitmap bitmap, Uri outputUri, Bitmap.CompressFormat format, int quality) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = contentResolver.openOutputStream(outputUri);
            if (outputStream == null) {
                throw new IOException("Failed to open output stream for URI: " + outputUri.toString());
            }
            bitmap.compress(format, quality, outputStream);
            outputStream.flush();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing output stream: " + e.getMessage());
                }
            }
        }
    }

    public static Uri saveBitmapToCache(Context context, Bitmap bitmap) {
        /*File file = new File(context.getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg");
        try (OutputStream os = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
        } catch (Exception e) {
            return null;
        }
        return Uri.fromFile(file);*/

        //apply filter 12_03_2026
        File cacheDir = new File(context.getCacheDir(), "filtered_images");

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        File file = new File(cacheDir, "filtered_" + System.currentTimeMillis() + ".jpg");

        try {

            OutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
            os.flush();
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return Uri.fromFile(file);
    }

    /**
     * Notifies the MediaStore about a new file, making it discoverable by other apps
     * and visible in galleries/file managers (if it's in public storage).
     * This is particularly useful for files saved via Uri.fromFile() to external storage
     * or app-private external storage that MediaStore should scan.
     * If using SAF (Storage Access Framework) to save, this might not be strictly necessary
     * as SAF often handles MediaStore updates.
     *
     * @param context The application context.
     * @param file The File object of the newly created/modified file.
     */
    public static void addFileToMediaStore(Context context, File file) {
        MediaScannerConnection.scanFile(context,
                new String[]{file.getAbsolutePath()},
                null, // Mime type can be specified, null lets MediaScanner deduce
                (path, uri) -> {
                    Log.d(TAG, "MediaScanner scanned " + path + ". Content URI: " + uri);
                });
    }

    //Add filters 12_03_2026
    public static Bitmap autoEnhance(Bitmap src) {

        Bitmap bmp = src.copy(Bitmap.Config.ARGB_8888, true);

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(1.3f);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(bmp, 0, 0, paint);

        return bmp;
    }

    public static Bitmap documentMode(Bitmap src) {

        Bitmap bmp = src.copy(Bitmap.Config.ARGB_8888, true);

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(bmp, 0, 0, paint);

        return bmp;
    }

    public static Bitmap removeShadow(Bitmap src) {

        Bitmap bmp = src.copy(Bitmap.Config.ARGB_8888, true);

        ColorMatrix cm = new ColorMatrix(new float[]{
                1.4f,0,0,0,-40,
                0,1.4f,0,0,-40,
                0,0,1.4f,0,-40,
                0,0,0,1,0
        });

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(bmp,0,0,paint);

        return bmp;
    }

    public static Bitmap colorBoost(Bitmap src) {

        Bitmap bmp = src.copy(Bitmap.Config.ARGB_8888,true);

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(1.8f);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(bmp,0,0,paint);

        return bmp;
    }

    public static Bitmap enhance2(Bitmap src) {

        Bitmap bmp = src.copy(Bitmap.Config.ARGB_8888,true);

        ColorMatrix cm = new ColorMatrix(new float[]{
                1.3f,0,0,0,20,
                0,1.3f,0,0,20,
                0,0,1.3f,0,20,
                0,0,0,1,0
        });

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(bmp,0,0,paint);

        return bmp;
    }

    public static Bitmap blackWhite(Bitmap src) {

        Bitmap bmp = src.copy(Bitmap.Config.ARGB_8888,true);

        ColorMatrix cm = new ColorMatrix(new float[]{
                0.3f,0.59f,0.11f,0,0,
                0.3f,0.59f,0.11f,0,0,
                0.3f,0.59f,0.11f,0,0,
                0,0,0,1,0
        });

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(bmp,0,0,paint);

        return bmp;
    }

    public static Bitmap gray(Bitmap src) {

        Bitmap bmp = src.copy(Bitmap.Config.ARGB_8888,true);

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(bmp,0,0,paint);

        return bmp;
    }

    public static Bitmap invert(Bitmap src) {

        Bitmap bmp = src.copy(Bitmap.Config.ARGB_8888,true);

        ColorMatrix cm = new ColorMatrix(new float[]{
                -1,0,0,0,255,
                0,-1,0,0,255,
                0,0,-1,0,255,
                0,0,0,1,0
        });

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(bmp,0,0,paint);

        return bmp;
    }

}