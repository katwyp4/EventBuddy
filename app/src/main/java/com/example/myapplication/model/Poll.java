package com.example.myapplication.model;

import java.io.Serializable;
import java.util.List;

public class Poll implements Serializable {
    private Long id;
    private String question;
    private Long eventId;
    private List<PollOption> options;

    private String endDate;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }

    public Long getEventId() {
        return eventId;
    }
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEndDate() { return endDate; } // <-- Getter
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public List<PollOption> getPollOptions() {
        return pollOptions;
    public List<PollOption> getOptions() {
        return options;
    }
    public void setOptions(List<PollOption> options) {
        this.options = options;
    }
}
