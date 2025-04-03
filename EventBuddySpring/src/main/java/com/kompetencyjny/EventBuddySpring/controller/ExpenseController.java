package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.Expense;
import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import com.kompetencyjny.EventBuddySpring.repo.ExpenseRepository;
import com.kompetencyjny.EventBuddySpring.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ExpenseController(ExpenseRepository expenseRepository,
                             EventRepository eventRepository,
                             UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    // [GET] /api/expenses
    @GetMapping
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    // [POST] /api/expenses?eventId=...&payerId=...&amount=...&description=...
    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestParam Long eventId,
                                                 @RequestParam Long payerId,
                                                 @RequestParam BigDecimal amount,
                                                 @RequestParam String description) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Optional<User> userOpt = userRepository.findById(payerId);

        if (eventOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Tworzymy obiekt przez konstruktor bezargumentowy
        Expense expense = new Expense();

        // Ustawiamy pola przez settery
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setPayer(userOpt.get());
        expense.setEvent(eventOpt.get());

        // Zapisujemy w repozytorium
        Expense saved = expenseRepository.save(expense);
        return ResponseEntity.ok(saved);
    }
}
