package org.example.repository;

import org.example.entity.InstallmentSale;
import org.example.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstallmentSaleRepository extends JpaRepository<InstallmentSale, Long> {
    // အခြေခံ Save, Find, Delete တွေအားလုံးကို JpaRepository ကနေ အလိုအလျောက် ပံ့ပိုးပေးသွားမှာဖြစ်ပါတယ်
    Optional<InstallmentSale> findBySaleId(Long saleId);
    Optional<InstallmentSale> findBySale(Sale sale);
}
