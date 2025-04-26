package com.kompetencyjny.EventBuddySpring;

import com.kompetencyjny.EventBuddySpring.dto.EventRequest;
import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.EventPrivacy;
import com.kompetencyjny.EventBuddySpring.model.Role;
import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.service.UserService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public class TestDataUtil {
    public static Event getEventA(){
        Event event = new Event();
        event.setTitle("Event A");
        event.setDate(LocalDate.of(2025,3,1));
        event.setDescription("Event A Description");
        event.setLatitude(1.0);
        event.setLongitude(2.0);
        event.setLocation("Here");
        event.setShareLink("link");
        event.setEventPrivacy(EventPrivacy.PUBLIC_OPEN);
        return event;
    }

    public static Event getEventB(){
        Event event = new Event();
        event.setTitle("Event B");
        event.setDate(LocalDate.of(2025,4,3));
        event.setDescription("Event B Description");
        event.setLatitude(1.0);
        event.setLongitude(1.0);
        event.setLocation("There");
        event.setShareLink("link");
        event.setEventPrivacy(EventPrivacy.PUBLIC_OPEN);
        return event;
    }

    public static Event getEventC(){
        Event event = new Event();
        event.setTitle("Event C");
        event.setDate(LocalDate.of(2025,4,5));
        event.setDescription("Event C Description");
        event.setLatitude(1.0);
        event.setLongitude(2.0);
        event.setLocation("There");
        event.setShareLink("link");
        event.setEventPrivacy(EventPrivacy.PUBLIC_OPEN);
        return event;
    }
    public static Event getEventPrivate1(){
        Event event = new Event();
        event.setTitle("Event Private 1");
        event.setDate(LocalDate.of(2025,4,5));
        event.setDescription("Event Private 1 Description");
        event.setLatitude(1.0);
        event.setLongitude(2.0);
        event.setLocation("There");
        event.setShareLink("link");
        event.setEventPrivacy(EventPrivacy.PRIVATE);
        return event;
    }
    public static Event getEventPublicClosed(){
        Event event = new Event();
        event.setTitle("Event Public Closed 1");
        event.setDate(LocalDate.of(2025,4,5));
        event.setDescription("Event Public Closed 1 Description");
        event.setLatitude(1.0);
        event.setLongitude(2.0);
        event.setLocation("There");
        event.setShareLink("link");
        event.setEventPrivacy(EventPrivacy.PUBLIC_CLOSED);
        return event;
    }

    public static EventRequest getEventRequestA(){
        EventRequest event = new EventRequest();
        event.setTitle("Event A");
        event.setDate(LocalDate.of(2025,3,1));
        event.setDescription("Event A Description");
        event.setLatitude(1.0);
        event.setLongitude(2.0);
        event.setLocation("Here");
        event.setEventPrivacy("PUBLIC_OPEN");
        return event;
    }

    public static EventRequest getEventRequestB() {
        EventRequest event = new EventRequest();
        event.setTitle("Event B");
        event.setDate(LocalDate.of(2025,4,3));
        event.setDescription("Event B Description");
        event.setLatitude(1.0);
        event.setLongitude(1.0);
        event.setLocation("There");
        event.setEventPrivacy("PUBLIC_OPEN");
        return event;
    }

    public static EventRequest getEventRequestC(){
        EventRequest event = new EventRequest();
        event.setTitle("Event C");
        event.setDate(LocalDate.of(2025,4,5));
        event.setDescription("Event C Description");
        event.setLatitude(1.0);
        event.setLongitude(2.0);
        event.setLocation("There");
        event.setEventPrivacy("PUBLIC_OPEN");
        return event;
    }
    public static EventRequest getEventRequestPrivate1(){
        EventRequest event = new EventRequest();
        event.setTitle("Event Private 1");
        event.setDate(LocalDate.of(2025,4,5));
        event.setDescription("Event Private 1 Description");
        event.setLatitude(1.0);
        event.setLongitude(2.0);
        event.setLocation("There");
        event.setEventPrivacy("PRIVATE");
        return event;
    }
    public static EventRequest getEventRequestPublicClosed(){
        EventRequest event = new EventRequest();
        event.setTitle("Event Public Closed 1");
        event.setDate(LocalDate.of(2025,4,5));
        event.setDescription("Event Public Closed 1 Description");
        event.setLatitude(1.0);
        event.setLongitude(2.0);
        event.setLocation("There");
        event.setEventPrivacy("PUBLIC_CLOSED");
        return event;
    }

    public static User getRegisteredUserA(UserService userService){
        return userService.registerUser("userTestA", "qwerty", "test", "testowy", "testA@example.example");
    }

    public static User getRegisteredUserB(UserService userService){
        return userService.registerUser("userTestB", "qwerty", "test", "testowy", "testB@example.example");
    }

    public static User getRegisteredUserC(UserService userService){
        return userService.registerUser("userTestC", "qwerty", "test", "testowy", "testC@example.example");
    }

    public static User getRegisteredUserD(UserService userService){
        return userService.registerUser("userTestD", "qwerty", "test", "testowy", "testC@example.example");
    }

    @Transactional
    public static User getRegisteredUserAdmin1(UserService userService){
        User user =  userService.registerUser("userTestAdmin1", "qwerty", "test", "testowy", "testTestAdmin1@example.example");
        return userService.setUserRole(user.getUsername(), Role.ADMIN);
    }

    @Transactional
    public static User getRegisteredUserAdmin2(UserService userService){
        User user =  userService.registerUser("userTestTestAdmin2", "qwerty", "test", "testowy", "testTestAdmin2@example.example");
        return userService.setUserRole(user.getUsername(), Role.ADMIN);
    }


}
