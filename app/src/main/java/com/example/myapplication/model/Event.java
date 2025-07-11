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

    private String dateVotingEnd;
    private String locationVotingEnd;

    private boolean participant;

    private String budgetDeadline;

    private String datePollDeadline;
    private String locationPollDeadline;

    public String getBudgetDeadline() {
        return budgetDeadline;
    }

    public void setBudgetDeadline(String budgetDeadline) {
        this.budgetDeadline = budgetDeadline;
    }



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


    private Poll datePoll;

    private Poll locationPoll;

    private Boolean enableDateVoting;
    private Boolean enableLocationVoting;

    public void setDatePoll(Poll poll){
        datePoll = poll;
    }
    public void setLocationPoll(Poll poll){
        locationPoll = poll;
    }

    public Poll getDatePoll(){
        return datePoll;
    }
    public Poll getLocationPoll(){
        return locationPoll;
    }
    public void setEnableDateVoting(Boolean value){
        enableDateVoting = value;
    }
    public void setEnableLocationVoting(Boolean value){
        enableLocationVoting = value;
    }

    public Boolean getEnableDateVoting(){
        return enableDateVoting;
    }
    public Boolean getEnableLocationVoting(){
        return enableLocationVoting;
    }

    public boolean isParticipant() {
        return participant;
    }

    public void setParticipant(boolean participant) {
        participant = participant;
    }

    public String getDateVotingEnd() {
        return dateVotingEnd;
    }

    public void setDateVotingEnd(String dateVotingEnd) {
        this.dateVotingEnd = dateVotingEnd;
    }

    public String getLocationVotingEnd() {
        return locationVotingEnd;
    }

    public void setLocationVotingEnd(String locationVotingEnd) {
        this.locationVotingEnd = locationVotingEnd;
    }

    public String getDatePollDeadline() {
        return datePollDeadline;
    }
    public String getLocationPollDeadline() {
        return locationPollDeadline;
    }

    public void setDatePollDeadline(String datePollDeadline){
        this.datePollDeadline = datePollDeadline;
    }
    public void setLocationPollDeadline(String locationPollDeadline){
        this.locationPollDeadline = locationPollDeadline;
    }
}
