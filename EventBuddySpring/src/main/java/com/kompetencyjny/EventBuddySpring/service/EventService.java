package com.kompetencyjny.EventBuddySpring.service;

import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.EventParticipant;
import com.kompetencyjny.EventBuddySpring.model.EventRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EventService {
    Event create(Event event, String loggedUserName);

    List<Event> findAll();
    Page<Event> findAll(Pageable pageable);
    Optional<Event> findById(Long id);

    boolean existsById(Long id);

    Event fullUpdate(Long id, Event event, String loggedUserName);

    Event partialUpdate(Long id, Event event, String loggedUserName);

    void deleteById(Long id, String loggedUserName);

    boolean isUserPermitted(Event event, String username, EventRole minRole);

    boolean isUserAParticipantOf(Long eventId, Long userId);

    EventParticipant addEventParticipant(Long eventId, Long userId, EventRole role, String loggedUserName);

    void removeEventParticipant(Long eventId, Long userId, String loggedInUserName);
    Optional<EventParticipant> getEventParticipant(Long eventId, Long userId);

    Page<EventParticipant> findAllEventParticipants(Pageable pageable, Long eventId);

    EventParticipant updateEventParticipantRole(Long eventId, Long userId, EventRole eventRole, String loggedUserName);
}
