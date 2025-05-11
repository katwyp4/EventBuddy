package com.kompetencyjny.EventBuddySpring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
}
