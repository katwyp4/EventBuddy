package com.kompetencyjny.EventBuddySpring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class EventRequest {
    @Size(min=2, max = 50, message = "Event title must be between {min} and {max} characters!")
    private String title;
    private String description;
    private LocalDate date;
    private String location;

    private Double latitude;
    private Double longitude;
    @Pattern(regexp = "(PRIVATE|PUBLIC_OPEN|PUBLIC_CLOSED)")
    private String eventPrivacy;
    private String imageUrl;
    private boolean enableDateVoting;
    private boolean enableLocationVoting;
    private PollDto datePoll;
    private PollDto locationPoll;
    private LocalDate budgetDeadline;
    private LocalDate datePollDeadline;
    private LocalDate locationPollDeadline;
    public boolean isEnableDateVoting() { return enableDateVoting; }
    public void setEnableDateVoting(boolean enableDateVoting) { this.enableDateVoting = enableDateVoting; }

    public boolean isEnableLocationVoting() { return enableLocationVoting; }
    public void setEnableLocationVoting(boolean enableLocationVoting) { this.enableLocationVoting = enableLocationVoting; }

}
