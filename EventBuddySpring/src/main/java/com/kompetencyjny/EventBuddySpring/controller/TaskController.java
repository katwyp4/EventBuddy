package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.Task;
import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import com.kompetencyjny.EventBuddySpring.repo.TaskRepository;
import com.kompetencyjny.EventBuddySpring.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository,
                          EventRepository eventRepository,
                          UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    // [GET] /api/tasks
    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // [GET] /api/tasks/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return task.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // [POST] /api/tasks?eventId=... (tworzenie zadania i przypisanie do eventu)
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestParam("eventId") Long eventId,
                                           @RequestBody Task task,
                                           @RequestParam(name = "assignedUserId", required = false) Long assignedUserId) {

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Event event = eventOpt.get();
        task.setEvent(event);

        // Opcjonalnie przypisanie do konkretnego usera
        if (assignedUserId != null) {
            Optional<User> userOpt = userRepository.findById(assignedUserId);
            userOpt.ifPresent(task::setAssignedUser);
        }

        Task savedTask = taskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    // [PUT] /api/tasks/{id}
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setName(updatedTask.getName());
            task.setStatus(updatedTask.getStatus());
            // ewentualnie zmiana assignedUser, event etc.
            return ResponseEntity.ok(taskRepository.save(task));
        }
        return ResponseEntity.notFound().build();
    }

    // [DELETE] /api/tasks/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
