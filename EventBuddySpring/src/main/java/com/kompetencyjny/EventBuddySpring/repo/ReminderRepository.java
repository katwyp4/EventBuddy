package com.kompetencyjny.EventBuddySpring.repo;

import com.kompetencyjny.EventBuddySpring.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    @Query("""
    SELECT r FROM Reminder r
    WHERE r.sent = false
      AND r.event.date IS NOT NULL
      AND r.event.date BETWEEN :min AND :max
      AND r.event.active = true
""")
    List<Reminder> findCandidates(@Param("min") LocalDate min, @Param("max") LocalDate max);
}
