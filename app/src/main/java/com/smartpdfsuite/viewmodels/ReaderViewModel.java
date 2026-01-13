package com.smartpdfsuite.viewmodels;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

//import com.smartpdfsuite.models.Bookmark; // You will create this Room entity
import com.smartpdfsuite.models.PdfDocument;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReaderViewModel extends AndroidViewModel {

    private final MutableLiveData<PdfDocument> currentPdf = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBookmarked = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ReaderViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<PdfDocument> getCurrentPdf() {
        return currentPdf;
    }

    public LiveData<Boolean> getIsBookmarked() {
        return isBookmarked;
    }

    /**
     * Sets the PDF currently being viewed.
     * @param pdf The PdfDocument being read.
     */
    public void setCurrentPdf(PdfDocument pdf) {
        currentPdf.setValue(pdf);
        checkIfBookmarked(pdf.getUri());
    }

    /**
     * Checks if the given PDF URI is bookmarked and updates the LiveData.
     * @param pdfUri The URI of the PDF.
     */
    public void checkIfBookmarked(Uri pdfUri) {
        executorService.execute(() -> {
            // TODO: Query Room database to see if this PDF is bookmarked.
            // For example: boolean bookmarked = appDatabase.bookmarkDao().isPdfBookmarked(pdfUri.toString());
            // isBookmarked.postValue(bookmarked);
            isBookmarked.postValue(false); // Placeholder for now
        });
    }

    /**
     * Toggles the bookmark status for the current PDF.
     * @param pdfUri The URI of the PDF to bookmark/unbookmark.
     * @param pageNumber The current page number to bookmark.
     */
    public void toggleBookmark(Uri pdfUri, int pageNumber) {
        executorService.execute(() -> {
            // TODO: Implement logic to add/remove bookmark from Room database.
            // Check current status and perform insert/delete.
            boolean currentlyBookmarked = isBookmarked.getValue() != null && isBookmarked.getValue();
            if (currentlyBookmarked) {
                // Delete bookmark
                // appDatabase.bookmarkDao().deleteBookmark(pdfUri.toString());
                isBookmarked.postValue(false);
            } else {
                // Add bookmark
                // appDatabase.bookmarkDao().insertBookmark(new Bookmark(pdfUri.toString(), pageNumber));
                isBookmarked.postValue(true);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}