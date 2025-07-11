package com.kompetencyjny.EventBuddySpring.dto;

import com.kompetencyjny.EventBuddySpring.model.EventPrivacy;
import com.kompetencyjny.EventBuddySpring.model.Poll;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate date;
    private String location;
    private Double longitude;
    private Double latitude;
    private String shareLink;
    private String eventPrivacy;
    private String imageUrl;
    private PollDto datePoll;
    private PollDto locationPoll;
    private LocalDate budgetDeadline;
    private boolean participant;
    private LocalDate datePollDeadline;
    private LocalDate locationPollDeadline;
}
