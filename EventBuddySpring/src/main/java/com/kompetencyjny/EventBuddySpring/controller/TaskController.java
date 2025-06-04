package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.dto.TaskDto;
import com.kompetencyjny.EventBuddySpring.dto.TaskRequest;
import com.kompetencyjny.EventBuddySpring.mappers.TaskMapper;
import com.kompetencyjny.EventBuddySpring.model.Task;
import com.kompetencyjny.EventBuddySpring.service.EventService;
import com.kompetencyjny.EventBuddySpring.service.TaskService;
import com.kompetencyjny.EventBuddySpring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final EventService eventService;
    private final UserService userService;

    // [GET] /api/tasks
    @GetMapping(params = "!eventId")
    public ResponseEntity<Page<TaskDto>> getAllTasks(Pageable pageable, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.findAll(pageable, userDetails.getUsername()).map(taskMapper::toDto));
    }

    // [GET] /api/tasks?eventId=...
    @GetMapping(params = "eventId")
    public ResponseEntity<Page<TaskDto>> getEventsTasks(@RequestParam("eventId") Long eventId,
                                                        Pageable pageable,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.findOfEvent(eventId, pageable, userDetails.getUsername()).map(taskMapper::toDto));
    }

    // [GET] /api/tasks/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Task> task = taskService.findById(id, userDetails.getUsername());
        return task.map(task_ -> ResponseEntity.ok(taskMapper.toDto(task_)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // [POST] /api/tasks?eventId=... (tworzenie zadania i przypisanie do eventu)
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestParam("eventId") Long eventId,
                                           @RequestBody TaskRequest taskRequest,
                                           @RequestParam(name = "assignedUserId", required = false) Long assignedUserId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        Task task = taskService.create(eventId, taskMapper.toEntity(taskRequest), userDetails.getUsername());
        if (assignedUserId != null){
            task = taskService.assignUser(task.getId(), assignedUserId, userDetails.getUsername());
        }
        return ResponseEntity.ok(taskMapper.toDto(task));
    }

    // [PUT] /api/tasks/{id}
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id,
                                              @RequestBody TaskRequest taskRequest,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskMapper.toDto(
                taskService.updateTask(id, taskMapper.toEntity(taskRequest), userDetails.getUsername())));
    }

    // [PUT] /api/tasks/{id}/assign?assignUserId=...
    @PutMapping("/{id}/assign")
    public ResponseEntity<TaskDto> assignTask(@PathVariable Long id,
                                              @RequestParam(name = "assignedUserId", required = false) Long assignedUserId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskMapper.toDto(
                taskService.assignUser(id, assignedUserId, userDetails.getUsername())));
    }

    // [DELETE] /api/tasks/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        taskService.removeTask(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
