package org.example.controller;

import org.example.entity.Product;
import org.example.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepository;

    // Constructor Injection
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ၁။ ပစ္စည်းအားလုံးကို ဆွဲထုတ်ပြီး UI ဇယားထဲပြရန် (GET)
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ၂။ ပစ္စည်းအသစ်သိမ်းရန် (POST)
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    // ၃။ ပစ္စည်းအချက်အလက် ပြင်ဆင်ရန် (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ပြင်ဆင်မည့် ပစ္စည်းရှာမတွေ့ပါ ID: " + id));

        product.setBarcode(productDetails.getBarcode());
        product.setName(productDetails.getName());
        product.setCostPrice(productDetails.getCostPrice());
        product.setSellingPrice(productDetails.getSellingPrice());
        product.setStockQty(productDetails.getStockQty());

        Product updatedProduct = productRepository.save(product);
        return ResponseEntity.ok(updatedProduct);
    }

    // ၄။ ပစ္စည်းဖျက်ရန် (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ဖျက်ဆီးမည့် ပစ္စည်းရှာမတွေ့ပါ ID: " + id));

        productRepository.delete(product);
        return ResponseEntity.ok().build();
    }
}