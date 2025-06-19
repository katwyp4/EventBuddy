package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.repo.UserRepository;
import com.kompetencyjny.EventBuddySpring.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final PushNotificationService pushNotificationService;
    private final UserRepository userRepository;

    public NotificationController(PushNotificationService service, UserRepository userRepository) {
        this.pushNotificationService = service;
        this.userRepository = userRepository;
    }

    @PostMapping("/send")
    public void sendNotification(@RequestParam String token,
                                 @RequestParam String title,
                                 @RequestParam String body) {
        pushNotificationService.sendPushNotification(token, title, body);
    }


    @PostMapping("/token")
    public void registerToken(@AuthenticationPrincipal UserDetails principal,
                              @RequestParam String token) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nie znaleziono użytkownika"));

        user.setFcmToken(token);
        userRepository.save(user);

        System.out.println("Zarejestrowano FCM-token dla użytkownika: " + user.getEmail());
    }

}

