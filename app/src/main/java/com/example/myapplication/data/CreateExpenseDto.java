package com.example.myapplication.data;

/** Obiekt wysyłany w POST /api/expenses */
public class CreateExpenseDto {
    private String description;
    private double amount;
    private Long eventId;

    public CreateExpenseDto() {}  // dla Gson

    public CreateExpenseDto(String description, double amount,
                            Long eventId) {
        this.description = description;
        this.amount     = amount;
        this.eventId    = eventId;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
