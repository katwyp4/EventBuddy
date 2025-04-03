package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username, @RequestParam String password) {
        User user = userService.registerUser(username, password);
        logger.info("Zarejestrowano użytkownika: {}", user.getUsername());
        return ResponseEntity.ok("Zarejestrowano użytkownika: " + user.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        logger.info("Próba logowania: username={}", username);

        boolean isAuthenticated = userService.authenticateUser(username, password);

        if (isAuthenticated) {
            logger.info("Zalogowano pomyślnie: {}", username);
            return ResponseEntity.ok("Zalogowano pomyślnie! Witaj, " + username + "!");
        } else {
            logger.warn("Nieudane logowanie: {}", username);
            return ResponseEntity.status(401).body("Błędna nazwa użytkownika lub hasło. Spróbuj ponownie.");
        }
    }
}
