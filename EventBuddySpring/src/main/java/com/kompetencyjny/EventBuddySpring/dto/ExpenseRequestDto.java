package com.kompetencyjny.EventBuddySpring.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseRequestDto {
    private Long eventId;
    private Long payerId;
    private BigDecimal amount;
    private String description;
}

