package com.kompetencyjny.EventBuddySpring.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private String content;
    private LocalDateTime sentAt;
    private String senderFullName;
    private Long eventId;
}
