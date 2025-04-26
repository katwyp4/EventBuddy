package com.kompetencyjny.EventBuddySpring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kompetencyjny.EventBuddySpring.TestDataUtil;
import com.kompetencyjny.EventBuddySpring.dto.EventRequest;
import com.kompetencyjny.EventBuddySpring.dto.EventRoleRequest;
import com.kompetencyjny.EventBuddySpring.model.*;
import com.kompetencyjny.EventBuddySpring.security.JwtUtil;
import com.kompetencyjny.EventBuddySpring.service.EventService;
import com.kompetencyjny.EventBuddySpring.service.UserService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.control.MappingControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class EventControllerIntegrationTests {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private EventService eventService;
    private UserService userService;
    private JwtUtil jwtUtil;

    @Autowired
    public EventControllerIntegrationTests(UserService userService, JwtUtil jwtUtil, MockMvc mockMvc, ObjectMapper objectMapper, EventService eventService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.eventService = eventService;
        this.jwtUtil = jwtUtil;
        this.userService =userService;
    }

    @Test
    public void testThatCreateEventSuccessfullyReturnsHttp201Created() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        EventRequest eventA = TestDataUtil.getEventRequestA();
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(
                MockMvcResultMatchers.status().isCreated()
        );
    }

    @Test
    public void testThatCreateEventSuccessfullyAddsToDataBase() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        EventRequest eventA = TestDataUtil.getEventRequestA();
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer " + token)
        );
        Optional<Event> eventOpt = eventService.findByIdInternal(1L);
        assert eventOpt.isPresent(): "Event not saved to database!";
        Event event = eventOpt.get();
        assert event.getActive() == Boolean.TRUE: "Event saved to database with active sent to false";
        assert event.getTitle().equals(eventA.getTitle()): "Event saved to database with wrong tile";
        assert event.getEventPrivacy().equals(EventPrivacy.valueOf(eventA.getEventPrivacy())): "Event saved to database with wrong privacy";
    }

    @Test
    public void testThatTryingToCreateEventWithoutBeingLoggedInReturnsHttp403Forbidden() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        EventRequest eventA = TestDataUtil.getEventRequestA();
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }

    @Test
    public void testThatEventWithCustomIdCannotBeCreated() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(100L);
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(1)
        );
        assert !eventService.existsById(100L): "GET with id in body set to 100 created event with id 100";
        assert eventService.existsById(1L): "GET with id in body set to 100 did not created event with id 1";
    }

    @Test
    public void testThatCreateEventSuccessfullyReturnsSavedObject() throws Exception {
        EventRequest eventA = TestDataUtil.getEventRequestA();
        String eventAJson = objectMapper.writeValueAsString(eventA);
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer " + token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value("Event A")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value("Event A Description")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(2.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value("Here")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value("2025-03-01")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventPrivacy").value(eventA.getEventPrivacy())
        );
    }

    @Test
    public void testThatCreateEventSuccessfullyReturnsSavedObjectPrivateEvent() throws Exception {
        EventRequest eventA = TestDataUtil.getEventRequestPrivate1();
        String eventAJson = objectMapper.writeValueAsString(eventA);
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer " + token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value(eventA.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value(eventA.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(eventA.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(eventA.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value(eventA.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value(eventA.getDate().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventPrivacy").value(eventA.getEventPrivacy())
        );
    }

    @Test
    public void testThatCreateEventSuccessfullyAddsToDataBasePrivateEvent() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        EventRequest eventA = TestDataUtil.getEventRequestPrivate1();
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer " + token)
        );
        Optional<Event> eventOpt = eventService.findByIdInternal(1L);
        assert eventOpt.isPresent(): "Event not saved to database!";
        Event event = eventOpt.get();
        assert event.getActive() == Boolean.TRUE: "Event saved to database with active sent to false";
        assert event.getTitle().equals(eventA.getTitle()): "Event saved to database with wrong tile";
        assert event.getEventPrivacy().equals(EventPrivacy.valueOf(eventA.getEventPrivacy())): "Event saved to database with wrong privacy";
    }

    @Test
    public void testThatCreateEventSuccessfullyReturnsSavedObjectPublicClosedEvent() throws Exception {
        EventRequest eventA = TestDataUtil.getEventRequestPublicClosed();
        String eventAJson = objectMapper.writeValueAsString(eventA);
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer " + token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value(eventA.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value(eventA.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(eventA.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(eventA.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value(eventA.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value(eventA.getDate().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventPrivacy").value(eventA.getEventPrivacy())
        );
    }

    @Test
    public void testThatCreateEventSuccessfullyAddsToDataBasePublicClosedEvent() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        EventRequest eventA = TestDataUtil.getEventRequestPublicClosed();
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer " + token)
        );
        Optional<Event> eventOpt = eventService.findByIdInternal(1L);
        assert eventOpt.isPresent(): "Event not saved to database!";
        Event event = eventOpt.get();
        assert event.getActive() == Boolean.TRUE: "Event saved to database with active sent to false";
        assert event.getTitle().equals(eventA.getTitle()): "Event saved to database with wrong tile";
        assert event.getEventPrivacy().equals(EventPrivacy.valueOf(eventA.getEventPrivacy())): "Event saved to database with wrong privacy";
    }

    @Test
    public void testThatCreateEventSuccessfullyReturnsHttp201CreatedMultipleEvents() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        EventRequest eventA = TestDataUtil.getEventRequestA();
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(
                MockMvcResultMatchers.status().isCreated()
        );

        EventRequest eventB = TestDataUtil.getEventRequestB();
        String eventBJson = objectMapper.writeValueAsString(eventB);
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventBJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(
                MockMvcResultMatchers.status().isCreated()
        );
    }

    @Test
    public void testThatCreateEventSuccessfullyReturnsSavedObjectMultipleEvents() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());

        EventRequest eventA = TestDataUtil.getEventRequestA();
        String eventAJson = objectMapper.writeValueAsString(eventA);
        EventRequest eventB = TestDataUtil.getEventRequestB();
        String eventBJson = objectMapper.writeValueAsString(eventB);
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer " + token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value("Event A")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value("Event A Description")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(2.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value("Here")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value("2025-03-01")
        );
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventBJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value("Event B")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value("Event B Description")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value("There")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value("2025-04-03")
        );
    }

    // GET--------------------------------------------------------------------------------------------------------------
    @Test
    public void testThatListEventsReturnsHttpStatus403WhenNotLoggedIn() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        mockMvc.perform(
        MockMvcRequestBuilders.get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }

    @Test
    public void testThatListEventsReturnsListOfEventsPrivateNotIncludedNotParticipant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event privateEvent = TestDataUtil.getEventPrivate1();
        privateEvent.setId(null);
        eventService.create(privateEvent, userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].title").value("Event A")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].description").value("Event A Description")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].latitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].longitude").value(2.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].location").value("Here")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].shareLink").value("link")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].date").value("2025-03-01")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1]").doesNotExist()
        );
    }

    @Test
    public void testThatListEventsReturnsListOfEventsPrivateIncludedNotParticipantButAdmin() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userAdmin1 = TestDataUtil.getRegisteredUserAdmin1(userService);
        String token = jwtUtil.generateToken(userAdmin1.getUsername());
        Event privateEvent = TestDataUtil.getEventPrivate1();
        privateEvent.setId(null);
        eventService.create(privateEvent, userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].title").value(privateEvent.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].description").value(privateEvent.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].latitude").value(privateEvent.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].longitude").value(privateEvent.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].location").value(privateEvent.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].shareLink").value(privateEvent.getShareLink())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].date").value(privateEvent.getDate().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].eventPrivacy").value(privateEvent.getEventPrivacy().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1]").exists()
        );
    }

    @Test
    public void testThatListEventsReturnsListOfEventsPrivateIncludedCreator() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event privateEvent = TestDataUtil.getEventPrivate1();
        privateEvent.setId(null);
        eventService.create(privateEvent, userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].title").value(privateEvent.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].description").value(privateEvent.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].latitude").value(privateEvent.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].longitude").value(privateEvent.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].location").value(privateEvent.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].shareLink").value(privateEvent.getShareLink())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].date").value(privateEvent.getDate().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].eventPrivacy").value(privateEvent.getEventPrivacy().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1]").exists()
        );
    }

    @Test
    public void testThatListEventsReturnsListOfEventsPrivateIncludedParticipant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event privateEvent = TestDataUtil.getEventPrivate1();
        privateEvent.setId(null);
        privateEvent = eventService.create(privateEvent, userA.getUsername());
        eventService.addEventParticipant(privateEvent.getId(), userB.getId(), EventRole.PASSIVE, userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].title").value(privateEvent.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].description").value(privateEvent.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].latitude").value(privateEvent.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].longitude").value(privateEvent.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].location").value(privateEvent.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].shareLink").value(privateEvent.getShareLink())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].date").value(privateEvent.getDate().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].eventPrivacy").value(privateEvent.getEventPrivacy().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1]").exists()
        );
    }

    @Test
    public void testThatListEventsReturnsListOfEventsClosedPublicIncludedParticipant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event publicClosedEvent = TestDataUtil.getEventPublicClosed();
        publicClosedEvent.setId(null);
        publicClosedEvent = eventService.create(publicClosedEvent, userA.getUsername());
        eventService.addEventParticipant(publicClosedEvent.getId(), userB.getId(), EventRole.PASSIVE, userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].title").value(publicClosedEvent.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].description").value(publicClosedEvent.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].latitude").value(publicClosedEvent.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].longitude").value(publicClosedEvent.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].location").value(publicClosedEvent.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].shareLink").value(publicClosedEvent.getShareLink())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].date").value(publicClosedEvent.getDate().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].eventPrivacy").value(publicClosedEvent.getEventPrivacy().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1]").exists()
        );
    }

    @Test
    public void testThatListEventsReturnsListOfEventsClosedPublicIncludedNotParticipant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event publicClosedEvent = TestDataUtil.getEventPublicClosed();
        publicClosedEvent.setId(null);
        publicClosedEvent = eventService.create(publicClosedEvent, userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].title").value(publicClosedEvent.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].description").value(publicClosedEvent.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].latitude").value(publicClosedEvent.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].longitude").value(publicClosedEvent.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].location").value(publicClosedEvent.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].shareLink").value(publicClosedEvent.getShareLink())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].date").value(publicClosedEvent.getDate().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].eventPrivacy").value(publicClosedEvent.getEventPrivacy().toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1]").exists()
        );
    }

    @Test
    public void testThatListEventsReturnsListOfEventsMultipleEvents() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventA.setId(null);
        eventService.create(eventB, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].title").value("Event A")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].description").value("Event A Description")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].latitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].longitude").value(2.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].location").value("Here")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].shareLink").value("link")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].date").value("2025-03-01")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].title").value("Event B")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].description").value("Event B Description")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].latitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].longitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].location").value("There")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].shareLink").value("link")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].date").value("2025-04-03")
        );
    }

    @Test
    public void testThatListEventsReturnsListOfEventsAfterAddingThroughAPIMultipleEvents() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        EventRequest eventA = TestDataUtil.getEventRequestA();
        EventRequest eventB = TestDataUtil.getEventRequestB();
        String eventAJson = objectMapper.writeValueAsString(eventA);
        String eventBJson = objectMapper.writeValueAsString(eventB);


        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
                        .content(eventAJson));

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
                        .content(eventBJson));


        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].title").value("Event A")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].description").value("Event A Description")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].latitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].longitude").value(2.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].location").value("Here")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].date").value("2025-03-01")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].title").value("Event B")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].description").value("Event B Description")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].latitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].longitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].location").value("There")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].date").value("2025-04-03")
        );
    }
    @Test
    public void testThatGetEventByIdOnExistingReturnsHttpStatus200() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        String token = jwtUtil.generateToken(userA.getUsername());
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatGetEventByIdOnExistingReturnsEvent() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value("Event A")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value("Event A Description")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(2.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value("Here")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value("2025-03-01")
        );
    }

    @Test
    public void testThatGetEventByIdOnExistingReturns403ForNotLoggedIn() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)

        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }

    @Test
    public void testThatGetEventByIdOnPrivateReturns403NotLoggedIn() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventPrivate1();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)

        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }

    @Test
    public void testThatGetEventByIdOnPrivateReturns404NotParticipant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventPrivate1();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }
    @Test
    public void testThatGetEventByIdOnPrivateReturns200AdminNotParticipant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userAdmin = TestDataUtil.getRegisteredUserAdmin1(userService);
        String token = jwtUtil.generateToken(userAdmin.getUsername());
        Event eventA = TestDataUtil.getEventPrivate1();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }
    @Test
    public void testThatGetEventByIdOnPrivateReturnsEventAdminNotParticipant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userAdmin = TestDataUtil.getRegisteredUserAdmin1(userService);
        String token = jwtUtil.generateToken(userAdmin.getUsername());
        Event eventA = TestDataUtil.getEventPrivate1();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value(eventA.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value(eventA.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(eventA.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(eventA.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value(eventA.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value(eventA.getDate().toString())
        );
    }

    @Test
    public void testThatGetEventByIdOnPrivateReturns200Participant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventPrivate1();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.PASSIVE, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatGetEventByIdOnPrivateReturnsEventParticipant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventPrivate1();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.PASSIVE, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value(eventA.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value(eventA.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(eventA.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(eventA.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value(eventA.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value(eventA.getDate().toString())
        );
    }

    @Test
    public void testThatGetEventByIdOnPrivateReturns200Creator() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventPrivate1();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatGetEventByIdOnPrivateReturnsEventCreator() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventPrivate1();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value(eventA.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value(eventA.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(eventA.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(eventA.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value(eventA.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value(eventA.getDate().toString())
        );
    }

    @Test
    public void testThatGetEventByIdOnClosedPublicReturns403NotLoggedIn() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventPublicClosed();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)

        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }

    @Test
    public void testThatGetEventByIdOnClosedPublicReturns200Creator() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventPublicClosed();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+ token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatGetEventByIdOnPublicClosedReturnsEventCreator() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventPublicClosed();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+ token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value(eventA.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value(eventA.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(eventA.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(eventA.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value(eventA.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value(eventA.getDate().toString())
        );
    }

    @Test
    public void testThatGetEventByIdOnClosedPublicReturns200NotParticipant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventPublicClosed();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+ token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatGetEventByIdOnPublicClosedReturnsEventNotParticipant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventPublicClosed();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+ token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value(eventA.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value(eventA.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(eventA.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(eventA.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value(eventA.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value(eventA.getDate().toString())
        );
    }

    @Test
    public void testThatGetEventByIdOnClosedPublicReturns200Participant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventPublicClosed();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.PASSIVE, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+ token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatGetEventByIdOnPublicClosedReturnsEventParticipant() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventPublicClosed();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.PASSIVE, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+ token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value(eventA.getTitle())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value(eventA.getDescription())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(eventA.getLatitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(eventA.getLongitude())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value(eventA.getLocation())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value(eventA.getDate().toString())
        );
    }
    @Test
    public void testThatGetEventByIdWhenNoRecordsReturnsHttpStatus404() throws Exception{
        User user = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(user.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+ token)

        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }
    @Test
    public void testThatGetEventByIdWhenDoesNotExistsReturnsHttpStatus404() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+ token)

        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }

    // UPDATE ----------------------------------------------------------------------------------------------------------

    @Test
    public void testThatUpdateEventOnExistingDoneNothingReturnsHttpStatus200() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }
    @Test
    public void testThatUpdateEventOnExistingChangedReturnsHttpStatus200() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatUpdateEventOnExistingDoneNothingReturnsEvent() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)


        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(1)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value("Event A")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value("Event A Description")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(2.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value("Here")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value("2025-03-01")
        );
    }


    @Test
    public void testThatUpdateEventOnExistingChangedReturnsUpdatedEvent() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(1)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value("Changed title")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.description").value("Changed description")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.latitude").value(1.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.longitude").value(2.0)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.location").value("Here")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.date").value("2025-03-01")
        );
    }

    @Test
    public void testThatEventCannotHaveIdUpdated() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventA.setId(100L);
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer " + token)
        );
        assert !eventService.existsById(100L): "PUT with id in body set to 100 changed event id to 100";
        assert eventService.existsById(1L): "Cannot find event with id 1 after PUT to /api/events/1 with id in body set to 100";
    }


    @Test
    public void testThatUpdateEventOnExistingChangedChangesInDataBase() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        );
        Optional<Event> updatedEventOpt = eventService.findByIdInternal(Long.valueOf(1L));
        assert updatedEventOpt.isPresent(): "Event not existing after updating!";
        assert updatedEventOpt.get().getTitle().equals("Changed title"): "The event's title hasn't changed after the update";
        assert updatedEventOpt.get().getDescription().equals("Changed description"): "The event's description hasn't changed after the update";
        assert updatedEventOpt.get().getLatitude().equals(eventA.getLatitude()): "The event's latitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLongitude().equals(eventA.getLongitude()): "The event's longitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDate().equals(eventA.getDate()): "The event's date has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLocation().equals(eventA.getLocation()): "The event's location has changed after the update and was not supposed to";
    }

    @Test
    public void testThatUpdateEventWhenNoRecordsReturnsHttpStatus404() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }
    @Test
    public void testThatUpdateEventWhenDoesNotExistsReturnsHttpStatus404() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }
    @Test
    public void testThatUpdateEventWhenNotLoggedInReturnsHttpStatus403() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)

        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }

    @Test
    public void testThatUpdateEventWhenNotLoggedInDidNotChangeAnythingInTheDataBase() throws Exception {
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
        );
        Event eventAOG = TestDataUtil.getEventA();
        Optional<Event> updatedEventOpt = eventService.findByIdInternal(Long.valueOf(1L));
        assert updatedEventOpt.isPresent(): "Event not existing after updating!";
        assert updatedEventOpt.get().getTitle().equals(eventAOG.getTitle()): "The event's title has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDescription().equals(eventAOG.getDescription()): "The event's description has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLatitude().equals(eventAOG.getLatitude()): "The event's latitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLongitude().equals(eventAOG.getLongitude()): "The event's longitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDate().equals(eventAOG.getDate()): "The event's date has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLocation().equals(eventAOG.getLocation()): "The event's location has changed after the update and was not supposed to";
    }

    @Test
    public void testThatUpdateEventWhenNotParticipantHttpStatus403() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }

    @Test
    public void testThatUpdateEventWhenNotParticipantDidNotChangeAnythingInTheDataBase() throws Exception {
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)
        );
        Event eventAOG = TestDataUtil.getEventA();
        Optional<Event> updatedEventOpt = eventService.findByIdInternal(Long.valueOf(1L));
        assert updatedEventOpt.isPresent(): "Event not existing after updating!";
        assert updatedEventOpt.get().getTitle().equals(eventAOG.getTitle()): "The event's title has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDescription().equals(eventAOG.getDescription()): "The event's description has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLatitude().equals(eventAOG.getLatitude()): "The event's latitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLongitude().equals(eventAOG.getLongitude()): "The event's longitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDate().equals(eventAOG.getDate()): "The event's date has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLocation().equals(eventAOG.getLocation()): "The event's location has changed after the update and was not supposed to";
    }

    @Test
    public void testThatUpdateEventWhenNotParticipantButAdminOfAnotherEventHttpStatus403() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventA.setId(null);
        eventB.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.create(eventB, userB.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }

    @Test
    public void testThatUpdateEventWhenNotParticipantOfThatEventButAdminOfAnotherDidNotChangeAnythingInTheDataBase() throws Exception {
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        Event eventB = TestDataUtil.getEventB();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventB.setId(null);
        eventService.create(eventB, userB.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)
        );
        Event eventAOG = TestDataUtil.getEventA();
        Optional<Event> updatedEventOpt = eventService.findByIdInternal(Long.valueOf(1L));
        assert updatedEventOpt.isPresent(): "Event not existing after updating!";
        assert updatedEventOpt.get().getTitle().equals(eventAOG.getTitle()): "The event's title has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDescription().equals(eventAOG.getDescription()): "The event's description has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLatitude().equals(eventAOG.getLatitude()): "The event's latitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLongitude().equals(eventAOG.getLongitude()): "The event's longitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDate().equals(eventAOG.getDate()): "The event's date has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLocation().equals(eventAOG.getLocation()): "The event's location has changed after the update and was not supposed to";
    }

    @Test
    public void testThatUpdateEventWhenPassiveParticipantOfThatEventButAdminOfAnotherEventHttpStatus403() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventA.setId(null);
        eventB.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.create(eventB, userB.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.PASSIVE, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }

    @Test
    public void testThatUpdateEventWhenPassiveParticipantOfThatEventButAdminOfAnotherDidNotChangeAnythingInTheDataBase() throws Exception {
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        Event eventB = TestDataUtil.getEventB();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventB.setId(null);
        eventService.create(eventB, userB.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.PASSIVE, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)
        );
        Event eventAOG = TestDataUtil.getEventA();
        Optional<Event> updatedEventOpt = eventService.findByIdInternal(Long.valueOf(1L));
        assert updatedEventOpt.isPresent(): "Event not existing after updating!";
        assert updatedEventOpt.get().getTitle().equals(eventAOG.getTitle()): "The event's title has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDescription().equals(eventAOG.getDescription()): "The event's description has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLatitude().equals(eventAOG.getLatitude()): "The event's latitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLongitude().equals(eventAOG.getLongitude()): "The event's longitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDate().equals(eventAOG.getDate()): "The event's date has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLocation().equals(eventAOG.getLocation()): "The event's location has changed after the update and was not supposed to";
    }

    @Test
    public void testThatUpdateEventWhenActiveParticipantOfThatEventButAdminOfAnotherEventHttpStatus403() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventA.setId(null);
        eventB.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.create(eventB, userB.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.ACTIVE, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }

    @Test
    public void testThatUpdateEventWhenActiveParticipantOfThatEventButAdminOfAnotherDidNotChangeAnythingInTheDataBase() throws Exception {
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        Event eventB = TestDataUtil.getEventB();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventB.setId(null);
        eventService.create(eventB, userB.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.ACTIVE, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)
        );
        Event eventAOG = TestDataUtil.getEventA();
        Optional<Event> updatedEventOpt = eventService.findByIdInternal(Long.valueOf(1L));
        assert updatedEventOpt.isPresent(): "Event not existing after updating!";
        assert updatedEventOpt.get().getTitle().equals(eventAOG.getTitle()): "The event's title has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDescription().equals(eventAOG.getDescription()): "The event's description has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLatitude().equals(eventAOG.getLatitude()): "The event's latitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLongitude().equals(eventAOG.getLongitude()): "The event's longitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDate().equals(eventAOG.getDate()): "The event's date has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLocation().equals(eventAOG.getLocation()): "The event's location has changed after the update and was not supposed to";
    }

    @Test
    public void testThatUpdateEventWhenAdminParticipantOfThatEventReturnsHttpStatus200() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventA.setId(null);
        eventB.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.create(eventB, userB.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.ADMIN, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatUpdateEventWhenAdminParticipantOfThatEventChangedInTheDataBase() throws Exception {
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        Event eventB = TestDataUtil.getEventB();
        eventB.setId(null);
        eventService.create(eventB, userB.getUsername());
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.ADMIN, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)
        );
        Optional<Event> updatedEventOpt = eventService.findByIdInternal(Long.valueOf(1L));
        assert updatedEventOpt.isPresent(): "Event not existing after updating!";
        assert updatedEventOpt.get().getTitle().equals(eventA.getTitle()): "The event's title has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDescription().equals(eventA.getDescription()): "The event's description has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLatitude().equals(eventA.getLatitude()): "The event's latitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLongitude().equals(eventA.getLongitude()): "The event's longitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDate().equals(eventA.getDate()): "The event's date has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLocation().equals(eventA.getLocation()): "The event's location has changed after the update and was not supposed to";
    }

    @Test
    public void testThatUpdateEventWhenAdminOfServiceNotParticipantReturnsHttpStatus200() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userAdmin1 = TestDataUtil.getRegisteredUserAdmin1(userService);
        String token = jwtUtil.generateToken(userAdmin1.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatUpdateEventWhenAdminOfServiceNotParticipantOfThatEventChangedInTheDataBase() throws Exception {
        User userAdmin1 = TestDataUtil.getRegisteredUserAdmin1(userService);
        String token = jwtUtil.generateToken(userAdmin1.getUsername());
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)
        );
        Optional<Event> updatedEventOpt = eventService.findByIdInternal(Long.valueOf(1L));
        assert updatedEventOpt.isPresent(): "Event not existing after updating!";
        assert updatedEventOpt.get().getTitle().equals(eventA.getTitle()): "The event's title has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDescription().equals(eventA.getDescription()): "The event's description has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLatitude().equals(eventA.getLatitude()): "The event's latitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLongitude().equals(eventA.getLongitude()): "The event's longitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDate().equals(eventA.getDate()): "The event's date has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLocation().equals(eventA.getLocation()): "The event's location has changed after the update and was not supposed to";
    }

    @Test
    public void testThatUpdateEventWhenAdminOfServicePassiveParticipantReturnsHttpStatus200() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userAdmin1 = TestDataUtil.getRegisteredUserAdmin1(userService);
        String token = jwtUtil.generateToken(userAdmin1.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userAdmin1.getId(), EventRole.PASSIVE, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatUpdateEventWhenAdminOfServicePassiveParticipantOfThatEventChangedInTheDataBase() throws Exception {
        User userAdmin1 = TestDataUtil.getRegisteredUserAdmin1(userService);
        String token = jwtUtil.generateToken(userAdmin1.getUsername());
        User userA = TestDataUtil.getRegisteredUserA(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userAdmin1.getId(), EventRole.PASSIVE, userA.getUsername());
        eventA.setTitle("Changed title");
        eventA.setDescription("Changed description");
        String eventAJson = objectMapper.writeValueAsString(eventA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)
        );
        Optional<Event> updatedEventOpt = eventService.findByIdInternal(Long.valueOf(1L));
        assert updatedEventOpt.isPresent(): "Event not existing after updating!";
        assert updatedEventOpt.get().getTitle().equals(eventA.getTitle()): "The event's title has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDescription().equals(eventA.getDescription()): "The event's description has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLatitude().equals(eventA.getLatitude()): "The event's latitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLongitude().equals(eventA.getLongitude()): "The event's longitude has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getDate().equals(eventA.getDate()): "The event's date has changed after the update and was not supposed to";
        assert updatedEventOpt.get().getLocation().equals(eventA.getLocation()): "The event's location has changed after the update and was not supposed to";
    }

    // DELETE-----------------------------------------------------------------------------------------------------------
    @Test
    public void testThatDeleteReturnsStatusNoContent() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isNoContent()
        );
    }
    @Test
    public void testThatDeleteDeletesFromDatabase() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
        );
        Optional<Event> eventOpt = eventService.findByIdInternal(eventA.getId());
        assert eventOpt.isEmpty(): "Delete didn't remove";
    }
    @Test
    public void testThatDeleteNotLoggedInReturnsStatus403() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)

        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }
    @Test
    public void testThatDeleteNotLoggedInDoesNotDeleteFromDatabase() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
        );
        Optional<Event> eventOpt = eventService.findByIdInternal(eventA.getId());
        assert eventOpt.isPresent(): "Delete did remove when not logged in";
    }

    @Test
    public void testThatDeleteNotParticipantReturnsStatus403() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventB.setId(null);
        eventService.create(eventB, userB.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }
    @Test
    public void testThatDeleteNotParticipantDoesNotDeleteFromDatabase() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventB.setId(null);
        eventService.create(eventB, userB.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
        );
        Optional<Event> eventOpt = eventService.findByIdInternal(eventA.getId());
        assert eventOpt.isPresent(): "Delete did remove when not logged in";
    }


    @Test
    public void testThatDeletePassiveParticipantReturnsStatus403() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.PASSIVE, userA.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventB.setId(null);
        eventService.create(eventB, userB.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }

    @Test
    public void testThatDeletePassiveParticipantDoesNotDeleteFromDatabase() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.PASSIVE, userA.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventB.setId(null);
        eventService.create(eventB, userB.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
        );
        Optional<Event> eventOpt = eventService.findByIdInternal(eventA.getId());
        assert eventOpt.isPresent(): "Delete did remove when user is passive participant";
    }

    @Test
    public void testThatDeleteActiveParticipantReturnsStatus403() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.ACTIVE, userA.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventB.setId(null);
        eventService.create(eventB, userB.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isForbidden()
        );
    }

    @Test
    public void testThatDeleteActiveParticipantDoesNotDeleteFromDatabase() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.ACTIVE, userA.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventB.setId(null);
        eventService.create(eventB, userB.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
        );
        Optional<Event> eventOpt = eventService.findByIdInternal(eventA.getId());
        assert eventOpt.isPresent(): "Delete did remove when user is active participant";
    }

    @Test
    public void testThatDeleteAdminParticipantReturnsStatusNoContent() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.ADMIN, userA.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventB.setId(null);
        eventService.create(eventB, userB.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isNoContent()
        );
    }

    @Test
    public void testThatDeleteAdminParticipantDeletesFromDatabase() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userB = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(userB.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userB.getId(), EventRole.ADMIN, userA.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventB.setId(null);
        eventService.create(eventB, userB.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
        );
        Optional<Event> eventOpt = eventService.findByIdInternal(eventA.getId());
        assert eventOpt.isEmpty(): "Delete did not remove when user is admin participant";
    }

    @Test
    public void testThatDeleteAdminOfServiceReturnsStatusNoContent() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userAdmin1 = TestDataUtil.getRegisteredUserAdmin1(userService);
        String token = jwtUtil.generateToken(userAdmin1.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isNoContent()
        );
    }

    @Test
    public void testThatDeleteAdminOfServiceDeletesFromDatabase() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userAdmin1 = TestDataUtil.getRegisteredUserAdmin1(userService);
        String token = jwtUtil.generateToken(userAdmin1.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
        );
        Optional<Event> eventOpt = eventService.findByIdInternal(eventA.getId());
        assert eventOpt.isEmpty(): "Delete did not remove when user is admin of the service";
    }


    @Test
    public void testThatDeleteAdminOfServiceThatIsAParticipantReturnsStatusNoContent() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userAdmin1 = TestDataUtil.getRegisteredUserAdmin1(userService);
        String token = jwtUtil.generateToken(userAdmin1.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userAdmin1.getId(), EventRole.PASSIVE, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isNoContent()
        );
    }

    @Test
    public void testThatDeleteAdminOfServiceThatIsAParticipantDeletesFromDatabase() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        User userAdmin1 = TestDataUtil.getRegisteredUserAdmin1(userService);
        String token = jwtUtil.generateToken(userAdmin1.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        eventService.addEventParticipant(eventA.getId(), userAdmin1.getId(), EventRole.PASSIVE, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
        );
        Optional<Event> eventOpt = eventService.findByIdInternal(eventA.getId());
        assert eventOpt.isEmpty(): "Delete did not remove when user is admin of the service and a passive participant of that event";
    }

    @Test
    public void testThatDeleteNonExistingReturnsStatusNotFound() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }
    @Test
    public void testThatDeleteAlreadyDeletedReturnsStatusNotFound() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token));

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/events/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }
    @Test
    public void testThatGetAlreadyDeletedReturnsStatusNotFound() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
        );

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }
    @Test
    public void testThatListAllNotDoesNotIncludeDeletedEvent() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        Event eventB = TestDataUtil.getEventB();
        eventB.setId(null);
        eventService.create(eventB, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
        );

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].id").value(2)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].title").value(eventB.getTitle())
        );
    }
    @Test
    public void testThatFullUpdateAlreadyDeletedReturnsStatusNotFound() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
        );

        eventA.setTitle("Changed");
        String eventAJson = objectMapper.writeValueAsString(eventA);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }

    //EVENT PARTICIPANT-------------------------------------------------------------------------------------------------
    //PUT---------------------------------------------------------------------------------------------------------------
    @Test
    public void testThatSuccessfullSetParticipantRoleReturns200() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        User userB = TestDataUtil.getRegisteredUserB(userService);
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("PASSIVE");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatSuccessfullSetParticipantRoleReturnsParticipantDto() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        User userB = TestDataUtil.getRegisteredUserB(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("PASSIVE");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventId").value(eventA.getId())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventRole").value(eventRoleRequest.getRole())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.id").value(2)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.username").value(userB.getUsername())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.firstName").value(userB.getFirstName())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.lastName").value(userB.getLastName())
        );
    }

    @Test
    public void testThatSetParticipantRoleSavedToDataBase() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        User userB = TestDataUtil.getRegisteredUserB(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("PASSIVE");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)
        );

        Optional<EventParticipant> eventParticipantOpt = eventService.getEventParticipantInternal(eventA.getId(), userB.getId());
        assert eventParticipantOpt.isPresent(): "Event Participant not saved to database";
        EventParticipant eventParticipant = eventParticipantOpt.get();
        assert eventParticipant.getEventRole() == EventRole.PASSIVE: "Event Participant saved with wrong role";
    }

    @Test
    public void testThatSuccessfullSetParticipantRoleReturns200MultipleParticipantsAdded() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        User userB = TestDataUtil.getRegisteredUserB(userService);
        User userC = TestDataUtil.getRegisteredUserC(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("PASSIVE");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatSuccessfullSetParticipantRoleReturnsParticipantDtoMultipleParticipantsAdded() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        User userB = TestDataUtil.getRegisteredUserB(userService);
        User userC = TestDataUtil.getRegisteredUserC(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("PASSIVE");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventId").value(eventA.getId())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventRole").value(eventRoleRequest.getRole())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.id").value(2)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.username").value(userB.getUsername())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.firstName").value(userB.getFirstName())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.lastName").value(userB.getLastName())
        );
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventId").value(eventA.getId())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventRole").value(eventRoleRequest.getRole())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.id").value(3)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.username").value(userC.getUsername())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.firstName").value(userC.getFirstName())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.lastName").value(userC.getLastName())
        );
    }

    @Test
    public void testThatSetParticipantRoleSavedToDataBaseMultipleParticipantsAdded() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        User userB = TestDataUtil.getRegisteredUserB(userService);
        User userC = TestDataUtil.getRegisteredUserC(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("PASSIVE");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)
        );
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)
        );

        Optional<EventParticipant> eventParticipantOpt = eventService.getEventParticipantInternal(eventA.getId(), userB.getId());
        assert eventParticipantOpt.isPresent(): "Event Participant not saved to database";
        EventParticipant eventParticipant = eventParticipantOpt.get();
        assert eventParticipant.getEventRole() == EventRole.PASSIVE: "Event Participant saved with wrong role";
        Optional<EventParticipant> eventParticipantOpt2 = eventService.getEventParticipantInternal(eventA.getId(), userC.getId());
        assert eventParticipantOpt2.isPresent(): "Event Participant not saved to database";
        EventParticipant eventParticipant2 = eventParticipantOpt2.get();
        assert eventParticipant2.getEventRole() == EventRole.PASSIVE: "Event Participant saved with wrong role";
    }

    @Test
    public void testThatSuccessfullSetParticipantRoleReturns200ActiveRole() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        User userB = TestDataUtil.getRegisteredUserB(userService);
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("ACTIVE");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatSuccessfullSetParticipantRoleReturnsParticipantDtoActiveRole() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        User userB = TestDataUtil.getRegisteredUserB(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("ACTIVE");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventId").value(eventA.getId())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventRole").value(eventRoleRequest.getRole())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.id").value(2)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.username").value(userB.getUsername())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.firstName").value(userB.getFirstName())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.lastName").value(userB.getLastName())
        );
    }

    @Test
    public void testThatSetParticipantRoleSavedToDataBaseActiveRole() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        User userB = TestDataUtil.getRegisteredUserB(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("ACTIVE");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)
        );

        Optional<EventParticipant> eventParticipantOpt = eventService.getEventParticipantInternal(eventA.getId(), userB.getId());
        assert eventParticipantOpt.isPresent(): "Event Participant not saved to database";
        EventParticipant eventParticipant = eventParticipantOpt.get();
        assert eventParticipant.getEventRole() == EventRole.ACTIVE: "Event Participant saved with wrong role";
    }

    @Test
    public void testThatSuccessfullSetParticipantRoleReturns200AdminRole() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        Event eventA = TestDataUtil.getEventA();
        User userB = TestDataUtil.getRegisteredUserB(userService);
        eventA.setId(null);
        eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("ADMIN");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatSuccessfullSetParticipantRoleReturnsParticipantDtoAdminRole() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        User userB = TestDataUtil.getRegisteredUserB(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("ADMIN");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)

        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventId").value(eventA.getId())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.eventRole").value(eventRoleRequest.getRole())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.id").value(2)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.username").value(userB.getUsername())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.firstName").value(userB.getFirstName())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.user.lastName").value(userB.getLastName())
        );
    }

    @Test
    public void testThatSetParticipantRoleSavedToDataBaseAdminRole() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        User userB = TestDataUtil.getRegisteredUserB(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("ADMIN");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)
        );

        Optional<EventParticipant> eventParticipantOpt = eventService.getEventParticipantInternal(eventA.getId(), userB.getId());
        assert eventParticipantOpt.isPresent(): "Event Participant not saved to database";
        EventParticipant eventParticipant = eventParticipantOpt.get();
        assert eventParticipant.getEventRole() == EventRole.ADMIN: "Event Participant saved with wrong role";
    }

    @Test
    public void testThatSetParticipantRoleCanChangeParticipantsRole() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        User userB = TestDataUtil.getRegisteredUserB(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("PASSIVE");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)
        );
        eventRoleRequest = new EventRoleRequest("ACTIVE");
        roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)
        );

        Optional<EventParticipant> eventParticipantOpt = eventService.getEventParticipantInternal(eventA.getId(), userB.getId());
        assert eventParticipantOpt.isPresent(): "Event Participant not saved to database";
        EventParticipant eventParticipant = eventParticipantOpt.get();
        assert eventParticipant.getEventRole() == EventRole.ACTIVE: "Event Participant saved with wrong role";
    }

    @Test
    public void testThatNotParticipantCannotAddAnotherParticipant() throws Exception{
        User userA = TestDataUtil.getRegisteredUserA(userService);
        String token = jwtUtil.generateToken(userA.getUsername());
        User userB = TestDataUtil.getRegisteredUserB(userService);
        Event eventA = TestDataUtil.getEventA();
        eventA.setId(null);
        eventA = eventService.create(eventA, userA.getUsername());
        EventRoleRequest eventRoleRequest = new EventRoleRequest("PASSIVE");
        String roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)
        );
        eventRoleRequest = new EventRoleRequest("ACTIVE");
        roleRequestJson = objectMapper.writeValueAsString(eventRoleRequest);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/1/participants/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleRequestJson)
                        .header("Authorization", "Bearer "+token)
        );

        Optional<EventParticipant> eventParticipantOpt = eventService.getEventParticipantInternal(eventA.getId(), userB.getId());
        assert eventParticipantOpt.isPresent(): "Event Participant not saved to database";
        EventParticipant eventParticipant = eventParticipantOpt.get();
        assert eventParticipant.getEventRole() == EventRole.ACTIVE: "Event Participant saved with wrong role";
    }

    @Test
    public void testThatPassiveParticipantCannotAddAnotherParticipant() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User passiveUser = TestDataUtil.getRegisteredUserB(userService);
        Event event = eventService.create(TestDataUtil.getEventPrivate1(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), passiveUser.getId(), EventRole.PASSIVE, creator.getUsername());
        String token = jwtUtil.generateToken(passiveUser.getUsername());

        User targetUser = TestDataUtil.getRegisteredUserC(userService);
        EventRoleRequest roleRequest = new EventRoleRequest("ACTIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());

        assertFalse(eventService.isUserAParticipantOf(event.getId(), targetUser.getId()));
    }

    @Test
    public void testThatAdminParticipantCanAddAnotherParticipantForPrivate() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        Event event = eventService.create(TestDataUtil.getEventPrivate1(), creator.getUsername());
        String token = jwtUtil.generateToken(creator.getUsername());

        User targetUser = TestDataUtil.getRegisteredUserB(userService);
        EventRoleRequest roleRequest = new EventRoleRequest("PASSIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        assertTrue(eventService.isUserAParticipantOf(event.getId(), targetUser.getId()));
    }

    @Test
    public void testThatAdminParticipantCanAddAnotherParticipantForPublicClosed() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        Event event = eventService.create(TestDataUtil.getEventPublicClosed(), creator.getUsername());
        String token = jwtUtil.generateToken(creator.getUsername());

        User targetUser = TestDataUtil.getRegisteredUserB(userService);
        EventRoleRequest roleRequest = new EventRoleRequest("ACTIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        assertTrue(eventService.isUserAParticipantOf(event.getId(), targetUser.getId()));
    }

    @Test
    public void testThatActiveParticipantCannotAddAnotherParticipant() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User activeUser = TestDataUtil.getRegisteredUserB(userService);
        Event event = eventService.create(TestDataUtil.getEventPrivate1(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), activeUser.getId(), EventRole.ACTIVE, creator.getUsername());
        String token = jwtUtil.generateToken(activeUser.getUsername());

        User targetUser = TestDataUtil.getRegisteredUserC(userService);
        EventRoleRequest roleRequest = new EventRoleRequest("PASSIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());

        assertFalse(eventService.isUserAParticipantOf(event.getId(), targetUser.getId()));
    }

    @Test
    public void testThatUserCannotAddItselfForPrivate() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        Event event = eventService.create(TestDataUtil.getEventPrivate1(), creator.getUsername());
        User user = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(user.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("ACTIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());

        assertFalse(eventService.isUserAParticipantOf(event.getId(), user.getId()));
    }

    @Test
    public void testThatUserCannotAddItselfForPublicClosed() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        Event event = eventService.create(TestDataUtil.getEventPublicClosed(), creator.getUsername());
        User user = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(user.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("PASSIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());

        assertFalse(eventService.isUserAParticipantOf(event.getId(), user.getId()));
    }

    @Test
    public void testThatUserCanAddItselfAsPassiveForPublicOpen() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        User user = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(user.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("PASSIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        assertTrue(eventService.isUserAParticipantOf(event.getId(), user.getId()));
    }

    @Test
    public void testThatUserCannotAddItselfAsActiveForPublicOpen() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        User user = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(user.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("ACTIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());

        assertFalse(eventService.isUserAParticipantOf(event.getId(), user.getId()));
    }

    @Test
    public void testThatUserCannotAddItselfAsAdminForPublicOpen() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        User user = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(user.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("ADMIN");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());

        assertFalse(eventService.isUserAParticipantOf(event.getId(), user.getId()));
    }

    @Test
    public void testThatUserCannotChangeItsRoleToAdminFromPassiveForPublicOpen() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        User user = TestDataUtil.getRegisteredUserB(userService);
        String tokenUser = jwtUtil.generateToken(user.getUsername());
        String tokenCreator = jwtUtil.generateToken(creator.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("PASSIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + tokenCreator)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), user.getId());
        assertTrue(epOpt.isPresent());
        EventParticipant ep = epOpt.get();
        assertSame(EventRole.PASSIVE, ep.getEventRole());


        roleRequest = new EventRoleRequest("ADMIN");
        roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + tokenUser)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());
        epOpt = eventService.getEventParticipantInternal(event.getId(), user.getId());
        assertTrue(epOpt.isPresent());
        ep = epOpt.get();
        assertSame(EventRole.PASSIVE, ep.getEventRole());
    }

    @Test
    public void testThatUserCannotChangeItsRoleToActiveFromPassiveForPublicOpen() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        User user = TestDataUtil.getRegisteredUserB(userService);
        String token = jwtUtil.generateToken(user.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("ADMIN");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());

        assertFalse(eventService.isUserAParticipantOf(event.getId(), user.getId()));
    }

    @Test
    public void testAdminCanDowngradeToActive() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User user = TestDataUtil.getRegisteredUserB(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), user.getId(), EventRole.ADMIN, creator.getUsername());
        String token = jwtUtil.generateToken(user.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("ACTIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), user.getId());
        assertTrue(epOpt.isPresent());
        assertSame(EventRole.ACTIVE, epOpt.get().getEventRole());
    }

    @Test
    public void testAdminCanDowngradeToPassive() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User user = TestDataUtil.getRegisteredUserB(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), user.getId(), EventRole.ADMIN, creator.getUsername());
        String token = jwtUtil.generateToken(user.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("PASSIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), user.getId());
        assertTrue(epOpt.isPresent());
        assertSame(EventRole.PASSIVE, epOpt.get().getEventRole());
    }

    @Test
    public void testActiveCanDowngradeToPassive() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User user = TestDataUtil.getRegisteredUserB(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), user.getId(), EventRole.ACTIVE, creator.getUsername());
        String token = jwtUtil.generateToken(user.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("PASSIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), user.getId());
        assertTrue(epOpt.isPresent());
        assertSame(EventRole.PASSIVE, epOpt.get().getEventRole());
    }

    @Test
    public void testPassiveCannotUpgradeToActive() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User user = TestDataUtil.getRegisteredUserB(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), user.getId(), EventRole.PASSIVE, creator.getUsername());
        String token = jwtUtil.generateToken(user.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("ACTIVE");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());

        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), user.getId());
        assertTrue(epOpt.isPresent());
        assertSame(EventRole.PASSIVE, epOpt.get().getEventRole());
    }

    @Test
    public void testPassiveCannotUpgradeToAdmin() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User user = TestDataUtil.getRegisteredUserB(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), user.getId(), EventRole.PASSIVE, creator.getUsername());
        String token = jwtUtil.generateToken(user.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("ADMIN");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());

        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), user.getId());
        assertTrue(epOpt.isPresent());
        assertSame(EventRole.PASSIVE, epOpt.get().getEventRole());
    }

    @Test
    public void testActiveCannotUpgradeToAdmin() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User user = TestDataUtil.getRegisteredUserB(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), user.getId(), EventRole.ACTIVE, creator.getUsername());
        String token = jwtUtil.generateToken(user.getUsername());

        EventRoleRequest roleRequest = new EventRoleRequest("ADMIN");
        String roleJson = objectMapper.writeValueAsString(roleRequest);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson)
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());

        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), user.getId());
        assertTrue(epOpt.isPresent());
        assertSame(EventRole.ACTIVE, epOpt.get().getEventRole());
    }


    @Test
    public void testViewParticipantsOfPrivateEvent() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User participant = TestDataUtil.getRegisteredUserB(userService);
        User outsider = TestDataUtil.getRegisteredUserC(userService);
        User globalAdmin = TestDataUtil.getRegisteredUserAdmin1(userService);

        Event event = eventService.create(TestDataUtil.getEventPrivate1(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), participant.getId(), EventRole.PASSIVE, creator.getUsername());

        String creatorToken = jwtUtil.generateToken(creator.getUsername());
        String participantToken = jwtUtil.generateToken(participant.getUsername());
        String outsiderToken = jwtUtil.generateToken(outsider.getUsername());
        String adminToken = jwtUtil.generateToken(globalAdmin.getUsername());

        // Creator should be able to view
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/" + event.getId() + "/participants")
                        .header("Authorization", "Bearer " + creatorToken)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        // Participant should be able to view
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/" + event.getId() + "/participants")
                        .header("Authorization", "Bearer " + participantToken)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        // Global admin should be able to view
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/" + event.getId() + "/participants")
                        .header("Authorization", "Bearer " + adminToken)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        // Outsider (not a participant) should be forbidden
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/" + event.getId() + "/participants")
                        .header("Authorization", "Bearer " + outsiderToken)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());

        // Unauthenticated user should be unauthorized
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/" + event.getId() + "/participants")
        ).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void testViewParticipantsOfPublicClosedEvent() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User participant = TestDataUtil.getRegisteredUserB(userService);
        User outsider = TestDataUtil.getRegisteredUserC(userService);
        User globalAdmin = TestDataUtil.getRegisteredUserAdmin1(userService);

        Event event = eventService.create(TestDataUtil.getEventPublicClosed(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), participant.getId(), EventRole.ACTIVE, creator.getUsername());

        String creatorToken = jwtUtil.generateToken(creator.getUsername());
        String participantToken = jwtUtil.generateToken(participant.getUsername());
        String outsiderToken = jwtUtil.generateToken(outsider.getUsername());
        String adminToken = jwtUtil.generateToken(globalAdmin.getUsername());

        // Creator should be able to view
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/" + event.getId() + "/participants")
                        .header("Authorization", "Bearer " + creatorToken)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        // Participant should be able to view
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/" + event.getId() + "/participants")
                        .header("Authorization", "Bearer " + participantToken)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        // Global admin should be able to view
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/" + event.getId() + "/participants")
                        .header("Authorization", "Bearer " + adminToken)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        // Outsider (not a participant) should also be able to view
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/" + event.getId() + "/participants")
                        .header("Authorization", "Bearer " + outsiderToken)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    //DELETE -----------------------------------------------------------------------------------------------------------
    @Test
    public void testUserCanDeleteThemself() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User user = TestDataUtil.getRegisteredUserB(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), user.getId(), EventRole.ACTIVE, creator.getUsername());
        String token = jwtUtil.generateToken(user.getUsername());

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/" + event.getId() + "/participants/" + user.getId())
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), user.getId());
        assertFalse(epOpt.isPresent());
    }

    @Test
    public void testAdminCanDeleteOtherUser() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User admin = TestDataUtil.getRegisteredUserB(userService);
        User targetUser = TestDataUtil.getRegisteredUserC(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), admin.getId(), EventRole.ADMIN, creator.getUsername());
        eventService.addEventParticipant(event.getId(), targetUser.getId(), EventRole.ACTIVE, creator.getUsername());
        String token = jwtUtil.generateToken(admin.getUsername());

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/" + event.getId() + "/participants/" + targetUser.getId())
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), targetUser.getId());
        assertFalse(epOpt.isPresent());
    }

    @Test
    public void testAdminCanDeleteOtherAdmin() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User admin1 = TestDataUtil.getRegisteredUserB(userService);
        User admin2 = TestDataUtil.getRegisteredUserC(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), admin1.getId(), EventRole.ADMIN, creator.getUsername());
        eventService.addEventParticipant(event.getId(), admin2.getId(), EventRole.ADMIN, creator.getUsername());
        String token = jwtUtil.generateToken(admin1.getUsername());

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/" + event.getId() + "/participants/" + admin2.getId())
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), admin2.getId());
        assertFalse(epOpt.isPresent());
    }

    @Test
    public void testActiveUserCannotDeleteOtherUser() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User activeUser = TestDataUtil.getRegisteredUserB(userService);
        User targetUser = TestDataUtil.getRegisteredUserC(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), activeUser.getId(), EventRole.ACTIVE, creator.getUsername());
        eventService.addEventParticipant(event.getId(), targetUser.getId(), EventRole.PASSIVE, creator.getUsername());
        String token = jwtUtil.generateToken(activeUser.getUsername());

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/" + event.getId() + "/participants/" + targetUser.getId())
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());

        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), targetUser.getId());
        assertTrue(epOpt.isPresent());
    }

    @Test
    public void testPassiveUserCannotDeleteOtherUser() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User passiveUser = TestDataUtil.getRegisteredUserB(userService);
        User targetUser = TestDataUtil.getRegisteredUserC(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), passiveUser.getId(), EventRole.PASSIVE, creator.getUsername());
        eventService.addEventParticipant(event.getId(), targetUser.getId(), EventRole.ACTIVE, creator.getUsername());
        String token = jwtUtil.generateToken(passiveUser.getUsername());

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/" + event.getId() + "/participants/" + targetUser.getId())
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());

        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), targetUser.getId());
        assertTrue(epOpt.isPresent());
    }

    @Test
    public void testNotParticipantUserCannotDeleteOtherUser() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User stranger = TestDataUtil.getRegisteredUserB(userService);
        User targetUser = TestDataUtil.getRegisteredUserC(userService);
        Event event = eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.addEventParticipant(event.getId(), targetUser.getId(), EventRole.ACTIVE, creator.getUsername());
        String token = jwtUtil.generateToken(stranger.getUsername());

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/events/" + event.getId() + "/participants/" + targetUser.getId())
                        .header("Authorization", "Bearer " + token)
        ).andExpect(MockMvcResultMatchers.status().isForbidden());

        Optional<EventParticipant> epOpt = eventService.getEventParticipantInternal(event.getId(), targetUser.getId());
        assertTrue(epOpt.isPresent());
    }
    @Test
    public void testUserSeesOnlyTheirOwnEvents() throws Exception {
        User user = TestDataUtil.getRegisteredUserA(userService);
        Event privateEvent = eventService.create(TestDataUtil.getEventPrivate1(), user.getUsername());
        Event publicClosedEvent = eventService.create(TestDataUtil.getEventPublicClosed(), user.getUsername());
        Event publicOpenEvent = eventService.create(TestDataUtil.getEventA(), user.getUsername());

        User otherUser = TestDataUtil.getRegisteredUserB(userService);
        eventService.create(TestDataUtil.getEventPrivate1(), otherUser.getUsername());

        String token = jwtUtil.generateToken(user.getUsername());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/events/events-of-user/" + user.getId())
                                .header("Authorization", "Bearer " + token)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains(privateEvent.getTitle()));
        assertTrue(response.contains(publicClosedEvent.getTitle()));
        assertTrue(response.contains(publicOpenEvent.getTitle()));
        assertFalse(response.contains("EventOfOtherUser"));
    }

    @Test
    public void testAdminSeesAllEventsOfAnyUser() throws Exception {
        User admin = TestDataUtil.getRegisteredUserAdmin1(userService);
        User user = TestDataUtil.getRegisteredUserA(userService);

        Event privateEvent = eventService.create(TestDataUtil.getEventPrivate1(), user.getUsername());
        Event publicClosedEvent = eventService.create(TestDataUtil.getEventPublicClosed(), user.getUsername());
        Event publicOpenEvent = eventService.create(TestDataUtil.getEventA(), user.getUsername());

        String token = jwtUtil.generateToken(admin.getUsername());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/events/events-of-user/" + user.getId())
                                .header("Authorization", "Bearer " + token)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains(privateEvent.getTitle()));
        assertTrue(response.contains(publicClosedEvent.getTitle()));
        assertTrue(response.contains(publicOpenEvent.getTitle()));
    }

    @Test
    public void testOtherUserSeesOnlyPublicParticipatedEvents() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User otherUser = TestDataUtil.getRegisteredUserB(userService);

        Event privateEvent = eventService.create(TestDataUtil.getEventPrivate1(), creator.getUsername());
        Event publicClosedEvent = eventService.create(TestDataUtil.getEventPublicClosed(), creator.getUsername());
        Event publicOpenEvent = eventService.create(TestDataUtil.getEventA(), creator.getUsername());

        eventService.addEventParticipant(publicClosedEvent.getId(), otherUser.getId(), EventRole.ACTIVE, creator.getUsername());
        eventService.addEventParticipant(publicOpenEvent.getId(), otherUser.getId(), EventRole.ACTIVE, creator.getUsername());

        String token = jwtUtil.generateToken(otherUser.getUsername());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/events/events-of-user/" + creator.getId())
                                .header("Authorization", "Bearer " + token)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains(publicClosedEvent.getTitle()));
        assertTrue(response.contains(publicOpenEvent.getTitle()));
        assertFalse(response.contains(privateEvent.getTitle()));
    }

    @Test
    public void testAnonymousUserCannotAccessEvents() throws Exception {
        User user = TestDataUtil.getRegisteredUserA(userService);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/events/events-of-user/" + user.getId())
        ).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void testUserSeesOnlyEventsTheyParticipateIn() throws Exception {
        User creator1 = TestDataUtil.getRegisteredUserA(userService);
        User creator2 = TestDataUtil.getRegisteredUserB(userService);
        User participantUser = TestDataUtil.getRegisteredUserC(userService);

        Event eventCreatedByCreator1 = eventService.create(TestDataUtil.getEventPublicClosed(), creator1.getUsername());
        Event eventCreatedByCreator2 = eventService.create(TestDataUtil.getEventA(), creator2.getUsername());
        Event eventNotParticipated = eventService.create(TestDataUtil.getEventB(), creator1.getUsername());

        // Participant joins two events
        eventService.addEventParticipant(eventCreatedByCreator1.getId(), participantUser.getId(), EventRole.PASSIVE, creator1.getUsername());
        eventService.addEventParticipant(eventCreatedByCreator2.getId(), participantUser.getId(), EventRole.PASSIVE, creator2.getUsername());

        String token = jwtUtil.generateToken(participantUser.getUsername());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/events/events-of-user/" + participantUser.getId())
                                .header("Authorization", "Bearer " + token)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains(eventCreatedByCreator1.getTitle()));
        assertTrue(response.contains(eventCreatedByCreator2.getTitle()));
        assertFalse(response.contains(eventNotParticipated.getTitle()));
    }

    @Test
    public void testUserWithNoParticipationGetsEmptyList() throws Exception {
        User user = TestDataUtil.getRegisteredUserA(userService);
        User creator = TestDataUtil.getRegisteredUserB(userService);

        // Creator makes events, but user does not join anything
        eventService.create(TestDataUtil.getEventPrivate1(), creator.getUsername());
        eventService.create(TestDataUtil.getEventA(), creator.getUsername());
        eventService.create(TestDataUtil.getEventPublicClosed(), creator.getUsername());

        String token = jwtUtil.generateToken(user.getUsername());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/events/events-of-user/" + user.getId())
                                .header("Authorization", "Bearer " + token)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertFalse(response.contains("title"));
    }

    @Test
    public void testOtherUserSeesEmptyListIfNoParticipation() throws Exception {
        User creator = TestDataUtil.getRegisteredUserA(userService);
        User participantUser = TestDataUtil.getRegisteredUserB(userService);
        User otherUser = TestDataUtil.getRegisteredUserC(userService);

        eventService.create(TestDataUtil.getEventPrivate1(), creator.getUsername());
        eventService.create(TestDataUtil.getEventPublicClosed(), creator.getUsername());
        eventService.create(TestDataUtil.getEventA(), creator.getUsername());

        String token = jwtUtil.generateToken(otherUser.getUsername());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/events/events-of-user/" + participantUser.getId())
                                .header("Authorization", "Bearer " + token)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertFalse(response.contains("title"));
    }

}


