package com.smartpdfsuite.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.media.MediaScannerConnection;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import com.smartpdfsuite.models.PdfDocument; // <--- This import is CRUCIAL

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PdfUtils {

    private static final String TAG = "PdfUtils";

    /**
     * Retrieves a list of all PDF documents found on the device's storage
     * by querying the MediaStore.
     *
     * @param context Application context needed for content resolver.
     * @return A List of PdfDocument objects.
     */
    public static List<PdfDocument> getAllPdfsFromStorage(Context context) {
        List<PdfDocument> pdfList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        Uri collection;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Files.getContentUri("external");
        }

        String[] projection = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                // MediaStore.Files.FileColumns.DATA is deprecated on Android 10+
                // but useful for identifying path on older versions. It can be null.
                (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) ? MediaStore.Files.FileColumns.DATA : null
        };
        // Filter out null projection elements for Android 10+ where DATA is not always available
        List<String> validProjection = new ArrayList<>();
        for (String col : projection) {
            if (col != null) {
                validProjection.add(col);
            }
        }
        String[] finalProjection = validProjection.toArray(new String[0]);


        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?";
        String[] selectionArgs = new String[]{"application/pdf"};
        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"; // Sort by newest first

        try (Cursor cursor = contentResolver.query(collection, finalProjection, selection, selectionArgs, sortOrder)) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                int dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int dataColumn = -1;
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                    dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                }

                while (cursor.moveToNext()) {
                    String id = cursor.getString(idColumn);
                    String name = cursor.getString(nameColumn);
                    long size = cursor.getLong(sizeColumn);
                    long dateModified = cursor.getLong(dateModifiedColumn);

                    Uri contentUri = Uri.withAppendedPath(collection, id);
                    String path = null;
                    if (dataColumn != -1) { // Only available on older APIs
                        path = cursor.getString(dataColumn);
                    } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        path = contentUri.toString(); // For Q+, use content URI as identifier or resolve path if needed
                    }

                    // Convert dateModified to milliseconds if it's in seconds (Q+)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        dateModified *= 1000;
                    }

                    // Basic validation
                    if (name != null && !name.isEmpty() && size >= 0) {
                        pdfList.add(new PdfDocument(id, name, contentUri, path, size, dateModified));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying PDFs: " + e.getMessage(), e);
        }
        return pdfList;
    }

    /**
     * Retrieves the file size for a given content URI.
     * @param context Application context.
     * @param uri The content URI of the file.
     * @return The size of the file in bytes, or 0 if an error occurs.
     */
    public static long getFileSize(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file size for URI: " + uri, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    /**
     * Creates a PDF document from a single image and writes its content to the provided OutputStream.
     * Uses iTextG library for PDF generation.
     *
     * @param context The application context.
     * @param imageUri The URI of the image to convert.
     * @param outputStream The OutputStream to write the PDF content to. This stream is managed by the caller.
     * @throws IOException If there's an error reading the image.
     * @throws com.itextpdf.text.DocumentException If there's an iText-specific error during PDF creation.
     */
    public static void createPdfFromImage(Context context, Uri imageUri, OutputStream outputStream) throws IOException, com.itextpdf.text.DocumentException {
        InputStream imageInputStream = null;
        Document document = null;
        try {
            imageInputStream = context.getContentResolver().openInputStream(imageUri);
            if (imageInputStream == null) {
                throw new IOException("Unable to open input stream for image URI: " + imageUri);
            }

            // Decode the image into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(imageInputStream);
            if (bitmap == null) {
                throw new IOException("Failed to decode image from URI: " + imageUri);
            }

            // Convert Bitmap to iText Image
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // Use PNG for lossless quality in PDF
            Image img = Image.getInstance(stream.toByteArray());
            stream.close();
            bitmap.recycle(); // Recycle bitmap to free up memory

            // Create iText Document
            // Set page size to match the image dimensions for best fit
            Rectangle pageSize = new Rectangle(img.getScaledWidth(), img.getScaledHeight());
            document = new Document(pageSize, 0, 0, 0, 0); // No margins

            // Get a PdfWriter instance to write the PDF to the outputStream
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Scale image to fit the page without distortion if necessary and add to document
            img.setAbsolutePosition(0, 0); // Position at (0,0)
            img.scaleToFit(pageSize.getWidth(), pageSize.getHeight()); // Scale if image is larger than page
            document.add(img);

            document.close();
            Log.d(TAG, "PDF created successfully from image: " + imageUri.toString());

        } finally {
            if (imageInputStream != null) {
                try {
                    imageInputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing image input stream: " + e.getMessage());
                }
            }
            if (document != null && document.isOpen()) {
                // Ensure document is closed even if an error occurred during adding content
                document.close();
            }
            // The outputStream is managed by the caller (ScannerViewModel) and should not be closed here.
        }
    }
}