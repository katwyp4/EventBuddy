package com.kompetencyjny.EventBuddySpring.repo;

import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.Task;
import com.kompetencyjny.EventBuddySpring.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByAssignedUser(User assignedUser, Pageable pageable);

    Page<Task> findByEvent(Event event, Pageable pageable);
}
