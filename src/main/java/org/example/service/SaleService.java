package org.example.service;

import org.example.entity.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SaleService {
    @Autowired
    private InstallmentSaleRepository installmentSaleRepository;
    @Autowired
    private InstallmentDetailRepository installmentDetailRepository;
    @Autowired
    private final SaleRepository saleRepository;
    @Autowired
    private final CustomerRepository customerRepository;
    @Autowired
    private final ProductRepository productRepository;
    @Autowired
    private final CreditSaleRepository creditSaleRepository;
    @Autowired
    private final PaymentRepository paymentRepository; // 💡 [ထည့်သွင်းချက်] Payment သိမ်းရန် Repository တိုးလိုက်သည်

    public SaleService(SaleRepository saleRepository, CustomerRepository customerRepository,
                       ProductRepository productRepository, CreditSaleRepository creditSaleRepository,
                       PaymentRepository paymentRepository) { // 💡 Constructor ထဲတွင်ပါ ပေါင်းထည့်ပေးခြင်း
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.creditSaleRepository = creditSaleRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * အဆင့်မြှင့်တင်ထားသော အရောင်းစာရင်းသွင်းခြင်းနှင့် ငွေပေးချေမှုပုံစံ (၃) မျိုး Flow လုပ်ဆောင်ချက်
     */
    /**
     * ဝယ်သူပေးလာသော ငွေအပေါ်မူတည်၍ ရက်စွဲအဟောင်းဆုံး အကြွေးဘောက်ချာမှစတင်၍ အလိုအလျောက် လိုက်နှိမ်ခြင်း (FIFO Repayment)
     */
    @Transactional
    public Payment processFifoPayment(Long customerId, BigDecimal payAmount, String paymentMethod, String note) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("ဝယ်သူအား ရှာမတွေ့ပါ"));

        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("ပေးချေမည့် ပမာဏသည် ၀ ထက် ကြီးရပါမည်");
        }

        // ၁။ ဤဝယ်သူ၏ မဆပ်ရသေးသော အကြွေးဘောက်ချာများအားလုံးကို ရက်စွဲအလိုက် ဆွဲထုတ်ခြင်း
        List<CreditSale> unpaidCredits = creditSaleRepository
                .findBySaleCustomerAndPaidStatusOrderBySaleSaleDateAsc(customer, CreditSale.PaidStatus.UNPAID);

        BigDecimal runningAmount = payAmount; // လိုက်နှိမ်ရန် လက်ကျန်ငွေ
        Payment lastSavedPayment = null;       // Controller ကို return ပြန်ပေးရန် ကနဦးသိမ်းဆည်းထားမည့် variable

        // ၂။ ဘောက်ချာတစ်ခုချင်းစီကို Loop ပတ်၍ လိုက်နှိမ်ခြင်း
        for (CreditSale credit : unpaidCredits) {
            if (runningAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal remaining = credit.getRemainingAmount();
            BigDecimal actualPaymentForThisInvoice = BigDecimal.ZERO; // ဒီဘောက်ချာအတွက် အမှန်တကယ် နှိမ်ပေးလိုက်သည့် ပမာဏ

            if (runningAmount.compareTo(remaining) >= 0) {
                // (က) ပေးငွေက ဘောက်ချာကျန်ငွေထက် များလျှင် သို့မဟုတ် ကွက်တိဖြစ်လျှင်
                actualPaymentForThisInvoice = remaining; // ကျန်သမျှအကုန်နှိမ်မည်

                runningAmount = runningAmount.subtract(remaining);
                credit.setCollectedAmount(credit.getCollectedAmount().add(remaining));
                credit.setRemainingAmount(BigDecimal.ZERO);
                credit.setPaidStatus(CreditSale.PaidStatus.PAID);
            } else {
                // (ခ) ပေးငွေက ဘောက်ချာကျန်ငွေထက် နည်းလျှင်
                actualPaymentForThisInvoice = runningAmount; // ရသလောက်ပဲနှိမ်မည်

                credit.setRemainingAmount(remaining.subtract(runningAmount));
                credit.setCollectedAmount(credit.getCollectedAmount().add(runningAmount));
                runningAmount = BigDecimal.ZERO;
            }
            creditSaleRepository.save(credit);

            // 💡 🎯 [CRITICAL FIX] နှိမ်လိုက်ရသော ဘောက်ချာတစ်ခုချင်းစီအတွက် Payment သမိုင်းမှတ်တမ်း သီးသန့်စီ သွင်းခြင်း
            Payment payment = new Payment();
            payment.setCustomer(customer);
            payment.setAmount(actualPaymentForThisInvoice); // ဒီဘောက်ချာအတွက် နှိမ်ပေးလိုက်သော ပမာဏအတိအကျ
            payment.setTransactionType("DEBT_REPAYMENT");
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentDate(LocalDateTime.now()); // လိုအပ်လျှင် ထည့်ရန်
            payment.setNote(note == null || note.isEmpty() ? "Invoice No: " + credit.getSale().getInvoiceNumber() + " အတွက် အကြွေးနှိမ်ခြင်း" : note);

            // 🔥 မဖြစ်မနေ လိုအပ်နေသော Sale ID ကို သေသေချာချာ ချိတ်ဆက်ပေးခြင်း ဖြစ်ပါတယ်
            payment.setSale(credit.getSale());

            lastSavedPayment = paymentRepository.save(payment);
        }

        // ၃။ ဝယ်သူ၏ စုစုပေါင်း အကြွေးကျန် (Total Debt) ကို သွားနှုတ်ပေးခြင်း
        if (customer.getTotalDebt() != null) {
            customer.setTotalDebt(customer.getTotalDebt().subtract(payAmount));
            if (customer.getTotalDebt().compareTo(BigDecimal.ZERO) < 0) {
                customer.setTotalDebt(BigDecimal.ZERO);
            }
        }
        customerRepository.save(customer);

        // 💡 တကယ်လို့ အကြွေးစာရင်း လုံးဝမရှိတဲ့သူက ငွေလာပေးရင် (သို့မဟုတ်) အကြွေးထက်ပိုပေးရင်
        // Sale မပါတဲ့ ကြွေးကြိုတင်ပေးငွေအဖြစ် သီးသန့်မှတ်တမ်းတစ်ခု သိမ်းပေးရန် (မတော်တဆ error မတက်စေရန် fallback လေး ထည့်ထားပေးပါတယ်)
        if (lastSavedPayment == null) {
            Payment advancePayment = new Payment();
            advancePayment.setCustomer(customer);
            advancePayment.setAmount(payAmount);
            advancePayment.setTransactionType("ADVANCE_DEBT_REPAYMENT");
            advancePayment.setPaymentMethod(paymentMethod);
            advancePayment.setNote("ကြွေးကျန်မရှိဘဲ လာရောက်ပေးချေခြင်း (သို့မဟုတ်) ပိုလျှံငွေ");
            // တကယ်လို့ မင်းရဲ့ database မှာ sale_id က 100% မပါမဖြစ်ဆိုရင် ဒီနေရာမှာ default sale တစ်ခုခု သတ်မှတ်ပေးဖို့ လိုနိုင်ပါတယ်ဗျာ။
            return paymentRepository.save(advancePayment);
        }

        return lastSavedPayment;
    }
    /**
     * ဝယ်သူ၏ အရောင်းဘောက်ချာများထဲမှ မဆပ်ရသေးသော အကြွေးစာရင်းများကို ရက်စွဲအလိုက် ဆွဲထုတ်ခြင်း
     */
    // 💡 SaleService.java အတွင်း ဖြည့်စွက်ပေးရမည့် Method များ

    // မရှိသေးက ဆောက်ပေးပါ

    public List<InstallmentSale> getAllInstallmentSales() {
        return installmentSaleRepository.findAll();
    }

    public InstallmentSale getInstallmentSaleById(Long id) {
        return installmentSaleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Installment Record ရှာမတွေ့ပါ"));
    }

    @Transactional
    public Payment processInstallmentPayment(Long installmentSaleId, Long detailId, BigDecimal amount, String paymentMethod, String note) {

        InstallmentSale installmentSale = installmentSaleRepository.findById(installmentSaleId)
                .orElseThrow(() -> new RuntimeException("အရစ်ကျ အရောင်းမှတ်တမ်း ရှာမတွေ့ပါ"));

        InstallmentDetail detail = installmentDetailRepository.findById(detailId)
                .orElseThrow(() -> new RuntimeException("လအလိုက် အရစ်ကျ Detail ကို ရှာမတွေ့ပါ"));

        // ၁။ ဒေတာများအား အပ်ဒိတ်လုပ်ခြင်း
        detail.setCollectedAmount(detail.getCollectedAmount().add(amount));
        if (detail.getCollectedAmount().compareTo(detail.getAmountDue()) >= 0) {
            detail.setPaid(true); // ပေးချေငွေ ပြည့်သွားပါက PAID ဖြစ်သွားမည်
        }
        installmentDetailRepository.save(detail);

        // Master Table ရဲ့ ကျန်ငွေကိုပါ လိုက်နှုတ်ပေးခြင်း
        installmentSale.setRemainingBalance(installmentSale.getRemainingBalance().subtract(amount));
        installmentSaleRepository.save(installmentSale);

        // ၂။ 💡 Payment Table ထဲတွင် ဒေတာ အလိုအလျောက် သမိုင်းမှတ်တမ်း သွင်းခြင်း
        Payment payment = new Payment();
        payment.setCustomer(installmentSale.getSale().getCustomer());
        payment.setSale(installmentSale.getSale());
        payment.setAmount(amount);

        // 🔥 မင်းတောင်းဆိုထားသည့် Default "INSTALLMENT" Transaction Type သတ်မှတ်ချက်
        payment.setTransactionType("INSTALLMENT");

        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setNote(note == null || note.isEmpty() ? "လစဉ်အရစ်ကျ လာရောက်ပေးချေခြင်း" : note);

        return paymentRepository.save(payment);
    }
    @Transactional(readOnly = true)
    public List<CreditSale> getUnpaidCreditSalesByCustomer(Long customerId) {
        // 💡 Customer ID ကို သုံးပြီး ၎င်းနှင့်သက်ဆိုင်သော UNPAID CreditSale စာရင်းကို Fetch Type ဘေးကင်းအောင် တစ်ခါတည်း ဆွဲထုတ်သည်
        // ၎င်း Query ကို အောက်ပါအတိုင်း ရေးနိုင်ရန် CreditSaleRepository ထဲတွင် ထည့်ပေးရပါမည်။
        return creditSaleRepository.findUnpaidByCustomerId(customerId, CreditSale.PaidStatus.UNPAID);
    }
    @Transactional
    public Sale createSale(Long customerId, List<SalesItem> inputItems, String paymentTypeStr, Integer installmentMonths) {

        Customer customer = null;
        if (customerId != null) {
            customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("ဝယ်သူအား ရှာမတွေ့ပါ"));
        }

        // ဝင်လာသော String အား Enum (CASH, INVOICE_CREDIT, INSTALLMENT) သို့ ပြောင်းလဲခြင်း
        Sale.PaymentType paymentType = Sale.PaymentType.valueOf(paymentTypeStr.toUpperCase());

        // ==================== 🎯 [၁] ဒီနေ့နှင့် အနီးစပ်ဆုံး/နောက်ဆုံး ယူထားသော အကြွေးဘောက်ချာအား စစ်ဆေးခြင်း ====================
        if (customer != null && paymentType == Sale.PaymentType.INVOICE_CREDIT) {

            // 💡 [FIXED] ဒီနေ့နှင့် အနီးစပ်ဆုံး/လတ်တလောဆုံး ယူထားသော အကြွေးဘောက်ချာကို ယူရန်အတွက်
            // PaidStatus မခွဲခြားဘဲ ရက်စွဲအလိုက် (အသစ်ဆုံးမှ အဟောင်းဆုံးသို့ Descending) စီ၍ ဆွဲထုတ်ခြင်း
            List<CreditSale> latestCredits = creditSaleRepository
                    .findBySaleCustomerAndPaidStatusOrderBySaleSaleDateDesc(customer, CreditSale.PaidStatus.UNPAID);

            // အကယ်၍ ဝယ်သူတွင် မဆပ်ရသေးသော အကြွေးစာရင်း ရှိခဲ့လျှင်
            if (latestCredits != null && !latestCredits.isEmpty()) {
                // Index 0 သည် ရက်စွဲအလိုက် Descending စီထားသဖြင့် ဒီနေ့နှင့် "အနီးစပ်ဆုံး/လတ်တလောဆုံး" ဖြစ်ခဲ့သော အကြွေးဘောက်ချာဖြစ်သည်
                CreditSale mostRecentCredit = latestCredits.get(0);

                BigDecimal originalTotal = mostRecentCredit.getSale().getTotalAmount();
                BigDecimal remainingDebt = mostRecentCredit.getRemainingAmount();

                if (originalTotal != null && originalTotal.compareTo(BigDecimal.ZERO) > 0) {
                    // ထို လတ်တလောဆုံးဘောက်ချာ မူလတန်ဖိုး၏ ၅၀% ကို တွက်ချက်ခြင်း
                    BigDecimal fiftyPercentOfTotal = originalTotal.multiply(new BigDecimal("0.5"));

                    // ⚠️ စည်းမျဉ်းအမှန်: အကယ်၍ ထိုအနီးစပ်ဆုံးဘောက်ချာ၏ လက်ကျန်ကြွေးသည် ၅၀% ထက် များနေသေးလျှင်
                    // (၅၀% ပြည့်အောင်/တစ်ဝက်ကျေအောင် မဆပ်ရသေးပါက) အကြွေးသစ် ထပ်ဝယ်ခွင့်ကို ပိတ်မည်!
                    if (remainingDebt.compareTo(fiftyPercentOfTotal) > 0) {
                        throw new RuntimeException("အကြွေးဝယ်ယူခွင့် မပြုပါ။ ဝယ်သူ၏ လတ်တလောဆုံး (အနီးစပ်ဆုံး) ယူထားသော အကြွေးဘောက်ချာ ("
                                + mostRecentCredit.getSale().getInvoiceNumber() + ") သည် အနည်းဆုံး ၅၀% (တစ်ဝက်) ပြည့်အောင် ပေးချေထားခြင်း မရှိသေးပါ! ထိုဘောက်ချာအား အရင်ဆုံး တစ်ဝက်ပြည့်အောင် ဆပ်ပေးရပါမည်။");
                    }
                }
            }
        }
        // =========================================================================================================

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SalesItem> finalItems = new ArrayList<>();

        // [၂] Invoice Master (ဘောက်ချာအခြေခံ) စတင်တည်ဆောက်ခြင်း
        Sale sale = new Sale();
        sale.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        sale.setSaleDate(LocalDateTime.now());
        sale.setCustomer(customer);

        // [၃] ကုန်ပစ္စည်းစစ်ဆေးခြင်း၊ Stock နှုတ်ခြင်းနှင့် တန်ဖိုးတွက်ချက်ခြင်း
        for (SalesItem item : inputItems) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("ကုန်ပစ္စည်း ရှာမတွေ့ပါ"));

            if (product.getStockQty() < item.getQuantity()) {
                throw new RuntimeException("ပစ္စည်းလက်ကျန် မလုံလောက်ပါ: " + product.getName());
            }

            product.setStockQty(product.getStockQty() - item.getQuantity());
            productRepository.save(product);

            item.setUnitPrice(product.getSellingPrice());
            item.setSale(sale);
            finalItems.add(item);

            BigDecimal itemTotal = product.getSellingPrice().multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        sale.setTotalAmount(totalAmount);
        sale.setItems(finalItems);
        sale.setPaymentType(paymentType);

        // Sale Voucher အား Database ထဲ အရင်သိမ်းဆည်းခြင်း
        sale = saleRepository.save(sale);

        // ==================== [၄] ငွေပေးချေမှုပုံစံအလိုက် သီးသန့် Flow များ လုပ်ဆောင်ချက် ====================

        if (paymentType == Sale.PaymentType.INVOICE_CREDIT) {
            if (customer == null) {
                throw new RuntimeException("အကြွေးစနစ် (INVOICE_CREDIT) ဖြင့် ဝယ်ယူရန် ဝယ်သူအမည် မဖြစ်မနေ လိုအပ်ပါသည်");
            }

            // အရောင်းဖွင့်ချိန်တွင် ဘာ Payment မှ တွက်ချက်သိမ်းဆည်းခြင်း မပြုပါ။
            // ကျသင့်ငွေ ၁၀၀% အပြည့် (Total Amount) ကို CreditSale ထဲသို့ တိုက်ရိုက် အကြွေးအဖြစ် သွားသိမ်းပါမည်။
            CreditSale creditSale = new CreditSale();
            creditSale.setSale(sale);
            creditSale.setRemainingAmount(totalAmount); // အကြွေးကျန် ၁၀၀% အပြည့် သတ်မှတ်ခြင်း
            creditSale.setCollectedAmount(BigDecimal.ZERO);
            creditSale.setPaidStatus(CreditSale.PaidStatus.UNPAID);
            creditSaleRepository.save(creditSale);

            // ဝယ်သူ၏ အကြွေးစုစုပေါင်း (Total Debt) ထဲသို့ သွားရောက်ပေါင်းထည့်ပေးခြင်း
            if (customer.getTotalDebt() == null) {
                customer.setTotalDebt(BigDecimal.ZERO);
            }
            customer.setTotalDebt(customer.getTotalDebt().add(totalAmount));
            customerRepository.save(customer);

        } else if (paymentType == Sale.PaymentType.INSTALLMENT) {
            if (customer == null) {
                throw new RuntimeException("အရစ်ကျစနစ် (INSTALLMENT) ဖြင့် ဝယ်ယူရန် ဝယ်သူအမည် မဖြစ်မနေ လိုအပ်ပါသည်");
            }
            if (installmentMonths == null || installmentMonths <= 0) {
                throw new RuntimeException("ကျေးဇူးပြု၍ အရစ်ကျဆပ်မည့် လအရေအတွက်ကို သတ်မှတ်ပေးပါ");
            }

            InstallmentSale installmentSale = new InstallmentSale();
            installmentSale.setSale(sale);
            installmentSale.setTotalMonths(installmentMonths);
            installmentSale.setRemainingBalance(totalAmount);

            List<InstallmentDetail> details = new ArrayList<>();
            BigDecimal monthlyAmount = totalAmount.divide(new BigDecimal(installmentMonths), 0, RoundingMode.HALF_UP);

            for (int i = 1; i <= installmentMonths; i++) {
                InstallmentDetail detail = new InstallmentDetail();
                detail.setInstallmentSale(installmentSale);
                detail.setDueDate(LocalDate.now().plusMonths(i));
                detail.setAmountDue(monthlyAmount);
                detail.setCollectedAmount(BigDecimal.ZERO);
                detail.setPaid(false);
                details.add(detail);
            }
            installmentSale.setDetails(details);
            installmentSaleRepository.save(installmentSale);

            if (customer.getTotalDebt() == null) {
                customer.setTotalDebt(BigDecimal.ZERO);
            }
            customer.setTotalDebt(customer.getTotalDebt().add(totalAmount));
            customerRepository.save(customer);

        } else {
            // CASH (လက်ငင်း) စနစ်ဖြစ်ပါက တိုက်ရိုက် ငွေအပြည့်အဝရသဖြင့် Payment Table ထဲသို့ သိမ်းဆည်းမည်
            Payment payment = new Payment();
            payment.setSale(sale);
            payment.setCustomer(customer);
            payment.setAmount(totalAmount);
            payment.setTransactionType("INVOICE");
            payment.setPaymentMethod("CASH");
            payment.setPaymentDate(LocalDateTime.now());
            payment.setNote("ဘောက်ချာ " + sale.getInvoiceNumber() + " အတွက် လက်ငင်းငွေအပြည့်အဝချေမှု");

            paymentRepository.save(payment);
        }

        return sale;
    }
}