package com.smartpdfsuite.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.smartpdfsuite.models.Bookmark; // Make sure to import Bookmark entity

@Database(entities = {Bookmark.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BookmarkDao bookmarkDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "smart_pdf_suite_db")
                            .fallbackToDestructiveMigration() // ONLY FOR DEVELOPMENT! Handle migrations properly in production.
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}