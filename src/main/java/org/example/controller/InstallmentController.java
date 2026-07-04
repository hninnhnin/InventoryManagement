package org.example.controller;

import org.example.entity.InstallmentSale;
import org.example.entity.Payment;
import org.example.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/installments")
@CrossOrigin(origins = "*")
public class InstallmentController {
    @Autowired
    private SaleService saleService;

    // ၁။ Installment စာရင်းအားလုံးကို Master Row အနေဖြင့် ဆွဲထုတ်မည့် API
    // 💡 InstallmentController.java ထဲက မက်သတ်နှစ်ခုလုံးကို ဒီကုဒ်အတိုင်း အစားထိုးပေးပါဦး

    @GetMapping("/list")
    public ResponseEntity<?> getAllInstallments() {
        List<InstallmentSale> list = saleService.getAllInstallmentSales();

        // ၁။ 📌 ၎င်းထဲမှ လက်ကျန်ငွေ သုညထက်ကြီးသော (မဆပ်ရသေးသော) စာရင်းများကိုသာ Filter လုပ်ခြင်း
        List<Map<String, Object>> response = list.stream()
                .filter(ins -> ins.getRemainingBalance() != null && ins.getRemainingBalance().compareTo(BigDecimal.ZERO) > 0)
                .map(ins -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", ins.getId());
                    map.put("totalMonths", ins.getTotalMonths());
                    map.put("remainingBalance", ins.getRemainingBalance());
                    map.put("invoiceNumber", ins.getSale() != null ? ins.getSale().getInvoiceNumber() : "N/A");
                    map.put("saleDate", ins.getSale() != null ? ins.getSale().getSaleDate() : null);

                    // ဝယ်သူအမည်ကို ယူခြင်း
                    String customerName = "အမည်မသိ";
                    if (ins.getSale() != null && ins.getSale().getCustomer() != null) {
                        customerName = ins.getSale().getCustomer().getName();
                    }
                    map.put("customerName", customerName);

                    return map;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInstallmentById(@PathVariable Long id) {
        // 💡 .map() မသုံးဘဲ Object အဖြစ် တိုက်ရိုက် ဖမ်းယူခြင်း
        InstallmentSale ins = saleService.getInstallmentSaleById(id);

        // အကယ်၍ ရှာမတွေ့ပါက 404 ပြန်မည်
        if (ins == null) {
            return ResponseEntity.notFound().build();
        }

        // ဒေတာများကို Map ထဲသို့ ထည့်သွင်းခြင်း
        Map<String, Object> map = new HashMap<>();
        map.put("id", ins.getId());
        map.put("totalMonths", ins.getTotalMonths());
        map.put("remainingBalance", ins.getRemainingBalance());
        map.put("details", ins.getDetails());
        map.put("invoiceNumber", ins.getSale() != null ? ins.getSale().getInvoiceNumber() : "N/A");

        // Customer Name ကို တိုက်ရိုက် ဆွဲထုတ်ထည့်ပေးခြင်း
        String customerName = "N/A";
        if (ins.getSale() != null && ins.getSale().getCustomer() != null) {
            customerName = ins.getSale().getCustomer().getName();
        }
        map.put("customerName", customerName);

        return ResponseEntity.ok(map);
    }
    // 💡 အပေါ်က getInstallmentById ရဲ့ အောက်မှာ ဒီကုဒ်လေး ရှိနေဖို့ လိုအပ်ပါတယ်ဗျာ

    @PostMapping("/{id}/pay-installment") // 👈 Frontend က လှမ်းခေါ်နေတဲ့ ငွေသွင်း API လမ်းကြောင်း
    public ResponseEntity<?> payInstallment(
            @PathVariable Long id,
            @RequestParam Long detailId,
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String note) {
        try {
            // Service ထဲက ငွေသွင်းခြင်း Logic ကို လှမ်းခေါ်ယူခြင်း
            Payment payment = saleService.processInstallmentPayment(id, detailId, amount, paymentMethod, note);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ငွေသွင်းခြင်း မအောင်မြင်ပါ- " + e.getMessage());
        }
    }
}
