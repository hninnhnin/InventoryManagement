package org.example.repository;

import org.example.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // ပစ္စည်းဘားကုဒ်ဖြင့် စကန်ဖတ်ကာ စျေးနှုန်းနှင့် စတော့ရှာရန်
    Optional<Product> findByBarcode(String barcode);
}