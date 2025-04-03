package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import com.kompetencyjny.EventBuddySpring.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventController(EventRepository eventRepository,
                           UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    // [GET] /api/events
    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // [GET] /api/events/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventRepository.findById(id);
        return event.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // [POST] /api/events
    @PostMapping
    public Event createEvent(@RequestBody Event event) {
        return eventRepository.save(event);
    }

    // [PUT] /api/events/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id,
                                             @RequestBody Event updatedEvent) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();
            event.setTitle(updatedEvent.getTitle());
            event.setDescription(updatedEvent.getDescription());
            event.setDate(updatedEvent.getDate());
            event.setLocation(updatedEvent.getLocation());
            event.setLatitude(updatedEvent.getLatitude());
            event.setLongitude(updatedEvent.getLongitude());
            event.setShareLink(updatedEvent.getShareLink());
            return ResponseEntity.ok(eventRepository.save(event));
        }
        return ResponseEntity.notFound().build();
    }

    // [DELETE] /api/events/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Dodanie uczestnika (userId) do wydarzenia (eventId)
    // [POST] /api/events/{eventId}/participants/{userId}
    @PostMapping("/{eventId}/participants/{userId}")
    public ResponseEntity<Event> addParticipantToEvent(@PathVariable Long eventId,
                                                       @PathVariable Long userId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (eventOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Event event = eventOpt.get();
        User user = userOpt.get();

        event.addParticipant(user);
        eventRepository.save(event);

        return ResponseEntity.ok(event);
    }
}
