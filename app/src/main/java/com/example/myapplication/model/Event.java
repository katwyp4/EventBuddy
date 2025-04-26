package com.example.myapplication.model;

public class Event {
    private String date;
    private String title;
    private String description;
    private int imageResId;
    private boolean isPrivate;

    public Event(String date, String title, String description, int imageResId) {
        this.date = date;
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
    }
    public String getDate() {
        return  date;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }
}
