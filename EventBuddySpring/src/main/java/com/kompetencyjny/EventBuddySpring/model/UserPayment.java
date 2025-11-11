package com.kompetencyjny.EventBuddySpring.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Entity
@Table(name = "user_payments")
public class UserPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;
    private String currency;
    private String stripePaymentIntentId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @ManyToOne
    private Event event;

    @ManyToOne
    private User payer;
}
