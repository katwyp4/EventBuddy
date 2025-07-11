package com.kompetencyjny.EventBuddySpring.repo;

import com.kompetencyjny.EventBuddySpring.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
}
