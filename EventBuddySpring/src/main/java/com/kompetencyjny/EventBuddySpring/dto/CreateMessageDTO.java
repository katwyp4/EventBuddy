package com.kompetencyjny.EventBuddySpring.dto;

import lombok.Data;

@Data
public class CreateMessageDTO {
    private Long senderId;
    private Long eventId;
    private String content;
}
