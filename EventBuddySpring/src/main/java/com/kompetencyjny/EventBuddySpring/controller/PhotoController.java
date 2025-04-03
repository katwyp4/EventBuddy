package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.Photo;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import com.kompetencyjny.EventBuddySpring.repo.PhotoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoRepository photoRepository;
    private final EventRepository eventRepository;

    public PhotoController(PhotoRepository photoRepository,
                           EventRepository eventRepository) {
        this.photoRepository = photoRepository;
        this.eventRepository = eventRepository;
    }

    // [GET] /api/photos
    @GetMapping
    public List<Photo> getAllPhotos() {
        return photoRepository.findAll();
    }

    // Dodanie zdjęcia do wydarzenia
    // [POST] /api/photos?eventId=...&url=...
    @PostMapping
    public ResponseEntity<Photo> addPhoto(@RequestParam Long eventId,
                                          @RequestParam String url) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Tworzymy obiekt przez konstruktor bezargumentowy
        Photo photo = new Photo();
        // Ustawiamy pola przez settery
        photo.setUrl(url);
        photo.setEvent(eventOpt.get());

        Photo saved = photoRepository.save(photo);
        return ResponseEntity.ok(saved);
    }
}
