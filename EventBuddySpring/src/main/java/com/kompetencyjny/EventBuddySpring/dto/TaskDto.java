package com.kompetencyjny.EventBuddySpring.dto;

import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.User;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private Long id;
    private String title;
    private boolean done;
    private Long eventId;
    private Long assignedUserId;
    private String assigneeFullName;
}
