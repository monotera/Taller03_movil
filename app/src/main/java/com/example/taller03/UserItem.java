package com.example.taller03;

import android.net.Uri;

import com.google.maps.model.LatLng;


public class UserItem {
    private String name;
    private Uri image;
    private String id;

    public UserItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserItem(String name, Uri image, String id) {
        this.name = name;
        this.image = image;
        this.id = id;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
