package org.example.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "installment_details")

public class InstallmentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "installment_sale_id", nullable = false)
    @JsonBackReference
    private InstallmentSale installmentSale;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "amount_due", nullable = false)
    private BigDecimal amountDue;

    @Column(name = "collected_amount", nullable = false)
    private BigDecimal collectedAmount = BigDecimal.ZERO;

    @Column(name = "is_paid")
    private Boolean isPaid = false;

    public Long getId() {
        return id;
    }

    public InstallmentSale getInstallmentSale() {
        return installmentSale;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public BigDecimal getCollectedAmount() {
        return collectedAmount;
    }

    public Boolean getPaid() {
        return isPaid;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setInstallmentSale(InstallmentSale installmentSale) {
        this.installmentSale = installmentSale;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setAmountDue(BigDecimal amountDue) {
        this.amountDue = amountDue;
    }

    public void setCollectedAmount(BigDecimal collectedAmount) {
        this.collectedAmount = collectedAmount;
    }

    public void setPaid(Boolean paid) {
        isPaid = paid;
    }
}