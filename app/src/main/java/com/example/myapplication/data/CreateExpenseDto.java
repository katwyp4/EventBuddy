package com.example.myapplication.data;

/** Obiekt wysyłany w POST /api/expenses */
public class CreateExpenseDto {
    private String description;
    private double amount;
    private Long payerId;
    private Long eventId;

    public CreateExpenseDto() {}  // dla Gson

    public CreateExpenseDto(String description, double amount,
                            Long payerId, Long eventId) {
        this.description = description;
        this.amount     = amount;
        this.payerId    = payerId;
        this.eventId    = eventId;
    }

    /* gettery/settery – wygeneruj tak samo jak wyżej */
}
