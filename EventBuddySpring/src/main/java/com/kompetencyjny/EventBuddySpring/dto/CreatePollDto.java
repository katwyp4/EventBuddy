package com.kompetencyjny.EventBuddySpring.dto;

import lombok.Data;

@Data
public class CreatePollDto {
    private String question;
    private String type;
}

