package com.kompetencyjny.EventBuddySpring.dto;

import lombok.Data;

@Data
public class PollOptionDto {
    private Long id;
    private String value;
    private int voteCount;

    private Long pollId;

    public PollOptionDto(Long id, String value, int voteCount, Long pollId) {
        this.id = id;
        this.value = value;
        this.voteCount = voteCount;
        this.pollId = pollId;
    }

    public Long getId() { return id; }
    public String getValue() { return value; }
    public int getVoteCount() { return voteCount; }

    public Long getPollId() {return pollId;}

    public void setPollId(Long pollId){this.pollId = pollId;}
}

