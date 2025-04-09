package com.kompetencyjny.EventBuddySpring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

@Data // generuje gettery, settery, equals, hashCode, toString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private LocalDate date;
    private String location;

    private Double latitude;
    private Double longitude;
    private String shareLink;
    @JsonIgnore private Boolean active=true;
    @JsonIgnore private LocalDate deactivationDate=null;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<EventParticipant> participants = new HashSet<>();

    // Metody pomocnicze do obsługi listy Task
    public void addTask(Task task) {
        tasks.add(task);
        task.setEvent(this);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setEvent(null);
    }

    // Metody pomocnicze do obsługi uczestników
    public EventParticipant addParticipant(User user, EventRole role) {
        EventParticipant eventParticipant = new EventParticipant(this, user, role);
        participants.add(eventParticipant);
        return eventParticipant;
    }

    public EventParticipant addParticipant(User user) {
        EventParticipant eventParticipant = new EventParticipant(this, user);
        participants.add(eventParticipant);
        return eventParticipant;
    }

    public void removeParticipant(User user) {
        participants.remove(new EventParticipant(this, user));
    }

    public boolean isParticipant(User user){
        return participants.contains(new EventParticipant(this, user));
    }

    public Optional<EventParticipant> getEventParticipant(User user){
        return participants.stream().filter(participant->participant.getUser().equals(user)).findFirst();
    }

    public EventParticipant setParticipantRole(User user, EventRole role) {
        Optional<EventParticipant> eventParticipantOpt = getEventParticipant(user);
        if (eventParticipantOpt.isEmpty()) return addParticipant(user, role);

        EventParticipant eventParticipant = eventParticipantOpt.get();
        eventParticipant.setEventRole(role);
        return eventParticipant;
    }
}
