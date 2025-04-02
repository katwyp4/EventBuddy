package com.kompetencyjny.EventBuddySpring.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String status; // np. "TO_DO", "IN_PROGRESS", "DONE"

    @ManyToOne
    @JoinColumn(name = "event_id") // klucz obcy w tabeli tasks
    private Event event;

    public Task() {
    }

    public Task(String name, String status) {
        this.name = name;
        this.status = status;
    }

    // Gettery i Settery

    public Long getId() {
        return id;
    }

    // ID zazwyczaj nie zmieniamy ręcznie, ale można dodać setter w razie potrzeby
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
