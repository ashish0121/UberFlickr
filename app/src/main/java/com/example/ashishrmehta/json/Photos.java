package com.example.ashishrmehta.json;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Photos {

    @SerializedName("page")
    String page;

    @SerializedName("pages")
    String pages;

    @SerializedName("perpage")
    String perpage;

    @SerializedName("total")
    String total;

    @SerializedName("photo")
    ArrayList<Photo> photo = new ArrayList<Photo>();

    public ArrayList<Photo> getPhoto() {
        return photo;
    }
}
