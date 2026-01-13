package com.smartpdfsuite.viewmodels;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartpdfsuite.utils.ImageUtils;
import com.smartpdfsuite.utils.PdfUtils; // NEW: Import PdfUtils for PDF creation
import android.content.Context; // Make sure this import exists

import java.io.IOException;
import java.io.File; // Make sure this import exists
import java.io.OutputStream; // NEW: Import OutputStream
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerViewModel extends AndroidViewModel {

    private static final String TAG = "ScannerViewModel";

    public enum ProcessingState {
        IDLE,
        PROCESSING,
        SUCCESS,
        FAILURE
    }

    private final MutableLiveData<String> mText = new MutableLiveData<>();
    private final MutableLiveData<Uri> capturedImageUri = new MutableLiveData<>();
    private final MutableLiveData<ProcessingState> processingState = new MutableLiveData<>();
    private final MutableLiveData<Uri> lastCreatedPdfUri = new MutableLiveData<>(); // LiveData to hold the URI of the last created PDF
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ScannerViewModel(@NonNull Application application) {
        super(application);
        mText.setValue("This is the PDF Scanner fragment");
        processingState.setValue(ProcessingState.IDLE); // Initialize to IDLE
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<Uri> getCapturedImageUri() {
        return capturedImageUri;
    }

    public LiveData<ProcessingState> getProcessingState() {
        return processingState;
    }

    /**
     * Getter for the LiveData holding the URI of the last created PDF.
     * This allows Fragments to observe the result of PDF creation.
     */
    public LiveData<Uri> getLastCreatedPdfUri() {
        return lastCreatedPdfUri;
    }

    /**
     * Clears the value of the last created PDF URI.
     * This should be called by the Fragment after consuming the URI to prevent stale data
     * or unwanted re-processing/navigation.
     */
    public void clearLastCreatedPdfUri() {
        // Use postValue as this method might be called from either the main or background thread
        lastCreatedPdfUri.postValue(null);
    }

    public void setCapturedImageUri(Uri uri) {
        capturedImageUri.setValue(uri);
    }

    /**
     * Processes a captured image to auto-crop, de-skew, and enhance it.
     * This operation is performed on a background thread.
     *
     * @param inputUri The URI of the raw captured image (e.g., from CameraX).
     * @param outputUri The URI where the processed image should be saved.
     */
    public void processScannedImage(Uri inputUri, Uri outputUri) {
        if (processingState.getValue() == ProcessingState.PROCESSING) {
            Log.d(TAG, "Already processing an image. Skipping new request.");
            return;
        }
        processingState.postValue(ProcessingState.PROCESSING); // Indicate that processing has started
        Log.d(TAG, "Starting image processing for: " + inputUri.toString());

        executorService.execute(() -> {
            Bitmap originalBitmap = null;
            Bitmap processedBitmap = null;
            try {
                originalBitmap = ImageUtils.loadBitmapFromUri(getApplication().getContentResolver(), inputUri, 2048);
                if (originalBitmap == null) {
                    Log.e(TAG, "Failed to load original bitmap from URI: " + inputUri.toString());
                    throw new IOException("Failed to load image.");
                }
                Log.d(TAG, "Original bitmap loaded. Dimensions: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());

                Bitmap croppedBitmap = ImageUtils.detectAndCropDocument(originalBitmap);
                if (croppedBitmap == null) {
                    Log.w(TAG, "Could not auto-crop document. Proceeding with original or falling back.");
                    croppedBitmap = originalBitmap;
                } else {
                    Log.d(TAG, "Document auto-cropped. New dimensions: " + croppedBitmap.getWidth() + "x" + croppedBitmap.getHeight());
                }

                Bitmap deskewedBitmap = ImageUtils.applyPerspectiveCorrection(croppedBitmap);
                if (deskewedBitmap == null) {
                    Log.w(TAG, "Could not apply perspective correction. Proceeding with cropped/original.");
                    deskewedBitmap = croppedBitmap;
                } else {
                    Log.d(TAG, "Perspective correction applied. Dimensions: " + deskewedBitmap.getWidth() + "x" + deskewedBitmap.getHeight());
                }

                processedBitmap = ImageUtils.applyEnhancements(deskewedBitmap);
                if (processedBitmap == null) {
                    Log.w(TAG, "Could not apply enhancements. Proceeding with deskewed/cropped/original.");
                    processedBitmap = deskewedBitmap;
                } else {
                    Log.d(TAG, "Image enhancements applied. Dimensions: " + processedBitmap.getWidth() + "x" + processedBitmap.getHeight());
                }

                ImageUtils.saveBitmapToUri(getApplication().getContentResolver(), processedBitmap, outputUri, Bitmap.CompressFormat.JPEG, 90);
                Log.d(TAG, "Processed bitmap saved to: " + outputUri.toString());

                processingState.postValue(ProcessingState.SUCCESS); // Indicate successful processing

            } catch (OutOfMemoryError e) {
                Log.e(TAG, "Out Of Memory Error during image processing: " + e.getMessage(), e);
                processingState.postValue(ProcessingState.FAILURE);
            } catch (IOException e) {
                Log.e(TAG, "I/O Error during image processing: " + e.getMessage(), e);
                processingState.postValue(ProcessingState.FAILURE);
            } catch (Exception e) {
                Log.e(TAG, "An unexpected error occurred during image processing: " + e.getMessage(), e);
                processingState.postValue(ProcessingState.FAILURE);
            } finally {
                // Recycle bitmaps to free up memory
                if (originalBitmap != null && !originalBitmap.isRecycled() && originalBitmap != processedBitmap) {
                    originalBitmap.recycle();
                }
                if (processedBitmap != null && !processedBitmap.isRecycled()) {
                    processedBitmap.recycle(); // Recycle only if it's the final distinct bitmap
                }
            }
        });
    }

    /**
     * Converts a processed image to a PDF document and saves it to the specified output URI.
     * This operation is performed on a background thread.
     *
     * @param imageUri The URI of the (processed) image to convert.
     * @param outputPdfUri The URI where the new PDF should be saved (typically from SAF).
     */
    public void convertImageToPdf(Uri imageUri, Uri outputPdfUri) {
        if (processingState.getValue() == ProcessingState.PROCESSING) {
            Log.d(TAG, "Already processing. Skipping PDF creation request.");
            return;
        }
        processingState.postValue(ProcessingState.PROCESSING); // Indicate that conversion has started
        executorService.execute(() -> {
            OutputStream pdfOutputStream = null;
            try {
                // Open an OutputStream to the user-selected PDF URI using ContentResolver
                pdfOutputStream = getApplication().getContentResolver().openOutputStream(outputPdfUri);
                if (pdfOutputStream == null) {
                    throw new IOException("Failed to open output stream for PDF URI: " + outputPdfUri);
                }

                // Call the actual PDF creation logic from PdfUtils
                PdfUtils.createPdfFromImage(getApplication().getApplicationContext(), imageUri, pdfOutputStream);

                // Explicitly notify MediaStore if the file was created in a non-SAF way
                // This is less critical with SAF, but good for robustness.
                // Note: getPath() might return null for content URIs, so check for it.
                if ("file".equals(outputPdfUri.getScheme()) && outputPdfUri.getPath() != null) {
                    ImageUtils.addFileToMediaStore(getApplication().getApplicationContext(), new File(outputPdfUri.getPath()));
                }

                lastCreatedPdfUri.postValue(outputPdfUri); // Update with the URI of the newly created PDF
                processingState.postValue(ProcessingState.SUCCESS);

            } catch (IOException e) {
                Log.e(TAG, "I/O Error during PDF creation: " + e.getMessage(), e);
                lastCreatedPdfUri.postValue(null);
                processingState.postValue(ProcessingState.FAILURE);
            } catch (com.itextpdf.text.DocumentException e) { // Catch iText-specific exceptions
                Log.e(TAG, "PDF Document Error during PDF creation: " + e.getMessage(), e);
                lastCreatedPdfUri.postValue(null);
                processingState.postValue(ProcessingState.FAILURE);
            } catch (Exception e) { // Catch any other unexpected exceptions
                Log.e(TAG, "An unexpected error occurred during PDF creation: " + e.getMessage(), e);
                lastCreatedPdfUri.postValue(null);
                Thread.currentThread().interrupt(); // Interrupt if an unexpected error occurs
                processingState.postValue(ProcessingState.FAILURE);
            } finally {
                if (pdfOutputStream != null) {
                    try {
                        pdfOutputStream.close(); // Close the output stream regardless of success/failure
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing PDF output stream: " + e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}