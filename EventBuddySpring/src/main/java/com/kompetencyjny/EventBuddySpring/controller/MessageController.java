package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.dto.CreateMessageDTO;
import com.kompetencyjny.EventBuddySpring.dto.MessageDTO;
import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.Message;
import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import com.kompetencyjny.EventBuddySpring.repo.MessageRepository;
import com.kompetencyjny.EventBuddySpring.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public MessageController(MessageRepository messageRepository,
                             EventRepository eventRepository,
                             UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    // [GET] /api/messages
    @CrossOrigin(origins = "*")
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    // Dodanie wiadomości do czatu
    // [POST] /api/messages?eventId=...&senderId=...
    @CrossOrigin(origins = "*")
    @PostMapping
    public ResponseEntity<MessageDTO> addMessage(@RequestBody CreateMessageDTO dto) {
        Optional<User> userOpt = userRepository.findById(dto.getSenderId());
        Optional<Event> eventOpt = eventRepository.findById(dto.getEventId());

        if (userOpt.isEmpty() || eventOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Message message = new Message();
        message.setContent(dto.getContent());
        message.setSentAt(LocalDateTime.now());
        message.setSender(userOpt.get());
        message.setEvent(eventOpt.get());

        Message saved = messageRepository.save(message);
        return ResponseEntity.ok(mapToDto(saved));  // użyjemy istniejącego mappera
    }

    @CrossOrigin(origins = "*")
    @GetMapping
    public ResponseEntity<List<MessageDTO>> getMessagesByEvent(@RequestParam Long eventId) {
        List<Message> messages = messageRepository.findByEventIdOrderBySentAtAsc(eventId);
        List<MessageDTO> dtoList = messages.stream()
                .map(this::mapToDto)
                .toList();

        return ResponseEntity.ok(dtoList);
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/latest")
    public ResponseEntity<List<Message>> getLatestMessages(
            @RequestParam Long eventId,
            @RequestParam String after) {

        LocalDateTime afterTime = LocalDateTime.parse(after);
        List<Message> messages = messageRepository.findByEventIdAndSentAtAfterOrderBySentAtAsc(eventId, afterTime);
        return ResponseEntity.ok(messages);
    }

    private MessageDTO mapToDto(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSentAt(message.getSentAt());

        User sender = message.getSender();
        dto.setSenderFullName(sender.getFirstName() + " " + sender.getLastName());

        dto.setEventId(message.getEvent().getId());
        return dto;
    }

}
