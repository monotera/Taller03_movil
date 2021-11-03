package com.example.taller03;

import com.google.maps.model.LatLng;

public class UserItem {
    private String name;
    private int image;
    private String id;

    public UserItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserItem(String name, int image, String id) {
        this.name = name;
        this.image = image;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
