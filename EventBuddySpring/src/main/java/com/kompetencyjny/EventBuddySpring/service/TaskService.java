package com.kompetencyjny.EventBuddySpring.service;

import com.kompetencyjny.EventBuddySpring.exception.ForbiddenException;
import com.kompetencyjny.EventBuddySpring.exception.NotFoundException;
import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.EventRole;
import com.kompetencyjny.EventBuddySpring.model.Task;
import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.repo.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final EventService eventService;
    private final UserService userService;

    @Transactional
    public Page<Task> findAll(Pageable pageable, String loggedEmail) {
        User loggedUser = userService.findByEmail(loggedEmail)
                .orElseThrow(() -> new NotFoundException("Logged in user not found!"));

        return taskRepository.findByAssignedUser(loggedUser, pageable);
    }

    @Transactional
    public Optional<Task> findById(Long id, String loggedEmail) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) return taskOpt;
        Task task = taskOpt.get();
        if (! isTaskVisible(task, loggedEmail)) return Optional.empty();

        return taskOpt;
    }

    public boolean isTaskVisible(Task task, String loggedEmail){
        return eventService.isUserPermitted(task.getEvent().getId(), loggedEmail, EventRole.ACTIVE);
    }

    @Transactional
    public Task create(Long eventId, Task task, String loggedEmail) {
        if (!eventService.isUserPermitted(eventId, loggedEmail, EventRole.ACTIVE)) throw new ForbiddenException("User (of email: "+loggedEmail+") not allowed to create task to event of id: "+eventId);
        Event event = eventService.findVisibleById(eventId, loggedEmail)
                .orElseThrow(() -> new NotFoundException("Event of id: "+eventId+" not found"));
        task.setEvent(event);
        return taskRepository.save(task);
    }

    @Transactional
    public Task assignUser(Long id, Long assignedUserId, String loggedEmail) {
        Task task = findById(id, loggedEmail)
                .orElseThrow(() -> new NotFoundException("Task with id: "+id+" not found!"));

        if (!eventService.isUserPermitted(task.getEvent().getId(), loggedEmail, EventRole.ACTIVE)) throw new ForbiddenException("User (of email: "+loggedEmail+") not allowed to assign tasks to event of id: "+task.getEvent().getId());
        User assignedUser = userService.findById(assignedUserId)
                        .orElseThrow(() -> new NotFoundException("User of id: "+assignedUserId+" not found!"));
        task.setAssignedUser(assignedUser);

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Long id, Task updatedTask, String loggedEmail){
        Task task = findById(id, loggedEmail)
                .orElseThrow(() -> new NotFoundException("Task with id: "+id+" not found!"));

        if (!eventService.isUserPermitted(task.getEvent().getId(), loggedEmail, EventRole.ACTIVE)) throw new ForbiddenException("User (of email: "+loggedEmail+") not allowed to update tasks of event with id: "+task.getEvent().getId());

        if (updatedTask.getName() != null) task.setName(updatedTask.getName());
        if (updatedTask.getStatus() != null) task.setStatus(updatedTask.getStatus());

        return taskRepository.save(task);
    }

    @Transactional
    public void removeTask(Long id, String loggedEmail){
        Optional<Task> taskOpt = findById(id, loggedEmail);
        if (taskOpt.isEmpty()) return;
        Task task = taskOpt.get();

        if (!eventService.isUserPermitted(task.getEvent().getId(), loggedEmail, EventRole.ACTIVE)) throw new ForbiddenException("User (of email: "+loggedEmail+") not allowed to remove tasks from event of id: "+task.getEvent().getId());

        taskRepository.delete(task);
    }

    @Transactional
    public Page<Task> findOfEvent(Long eventId, Pageable pageable, String loggedEmail) {
        Event event = eventService.findVisibleById(eventId, loggedEmail)
                .orElseThrow(()-> new NotFoundException("Event with id: "+eventId+" not found!"));
        if (!eventService.isUserPermitted(eventId, loggedEmail, EventRole.ACTIVE)) throw new ForbiddenException("User (of email: "+loggedEmail+") not allowed to display tasks of event with id: "+eventId);

        return taskRepository.findByEvent(event, pageable);
    }
}
