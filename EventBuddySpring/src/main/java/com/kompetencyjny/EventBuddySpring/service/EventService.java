package com.kompetencyjny.EventBuddySpring.service;

import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.EventParticipant;
import com.kompetencyjny.EventBuddySpring.model.EventRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public interface EventService {
    Event create(Event event, String loggedUserName);

    Page<Event> findAllVisible(Pageable pageable, String loggedUserName);

    Page<Event> findAllPublic(Pageable pageable);

    Optional<Event> findVisibleById(Long id, String loggedUserName);

    Optional<Event> findPublicById(Long id);

    Optional<Event> findByIdInternal(Long id);

    Page<Event> findAllInternal(Pageable pageable);

    boolean existsById(Long id);

    Event fullUpdate(Long id, Event event, String loggedUserName);

    void deleteById(Long id, String loggedUserName);

    boolean isUserPermitted(Long eventId, String username, EventRole minRole);

    boolean isUserAParticipantOf(Long eventId, Long userId);

    EventParticipant addEventParticipant(Long eventId, Long userId, EventRole role, String loggedUserName);

    void removeEventParticipant(Long eventId, Long userId, String loggedInUserName);

    Optional<EventParticipant> getEventParticipantInternal(Long eventId, Long userId);

    Optional<EventParticipant> getEventParticipant(Long eventId, Long userId, String loggedUserName);

    Page<EventParticipant> findAllEventParticipants(Pageable pageable, Long eventId, String loggedUserName);
    Page<EventParticipant> findAllEventParticipantsWithRole(Pageable pageable, Long eventId, EventRole role, String loggedUserName);

    EventParticipant updateEventParticipantRole(Long eventId, Long userId, EventRole eventRole, String loggedUserName);

    Page<Event> findAllEventsOfUser(Pageable pageable, Long userId, String username);

    Map<String, BigDecimal> calculateBalances(Long eventId);

    public boolean hasDateVotingEnded(Event event);

    public boolean hasLocationVotingEnded(Event event);

    public EventParticipant joinEvent(Long eventId, String loggedEmail);
}
