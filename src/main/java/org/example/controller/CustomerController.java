package org.example.controller;

import jakarta.transaction.Transactional;
import org.example.entity.CreditSale;
import org.example.entity.Customer;
import org.example.entity.CustomerDropdownDTO;
import org.example.repository.CreditSaleRepository;
import org.example.repository.CustomerRepository;
import org.example.repository.InstallmentSaleRepository;
import org.example.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {
    @Autowired
    private SaleService saleService;
    @Autowired
    private final CustomerRepository customerRepository;
    @Autowired
    private CreditSaleRepository creditSaleRepository;
    @Autowired
    private InstallmentSaleRepository installmentSaleRepository;

    // Constructor Injection
    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // ၁။ ဝယ်သူအားလုံးကို ဆွဲထုတ်ပြီး UI ဇယားထဲပြရန် (GET)
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    @GetMapping("/api/credit-sales/unpaid")
    public ResponseEntity<List<CreditSale>> getUnpaidCredits(@RequestParam Long customerId) {
        List<CreditSale> unpaidList = saleService.getUnpaidCreditSalesByCustomer(customerId);
        return ResponseEntity.ok(unpaidList);
    }
    // ၂။ ဝယ်သူအသစ်စာရင်းသွင်းရန် (POST)
    @PostMapping
    public ResponseEntity<String> createCustomer(@RequestBody Customer customer) { // 🎯 Return type ကို ResponseEntity<String> ပြောင်းလိုက်တယ်
        try {
            // 🎯 စည်းကမ်းချက် - Customer အသစ်ဆောက်လျှင် အကြွေးက 0 ပဲ ဖြစ်ရမည်
            if (customer.getTotalDebt() != null && customer.getTotalDebt().compareTo(BigDecimal.ZERO) > 0) {
                return ResponseEntity.badRequest().body("⚠️ ဝယ်သူအသစ်စာရင်းသွင်းရာတွင် အကြွေးပမာဏအား (0) အဖြစ်သာ စတင်သတ်မှတ်ရပါမည်။");
            }

            // အသစ်ဆောက်တာ သေချာစေရန် Status ကို အမြဲ True ပေးမည်
            customer.setActive(true);

            customerRepository.save(customer);
            return ResponseEntity.ok("ဝယ်သူအသစ်စာရင်းအား အောင်မြင်စွာ သိမ်းဆည်းပြီးပါပြီ။");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("အမှားအယွင်း ဖြစ်ပေါ်ခဲ့ပါသည်- " + e.getMessage());
        }
    }

    // ၃။ ဝယ်သူအချက်အလက် ပြင်ဆင်ရန် (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ပြင်ဆင်မည့် ဝယ်သူရှာမတွေ့ပါ ID: " + id));

        customer.setName(customerDetails.getName());
        customer.setPhone(customerDetails.getPhone());
        customer.setCustomerType(customerDetails.getCustomerType());
        // အကြွေးပမာဏကိုတော့ စနစ်ကပဲ ကိုင်တွယ်မှာမို့လို့ ဤနေရာတွင် Edit ပေးမထားပါ

        Customer updatedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(updatedCustomer);
    }

    // ၄။ ဝယ်သူစာရင်းဖျက်ရန် (DELETE)
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<String> deleteCustomer(@PathVariable Long id) {
        try {
            // ၁။ ဝယ်သူ ရှိ၊ မရှိ ရှာမယ်
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("ဖျက်ဆီးမည့် ဝယ်သူရှာမတွေ့ပါ ID: " + id));

            // ၂။ ဆိုင်တွင်းအကြွေးကျန် စစ်ဆေးခြင်း
            boolean hasActiveCreditDebt = creditSaleRepository.existsBySaleCustomerAndRemainingAmountGreaterThan(customer, BigDecimal.ZERO);
            if (hasActiveCreditDebt) {
                throw new RuntimeException("⚠️ ဤဝယ်သူတွင် ဆိုင်တွင်းအကြွေး (Invoice Credit) ကျန်ရှိနေသေးသဖြင့် ဖျက်၍မရနိုင်ပါ!");
            }

            // ၃။ အရစ်ကျအကြွေးကျန် စစ်ဆေးခြင်း
            boolean hasActiveInstallmentDebt = installmentSaleRepository.existsBySaleCustomerAndRemainingBalanceGreaterThan(customer, BigDecimal.ZERO);
            if (hasActiveInstallmentDebt) {
                throw new RuntimeException("⚠️ ဤဝယ်သူတွင် အရစ်ကျအကြွေး (Installment Balance) ကျန်ရှိနေသေးသဖြင့် ဖျက်၍မရနိုင်ပါ!");
            }

            // ==================== 🎯 SOFT DELETE LOGIC ====================

            // ၄။ Database ထဲက မဖျက်တော့ဘဲ အခြေအနေကို Inactive (false) သို့ ပြောင်းလဲသိမ်းဆည်းမည်
            customer.setActive(false);
            customerRepository.save(customer);

            return ResponseEntity.ok("ဝယ်သူအား Inactive အဖြစ် အောင်မြင်စွာ ပြောင်းလဲသတ်မှတ်ပြီးပါပြီ။");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
     // 💡 မင်းရဲ့ Repository အမည်အတိုင်း ပြင်ပေးပါ


}