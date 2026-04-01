package com.smartpdfsuite.models;

import android.graphics.Bitmap;

public class FilterModel {
    private String name;
    private Bitmap preview;

    public FilterModel(String name, Bitmap preview) {
        this.name = name;
        this.preview = preview;
    }

    public String getName() {
        return name;
    }

    public Bitmap getPreview() {
        return preview;
    }
    /*private String name;

    public FilterModel(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }*/
}
