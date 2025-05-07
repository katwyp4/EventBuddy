package com.kompetencyjny.EventBuddySpring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String status; // np. TO_DO, IN_PROGRESS, DONE

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @JsonIgnore private Boolean active;
    @JsonIgnore private LocalDate deactivationDate;
}
