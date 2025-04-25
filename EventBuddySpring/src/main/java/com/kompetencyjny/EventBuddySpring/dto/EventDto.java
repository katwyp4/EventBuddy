package com.kompetencyjny.EventBuddySpring.dto;

import com.kompetencyjny.EventBuddySpring.model.EventPrivacy;
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
}
