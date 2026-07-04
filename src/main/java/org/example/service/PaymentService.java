package org.example.service;

import org.example.dto.PaymentRequest;
import org.example.entity.Customer;
import org.example.entity.Payment;
import org.example.repository.CustomerRepository;
import org.example.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * ငွေချေမှုကို စီမံဆောင်ရွက်ပြီး ဝယ်သူ၏ အကြွေးကို အလိုအလျောက် နှုတ်ပေးသည့် လုပ်ငန်းစဉ်
     */
    @Transactional // 💡 လုပ်ငန်းစဉ်တစ်ခုခု ကျရှုံးပါက ဒေတာများကို မူလအတိုင်း Rollback လုပ်ပေးရန်
    public Payment processPayment(PaymentRequest request) {

        // ၁။ ဝယ်သူ ရှိ/မရှိ အရင်ဆုံး ရှာဖွေစစ်ဆေးခြင်း
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("မပြည့်စုံပါ - ဝယ်သူ အချက်အလက် ရှာမတွေ့ပါ။"));

        // ၂။ လာချေသည့် ငွေပမာဏသည် ၀ ထက် ကြီး/မကြီး စစ်ဆေးခြင်း
        BigDecimal updatedDebt = getBigDecimal(request, customer);

        // ၄။ အကြွေးအသစ်ကို Customer ထဲတွင် သွားရောက် Update လုပ်ပြီး သိမ်းဆည်းခြင်း
        customer.setTotalDebt(updatedDebt);
        customerRepository.save(customer);

        // ၅။ Payment Table ထဲသို့ ငွေချေမှု မှတ်တမ်းအသစ်ကို သွင်းခြင်း
        Payment payment = new Payment();
        payment.setCustomer(customer);
        payment.setAmount(request.getAmount());
        payment.setNote(request.getNote());

        return paymentRepository.save(payment);
    }

    private static BigDecimal getBigDecimal(PaymentRequest request, Customer customer) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("ငွေချေမှု ပမာဏသည် ၀ ထက် ကြီးရပါမည်။");
        }

        // ၃။ ဝယ်သူ၏ လက်ရှိအကြွေးထဲမှ လာချေသည့် ပမာဏကို နှုတ်ခြင်း (BigDecimal Math)
        // 💡 (Customer model ထဲတွင် totalDebt ကို BigDecimal သုံးထားသည်ဟု ယူဆပါသည်)
        BigDecimal currentDebt = customer.getTotalDebt() != null ? customer.getTotalDebt() : BigDecimal.ZERO;
        BigDecimal updatedDebt = currentDebt.subtract(request.getAmount());
        return updatedDebt;
    }
}