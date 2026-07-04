package org.example.controller;

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

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {
    @Autowired
    private SaleService saleService;
    private final CustomerRepository customerRepository;

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
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerRepository.save(customer);
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
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ဖျက်ဆီးမည့် ဝယ်သူရှာမတွေ့ပါ ID: " + id));

        customerRepository.delete(customer);
        return ResponseEntity.ok().build();
    }
     // 💡 မင်းရဲ့ Repository အမည်အတိုင်း ပြင်ပေးပါ


}