package com.example.myapplication.data;

/** Dane pojedynczego wydatku otrzymywane z backendu. */
public class ExpenseDto {
    private Long id;
    private String description;  // opis wydatku

    private String payerFullName;
    private double amount;       // kwota
    private Long payerId;
    private Long eventId;

    /* Konstruktor bezargumentowy – wymagany przez Gson/Retrofit */
    public ExpenseDto() {}

    /*────────── GETTERY I SETTERY ─────────*/

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
