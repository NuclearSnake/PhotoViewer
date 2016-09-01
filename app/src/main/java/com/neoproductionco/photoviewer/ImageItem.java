package com.neoproductionco.photoviewer;

import android.graphics.Bitmap;

/**
 * Created by Neo on 01.09.2016.
 */
public class ImageItem {
    private Bitmap image;
    private String title;
    private String author;
    private String camera;

    public ImageItem() {
        super();
    }

    public ImageItem(Bitmap image, String title, String author, String camera) {
        super();
        this.image = image;
        this.title = title;
        this.author = author;
        this.camera = camera;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

}