package com.kompetencyjny.EventBuddySpring.repo;

import com.kompetencyjny.EventBuddySpring.model.EventParticipant;
import com.kompetencyjny.EventBuddySpring.model.UserEventId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, UserEventId> {
    Page<EventParticipant> findAllById_EventId(Long eventId, Pageable pageable);
    Optional<EventParticipant> findById_EventIdAndId_UserId(Long eventId, Long userId);

}
