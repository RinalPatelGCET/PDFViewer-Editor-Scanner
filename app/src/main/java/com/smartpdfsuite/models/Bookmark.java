package com.smartpdfsuite.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookmarks")
public class Bookmark {
    @PrimaryKey(autoGenerate = true)
    public int id; // Primary key for the bookmark entry

    public String pdfUriString; // URI of the PDF (as a string)
    public String pdfTitle;     // Title of the PDF
    public int pageNumber;      // Page number of the bookmark
    public long timestamp;      // When the bookmark was created

    // Constructor
    public Bookmark(String pdfUriString, String pdfTitle, int pageNumber, long timestamp) {
        this.pdfUriString = pdfUriString;
        this.pdfTitle = pdfTitle;
        this.pageNumber = pageNumber;
        this.timestamp = timestamp;
    }

    // Getters for properties (Room often uses them internally too)
    public String getPdfUriString() { return pdfUriString; }
    public String getPdfTitle() { return pdfTitle; }
    public int getPageNumber() { return pageNumber; }
    public long getTimestamp() { return timestamp; }
}