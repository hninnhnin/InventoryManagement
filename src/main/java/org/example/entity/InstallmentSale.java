package org.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "installment_sales")

public class InstallmentSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sale sale;

    @Column(name = "total_months", nullable = false)
    private Integer totalMonths;

    @Column(name = "remaining_balance", nullable = false)
    private BigDecimal remainingBalance;

    @OneToMany(mappedBy = "installmentSale", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<InstallmentDetail> details;

    public Long getId() {
        return id;
    }

    public Sale getSale() {
        return sale;
    }

    public Integer getTotalMonths() {
        return totalMonths;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public List<InstallmentDetail> getDetails() {
        return details;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public void setTotalMonths(Integer totalMonths) {
        this.totalMonths = totalMonths;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public void setDetails(List<InstallmentDetail> details) {
        this.details = details;
    }
}