package com.kompetencyjny.EventBuddySpring.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/go")
public class DeepLinkController {

    @GetMapping("/event/{id}")
    public ResponseEntity<Void> redirect(@PathVariable String id,
                                         HttpServletRequest req) {

        String userAgent = req.getHeader("User-Agent");
        boolean mobile = userAgent != null &&
                (userAgent.contains("Android") || userAgent.contains("iPhone"));

        String deeplink = "myapp://event/" + id;
        String fallback = "https://play.google.com/store/apps/details?id=com.example.myapplication";

        HttpHeaders h = new HttpHeaders();
        h.add("Location", mobile ? deeplink : fallback);
        return ResponseEntity.status(302).headers(h).build();
    }
}


