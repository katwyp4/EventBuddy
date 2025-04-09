package com.kompetencyjny.EventBuddySpring.service.impl;

import com.kompetencyjny.EventBuddySpring.exeption.NotFoundExeption;
import com.kompetencyjny.EventBuddySpring.exeption.UnauthorizedExeption;
import com.kompetencyjny.EventBuddySpring.model.*;
import com.kompetencyjny.EventBuddySpring.repo.EventParticipantRepository;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import com.kompetencyjny.EventBuddySpring.service.EventService;
import com.kompetencyjny.EventBuddySpring.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class EventServiceImpl implements EventService {
    private EventRepository eventRepository;
    private UserService userService;
    private EventParticipantRepository eventParticipantRepository;

    public EventServiceImpl(EventRepository eventRepository, UserService userService, EventParticipantRepository eventParticipantRepository) {
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.eventParticipantRepository = eventParticipantRepository;
    }

    @Override
    public Event create(Event event, String loggedUserName) {
        event.setId(null);
        Optional<User> loggedUserOpt = userService.findByUserName(loggedUserName);
        if (loggedUserOpt.isEmpty())
            throw new NotFoundExeption("!!! YOU SHOULD NOT SEE THIS !!! Cannot find logged in user! username: \""+loggedUserName+"\".\nThis method expects to get a username of logged in user.");

        event.addParticipant(loggedUserOpt.get());
        return this.eventRepository.save(event);
    }

    @Override
    public List<Event> findAll() {
        return StreamSupport.stream(eventRepository.findByActiveTrue()
                .spliterator(),
                false)
                .collect(Collectors.toList());
    }
    @Override
    public Page<Event> findAll(Pageable pageable) {
        return eventRepository.findByActiveTrue(pageable);
    }

    @Override
    public Optional<Event> findById(Long id) {
            if (!this.existsById(id)) return Optional.empty();
           return eventRepository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) return false;
        return eventOpt.get().getActive();
    }

    @Override
    public Event fullUpdate(Long id, Event event, String loggedUserName) {
        if (!this.existsById(id)) throw new RuntimeException("Trying to update non existing event!");
        if (!isUserPermitted(event, loggedUserName, EventRole.ADMIN))
            throw new UnauthorizedExeption("User username:"+loggedUserName+" not allowed to update event "+event);

        event.setId(id);
        return eventRepository.save(event);
    }

    @Override
    public Event partialUpdate(Long id, Event event, String loggedUserName) {
        if (!this.existsById(id)) throw new RuntimeException("Trying to update non existing event!");
        if (!isUserPermitted(event, loggedUserName, EventRole.ADMIN))
            throw new UnauthorizedExeption("User username:"+loggedUserName+" not allowed to update event "+event);

        event.setId(id);
        return  eventRepository.findById(id).map(existingEvent -> {
            Optional.ofNullable(event.getTitle()).ifPresent(existingEvent::setTitle);
            Optional.ofNullable(event.getDescription()).ifPresent(existingEvent::setDescription);
            Optional.ofNullable(event.getDate()).ifPresent(existingEvent::setDate);
            Optional.ofNullable(event.getLatitude()).ifPresent(existingEvent::setLatitude);
            Optional.ofNullable(event.getLongitude()).ifPresent(existingEvent::setLongitude);
            Optional.ofNullable(event.getLocation()).ifPresent(existingEvent::setLocation);
            Optional.ofNullable(event.getShareLink()).ifPresent(existingEvent::setShareLink);
            return eventRepository.save(existingEvent);
        }).orElseThrow(() -> new RuntimeException("Trying to update non existing event!"));
    }

    @Override
    public void deleteById(Long id, String loggedUserName) {
        Optional<Event> existingEventOpt = eventRepository.findById(id);
        if (existingEventOpt.isEmpty()) throw new RuntimeException("Trying to delete non existing event!");

        Event existingEvent = existingEventOpt.get();
        if (!isUserPermitted(existingEvent, loggedUserName, EventRole.ADMIN))
            throw new UnauthorizedExeption("User username:"+loggedUserName+" not allowed to delete event "+existingEvent);

        if (!existingEvent.getActive()) return;
        existingEvent.setActive(false);
        existingEvent.setDeactivationDate(LocalDate.now());
        eventRepository.save(existingEvent);
    }

    @Override
    public boolean isUserPermitted(Event event, String username, EventRole minRole) {
        Optional<User> userOpt = userService.findByUserName(username);
        if (userOpt.isEmpty()) throw new RuntimeException("No user found of username "+username);

        User loggedInUser = userOpt.get();
        if (loggedInUser.getRole() == Role.ADMIN) return true;

        Optional<EventParticipant> eventParticipantOpt = event.getEventParticipant(loggedInUser);
        if (eventParticipantOpt.isEmpty()) return false;

        return eventParticipantOpt.get().getEventRole().compareTo(minRole)>=0;
    }

    @Override
    public boolean isUserAParticipantOf(Long eventId, Long userId) {
        Optional<Event> eventOpt = this.findById(eventId);
        Optional<User> userOpt = this.userService.findById(userId);
        if (userOpt.isEmpty() || eventOpt.isEmpty()) throw new RuntimeException("User or Event does not exists!");
        Event event = eventOpt.get();
        User user = userOpt.get();
        return event.isParticipant(user);
    }

    @Override
    public EventParticipant addEventParticipant(Long eventId, Long userId, EventRole role, String loggedUserName){
        Optional<Event> eventOpt = this.findById(eventId);
        Optional<User> userOpt = this.userService.findById(userId);
        if (userOpt.isEmpty() || eventOpt.isEmpty()) throw new RuntimeException("User or Event does not exists!");
        Event event = eventOpt.get();
        User user = userOpt.get();

        if (!isUserPermitted(event, loggedUserName, EventRole.ADMIN))
            throw new UnauthorizedExeption("User username:"+loggedUserName+" not allowed to add a participant to event: "+event);

        EventParticipant eventParticipant = event.addParticipant(user, role);
        eventRepository.save(event);
        return eventParticipant;
    }

    @Override
    public void removeEventParticipant(Long eventId, Long userId, String loggedUserName){
        Optional<Event> eventOpt = this.findById(eventId);
        Optional<User> userOpt = this.userService.findById(userId);
        if (userOpt.isEmpty() || eventOpt.isEmpty()) throw new RuntimeException("User or Event does not exists!");
        Event event = eventOpt.get();
        User user = userOpt.get();

        if (!isUserPermitted(event, loggedUserName, EventRole.ADMIN))
            throw new UnauthorizedExeption("User username:"+loggedUserName+" not allowed to remove participants from event: "+event);

        event.removeParticipant(user);
        eventRepository.save(event);
    }

    @Override
    public Optional<EventParticipant> getEventParticipant(Long eventId, Long userId) {
        Optional<Event> eventOpt = this.findById(eventId);
        Optional<User> userOpt = this.userService.findById(userId);
        if (userOpt.isEmpty() || eventOpt.isEmpty()) throw new NotFoundExeption("Event or user does not exist! eventId: "+eventId+" userId "+ userId);
        Event event = eventOpt.get();
        User user = userOpt.get();
        return event.getEventParticipant(user);
    }

    @Override
    public Page<EventParticipant> findAllEventParticipants(Pageable pageable, Long eventId) {
        if (eventRepository.existsById(eventId)) throw new NotFoundExeption("Event not found eventId: "+eventId);
        return eventParticipantRepository.findAllByEventId(eventId, pageable);
    }

    @Override
    public EventParticipant updateEventParticipantRole(Long eventId, Long userId, EventRole eventRole, String loggedUserName){
        Optional<Event> eventOpt = this.findById(eventId);
        Optional<User> userOpt = this.userService.findById(userId);
        if (userOpt.isEmpty() || eventOpt.isEmpty()) throw new NotFoundExeption("Event or user does not exist! eventId: "+eventId+" userId "+ userId);

        Event event = eventOpt.get();
        User user = userOpt.get();

        if (!isUserPermitted(event, loggedUserName, EventRole.ADMIN))
            throw new UnauthorizedExeption("User username:"+loggedUserName+" not allowed to change user's role for event "+event);

        return event.setParticipantRole(user, eventRole);
    }
}
