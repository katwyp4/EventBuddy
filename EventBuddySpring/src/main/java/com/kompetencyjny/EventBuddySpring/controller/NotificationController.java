package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.service.PushNotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final PushNotificationService pushNotificationService;

    public NotificationController(PushNotificationService service) {
        this.pushNotificationService = service;
    }

    @PostMapping("/send")
    public void sendNotification(@RequestParam String token,
                                 @RequestParam String title,
                                 @RequestParam String body) {
        pushNotificationService.sendPushNotification(token, title, body);
    }
}

