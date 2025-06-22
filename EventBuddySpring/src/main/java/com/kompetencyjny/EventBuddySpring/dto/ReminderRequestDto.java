package com.kompetencyjny.EventBuddySpring.dto;

import lombok.Data;

@Data
public class ReminderRequestDto {
    private Long eventId;
    private int daysBefore;
    private String fcmToken;
}
