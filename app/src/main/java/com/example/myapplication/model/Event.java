package com.example.myapplication.model;

public class Event {
    private Long id;
    private String title;
    private String description;
    private String date;
    private String location;
    private Double latitude;
    private Double longitude;

    private String deactivationDate;
    private String shareLink;
    private String eventPrivacy; // lub enum jeśli chcesz

    private String imageUrl;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getShareLink() {
        return shareLink;
    }

    public String getEventPrivacy() {
        return eventPrivacy;
    }
    public String getDeactivationDate() {
        return deactivationDate;
    }

    public void setDeactivationDate(String deactivationDate) {
        this.deactivationDate = deactivationDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setLocation(String location) { this.location = location; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setShareLink(String shareLink) { this.shareLink = shareLink; }
    public void setEventPrivacy(String eventPrivacy) { this.eventPrivacy = eventPrivacy; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
