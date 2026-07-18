package org.example.repository;

import org.example.entity.CreditSale;
import org.example.entity.Customer;
import org.example.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditSaleRepository extends JpaRepository<CreditSale, Long> {

    // ဘောက်ချာ Entity ကို သုံးပြီး သက်ဆိုင်ရာ Credit စာရင်းကို လှမ်းရှာမည့် Query
    Optional<CreditSale> findBySale(Sale sale);
    boolean existsBySaleCustomerAndRemainingAmountGreaterThan(Customer customer, BigDecimal amount);
    List<CreditSale> findBySaleCustomerAndPaidStatusOrderBySaleSaleDateDesc(Customer customer, CreditSale.PaidStatus paidStatus);
    // ဝယ်သူ ID အလိုက် မဆပ်ရသေးသော အကြွေးဟောင်းစာရင်းများကိုသာ ဆွဲထုတ်ရန် Custom JPQL
    @Query("SELECT cs FROM CreditSale cs WHERE cs.sale.customer.id = :customerId AND cs.paidStatus = 'UNPAID'")
    List<CreditSale> findUnpaidCreditSalesByCustomerId(@Param("customerId") Long customerId);
    @Query("SELECT cs FROM CreditSale cs JOIN FETCH cs.sale s WHERE s.customer.id = :customerId AND cs.paidStatus = :paidStatus ORDER BY s.saleDate ASC")
    List<CreditSale> findUnpaidByCustomerId(@Param("customerId") Long customerId, @Param("paidStatus") CreditSale.PaidStatus paidStatus);
    List<CreditSale> findBySaleCustomerAndPaidStatusOrderBySaleSaleDateAsc(Customer customer, CreditSale.PaidStatus paidStatus);
}