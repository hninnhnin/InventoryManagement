package org.example.controller;

import org.example.entity.Product;
import org.example.repository.InstallmentSaleRepository;
import org.example.repository.ProductRepository;
import org.example.repository.SaleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @Autowired
    private SaleItemRepository saleItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InstallmentSaleRepository installmentSaleRepository;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(@RequestParam(value = "month", required = false) Integer month,
                                                                   @RequestParam(value = "year", required = false) Integer year) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (month == null) month = java.time.LocalDate.now().getMonthValue();
            if (year == null) year = java.time.LocalDate.now().getYear();

            // မင်းရဲ့ ကျန်တဲ့ Logic တွေကို အောက်မှာ ဆက်ရေးပါ...
            String topItem = saleItemRepository.findTopSellingItemByMonth(month, year);
            response.put("topSellingItem", topItem != null ? topItem : "ယခုလတွင် အရောင်းစာရင်း မရှိသေးပါ");

            List<InstallmentSaleRepository.OverdueInstallmentDTO> overdueList = installmentSaleRepository.findOverdueInstallments();
            response.put("overdueInstallments", overdueList);

            // 🎯 ၃။ အရေအတွက် ၅ ခုထက်နည်းသော ပစ္စည်းများကို ရှာပြီး Response ထဲ ထည့်မယ်
            List<String> lowStockItems = productRepository.findLowStockItems(5);
            response.put("lowStockItems", lowStockItems);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
