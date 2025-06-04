package com.example.myapplication.model;

import java.io.Serializable;

public class PollOption implements Serializable {
    private Long id;
    private String value;
    private int voteCount;

    private Long pollId;

    public PollOption() {}

    public PollOption(String value) {
        this.value = value;
        this.voteCount = 0;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public Integer getVoteCount() {
        return voteCount;
    }
    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public Long getPollId() {
        return pollId;
    }

    public void setPollId(Long pollId) {
        this.pollId = pollId;
    }
}
