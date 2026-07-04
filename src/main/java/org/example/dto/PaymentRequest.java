package org.example.dto;

import java.math.BigDecimal;

public class PaymentRequest {
    private Long customerId;
    private BigDecimal amount;
    private String note;

    // --- Getters and Setters ---
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
