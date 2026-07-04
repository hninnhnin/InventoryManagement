package org.example.controller;

import org.example.entity.CreditSale;
import org.example.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/credit-sales") // 💡 ဒီနေရာက Base Path ကို သေချာကြည့်ပါ
@CrossOrigin(origins = "*")
public class CreditSaleController {
    @Autowired
    private SaleService saleService;

    /**
     * ဝယ်သူ ID အလိုက် မဆပ်ရသေးသော အကြွေးစာရင်းများကို ဆွဲထုတ်ပေးမည့် API
     * GET http://localhost:8080/api/credit-sales/unpaid?customerId=9
     */
    @GetMapping("/unpaid") // 💡 Base Path နဲ့ ပေါင်းလိုက်ရင် /api/credit-sales/unpaid ဖြစ်သွားပါမည်
    public ResponseEntity<List<CreditSale>> getUnpaidCreditSales(@RequestParam Long customerId) {
        List<CreditSale> unpaidCredits = saleService.getUnpaidCreditSalesByCustomer(customerId);
        return ResponseEntity.ok(unpaidCredits);
    }
}
