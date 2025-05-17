package com.kompetencyjny.EventBuddySpring.dto;

import java.util.List;

public class PollDto {
    private Long id;
    private String question;
    private List<PollOptionDto> options;

    public PollDto(Long id, String question, List<PollOptionDto> options) {
        this.id = id;
        this.question = question;
        this.options = options;
    }

    public Long getId() { return id; }
    public String getQuestion() { return question; }
    public List<PollOptionDto> getOptions() { return options; }
}

