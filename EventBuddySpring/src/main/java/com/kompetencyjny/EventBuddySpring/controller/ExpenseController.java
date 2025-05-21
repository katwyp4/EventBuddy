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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDto>> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAll();
        List<ExpenseResponseDto> dtos = expenses.stream()
                .map(expenseMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }


    @PostMapping
    public ResponseEntity<ExpenseResponseDto> createExpense(@RequestBody ExpenseRequestDto dto) {
        Optional<Event> eventOpt = eventRepository.findById(dto.getEventId());
        Optional<User> userOpt = userRepository.findById(dto.getPayerId());

        if (eventOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Expense expense = new Expense();
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setPayer(userOpt.get());
        expense.setEvent(eventOpt.get());

        Expense saved = expenseRepository.save(expense);
        return ResponseEntity.ok(expenseMapper.toDto(saved));
    }

    @GetMapping("/{eventId}/balances")
    public ResponseEntity<Map<String, BigDecimal>> getBalances(@PathVariable Long eventId) {
        Map<String, BigDecimal> balances = eventService.calculateBalances(eventId);
        return ResponseEntity.ok(balances);
    }
}

