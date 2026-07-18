package org.example.repository;

import org.example.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // ပစ္စည်းဘားကုဒ်ဖြင့် စကန်ဖတ်ကာ စျေးနှုန်းနှင့် စတော့ရှာရန်
    Optional<Product> findByBarcode(String barcode);

   // List<Product> findByQuantityLessThanAndIsActiveTrue(int stockQty);
    @Query(value = "SELECT name FROM products WHERE stock_qty < :limit", nativeQuery = true)
    List<String> findLowStockItems(@Param("limit") int limit);
}