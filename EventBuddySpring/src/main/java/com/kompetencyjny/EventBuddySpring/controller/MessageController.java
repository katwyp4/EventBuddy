package com.kompetencyjny.EventBuddySpring.controller;

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
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    // Dodanie wiadomości do czatu
    // [POST] /api/messages?eventId=...&senderId=...
    @PostMapping
    public ResponseEntity<Message> addMessage(@RequestParam Long eventId,
                                              @RequestParam Long senderId,
                                              @RequestBody String content) {

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Optional<User> userOpt = userRepository.findById(senderId);

        if (eventOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Message message = new Message();
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message.setSender(userOpt.get());
        message.setEvent(eventOpt.get());

        Message saved = messageRepository.save(message);
        return ResponseEntity.ok(saved);
    }
}
