package com.example.myapplication.data;

public class ExpenseDto {
    private Long id;
    private String description;

    private String payerFullName;
    private double amount;
    private Long payerId;
    private Long eventId;

    public ExpenseDto() {}


    public Long getId()          { return id; }
    public void setId(Long id)   { this.id = id; }

    public String getDescription()              { return description; }
    public void setDescription(String desc)     { this.description = desc; }

    public double getAmount()     { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Long getPayerId()      { return payerId; }
    public void setPayerId(Long p){ this.payerId = p; }

    public Long getEventId()      { return eventId; }
    public void setEventId(Long e){ this.eventId = e; }

    public String getPayerFullName() { return payerFullName; }
    public void setPayerFullName(String n) { payerFullName = n; }
}
