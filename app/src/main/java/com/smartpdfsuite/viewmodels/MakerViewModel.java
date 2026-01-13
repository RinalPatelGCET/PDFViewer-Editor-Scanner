package com.smartpdfsuite.viewmodels;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MakerViewModel extends AndroidViewModel {

    private final MutableLiveData<String> mText = new MutableLiveData<>();
    private final MutableLiveData<List<Uri>> selectedImages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> pdfCreationStatus = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MakerViewModel(@NonNull Application application) {
        super(application);
        mText.setValue("This is the PDF Maker fragment");
        selectedImages.setValue(new ArrayList<>());
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<List<Uri>> getSelectedImages() {
        return selectedImages;
    }

    public LiveData<Boolean> getPdfCreationStatus() {
        return pdfCreationStatus;
    }

    /**
     * Adds a list of image URIs to the current selection.
     * @param newImages List of new image URIs to add.
     */
    public void addSelectedImages(List<Uri> newImages) {
        List<Uri> currentList = selectedImages.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        currentList.addAll(newImages);
        selectedImages.setValue(currentList); // Use setValue as this is likely called from main thread (UI interaction)
    }

    /**
     * Clears all currently selected images.
     */
    public void clearSelectedImages() {
        selectedImages.setValue(new ArrayList<>());
    }

    /**
     * Placeholder for creating a PDF from selected images.
     * @param outputUri The URI where the new PDF should be saved.
     */
    public void createPdfFromImages(Uri outputUri) {
        pdfCreationStatus.postValue(false); // Indicate starting
        executorService.execute(() -> {
            // TODO: Implement PDF creation logic using a PDF library (e.g., iText, or a wrapper)
            // Iterate through selectedImages.getValue() and add each image to the PDF.
            // Save the PDF to outputUri.
            // Update pdfCreationStatus based on success/failure.
            try {
                // Simulate work
                Thread.sleep(2000);
                pdfCreationStatus.postValue(true); // Indicate success
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                pdfCreationStatus.postValue(false); // Indicate failure
            }
        });
    }

    /**
     * Placeholder for creating a PDF from text content.
     * @param textContent The text to be converted to PDF.
     * @param outputUri The URI where the new PDF should be saved.
     */
    public void createPdfFromText(String textContent, Uri outputUri) {
        pdfCreationStatus.postValue(false);
        executorService.execute(() -> {
            // TODO: Implement PDF creation logic from text.
            // Save the PDF to outputUri.
            try {
                // Simulate work
                Thread.sleep(2000);
                pdfCreationStatus.postValue(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                pdfCreationStatus.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
