package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.List;

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

    private String eventPrivacy;

    private String imageUrl;

    private List<Poll> polls;

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

    public List<Poll> getPolls() {
        return polls;
    }

    public void setPolls(List<Poll> polls) {
        this.polls = polls;
    }

    public List<Poll> getDatePollOptions() {
        if (polls == null) return new ArrayList<>();
        List<Poll> datePolls = new ArrayList<>();
        for (Poll poll : polls) {
            if ("DATE".equalsIgnoreCase(poll.getQuestion())) { // lub inny warunek identyfikujący
                datePolls.add(poll);
            }
        }
        return datePolls;
    }

    public List<Poll> getLocationPollOptions() {
        if (polls == null) return new ArrayList<>();
        List<Poll> locationPolls = new ArrayList<>();
        for (Poll poll : polls) {
            if ("LOCATION".equalsIgnoreCase(poll.getQuestion())) { // lub inny warunek identyfikujący
                locationPolls.add(poll);
            }
        }
        return locationPolls;
    }

    public String getDateVotingEndDate() {
        if (polls == null) return null;
        for (Poll poll : polls) {
            if (poll.getQuestion() != null && poll.getQuestion().toLowerCase().contains("data")) {
                return poll.getEndDate();
            }
        }
        return null;
    }
    public String getLocationVotingEndDate() {
        if (polls == null) return null;
        for (Poll poll : polls) {
            if (poll.getQuestion() != null && poll.getQuestion().toLowerCase().contains("lokalizac")) {
                return poll.getEndDate();
            }
        }
        return null;
    }

}
