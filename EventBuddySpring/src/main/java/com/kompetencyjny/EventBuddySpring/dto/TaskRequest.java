package com.kompetencyjny.EventBuddySpring.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    private String name;
    @Pattern(regexp = "(TODO|IN_PROCESS|DONE)")
    private String status;
}
