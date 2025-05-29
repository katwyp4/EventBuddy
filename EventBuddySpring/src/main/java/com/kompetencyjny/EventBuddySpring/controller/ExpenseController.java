package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.dto.ExpenseRequestDto;
import com.kompetencyjny.EventBuddySpring.dto.ExpenseResponseDto;
import com.kompetencyjny.EventBuddySpring.mappers.ExpenseMapper;
import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.Expense;
import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import com.kompetencyjny.EventBuddySpring.repo.ExpenseRepository;
import com.kompetencyjny.EventBuddySpring.repo.UserRepository;
import com.kompetencyjny.EventBuddySpring.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private final EventService eventService;
    private final ExpenseMapper expenseMapper;


    public ExpenseController(ExpenseRepository expenseRepository,
                             EventRepository eventRepository,
                             UserRepository userRepository,
                             ExpenseMapper expenseMapper,
                             EventService eventService) {
        this.expenseRepository = expenseRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.expenseMapper = expenseMapper;
        this.eventService = eventService;
    }

    @GetMapping("/event/{eventId}")
    public List<ExpenseResponseDto> getExpensesForEvent(@PathVariable Long eventId) {
        return expenseRepository.findByEventId(eventId).stream()
                .map(expenseMapper::toDto)
                .toList();
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDto>> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAll();
        List<ExpenseResponseDto> dtos = expenses.stream()
                .map(expenseMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }


    @PostMapping
    public ResponseEntity<ExpenseResponseDto> createExpense(
            @RequestBody ExpenseRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Szukamy wydarzenia
        Optional<Event> eventOpt = eventRepository.findById(dto.getEventId());
        if (eventOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Event event = eventOpt.get();

        // Sprawdzenie deadline
        if (event.getBudgetDeadline() != null && LocalDate.now().isAfter(event.getBudgetDeadline())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null); // lub możesz zwrócić JSON z informacją o błędzie
        }

        // Szukamy użytkownika po e-mailu z JWT
        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userOpt.get();

        // Sprawdź, czy użytkownik jest uczestnikiem wydarzenia
        boolean isParticipant = event.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId()));

        if (!isParticipant) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Tworzenie wydatku
        Expense expense = new Expense();
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setPayer(user);
        expense.setEvent(event);

        Expense saved = expenseRepository.save(expense);
        return ResponseEntity.ok(expenseMapper.toDto(saved));
    }



    @GetMapping("/{eventId}/balances")
    public ResponseEntity<Map<String, BigDecimal>> getBalances(@PathVariable Long eventId) {
        Map<String, BigDecimal> balances = eventService.calculateBalances(eventId);
        return ResponseEntity.ok(balances);
    }
}

