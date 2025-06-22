package com.kompetencyjny.EventBuddySpring.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "poll_options")
public class PollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value_;
    private int voteCount;

    @ManyToOne
    @JoinColumn(name = "poll_id")
    private Poll poll;
}
