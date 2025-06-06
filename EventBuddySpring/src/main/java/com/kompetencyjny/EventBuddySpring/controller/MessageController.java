package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.dto.CreateMessageDTO;
import com.kompetencyjny.EventBuddySpring.dto.MessageDTO;
import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.Message;
import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import com.kompetencyjny.EventBuddySpring.repo.MessageRepository;
import com.kompetencyjny.EventBuddySpring.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final EventRepository   eventRepository;
    private final UserRepository    userRepository;

    public MessageController(MessageRepository messageRepository,
                             EventRepository   eventRepository,
                             UserRepository    userRepository) {
        this.messageRepository = messageRepository;
        this.eventRepository   = eventRepository;
        this.userRepository    = userRepository;
    }

    /* -------- POST  /api/messages  -------- */
    @PostMapping
    public ResponseEntity<MessageDTO> addMessage(@AuthenticationPrincipal UserDetails principal,
                                                 @RequestBody CreateMessageDTO dto) {

        /* 1️⃣  Pobierz zalogowanego użytkownika z JWT */
        User user = userRepository
                .findByEmail(principal.getUsername())
                .orElseThrow();                             // 401/403 obsługuje Spring Security

        /* 2️⃣  Sprawdź, czy event istnieje */
        Event event = eventRepository
                .findById(dto.getEventId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));


        /* 3️⃣  Zbuduj i zapisz wiadomość */
        Message msg = new Message();
        msg.setContent(dto.getContent());
        msg.setSentAt(LocalDateTime.now());
        msg.setSender(user);
        msg.setEvent(event);

        Message saved = messageRepository.save(msg);
        return ResponseEntity.ok(mapToDto(saved));
    }

    /* -------- GET  /api/messages?eventId=...  -------- */
    @GetMapping
    public ResponseEntity<List<MessageDTO>> getMessagesByEvent(@RequestParam Long eventId) {
        List<Message> messages = messageRepository.findByEventIdOrderBySentAtAsc(eventId);
        return ResponseEntity.ok(
                messages.stream().map(this::mapToDto).toList()
        );
    }

    /* -------- GET  /api/messages/latest?eventId=...&after=2025-06-06T08:30:00  -------- */
    @GetMapping("/latest")
    public ResponseEntity<List<MessageDTO>> getLatestMessages(@RequestParam Long eventId,
                                                              @RequestParam String after) {
        LocalDateTime afterTime;
        try {
            afterTime = LocalDateTime.parse(after);               // ISO-8601
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().build();
        }

        List<Message> messages = messageRepository
                .findByEventIdAndSentAtAfterOrderBySentAtAsc(eventId, afterTime);

        return ResponseEntity.ok(
                messages.stream().map(this::mapToDto).toList()
        );
    }

    /* -------- mapper encja ➜ DTO -------- */
    private MessageDTO mapToDto(Message m) {
        MessageDTO dto = new MessageDTO();
        dto.setId(m.getId());
        dto.setContent(m.getContent());
        dto.setSentAt(m.getSentAt());

        User s = m.getSender();
        dto.setSenderFullName(s.getFirstName() + " " + s.getLastName());

        dto.setEventId(m.getEvent().getId());
        return dto;
    }
}
