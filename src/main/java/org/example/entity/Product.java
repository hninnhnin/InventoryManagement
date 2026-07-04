package org.example.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")

public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String barcode; // ပစ္စည်းဘားကုဒ် (ရှာရလွယ်အောင် Unique ပေးထားပါတယ်)

    @Column(nullable = false)
    private String name; // ပစ္စည်းအမည်

    @Column(name = "cost_price", nullable = false)
    private BigDecimal costPrice; // အရင်းဈေး (နေ့စဉ်အမြတ်တွက်ရန် မဖြစ်မနေလိုပါသည်)

    @Column(name = "selling_price", nullable = false)
    private BigDecimal sellingPrice; // ရောင်းဈေး

    @Column(name = "stock_qty", nullable = false)
    private Integer stockQty = 0; // လက်ကျန်အရေအတွက် (Default ကို ၀ ထားပါမည်)

    public Long getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }
}