package org.example.repository;

import org.example.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    // Invoice Number အတိအကျဖြင့် အရောင်းဘောက်ချာ ပြန်လည်ရှာဖွေရန်
    Optional<Sale> findByInvoiceNumber(String invoiceNumber);
    Optional<Sale> findFirstByCustomerIdOrderByIdDesc(Long customerId);
    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.customer WHERE s.paymentType != 'CASH'")
    List<Sale> findAllNonCashSales();
}