package com.example.ashishrmehta.json;
import com.google.gson.annotations.SerializedName;

public class Photo {

    @SerializedName("id")
    String id;

    @SerializedName("owner")
    String owner;

    @SerializedName("secret")
    String secret;

    @SerializedName("server")
    String server;

    @SerializedName("farm")
    String farm;

    @SerializedName("title")
    String title;

    @SerializedName("ispublic")
    String ispublic;

    @SerializedName("isfriend")
    String isfriend;

    @SerializedName("isfamily")
    String isfamily;

    public String getId() {
        return id;
    }

    public String getOwner() {return owner; }

    public String getTitle() {return title; }

    public String getSecret() {
        return secret;
    }

    public String getServer() {
        return server;
    }

    public String getFarm() {
        return farm;
    }

    public String getIspublic() {
        return ispublic;
    }

    public String getIsfriend() {
        return isfriend;
    }

    public String getIsfamily() {
        return isfamily;
    }
}
