package com.kompetencyjny.EventBuddySpring.repo;

import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.EventParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, PagingAndSortingRepository<Event, Long> {
    List<Event> findByActiveTrue();
    Page<Event> findByActiveTrue(Pageable pageable);

    @Query("""
    SELECT e FROM Event e
    WHERE e.active = true
    AND
    (e.eventPrivacy IN (
        com.kompetencyjny.EventBuddySpring.model.EventPrivacy.PUBLIC_OPEN,
        com.kompetencyjny.EventBuddySpring.model.EventPrivacy.PUBLIC_CLOSED
    )
    OR EXISTS (
        SELECT ep FROM EventParticipant ep
        WHERE ep.event = e AND ep.id.userId = :userId
    )
    OR EXISTS (
        SELECT u FROM User u
        WHERE u.id = :userId and u.role = ADMIN
    ))
""")
    Page<Event> findEventsVisibleToUser(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    SELECT e
    FROM Event e
    WHERE e.active = true AND e.eventPrivacy IN
    (com.kompetencyjny.EventBuddySpring.model.EventPrivacy.PUBLIC_OPEN,
    com.kompetencyjny.EventBuddySpring.model.EventPrivacy.PUBLIC_CLOSED)
""")
    Page<Event> findPublicEvents(Pageable pageable);
}
