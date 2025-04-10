package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.EventParticipant;
import com.kompetencyjny.EventBuddySpring.model.EventRole;
import com.kompetencyjny.EventBuddySpring.service.EventService;
import com.kompetencyjny.EventBuddySpring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;


    // [GET] /api/events?size={}?page={}
    @GetMapping
    public Page<Event> getAllEvents(Pageable pageable) {
        return eventService.findAll(pageable);
    }

    // [GET] /api/events/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventService.findById(id);
        return event.map(event_ -> new ResponseEntity<>(event_, HttpStatus.OK))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // [POST] /api/events
    @ResponseBody
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event,  @AuthenticationPrincipal UserDetails userDetails) {
        return new ResponseEntity<>(eventService.create(event, userDetails.getUsername()), HttpStatus.CREATED);
    }

    // [PUT] /api/events/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id,
                                             @RequestBody Event updatedEvent,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        if (! eventService.existsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(eventService.fullUpdate(id, updatedEvent, userDetails.getUsername()));
    }
    // [PATCH] /api/events/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<Event> partialUpdateEvent(@PathVariable Long id,
                                             @RequestBody Event updatedEvent,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        if (! eventService.existsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(eventService.partialUpdate(id, updatedEvent, userDetails.getUsername()));
    }

    // [DELETE] /api/events/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (eventService.existsById(id)) {
            eventService.deleteById(id, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Dodanie uczestnika (userId) do wydarzenia (eventId)
    // [POST] /api/events/{eventId}/participants/{userId}?role={eventRole}
    @PostMapping("/{eventId}/participants/{userId}")
    public ResponseEntity<EventParticipant> addParticipantToEvent(@PathVariable Long eventId,
                                                       @PathVariable Long userId,
                                                       @RequestParam(required = false) String role,
                                                       @AuthenticationPrincipal UserDetails userDetails) {

        EventRole eventRole;
        try {
            eventRole = EventRole.valueOf(role);
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        }
        EventParticipant eventParticipant = eventService.addEventParticipant(eventId, userId, eventRole, userDetails.getUsername());
        return ResponseEntity.ok(eventParticipant);
    }
    // Informacje o uczestniku (userId) z wydarzenia (eventId)
    // [GET] /api/events/{eventId}/participants/{userId}
    @GetMapping("/{eventId}/participants/{userId}")
    public ResponseEntity<EventParticipant> getParticipantRole(@PathVariable Long eventId,
                                                          @PathVariable Long userId
    ){
        Optional<EventParticipant> eventParticipantOpt =  eventService.getEventParticipant(eventId, userId);
        return eventParticipantOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Informacje o uczestnikach (userId) z wydarzenia (eventId)
    // [GET] /api/events/{eventId}/participants/{userId}
    @GetMapping("/{eventId}/participants")
    public ResponseEntity<Page<EventParticipant>> getParticipantRole(Pageable pageable,
                                                               @PathVariable Long eventId
    ){
        return ResponseEntity.ok(eventService.findAllEventParticipants(pageable, eventId));
    }

    // Usuwanie uczestnika (userId) z wydarzenia (eventId)
    // [DELETE] /api/events/{eventId}/participants/{userId}
    @DeleteMapping("/{eventId}/participants/{userId}")
    public ResponseEntity<Void> removeParticipantToEvent(@PathVariable Long eventId,
                                                         @PathVariable Long userId,
                                                         @AuthenticationPrincipal UserDetails userDetails
                                                         ){
        eventService.removeEventParticipant(eventId,userId, userDetails.getUsername());
        return null;
    }
}
