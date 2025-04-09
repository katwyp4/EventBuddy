package com.kompetencyjny.EventBuddySpring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;


@Data // generuje gettery, settery, equals, hashCode, toString
@NoArgsConstructor
@Entity
@Table(name = "events_participants")
public class EventParticipant {

    @EmbeddedId
    private UserEventId id = new UserEventId();

    @ManyToOne
    @MapsId("eventId")
    private Event event;

    @ManyToOne
    @MapsId("userId")
    private User user;


    @Enumerated(EnumType.STRING)
    private EventRole eventRole=EventRole.PASSIVE;

    public EventParticipant(Event event, User user){
        this.id.setUserId(user.getId());
        this.id.setEventId(event.getId());
        this.event=event;
        this.user=user;
    }

    public EventParticipant(Event event, User user, EventRole eventRole){
        this.id.setUserId(user.getId());
        this.id.setEventId(event.getId());
        this.event=event;
        this.user=user;
        this.eventRole = eventRole;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EventParticipant that = (EventParticipant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
