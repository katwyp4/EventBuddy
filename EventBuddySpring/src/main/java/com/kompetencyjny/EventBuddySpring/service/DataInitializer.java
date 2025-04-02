package com.kompetencyjny.EventBuddySpring.service;

import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.Task;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EventRepository eventRepository;

    public DataInitializer(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) {
        // Tworzymy przykładowe wydarzenie
        Event event = new Event(
                "Koncert Rockowy",
                "Koncert w plenerze",
                LocalDate.now().plusWeeks(2),
                "Amfiteatr w Łodzi"
        );

        // Zadania
        Task t1 = new Task("Kup bilety", "TO_DO");
        Task t2 = new Task("Zarezerwuj hotel", "TO_DO");

        // Dodajemy do wydarzenia
        event.addTask(t1);
        event.addTask(t2);

        eventRepository.save(event);

        System.out.println(">>> Zainicjowano przykładowe dane w bazie.");
    }
}
