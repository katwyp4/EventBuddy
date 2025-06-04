package com.kompetencyjny.EventBuddySpring.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kompetencyjny.EventBuddySpring.TestDataUtil;
import com.kompetencyjny.EventBuddySpring.dto.TaskRequest;
import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.Task;
import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import com.kompetencyjny.EventBuddySpring.repo.TaskRepository;
import com.kompetencyjny.EventBuddySpring.repo.UserRepository;
import com.kompetencyjny.EventBuddySpring.security.JwtUtil;
import com.kompetencyjny.EventBuddySpring.service.EventService;
import com.kompetencyjny.EventBuddySpring.service.TaskService;
import com.kompetencyjny.EventBuddySpring.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private User userA;
    private User userB;
    private Event eventA;
    private Task taskA;

    @BeforeEach
    void setUp() {
        // Clear previous data
        eventRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();
        userA = TestDataUtil.getRegisteredUserA(userService);
        userB = TestDataUtil.getRegisteredUserB(userService);
        eventA = TestDataUtil.getEventA();
        eventA = eventService.create(eventA, userA.getEmail());
        taskA = TestDataUtil.getTaskA(taskService, eventA.getId(), userA.getEmail());
        taskA = taskService.assignUser(taskA.getId(), userA.getId(), userA.getEmail());
    }

    @Test
    @DisplayName("Should get tasks for event")
    void shouldGetTasksForEvent() throws Exception {
        String tokenA = jwtUtil.generateToken(userA.getEmail());
        mockMvc.perform(get("/api/tasks")
                        .param("eventId", eventA.getId().toString())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].eventId", is(eventA.getId().intValue())));
    }

    @Test
    @DisplayName("Should only return tasks assigned to the logged-in user")
    void shouldReturnOnlyAssignedTasks() throws Exception {
        // Create a second task and assign it to userB
        Task taskB = TestDataUtil.getTaskB(taskService, eventA.getId(), userA.getEmail());
        String tokenA = jwtUtil.generateToken(userA.getEmail());
        String tokenB = jwtUtil.generateToken(userB.getEmail());
        taskService.assignUser(taskB.getId(), userB.getId(), userA.getEmail());

        // userA should only see taskA (assigned to userA)
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(taskA.getId().intValue())))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(taskB.getId().intValue()))));

        // userB should only see taskB (assigned to userB)
        mockMvc.perform(get("/api/tasks")
                    .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(taskB.getId().intValue())))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(taskA.getId().intValue()))));
    }

    @Test
    @DisplayName("Should return empty list if user has no assigned tasks")
    void shouldReturnEmptyIfNoAssignedTasks() throws Exception {
        // Create a new userC with no assigned tasks
        User userC = TestDataUtil.getRegisteredUserC(userService);
        String token = jwtUtil.generateToken(userC.getEmail());

        mockMvc.perform(get("/api/tasks")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
    @Test
    @DisplayName("Should get task by id")
    void shouldGetTaskById() throws Exception {
        String tokenA = jwtUtil.generateToken(userA.getEmail());
        mockMvc.perform(get("/api/tasks/{id}", taskA.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(taskA.getId().intValue())))
                .andExpect(jsonPath("$.name", is(taskA.getName())));
    }

    @Test
    @DisplayName("Should return 404 for non-existing task")
    void shouldReturnNotFoundForNonExistingTask() throws Exception {
        String tokenA = jwtUtil.generateToken(userA.getEmail());
        mockMvc.perform(get("/api/tasks/{id}", 99999L)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create task")
    void shouldCreateTask() throws Exception {
        String tokenA = jwtUtil.generateToken(userA.getEmail());
        TaskRequest request = new TaskRequest("New Task", "TODO");
        mockMvc.perform(post("/api/tasks")
                        .param("eventId", eventA.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Task")))
                .andExpect(jsonPath("$.status", is("TODO")));
    }

    @Test
    @DisplayName("Should not create task for forbidden event")
    void shouldNotCreateTaskForForbiddenEvent() throws Exception {
        String tokenB = jwtUtil.generateToken(userB.getEmail());
        TaskRequest request = new TaskRequest("Forbidden Task", "TODO");
        mockMvc.perform(post("/api/tasks")
                        .param("eventId", eventA.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should update task")
    void shouldUpdateTask() throws Exception {
        String tokenA = jwtUtil.generateToken(userA.getEmail());
        TaskRequest update = new TaskRequest("Updated Task", "DONE");
        mockMvc.perform(put("/api/tasks/{id}", taskA.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update))
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Task")))
                .andExpect(jsonPath("$.status", is("DONE")));
    }

    @Test
    @DisplayName("Should not update task for forbidden user")
    void shouldNotUpdateTaskForForbiddenUser() throws Exception {
        String tokenB = jwtUtil.generateToken(userB.getEmail());
        TaskRequest update = new TaskRequest("Updated Task", "DONE");
        mockMvc.perform(put("/api/tasks/{id}", taskA.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update))
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should assign user to task")
    void shouldAssignUserToTask() throws Exception {
        String tokenA = jwtUtil.generateToken(userA.getEmail());
        mockMvc.perform(put("/api/tasks/{id}/assign", taskA.getId())
                        .param("assignedUserId", userB.getId().toString())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUserId", is(userB.getId().intValue())));
    }

    @Test
    @DisplayName("Should not assign user to task for forbidden user")
    void shouldNotAssignUserToTaskForForbiddenUser() throws Exception {
        String tokenB = jwtUtil.generateToken(userB.getEmail());
        mockMvc.perform(put("/api/tasks/{id}/assign", taskA.getId())
                        .param("assignedUserId", userA.getId().toString())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should delete task")
    void shouldDeleteTask() throws Exception {
        String tokenA = jwtUtil.generateToken(userA.getEmail());
        Task task = TestDataUtil.getTaskB(taskService, eventA.getId(), userA.getEmail());
        mockMvc.perform(delete("/api/tasks/{id}", task.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should not delete task for forbidden user")
    void shouldNotDeleteTaskForForbiddenUser() throws Exception {
        String tokenB = jwtUtil.generateToken(userB.getEmail());
        mockMvc.perform(delete("/api/tasks/{id}", taskA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 for invalid status")
    void shouldReturnBadRequestForInvalidStatus() throws Exception {
        String tokenA = jwtUtil.generateToken(userA.getEmail());
        TaskRequest invalid = new TaskRequest("Invalid", "NOT_A_STATUS");
        mockMvc.perform(post("/api/tasks")
                        .param("eventId", eventA.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid))
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 for unauthenticated access")
    void shouldReturnUnauthorizedForUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }
}