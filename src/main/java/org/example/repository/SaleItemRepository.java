package org.example.repository;

import org.example.entity.SalesItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleItemRepository extends JpaRepository<SalesItem, Long> {
    @Query(value = "SELECT p.name FROM sales_items si " +
            "JOIN sales s ON si.sale_id = s.id " +
            "JOIN products p ON si.product_id = p.id " +
            "WHERE EXTRACT(MONTH FROM s.sale_date) = :month " +
            "AND EXTRACT(YEAR FROM s.sale_date) = :year " +
            "GROUP BY p.name " +
            "ORDER BY SUM(si.quantity) DESC " +
            "LIMIT 1", nativeQuery = true)
    String findTopSellingItemByMonth(@Param("month") int month, @Param("year") int year);
}
