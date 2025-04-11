package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.dto.EventDto;
import com.kompetencyjny.EventBuddySpring.dto.EventParticipantDto;
import com.kompetencyjny.EventBuddySpring.dto.EventRequest;
import com.kompetencyjny.EventBuddySpring.dto.EventRoleRequest;
import com.kompetencyjny.EventBuddySpring.mappers.EventMapper;
import com.kompetencyjny.EventBuddySpring.mappers.EventParticipantMapper;
import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.EventParticipant;
import com.kompetencyjny.EventBuddySpring.model.EventRole;
import com.kompetencyjny.EventBuddySpring.service.EventService;
import jakarta.validation.Valid;
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
    private final EventMapper eventMapper;
    private final EventParticipantMapper eventParticipantMapper;


    // [GET] /api/events?size={}?page={}
    @GetMapping
    public ResponseEntity<Page<EventDto>> getAllEvents(Pageable pageable) {
         Page<EventDto> eventDtos = eventService.findAll(pageable).map(eventMapper::toDto);
         return ResponseEntity.ok(eventDtos);
    }

    // [GET] /api/events/{id}
    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventService.findById(id);
        return event.map(event_ -> ResponseEntity.ok(eventMapper.toDto(event_)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // [POST] /api/events
    @ResponseBody
    @PostMapping
    public ResponseEntity<EventDto> createEvent(@Valid @RequestBody EventRequest eventRequest, @AuthenticationPrincipal UserDetails userDetails) {
        Event event = eventMapper.toEntity(eventRequest);
        event = eventService.create(event, userDetails.getUsername());
        return new ResponseEntity<>(eventMapper.toDto(event), HttpStatus.CREATED);
    }

    // [PUT] /api/events/{id}
    @PutMapping("/{id}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id,
                                                @Valid @RequestBody EventRequest eventRequest,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        if (! eventService.existsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Event updatedEvent = eventService.fullUpdate(id, eventMapper.toEntity(eventRequest), userDetails.getUsername());
        return ResponseEntity.ok(eventMapper.toDto(updatedEvent));
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
    // [PUT] /api/events/{eventId}/participants/{userId}?role={eventRole}
    @PutMapping("/{eventId}/participants/{userId}")
    public ResponseEntity<EventParticipantDto> setEventParticipantRole(@PathVariable Long eventId,
                                                                       @PathVariable Long userId,
                                                                       @Valid @RequestParam(required = false) EventRoleRequest eventRoleRequest,
                                                                       @AuthenticationPrincipal UserDetails userDetails) {


        EventRole eventRole;
        if (eventRoleRequest==null) eventRole = EventRole.PASSIVE;
        else eventRole = eventRoleRequest.toEventRoleEnum();

        EventParticipant eventParticipant = eventService.updateEventParticipantRole(eventId, userId, eventRole, userDetails.getUsername());
        return ResponseEntity.ok(eventParticipantMapper.toDto(eventParticipant));
    }
    // Informacje o uczestniku (userId) z wydarzenia (eventId)
    // [GET] /api/events/{eventId}/participants/{userId}
    @GetMapping("/{eventId}/participants/{userId}")
    public ResponseEntity<EventParticipantDto> getParticipant(@PathVariable Long eventId,
                                                                  @PathVariable Long userId
    ){
        Optional<EventParticipant> eventParticipantOpt =  eventService.getEventParticipant(eventId, userId);
        if (eventParticipantOpt.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(eventParticipantMapper.toDto(eventParticipantOpt.get()));
    }

    // Informacje o uczestnikach (userId) z wydarzenia (eventId)
    // [GET] /api/events/{eventId}/participants/
    @GetMapping("/{eventId}/participants")
    public ResponseEntity<Page<EventParticipantDto>> getParticipants(Pageable pageable,
                                                                    @PathVariable Long eventId
    ){
        return ResponseEntity.ok(eventService.findAllEventParticipants(pageable, eventId).map(eventParticipantMapper::toDto));
    }

    // Usuwanie uczestnika (userId) z wydarzenia (eventId)
    // [DELETE] /api/events/{eventId}/participants/{userId}
    @DeleteMapping("/{eventId}/participants/{userId}")
    public ResponseEntity<Void> removeParticipantFromEvent(@PathVariable Long eventId,
                                                           @PathVariable Long userId,
                                                           @AuthenticationPrincipal UserDetails userDetails
                                                         ){
        eventService.removeEventParticipant(eventId,userId, userDetails.getUsername());
        return null;
    }
}
