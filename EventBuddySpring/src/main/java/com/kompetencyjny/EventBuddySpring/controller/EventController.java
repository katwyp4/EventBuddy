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
    public ResponseEntity<Page<EventDto>> getAllEvents(Pageable pageable, @AuthenticationPrincipal UserDetails userDetails) {
        Page<EventDto> eventDtos;
        if (userDetails == null) eventDtos =  eventService.findAllPublic(pageable).map(eventMapper::toDto);
        eventDtos = eventService.findAllVisible(pageable, userDetails.getUsername()).map(eventMapper::toDto);
        return ResponseEntity.ok(eventDtos);
    }

    // [GET] /api/events/{id}
    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Event> eventOpt;
        if (userDetails == null) eventOpt = eventService.findPublicById(id);
        else eventOpt = eventService.findVisibleById(id, userDetails.getUsername());

        return eventOpt.map(event_ -> ResponseEntity.ok(eventMapper.toDto(event_)))
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
                                                                       @Valid @RequestBody(required = false) EventRoleRequest role,
                                                                       @AuthenticationPrincipal UserDetails userDetails) {


        EventRole eventRole;
        if (role==null) eventRole = EventRole.PASSIVE;
        else eventRole = role.toEventRoleEnum();

        EventParticipant eventParticipant = eventService.updateEventParticipantRole(eventId, userId, eventRole, userDetails.getUsername());
        return ResponseEntity.ok(eventParticipantMapper.toDto(eventParticipant));
    }
    // Informacje o uczestniku (userId) z wydarzenia (eventId)
    // [GET] /api/events/{eventId}/participants/{userId}
    @GetMapping("/{eventId}/participants/{userId}")
    public ResponseEntity<EventParticipantDto> getParticipant(@PathVariable Long eventId,
                                                              @PathVariable Long userId,
                                                              @AuthenticationPrincipal UserDetails userDetails
    ){
        if (userDetails == null) return new ResponseEntity<EventParticipantDto>(HttpStatus.UNAUTHORIZED);
        Optional<EventParticipant> eventParticipantOpt =  eventService.getEventParticipant(eventId, userId,userDetails.getUsername());
        if (eventParticipantOpt.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(eventParticipantMapper.toDto(eventParticipantOpt.get()));
    }

    // Informacje o uczestnikach (userId) z wydarzenia (eventId)
    // [GET] /api/events/{eventId}/participants/
    @GetMapping("/{eventId}/participants")
    public ResponseEntity<Page<EventParticipantDto>> getParticipants(Pageable pageable,
                                                                    @PathVariable Long eventId,
                                                                    @AuthenticationPrincipal UserDetails userDetails
    ){
        if (userDetails == null) return new ResponseEntity<Page<EventParticipantDto>>(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(eventService.findAllEventParticipants(pageable, eventId, userDetails.getUsername()).map(eventParticipantMapper::toDto));
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
