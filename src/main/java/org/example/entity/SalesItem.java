package org.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_items")

public class SalesItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnoreProperties("salesItems")
    private Sale sale; // ဘယ် Invoice ထဲကလဲ

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // ဘယ်ပစ္စည်းလဲ

    @Column(nullable = false)
    private Integer quantity; // အရေအတွက်

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice; // ထိုအချိန်က ရောင်းလိုက်သည့် ဈေးနှုန်း (ဝယ်သူ့လက်ခံဖြတ်ပိုင်းအတွက်)

    public Long getId() {
        return id;
    }

    public Sale getSale() {
        return sale;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}