package org.example.repository;

import org.example.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // လိုအပ်ပါက Customer ID အလိုက် Payment History ရှာသည့် Query မျိုး ဤနေရာတွင် ထည့်နိုင်သည်
}
