package org.example.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "credit_sales")

public class CreditSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Column(name = "remaining_amount", nullable = false)
    private BigDecimal remainingAmount;

    @Column(name = "collected_amount", nullable = false)
    private BigDecimal collectedAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "paid_status")
    private PaidStatus paidStatus = PaidStatus.UNPAID;

    public Long getId() {
        return id;
    }

    public Sale getSale() {
        return sale;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public BigDecimal getCollectedAmount() {
        return collectedAmount;
    }

    public PaidStatus getPaidStatus() {
        return paidStatus;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public void setCollectedAmount(BigDecimal collectedAmount) {
        this.collectedAmount = collectedAmount;
    }

    public void setPaidStatus(PaidStatus paidStatus) {
        this.paidStatus = paidStatus;
    }

    public enum PaidStatus {
        UNPAID, PAID
    }
}