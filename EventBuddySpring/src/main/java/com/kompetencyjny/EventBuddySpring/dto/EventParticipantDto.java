package com.kompetencyjny.EventBuddySpring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventParticipantDto {
    Long eventId;
    String eventRole;
    UserDto user;
}
