package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.security.JwtUtil;
import com.kompetencyjny.EventBuddySpring.service.UserService;
import com.kompetencyjny.EventBuddySpring.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String firstName,
            @RequestParam String lastName
    ) {
        User user = userService.registerUser(email, password, firstName, lastName);
        logger.info("Zarejestrowano użytkownika: {}", user.getEmail());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Zarejestrowano użytkownika: " + user.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String email,
            @RequestParam String password
    ) {
        boolean isAuthenticated = userService.authenticateUser(email, password);

        if (isAuthenticated) {
            String token = jwtUtil.generateToken(email);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(401)
                    .body("Błędny e-mail lub hasło.");
        }
    }
}
