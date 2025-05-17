package com.kompetencyjny.EventBuddySpring.dto;

public class PollOptionDto {
    private Long id;
    private String value;
    private int voteCount;

    public PollOptionDto(Long id, String value, int voteCount) {
        this.id = id;
        this.value = value;
        this.voteCount = voteCount;
    }

    // Gettery
    public Long getId() { return id; }
    public String getValue() { return value; }
    public int getVoteCount() { return voteCount; }
}

