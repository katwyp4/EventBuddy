package com.kompetencyjny.EventBuddySpring.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "event_participants",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

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
    public void addParticipant(User user) {
        participants.add(user);
    }

    public void removeParticipant(User user) {
        participants.remove(user);
    }
}
