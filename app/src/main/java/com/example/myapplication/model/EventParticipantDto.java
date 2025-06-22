package com.example.myapplication.model;

import com.example.myapplication.data.UserDto;

public class EventParticipantDto {
    private Long eventId;
    private String eventRole;
    private UserDto user;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventRole() {
        return eventRole;
    }

    public void setEventRole(String eventRole) {
        this.eventRole = eventRole;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}

