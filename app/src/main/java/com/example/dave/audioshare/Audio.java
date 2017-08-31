package com.example.dave.audioshare;


/*classname Audio.java
date 03/07/2017
author David Gunnigan 15043754
https://firebase.google.com/docs/android/setup*/


//This class represents a recording file.
public class Audio {

    public String id;
    public String name;
    public String url;
    public long date;

    /**
     * Firebase requires an empty public constructor.
     */
    public Audio(){}

    public Audio(String id, String name, String url, long date) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
