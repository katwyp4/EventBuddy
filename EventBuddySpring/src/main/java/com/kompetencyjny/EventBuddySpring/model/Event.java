package com.kompetencyjny.EventBuddySpring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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
    private EventPrivacy eventPrivacy;
    @JsonIgnore private Boolean active=true;
    @JsonIgnore private LocalDate deactivationDate=null;
    private String imageUrl;
    private LocalDate budgetDeadline;


    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<EventParticipant> participants = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "date_poll_id")
    private Poll datePoll;

    @OneToOne(cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "location_poll_id")
    private Poll locationPoll;

    private Boolean enableDateVoting;

    private Boolean enableLocationVoting;


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

    public boolean getEnableDateVoting() {
        return enableDateVoting;
    }
    public boolean getEnableLocationVoting() {
        return enableLocationVoting;
    }
}
