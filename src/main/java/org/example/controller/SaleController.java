package org.example.controller;

import org.example.entity.CreditSale;
import org.example.entity.InstallmentSale;
import org.example.entity.Sale;
import org.example.entity.SalesItem;
import org.example.repository.CreditSaleRepository;
import org.example.repository.InstallmentSaleRepository;
import org.example.repository.SaleRepository;
import org.example.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    // 💡 `@Autowired` များကို အပေါ်တွင် တစ်စုတစ်စည်းတည်း ထားရှိပေးခြင်းဖြင့် Null ဖြစ်ခြင်းမှ ကာကွယ်ပါသည်
    @Autowired
    private SaleService saleService;

    @Autowired
    private SaleRepository saleRepository; // 🔥 [FIXED] `@Autowired` ထည့်သွင်းလိုက်သဖြင့် Null မဖြစ်တော့ပါ

    @Autowired
    private InstallmentSaleRepository installmentSaleRepository;

    @Autowired
    private CreditSaleRepository creditSaleRepository;

    // 💡 ပြဿနာဖြစ်စေနိုင်သော မူလ Constructor အား ဖျက်လိုက်ပြီး Spring ၏ Auto-Wiring ကို သုံးခွင့်ပေးလိုက်ပါသည်

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @RequestBody List<SalesItem> items,
            @RequestParam String paymentType,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer installmentMonths) {

        try {
            // UI ကနေ ပို့လိုက်တဲ့ အချက်အလက်တွေကို Service ထံ လွှဲပေးခြင်း
            Sale sale = saleService.createSale(customerId, items, paymentType, installmentMonths);
            return ResponseEntity.ok(sale);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 🎯 CASH မဟုတ်သော အကြွေးနှင့် အရစ်ကျစာရင်းများကို PAID / UNPAID ขွဲထုတ်ပေးသည့် API
     */
    // 🎯 Controller ရဲ့ အပေါ်ဆုံးနားမှာ CreditSaleRepository ကို Inject လုပ်ထားဖို့ လိုပါမယ်
// @Autowired private CreditSaleRepository creditSaleRepository;

    @GetMapping("/list")
    public ResponseEntity<?> getAllNonCashSales() {
        try {
            List<Sale> allSales = saleRepository.findAllNonCashSales();
            List<Map<String, Object>> response = new ArrayList<>();

            if (allSales != null) {
                for (Sale sale : allSales) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", sale.getId());
                    map.put("invoiceNumber", sale.getInvoiceNumber());
                    map.put("saleDate", sale.getSaleDate());

                    String pType = sale.getPaymentType() != null ? sale.getPaymentType().toString() : "CREDIT";
                    map.put("paymentMethod", pType);
                    map.put("totalAmount", sale.getTotalAmount());

                    BigDecimal remainingBalance = BigDecimal.ZERO;

                    if (pType.equalsIgnoreCase("INSTALLMENT")) {
                        // INSTALLMENT အတွက် DB ထဲက အမှန်အတိုင်း ရှာဖွေခြင်း
                        remainingBalance = installmentSaleRepository.findBySaleId(sale.getId())
                                .map(InstallmentSale::getRemainingBalance)
                                .orElse(BigDecimal.ZERO);
                    } else {
                        // 🎯 CREDIT အတွက် ၅၀% Hardcode ကို ဖျက်ပြီး DB ထဲက အစစ်အမှန် ကျန်ငွေကို ဆွဲထုတ်ခြင်း
                        remainingBalance = creditSaleRepository.findBySale(sale)
                                .map(CreditSale::getRemainingAmount) // သို့မဟုတ် မင်းရဲ့ Entity ထဲက field နာမည်
                                .orElse(BigDecimal.ZERO);
                    }

                    map.put("remainingBalance", remainingBalance);

                    // 🎯 အစစ်အမှန် ကျန်ငွေ သုည ဖြစ်သွားရင် PAID ပြောင်းပေးပါပြီ
                    String status = (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) ? "PAID" : "UNPAID";
                    map.put("status", status);

                    String customerName = sale.getCustomer() != null ? sale.getCustomer().getName() : "အမည်မသိ";
                    map.put("customerName", customerName);

                    response.add(map);
                }
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}