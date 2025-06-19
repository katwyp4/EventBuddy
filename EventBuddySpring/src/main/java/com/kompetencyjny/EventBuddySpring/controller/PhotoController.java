package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.Photo;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import com.kompetencyjny.EventBuddySpring.repo.PhotoRepository;
import com.kompetencyjny.EventBuddySpring.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events/{eventId}/photos")
public class PhotoController {

    private final PhotoRepository     photoRepo;
    private final EventRepository     eventRepo;
    private final FileStorageService  storage;

    public PhotoController(PhotoRepository photoRepo,
                           EventRepository eventRepo,
                           FileStorageService storage) {
        this.photoRepo = photoRepo;
        this.eventRepo = eventRepo;
        this.storage   = storage;
    }

    /* ================== 1. pobierz listę zdjęć danego wydarzenia ================== */
    @GetMapping
    public ResponseEntity<List<String>> listPhotos(@PathVariable Long eventId) {
        List<String> urls = photoRepo.findByEvent_Id(eventId)
                .stream()
                .map(Photo::getUrl)
                .toList();
        return ResponseEntity.ok(urls);
    }

    /* ================== 2. wyślij nowe zdjęcie (multipart/form-data) =============== */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Void> upload(@PathVariable Long eventId,
                                       @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        Optional<Event> eventOpt = eventRepo.findById(eventId);
        if (eventOpt.isEmpty()) return ResponseEntity.notFound().build();

        try {
            String url = storage.store(file);

            Photo photo = new Photo();
            photo.setUrl(url);
            photo.setEvent(eventOpt.get());
            photoRepo.save(photo);

            return ResponseEntity.ok().build();
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
