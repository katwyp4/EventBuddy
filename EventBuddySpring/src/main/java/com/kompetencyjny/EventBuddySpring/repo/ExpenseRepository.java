package com.kompetencyjny.EventBuddySpring.repo;

import com.kompetencyjny.EventBuddySpring.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByEventId(Long eventId);

    List<Expense> findByPayer_Id(Long payerId);
}
