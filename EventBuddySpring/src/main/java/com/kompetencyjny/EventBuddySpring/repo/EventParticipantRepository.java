package com.kompetencyjny.EventBuddySpring.repo;

import com.kompetencyjny.EventBuddySpring.model.EventParticipant;
import com.kompetencyjny.EventBuddySpring.model.UserEventId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, UserEventId> {
    Page<EventParticipant> findAllByEventId(Long eventId, Pageable pageable);
}
