package com.kompetencyjny.EventBuddySpring.dto;

public class PollResultDto {
    private Long optionId;
    private String value;
    private int voteCount;

    public PollResultDto(Long optionId, String value, int voteCount) {
        this.optionId = optionId;
        this.value = value;
        this.voteCount = voteCount;
    }

    // Gettery i settery
    public Long getOptionId() { return optionId; }
    public String getValue() { return value; }
    public int getVoteCount() { return voteCount; }
}
