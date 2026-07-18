package org.example.repository;

import org.example.entity.Customer;
import org.example.entity.InstallmentSale;
import org.example.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstallmentSaleRepository extends JpaRepository<InstallmentSale, Long> {
    // အခြေခံ Save, Find, Delete တွေအားလုံးကို JpaRepository ကနေ အလိုအလျောက် ပံ့ပိုးပေးသွားမှာဖြစ်ပါတယ်
    Optional<InstallmentSale> findBySaleId(Long saleId);
    Optional<InstallmentSale> findBySale(Sale sale);
    boolean existsBySaleCustomerAndRemainingBalanceGreaterThan(Customer customer, BigDecimal amount);
    public interface OverdueInstallmentDTO {
        String getCustomerName();
        String getInvoiceNumber(); // ဗောက်ချာနံပါတ်
        java.time.LocalDate getDueDate(); // ပေးရမည့်ရက်
        java.math.BigDecimal getAmountDue(); // ပေးရမည့်ပမာဏ
    }
    @Query(value = "SELECT c.name as customerName, s.invoice_number as invoiceNumber, " +
            "ide.due_date as dueDate, (ide.amount_due - ide.collected_amount) as amountDue " +
            "FROM installment_details ide " +
            "JOIN installment_sales ids ON ide.installment_sale_id = ids.id " +
            "JOIN sales s ON ids.sale_id = s.id " +
            "JOIN customers c ON s.customer_id = c.id " +
            "WHERE ide.due_date < CURRENT_DATE " +
            "AND ide.collected_amount < ide.amount_due", nativeQuery = true)
    List<OverdueInstallmentDTO> findOverdueInstallments();
}
