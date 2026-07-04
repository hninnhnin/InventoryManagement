package org.example.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ဝယ်သူတစ်ဦးစီတွင် ငွေချေမှတ်တမ်း အများအပြား ရှိနိုင်၍ ManyToOne သုံးခြင်း
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;

    // 💡 [ထည့်သွင်းချက်] ဤ Payment သည် မည်သည့် Sale (Invoice) ဘောက်ချာနှင့် သက်ဆိုင်ကြောင်း ချိတ်ဆက်ခြင်း
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false) // General Debt Repayment ဆိုလျှင် null ဖြစ်နိုင်၍ nullable = true ထားပါသည်
    private Sale sale;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount; // ငွေကြေးပမာဏအတွက် ဒသမစိတ်ချရသော BigDecimal သုံးခြင်း

    @Column(name = "payment_date", nullable = false, updatable = false)
    private LocalDateTime paymentDate;

    @Column(name = "note")
    private String note;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // INVOICE, DEBT_REPAYMENT

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // CASH, KPAY, WAVEPAY

    // အလိုအလျောက် အချိန်မှတ်တမ်းတင်ပေးမည့် Lifecycle Hook
    @PrePersist
    protected void onCreate() {
        if (this.paymentDate == null) {
            this.paymentDate = LocalDateTime.now();
        }
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    // 💡 Sale အတွက် Getter / Setter အသစ်
    public Sale getSale() { return sale; }
    public void setSale(Sale sale) { this.sale = sale; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}