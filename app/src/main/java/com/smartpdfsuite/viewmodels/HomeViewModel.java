package com.smartpdfsuite.viewmodels;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.smartpdfsuite.database.AppDatabase;
import com.smartpdfsuite.database.BookmarkDao;
import com.smartpdfsuite.models.Bookmark;
import com.smartpdfsuite.models.PdfDocument;
import com.smartpdfsuite.utils.PdfUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = "HomeViewModel";

    private final MutableLiveData<List<PdfDocument>> recentPdfs = new MutableLiveData<>();
    // LiveData for bookmarks will now come directly from the Room database
    private final LiveData<List<Bookmark>> allBookmarks;
    private final BookmarkDao bookmarkDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        // Initialize Room database and DAO
        AppDatabase db = AppDatabase.getDatabase(application);
        bookmarkDao = db.bookmarkDao();
        // The LiveData from Room automatically observes database changes
        allBookmarks = bookmarkDao.getAllBookmarks();
    }

    public LiveData<List<PdfDocument>> getRecentPdfs() {
        return recentPdfs;
    }

    /**
     * Provides a LiveData of bookmarked PdfDocuments.
     * This transforms the raw List<Bookmark> from the database into a List<PdfDocument>.
     *
     * IMPORTANT NOTE: A Bookmark typically stores minimal info (URI, title, page).
     * A PdfDocument requires more (id, path, size, dateModified).
     * For a complete and accurate `PdfDocument` list based on bookmarks, you would need to:
     * 1. Store more `PdfDocument` details directly in the `Bookmark` entity.
     * OR
     * 2. Perform a lookup in `MediaStore` (or your app's internal PDF metadata cache) for each
     *    bookmarked PDF's full details using its `pdfUriString`.
     * The current implementation provides a simplified `PdfDocument` using available `Bookmark` data.
     */
    public LiveData<List<PdfDocument>> getBookmarkedPdfs() {
        return Transformations.map(allBookmarks, bookmarks -> {
            List<PdfDocument> bookmarkedPdfList = new ArrayList<>();
            if (bookmarks != null) {
                for (Bookmark bookmark : bookmarks) {
                    // This is a simplified reconstruction of PdfDocument from Bookmark.
                    // For full details, a MediaStore lookup or more comprehensive Bookmark entity is needed.
                    try {
                        bookmarkedPdfList.add(new PdfDocument(
                                bookmark.pdfUriString, // Using URI string as ID for simplicity
                                bookmark.pdfTitle != null ? bookmark.pdfTitle : "Bookmarked PDF",
                                Uri.parse(bookmark.pdfUriString),
                                bookmark.pdfUriString, // Path is often same as URI string for content URIs
                                0, // Placeholder for size (actual size would require MediaStore lookup)
                                bookmark.timestamp
                        ));
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing URI from bookmark: " + bookmark.pdfUriString, e);
                    }
                }
            }
            return bookmarkedPdfList;
        });
    }

    /**
     * Loads all PDFs from device storage and updates the recentPdfs LiveData.
     * This operation is performed on a background thread.
     * @param context Application context needed for content resolver.
     */
    public void loadPdfsFromStorage(Context context) {
        executorService.execute(() -> {
            // Call the utility method to get all PDFs from device storage.
            List<PdfDocument> pdfs = PdfUtils.getAllPdfsFromStorage(context);

            // In a real app, you might want to filter this list further
            // to only show genuinely "recent" PDFs based on app-specific usage history,
            // not just all PDFs found. For this initial setup, it populates with all found PDFs.

            // Update the LiveData. Since this is called from a background thread,
            // postValue() must be used instead of setValue().
            recentPdfs.postValue(pdfs);
            Log.d(TAG, "Loaded " + (pdfs != null ? pdfs.size() : 0) + " PDFs from storage.");
        });
    }

    /**
     * Placeholder for loading bookmarked PDFs from a Room database.
     * With LiveData from Room (`allBookmarks`), manual loading here is less critical,
     * as `allBookmarks` will automatically update its observers (like `getBookmarkedPdfs`).
     * This method could be used if you need to trigger a *one-off* synchronous fetch or
     * perform complex filtering not directly supported by the DAO.
     */
    public void loadBookmarkedPdfs() {
        Log.d(TAG, "loadBookmarkedPdfs() called. Observing LiveData from Room database.");
        // The `allBookmarks` LiveData from Room already provides real-time updates.
        // If a direct, immediate fetch (non-LiveData) is needed, you would:
        // 1. Add a synchronous query method (e.g., `List<Bookmark> getAllBookmarksSynchronous();`) to `BookmarkDao`.
        // 2. Call `bookmarkDao.getAllBookmarksSynchronous()` here within `executorService.execute()`.
        // 3. Process the results and post to a `MutableLiveData<List<PdfDocument>>`.
    }

    /**
     * Toggles a PDF's bookmark status. If bookmarked, it unbookmarks; otherwise, it bookmarks.
     * This operation is performed on a background thread for database interaction.
     *
     * @param pdf The PdfDocument to bookmark/unbookmark. Must not be null and must have a non-null URI.
     * @param pageNumber The page number relevant to the bookmark (e.g., last viewed page).
     */
    public void toggleBookmark(PdfDocument pdf, int pageNumber) {
        if (pdf == null || pdf.getUri() == null) {
            Log.e(TAG, "Cannot toggle bookmark: PDF object or its URI is null.");
            return;
        }

        executorService.execute(() -> {
            String pdfUriString = pdf.getUri().toString();
            // Check if a bookmark already exists for this PDF URI
            Bookmark existingBookmark = bookmarkDao.getBookmarkByPdfUri(pdfUriString);

            if (existingBookmark != null) {
                // If a bookmark exists, delete it (unbookmark)
                bookmarkDao.deleteBookmark(existingBookmark);
                Log.d(TAG, "Bookmark removed for PDF: " + pdf.getName());
            } else {
                // If no bookmark exists, create a new one and insert it
                Bookmark newBookmark = new Bookmark(
                        pdfUriString,
                        pdf.getName(),
                        pageNumber,
                        System.currentTimeMillis() // Record creation timestamp
                );
                bookmarkDao.insertBookmark(newBookmark);
                Log.d(TAG, "Bookmark added for PDF: " + pdf.getName() + " on page: " + pageNumber);
            }
            // Observers of `allBookmarks` (and thus `getBookmarkedPdfs()`) will automatically
            // be notified of this database change and update the UI.
        });
    }

    /**
     * Checks if a specific PDF is bookmarked.
     * This is useful for updating UI elements like a bookmark icon in real-time.
     * The result is provided via LiveData, reacting to database changes.
     *
     * @param pdfUri The URI of the PDF to check.
     * @return LiveData<Boolean> indicating if the PDF is bookmarked.
     */
    public LiveData<Boolean> isPdfBookmarked(Uri pdfUri) {
        MutableLiveData<Boolean> bookmarkedStatus = new MutableLiveData<>();
        if (pdfUri == null) {
            bookmarkedStatus.postValue(false);
            return bookmarkedStatus;
        }

        executorService.execute(() -> {
            // Query the database on a background thread
            boolean isBookmarked = bookmarkDao.isPdfBookmarked(pdfUri.toString()) > 0;
            bookmarkedStatus.postValue(isBookmarked);
        });
        return bookmarkedStatus;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        // Shut down the executor service to prevent memory leaks and
        // ensure all background tasks are properly terminated when the ViewModel is no longer used.
        executorService.shutdown();
        Log.d(TAG, "HomeViewModel cleared and executor shut down.");
    }
}