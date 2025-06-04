package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.dto.*;
import com.kompetencyjny.EventBuddySpring.mappers.EventMapper;
import com.kompetencyjny.EventBuddySpring.mappers.EventParticipantMapper;
import com.kompetencyjny.EventBuddySpring.mappers.ExpenseMapper;
import com.kompetencyjny.EventBuddySpring.model.*;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import com.kompetencyjny.EventBuddySpring.repo.ExpenseRepository;
import com.kompetencyjny.EventBuddySpring.repo.PollOptionRepository;
import com.kompetencyjny.EventBuddySpring.repo.UserRepository;
import com.kompetencyjny.EventBuddySpring.service.EventService;
import com.kompetencyjny.EventBuddySpring.service.FileStorageService;
import com.kompetencyjny.EventBuddySpring.service.PollService;
import com.kompetencyjny.EventBuddySpring.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventMapper eventMapper;
    private final EventParticipantMapper eventParticipantMapper;
    private  final FileStorageService fileStorageService;
    private final PollOptionRepository pollOptionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final PollService pollService;

    // [GET] /api/events?size={}?page={}
    @GetMapping
    public ResponseEntity<Page<EventDto>> getAllEvents(Pageable pageable, @AuthenticationPrincipal UserDetails userDetails) {
        Page<EventDto> eventDtos;
        Page<Event> eventsPage = eventService.findAllVisible(pageable, userDetails.getUsername());
        String email = userDetails.getUsername();

        Page<EventDto> dtoPage = eventsPage.map(event -> {

            if (event.getDate() == null && event.getDatePoll() != null && eventService.hasDateVotingEnded(event)) {
                PollOption winner = pollService.getWinner(event.getDatePoll());
                if (winner != null) {
                    event.setDate(LocalDate.parse(winner.getValue()));
                    eventRepository.save(event);
                }
            }
            if (event.getLocation() == null && event.getLocationPoll() != null && eventService.hasLocationVotingEnded(event)) {
                PollOption locationWinner = pollService.getWinner(event.getLocationPoll());
                if (locationWinner != null) {
                    event.setLocation(locationWinner.getValue());
                    eventRepository.save(event);
                }
            }


            EventDto dto = eventMapper.toDto(event);
            boolean isParticipant = event.getParticipants().stream()
                    .anyMatch(p -> {
                        String participantEmail = p.getUser().getEmail();
                        return email.equals(participantEmail);
                    });
            dto.setParticipant(isParticipant);
            return dto;
        });
        return ResponseEntity.ok(dtoPage);
    }

    // [GET] /api/events/{id}
    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Event> eventOpt = eventService.findVisibleById(id, userDetails.getUsername());

        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();
            EventDto dto = eventMapper.toDto(event);

            userRepository.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                dto.setParticipant(event.isParticipant(user));
            });

            return ResponseEntity.ok(dto);
        }

        return ResponseEntity.notFound().build();
    }


    @GetMapping("/event/{eventId}/settlement")
    public Map<String, BigDecimal> getSettlement(@PathVariable Long eventId) {
        return eventService.calculateBalances(eventId);
    }



    // [POST] /api/events
    @ResponseBody
    @PostMapping
    public ResponseEntity<EventDto> createEvent(
            @Valid @RequestBody EventRequest eventRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        Event event = eventMapper.toEntity(eventRequest);

        // Jeśli w EventRequest zaznaczono głosowanie na datę – twórz Poll
        if (eventRequest.isEnableDateVoting()) {
            Poll datePoll = new Poll();
            datePoll.setQuestion("Wybierz datę wydarzenia");
            event.setDatePoll(datePoll);
        }

        // Analogicznie dla lokalizacji
        if (eventRequest.isEnableLocationVoting()) {
            Poll locationPoll = new Poll();
            locationPoll.setQuestion("Wybierz lokalizację wydarzenia");
            event.setLocationPoll(locationPoll);
        }

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

    // Wyświetlanie eventów dla danego użytkownika
    @GetMapping("/events-of-user/{userId}")
    public ResponseEntity<Page<EventDto>> getEventsOfUser(Pageable pageable,
                                                                     @PathVariable Long userId,
                                                                     @AuthenticationPrincipal UserDetails userDetails
    ){
        return ResponseEntity.ok(eventService.findAllEventsOfUser(pageable, userId, userDetails.getUsername()).map(eventMapper::toDto));
    }

    @PostMapping("/with-image")
    public ResponseEntity<EventDto> createEventWithImage(
            @RequestPart("event") EventRequest eventRequest,
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            String imagePath = fileStorageService.saveImage(image); // tu zapisuje się zdjęcie do katalogu
            eventRequest.setImageUrl(imagePath);                    // tylko ścieżka trafia do bazy

            Event event = eventMapper.toEntity(eventRequest);
            event.setBudgetDeadline(eventRequest.getBudgetDeadline());
            event = eventService.create(event, userDetails.getUsername());

            return new ResponseEntity<>(eventMapper.toDto(event), HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{eventId}/datePollOptions")
    public ResponseEntity<List<PollOptionDto>> getDatePollOptions(@PathVariable Long eventId) {
        List<PollOption> options = pollOptionRepository.findDatePollOptionsByEventId(eventId);
        List<PollOptionDto> dtos = options.stream()
                .map(o -> new PollOptionDto(o.getId(), o.getValue(), o.getVoteCount(), o.getPoll().getId()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{eventId}/locationPollOptions")
    public ResponseEntity<List<PollOptionDto>> getLocationPollOptions(@PathVariable Long eventId) {
        List<PollOption> options = pollOptionRepository.findLocationPollOptionsByEventId(eventId);
        List<PollOptionDto> dtos = options.stream()
                .map(o -> new PollOptionDto(o.getId(), o.getValue(), o.getVoteCount(), o.getPoll().getId()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

}
