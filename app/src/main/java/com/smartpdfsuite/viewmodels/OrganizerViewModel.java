package com.smartpdfsuite.viewmodels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartpdfsuite.models.PdfDocument;
import com.smartpdfsuite.utils.PdfUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrganizerViewModel extends AndroidViewModel {

    private final MutableLiveData<List<PdfDocument>> allPdfs = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public OrganizerViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<PdfDocument>> getAllPdfs() {
        return allPdfs;
    }

    /**
     * Loads all PDFs from device storage using MediaStore.
     * This operation is performed on a background thread.
     * @param context Application context needed for content resolver.
     */
    public void loadAllPdfs(Context context) {
        executorService.execute(() -> {
            List<PdfDocument> pdfs = PdfUtils.getAllPdfsFromStorage(context);
            allPdfs.postValue(pdfs); // Use postValue as it's called from a background thread
        });
    }

    // TODO: Add methods for:
    // - Searching and sorting logic (though the Fragment handles client-side filtering/sorting for now,
    //   if you need server-side or complex filtering, it might go here).
    // - Database interactions for document organization (folders, tags, metadata).
    // - Deleting a PDF (requires permissions and MediaStore/SAF).
    // - Renaming a PDF (requires permissions and MediaStore/SAF).
    // - Moving a PDF (requires permissions and MediaStore/SAF).

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}