package com.example.ashishrmehta.json;
import com.google.gson.annotations.SerializedName;

public class FlickrClass {

    @SerializedName("photos")
    Photos photos;

    @SerializedName("stat")
    String stat;

    public Photos getPhotos() {
        return photos;
    }
}
