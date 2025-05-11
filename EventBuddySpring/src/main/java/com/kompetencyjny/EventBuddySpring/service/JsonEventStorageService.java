package com.kompetencyjny.EventBuddySpring.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kompetencyjny.EventBuddySpring.model.Event;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class JsonEventStorageService {

    private final Path eventFile = Paths.get("data/events.json");
    private final Path uploadDir = Paths.get("uploads/");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonEventStorageService() throws IOException {
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
        if (!Files.exists(eventFile)) {
            Files.createDirectories(eventFile.getParent());
            Files.write(eventFile, "[]".getBytes()); // pusty JSON array
        }
    }

    public List<Event> getAllEvents() throws IOException {
        return objectMapper.readValue(eventFile.toFile(), new TypeReference<List<Event>>() {});
    }

    public Event saveEvent(Event event, MultipartFile image) throws IOException {
        List<Event> events = getAllEvents();

        // Zapisz zdjęcie
        String imageFileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path imagePath = uploadDir.resolve(imageFileName);
        Files.copy(image.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);

        // Ustaw ścieżkę do zdjęcia
        event.setImageUrl("/uploads/" + imageFileName);
        //event.setId(System.currentTimeMillis()); // prosty ID

        events.add(event);
        objectMapper.writeValue(eventFile.toFile(), events);

        return event;
    }
}
