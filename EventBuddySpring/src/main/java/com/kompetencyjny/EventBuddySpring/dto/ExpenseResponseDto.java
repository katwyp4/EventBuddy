package com.kompetencyjny.EventBuddySpring.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseResponseDto {
    private Long id;
    private String description;
    private BigDecimal amount;
    private Long payerId;
    private Long eventId;
    private String payerFullName;
}
