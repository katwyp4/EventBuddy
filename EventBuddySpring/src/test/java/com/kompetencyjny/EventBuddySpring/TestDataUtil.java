package com.kompetencyjny.EventBuddySpring;

import com.kompetencyjny.EventBuddySpring.dto.EventRequest;
import com.kompetencyjny.EventBuddySpring.dto.TaskRequest;
import com.kompetencyjny.EventBuddySpring.model.*;
import com.kompetencyjny.EventBuddySpring.service.TaskService;
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
        event.setEnableDateVoting(false);
        event.setEnableLocationVoting(false);
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
        event.setEnableDateVoting(false);
        event.setEnableLocationVoting(false);
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
        event.setEnableDateVoting(false);
        event.setEnableLocationVoting(false);
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
        event.setEnableDateVoting(false);
        event.setEnableLocationVoting(false);
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
        event.setEnableDateVoting(false);
        event.setEnableLocationVoting(false);
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
        event.setEnableDateVoting(false);
        event.setEnableLocationVoting(false);
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
        event.setEnableDateVoting(false);
        event.setEnableLocationVoting(false);
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
        event.setEnableDateVoting(false);
        event.setEnableLocationVoting(false);
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
        event.setEnableDateVoting(false);
        event.setEnableLocationVoting(false);
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
        event.setEnableDateVoting(false);
        event.setEnableLocationVoting(false);
        return event;
    }

    public static User getRegisteredUserA(UserService userService){
        return userService.registerUser("testA@example.example", "qwerty", "test", "testowy");
    }

    public static User getRegisteredUserB(UserService userService){
        return userService.registerUser("testB@example.example", "qwerty", "test", "testowy");
    }

    public static User getRegisteredUserC(UserService userService){
        return userService.registerUser("testC@example.example", "qwerty", "test", "testowy");
    }

    public static User getRegisteredUserD(UserService userService){
        return userService.registerUser("testC@example.example", "qwerty", "test", "testowy");
    }

    @Transactional
    public static User getRegisteredUserAdmin1(UserService userService){
        User user =  userService.registerUser("testTestAdmin1@example.example", "qwerty", "test", "testowy");
        return userService.setUserRole(user.getEmail(), Role.ADMIN);
    }

    @Transactional
    public static User getRegisteredUserAdmin2(UserService userService){
        User user =  userService.registerUser("testTestAdmin2@example.example", "qwerty", "test", "testowy");
        return userService.setUserRole(user.getEmail(), Role.ADMIN);
    }

    @Transactional
    public static Task getTaskA (TaskService taskService, Long eventId, String loggedEmail){
        Task task = new Task();
        task.setStatus(TaskStatus.TODO);
        task.setName("Task A");
        return taskService.create(eventId, task, loggedEmail);
    }

    @Transactional
    public static Task getTaskB (TaskService taskService, Long eventId, String loggedEmail){
        Task task = new Task();
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setName("Task B");
        return taskService.create(eventId, task, loggedEmail);
    }

    @Transactional
    public static Task getTaskC (TaskService taskService, Long eventId, String loggedEmail){
        Task task = new Task();
        task.setStatus(TaskStatus.DONE);
        task.setName("Task C");
        return taskService.create(eventId, task, loggedEmail);
    }

    @Transactional
    public static TaskRequest getTaskRequestA(){
        return new TaskRequest("Task A", "TODO");
    }

    @Transactional
    public static TaskRequest getTaskRequestB(){
        return new TaskRequest("Task B", "IN_PROGRESS");
    }

    @Transactional
    public static TaskRequest getTaskRequestC(){
        return new TaskRequest("Task C", "DONE");
    }

}
