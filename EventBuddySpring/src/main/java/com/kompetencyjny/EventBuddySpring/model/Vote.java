package com.kompetencyjny.EventBuddySpring.model;

import com.kompetencyjny.EventBuddySpring.model.Poll;
import com.kompetencyjny.EventBuddySpring.model.PollOption;
import com.kompetencyjny.EventBuddySpring.model.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "votes")
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Poll poll;

    @ManyToOne
    private PollOption option;
}
