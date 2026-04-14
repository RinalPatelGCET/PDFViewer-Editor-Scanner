package com.smartpdfsuite.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PdfDocument implements Parcelable {
    private String id;
    private String name;
    private Uri uri;
    private String path; // Full file path, might be null for MediaStore URIs
    private long size; // in bytes
    private long dateModified; // in milliseconds
    private String folder; // For organization feature, e.g., "Downloads", "My Docs"
    private List<String> tags; // For organization feature

    public PdfDocument(String id, String name, Uri uri, String path, long size, long dateModified) {
        this.id = id;
        this.name = name;
        this.uri = uri;
        this.path = path;
        this.size = size;
        this.dateModified = dateModified;
        this.folder = "All Documents"; // Default folder
        this.tags = new ArrayList<>();
    }

    protected PdfDocument(Parcel in) {
        id = in.readString();
        name = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
        path = in.readString();
        size = in.readLong();
        dateModified = in.readLong();
        folder = in.readString();
        tags = in.createStringArrayList();
    }

    public static final Creator<PdfDocument> CREATOR = new Creator<PdfDocument>() {
        @Override
        public PdfDocument createFromParcel(Parcel in) {
            return new PdfDocument(in);
        }

        @Override
        public PdfDocument[] newArray(int size) {
            return new PdfDocument[size];
        }
    };

    public String getId() { return id; }
    public String getName() { return name; }
    public Uri getUri() { return uri; }
    public String getPath() { return path; }
    public long getSize() { return size; }
    public long getDateModified() { return dateModified; }
    public String getFolder() { return folder; }
    public List<String> getTags() { return tags; }

    public void setName(String name) { this.name = name; }
    public void setFolder(String folder) { this.folder = folder; }
    public void setTags(List<String> tags) { this.tags = tags; }
    // ✅ Setter (optional but useful)
    public void setPath(String path) {
        this.path = path;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeParcelable(uri, flags);
        dest.writeString(path);
        dest.writeLong(size);
        dest.writeLong(dateModified);
        dest.writeString(folder);
        dest.writeStringList(tags);
    }
}
