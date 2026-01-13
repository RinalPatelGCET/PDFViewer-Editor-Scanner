package com.smartpdfsuite.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.smartpdfsuite.models.Bookmark; // Make sure to import Bookmark entity

import java.util.List;

@Dao
public interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBookmark(Bookmark bookmark);

    @Delete
    void deleteBookmark(Bookmark bookmark);

    // Query to get all bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    LiveData<List<Bookmark>> getAllBookmarks();

    // Query to check if a specific PDF is bookmarked
    @Query("SELECT COUNT(*) FROM bookmarks WHERE pdfUriString = :pdfUriString")
    int isPdfBookmarked(String pdfUriString);

    // Query to get a specific bookmark by URI (useful for toggling)
    @Query("SELECT * FROM bookmarks WHERE pdfUriString = :pdfUriString LIMIT 1")
    Bookmark getBookmarkByPdfUri(String pdfUriString);
}