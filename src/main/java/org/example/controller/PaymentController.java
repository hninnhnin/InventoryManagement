package org.example.controller;

import org.example.dto.PaymentRequest;
import org.example.entity.CreditSale;
import org.example.entity.Payment;
import org.example.service.PaymentService;
import org.example.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private SaleService saleService;
    /**
     * ဝယ်သူ၏ ငွေချေမှုကို လက်ခံဆောင်ရွက်ပေးသည့် API Endpoint
     * POST http://localhost:8080/api/payments
     */
    @PostMapping("/repay-fifo")
    public ResponseEntity<?> payFifo(
            @RequestParam Long customerId,
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String note) {
        try {
            Payment payment = saleService.processFifoPayment(customerId, amount, paymentMethod, note);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // 💡 PaymentController.java ထဲတွင် ပေါင်းထည့်ရန်
    @GetMapping("/unpaid-credits")
    public ResponseEntity<List<CreditSale>> getUnpaidCreditSales(@RequestParam Long customerId) {
        // CreditSale table ထဲက UNPAID စာရင်းတွေကို ဆွဲထုတ်မည့် logic
        List<CreditSale> unpaidList = saleService.getUnpaidCreditSalesByCustomer(customerId);
        return ResponseEntity.ok(unpaidList);
    }

}
