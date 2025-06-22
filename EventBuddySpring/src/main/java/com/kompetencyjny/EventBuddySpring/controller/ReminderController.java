package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.dto.ReminderRequestDto;
import com.kompetencyjny.EventBuddySpring.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reminders")
public class ReminderController {
    private final ReminderService reminderService;

    @PostMapping
    public ResponseEntity<Void> createReminder(@RequestBody ReminderRequestDto reminderRequestDto) {
        reminderService.addReminder(reminderRequestDto.getEventId(), reminderRequestDto.getFcmToken(), reminderRequestDto.getDaysBefore());
        return ResponseEntity.ok().build();
    }
}
