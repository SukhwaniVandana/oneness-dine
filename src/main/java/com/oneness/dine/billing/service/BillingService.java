package com.oneness.dine.billing.service;

import com.oneness.dine.auth.repository.UserRepository;
import com.oneness.dine.billing.dto.*;
import com.oneness.dine.billing.entity.Bill;
import com.oneness.dine.billing.entity.BillPayment;
import com.oneness.dine.billing.repository.BillPaymentRepository;
import com.oneness.dine.billing.repository.BillRepository;
import com.oneness.dine.billing.repository.BillSplitRepository;
import com.oneness.dine.core.enums.*;
import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.customer.entity.Customer;
import com.oneness.dine.customer.repository.CustomerRepository;
import com.oneness.dine.menu.entity.TaxGroup;
import com.oneness.dine.order.entity.CustomerOrder;
import com.oneness.dine.order.entity.OrderItem;
import com.oneness.dine.order.repository.CustomerOrderRepository;
import com.oneness.dine.order.repository.OrderItemRepository;
import com.oneness.dine.restaurant.repository.BranchSettingsRepository;
import com.oneness.dine.table.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillRepository            billRepository;
    private final BillPaymentRepository     paymentRepository;
    private final BillSplitRepository       splitRepository;
    private final CustomerOrderRepository   orderRepository;
    private final OrderItemRepository       orderItemRepository;
    private final BranchSettingsRepository  branchSettingsRepository;
    private final CustomerRepository        customerRepository;
    private final UserRepository            userRepository;
    private final RestaurantTableRepository tableRepository;

    // ─── GENERATE BILL NUMBER ──────────────────────────
    private String generateBillNumber(Long branchId) {
        String date = LocalDate.now().toString().replace("-", "");
        long count  = billRepository.count() + 1;
        return String.format("INV-%s-%04d", date, count);
    }

    // ─── CALCULATE LOYALTY POINTS ──────────────────────
    private Integer calculateLoyaltyPoints(BigDecimal totalAmount) {
        return totalAmount.divide(
                BigDecimal.valueOf(100), 0, RoundingMode.FLOOR).intValue();
    }

    // ─── CORE CALCULATION ──────────────────────────────
    /*
     * Item-wise calculation (discount first, then GST):
     *
     * For each item:
     *   itemSubtotal   = unitPrice × quantity
     *   itemTaxable    = itemSubtotal × (1 - discountPct/100)
     *   itemCGST       = itemTaxable × cgstPct/100
     *   itemSGST       = itemTaxable × sgstPct/100
     *   itemIGST       = itemTaxable × igstPct/100
     *   itemCess       = itemTaxable × cessPct/100
     *   itemTotal      = itemTaxable + itemCGST + itemSGST + itemIGST + itemCess
     *
     * Order totals:
     *   subtotal       = Σ itemSubtotal
     *   orderDiscount  = Σ (itemSubtotal - itemTaxable)  [= subtotal × discountPct/100]
     *   cgstTotal      = Σ itemCGST
     *   sgstTotal      = Σ itemSGST
     *   igstTotal      = Σ itemIGST
     *   cessTotal      = Σ itemCess
     *   serviceCharge  = (Σ itemTaxable) × serviceChargePct/100
     *   grandTotal     = Σ itemTotal + serviceCharge + packagingCharge + roundOff
     */
    private BillCalculation calculateBill(
            List<OrderItem> items,
            BigDecimal discountPct,
            BigDecimal serviceChargePct,
            BigDecimal packagingCharge,
            GstCalcMode calcMode,
            TaxGroup defaultTaxGroup) {

        BigDecimal subtotal      = BigDecimal.ZERO;
        BigDecimal orderDiscount = BigDecimal.ZERO;
        BigDecimal totalTaxable  = BigDecimal.ZERO;
        BigDecimal cgstTotal     = BigDecimal.ZERO;
        BigDecimal sgstTotal     = BigDecimal.ZERO;
        BigDecimal igstTotal     = BigDecimal.ZERO;
        BigDecimal cessTotal     = BigDecimal.ZERO;

        for (OrderItem item : items) {
            if (item.getIsComplimentary()) continue;

            // itemSubtotal — before any discount
            BigDecimal itemSubtotal = item.getUnitPriceSnapshot()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .subtract(item.getDiscountAmount()); // item-level discount if any

            // itemTaxable — after order-level discount percentage
            BigDecimal itemTaxable = itemSubtotal
                    .multiply(BigDecimal.ONE.subtract(
                            discountPct.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);

            subtotal      = subtotal.add(itemSubtotal);
            orderDiscount = orderDiscount.add(itemSubtotal.subtract(itemTaxable));
            totalTaxable  = totalTaxable.add(itemTaxable);

            // GST on taxable amount (CGST+SGST for intra-state OR IGST for inter-state)
            // cess always applies if set
            // Determine which tax group to use
            var tg = (calcMode == GstCalcMode.ITEM_WISE)
                    ? item.getItem().getTaxGroup()
                    : null; // bill-wise: skip per-item GST, handled below

            if (tg != null) {
                cgstTotal = cgstTotal.add(itemTaxable.multiply(tg.getCgstPct())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                sgstTotal = sgstTotal.add(itemTaxable.multiply(tg.getSgstPct())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                igstTotal = igstTotal.add(itemTaxable.multiply(tg.getIgstPct())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                cessTotal = cessTotal.add(itemTaxable.multiply(tg.getCessPct())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            }
        }
        // Bill-wise GST — apply default tax group on total taxable amount
        if (calcMode == GstCalcMode.BILL_WISE && defaultTaxGroup != null) {
            cgstTotal = totalTaxable.multiply(defaultTaxGroup.getCgstPct())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            sgstTotal = totalTaxable.multiply(defaultTaxGroup.getSgstPct())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            igstTotal = totalTaxable.multiply(defaultTaxGroup.getIgstPct())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            cessTotal = totalTaxable.multiply(defaultTaxGroup.getCessPct())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // Service charge on total taxable amount
        BigDecimal serviceCharge = totalTaxable
                .multiply(serviceChargePct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Grand total before round off
        BigDecimal totalBeforeRoundOff = totalTaxable
                .add(cgstTotal).add(sgstTotal)
                .add(igstTotal).add(cessTotal)
                .add(serviceCharge)
                .add(packagingCharge != null ? packagingCharge : BigDecimal.ZERO);

        BigDecimal roundedTotal = totalBeforeRoundOff.setScale(0, RoundingMode.HALF_UP);
        BigDecimal roundOff     = roundedTotal.subtract(totalBeforeRoundOff);

        return new BillCalculation(
                subtotal, orderDiscount, totalTaxable,
                cgstTotal, sgstTotal, igstTotal, cessTotal,
                serviceCharge, roundOff, roundedTotal
        );
    }

    // ─── GENERATE BILL ─────────────────────────────────
    @Transactional
    public BillDto generateBill(BillRequest request) {

        if (billRepository.findByOrderIdAndDeletedAtIsNull(request.getOrderId()).isPresent()) {
            throw new BusinessException("Bill already generated for this order", "BILL_ALREADY_EXISTS");
        }

        CustomerOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", request.getOrderId()));

        if (order.getIsSettled()) {
            throw new BusinessException("Order is already settled", "ORDER_SETTLED");
        }

        List<OrderItem> items = orderItemRepository
                .findByOrderId(order.getId())
                .stream()
                .filter(i -> i.getItemStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            throw new BusinessException("No items found for billing", "NO_ITEMS");
        }

        // Initial generation always uses discountPct = 0
        BigDecimal discountPct = BigDecimal.ZERO;

        var settings = branchSettingsRepository.findByBranchId(order.getBranch().getId());
        BigDecimal serviceChargePct = settings.map(s -> s.getServiceChargePct()).orElse(BigDecimal.ZERO);

        BigDecimal packagingCharge = request.getPackagingCharge() != null
                ? request.getPackagingCharge() : BigDecimal.ZERO;

        GstCalcMode calcMode = settings.map(s -> s.getGstCalcMode()).orElse(GstCalcMode.BILL_WISE);
        TaxGroup defaultTaxGroup = settings.map(s -> s.getDefaultTaxGroup()).orElse(null);

        BillCalculation calc = calculateBill(items, discountPct, serviceChargePct, packagingCharge, calcMode, defaultTaxGroup);

        Integer pointsEarned = calculateLoyaltyPoints(calc.totalAmount);

        Bill bill = new Bill();
        bill.setOrder(order);
        bill.setBranch(order.getBranch());
        bill.setBillNumber(generateBillNumber(order.getBranch().getId()));
        bill.setSubtotal(calc.subtotal);
        bill.setItemDiscount(BigDecimal.ZERO);
        bill.setOrderDiscount(calc.orderDiscount);
        bill.setDiscountPct(discountPct);                                // [NEW]
        bill.setDiscountReason(request.getDiscountReason());
        bill.setCgstAmount(calc.cgstTotal);
        bill.setSgstAmount(calc.sgstTotal);
        bill.setIgstAmount(calc.igstTotal);
        bill.setCessAmount(calc.cessTotal);
        bill.setServiceCharge(calc.serviceCharge);
        bill.setPackagingCharge(packagingCharge);
        bill.setRoundOff(calc.roundOff);
        bill.setTotalAmount(calc.totalAmount);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setChangeReturned(BigDecimal.ZERO);
        bill.setBillStatus(BillStatus.DRAFT);
        bill.setIsGstInvoice(request.getIsGstInvoice());
        bill.setLoyaltyPointsEarned(pointsEarned);
        bill.setLoyaltyPointsUsed(0);

        if (request.getCustomerId() != null) {
            customerRepository.findById(request.getCustomerId())
                    .ifPresent(bill::setCustomer);
        } else if (order.getCustomer() != null) {
            bill.setCustomer(order.getCustomer());
        }

        if (request.getCreatedByUserId() != null) {
            userRepository.findById(request.getCreatedByUserId())
                    .ifPresent(bill::setCreatedByUser);
        }

        Bill savedBill = billRepository.save(bill);

        order.setOrderStatus(OrderStatus.BILLED);
        orderRepository.save(order);

        log.info("Bill generated: {} for order: {} (discount: {}%)",
                savedBill.getBillNumber(), order.getOrderNumber(), discountPct);
        return toBillDto(savedBill, items);
    }

    // ─── GET BILL ──────────────────────────────────────
    @Transactional(readOnly = true)
    public BillDto getBillById(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", billId));
        return toBillDto(bill, orderItemRepository.findByOrderId(bill.getOrder().getId()));
    }

    @Transactional(readOnly = true)
    public BillDto getBillByOrder(Long orderId) {
        Bill bill = billRepository.findByOrderIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "orderId", orderId));
        return toBillDto(bill, orderItemRepository.findByOrderId(orderId));
    }

    @Transactional(readOnly = true)
    public List<BillDto> getTodaysBills(Long branchId) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        return billRepository.findTodaysBills(branchId, startOfDay)
                .stream()
                .map(bill -> toBillDto(bill,
                        orderItemRepository.findByOrderId(bill.getOrder().getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillDto> getBillsByDateRange(Long branchId, LocalDate from, LocalDate to) {
        LocalDateTime startOfDay = LocalDateTime.of(from, LocalTime.MIDNIGHT);
        LocalDateTime endOfDay   = LocalDateTime.of(to, LocalTime.MAX);

        List<Bill> bills = billRepository
                .findByBranchIdAndCreatedAtBetween(branchId, startOfDay, endOfDay);

        // Fetch all order items in one query instead of N queries
        List<Long> orderIds = bills.stream()
                .map(b -> b.getOrder().getId())
                .collect(Collectors.toList());

        Map<Long, List<OrderItem>> itemsByOrderId = orderIds.isEmpty()
                ? Collections.emptyMap()
                : orderItemRepository.findByOrderIdIn(orderIds)
                  .stream()
                  .collect(Collectors.groupingBy(i -> i.getOrder().getId()));

        return bills.stream()
                .map(bill -> toBillDto(bill,
                        itemsByOrderId.getOrDefault(bill.getOrder().getId(), List.of())))
                .collect(Collectors.toList());
    }

    // ─── PRINT BILL ────────────────────────────────────
    @Transactional
    public BillDto printBill(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", billId));
        bill.setBillStatus(BillStatus.PRINTED);
        bill.setPrintedAt(LocalDateTime.now());
        log.info("Bill printed: {}", bill.getBillNumber());
        CustomerOrder order = bill.getOrder();
        order.setIsPrinted(true);
        order.setSaveAndPrintAt(LocalDateTime.now());
        orderRepository.save(order);
        return toBillDto(billRepository.save(bill),
                orderItemRepository.findByOrderId(bill.getOrder().getId()));
    }

    // ─── APPLY DISCOUNT ────────────────────────────────
    /*
     * Recalculates entire bill with new discountPct.
     * Called when cashier enters discount % on billing screen.
     * Updates all amounts: discount, taxable, GST, service charge, total.
     */
    @Transactional
    public BillDto applyDiscount(Long billId, ApplyDiscountRequest request) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", billId));

        if (bill.getBillStatus() == BillStatus.PAID) {
            throw new BusinessException(
                    "Cannot apply discount to paid bill", "BILL_ALREADY_PAID");
        }

        BigDecimal discountPct = request.getDiscountPct() != null
                ? request.getDiscountPct() : BigDecimal.ZERO;

        List<OrderItem> items = orderItemRepository
                .findByOrderId(bill.getOrder().getId())
                .stream()
                .filter(i -> i.getItemStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        // Recalculate everything with new discount %
        var settings = branchSettingsRepository.findByBranchId(bill.getBranch().getId());
        BigDecimal serviceChargePct = settings.map(s -> s.getServiceChargePct()).orElse(BigDecimal.ZERO);
        GstCalcMode calcMode = settings.map(s -> s.getGstCalcMode()).orElse(GstCalcMode.BILL_WISE);
        TaxGroup defaultTaxGroup = settings.map(s -> s.getDefaultTaxGroup()).orElse(null);
        BigDecimal packagingCharge = bill.getPackagingCharge() != null ? bill.getPackagingCharge() : BigDecimal.ZERO;

        BillCalculation calc = calculateBill(items, discountPct, serviceChargePct, packagingCharge, calcMode, defaultTaxGroup);

        // Update all bill fields
        bill.setDiscountPct(discountPct);
        bill.setOrderDiscount(calc.orderDiscount);
        bill.setDiscountReason(request.getDiscountReason());
        bill.setCgstAmount(calc.cgstTotal);
        bill.setSgstAmount(calc.sgstTotal);
        bill.setIgstAmount(calc.igstTotal);
        bill.setCessAmount(calc.cessTotal);
        bill.setServiceCharge(calc.serviceCharge);
        bill.setRoundOff(calc.roundOff);
        bill.setTotalAmount(calc.totalAmount);
        bill.setLoyaltyPointsEarned(calculateLoyaltyPoints(calc.totalAmount));

        log.info("Discount {}% applied to bill: {} — new total: {}",
                discountPct, bill.getBillNumber(), calc.totalAmount);
        return toBillDto(billRepository.save(bill), items);
    }

    // ─── SETTLE BILL ───────────────────────────────────
    @Transactional
    public BillDto settleBill(Long billId, SettleRequest request) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", billId));

        if (bill.getBillStatus() == BillStatus.PAID) {
            throw new BusinessException("Bill is already paid", "BILL_ALREADY_PAID");
        }

        BigDecimal totalPaid = request.getPayments().stream()
                .map(PaymentRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(bill.getTotalAmount()) < 0) {
            throw new BusinessException(
                    String.format("Insufficient payment. Required: %.2f, Paid: %.2f",
                            bill.getTotalAmount(), totalPaid),
                    "INSUFFICIENT_PAYMENT");
        }

        BigDecimal change = totalPaid.subtract(bill.getTotalAmount());

        for (PaymentRequest pr : request.getPayments()) {
            BillPayment payment = new BillPayment();
            payment.setBill(bill);
            payment.setPaymentMethod(pr.getPaymentMethod());
            payment.setAmount(pr.getAmount());
            payment.setReferenceNumber(pr.getReferenceNumber());
            payment.setRemarks(pr.getRemarks());
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
        }

        Integer pointsUsed = request.getLoyaltyPointsToUse() != null
                ? request.getLoyaltyPointsToUse() : 0;

        bill.setPaidAmount(totalPaid);
        bill.setChangeReturned(change);
        bill.setBillStatus(BillStatus.PAID);
        bill.setPaidAt(LocalDateTime.now());
        bill.setLoyaltyPointsUsed(pointsUsed);
        Bill saved = billRepository.save(bill);

        CustomerOrder order = bill.getOrder();
        order.setIsSettled(true);
        order.setOrderStatus(OrderStatus.BILLED);
        orderRepository.save(order);

        if (order.getTable() != null) {
            Long activeCount = (long) orderRepository
                    .findByTableIdAndIsSettledFalseAndDeletedAtIsNull(
                            order.getTable().getId()).size();
            if (activeCount == 0) {
                order.getTable().setTableStatus(TableStatus.AVAILABLE);
                order.getTable().setLockedBySession(null);
                order.getTable().setLockedAt(null);
                tableRepository.save(order.getTable());
                log.info("Table {} freed", order.getTable().getTableNumber());
            }
        }

        if (bill.getCustomer() != null) {
            Customer customer = bill.getCustomer();
            customer.setLoyaltyPointsBalance(
                    customer.getLoyaltyPointsBalance()
                    + bill.getLoyaltyPointsEarned() - pointsUsed);
            customer.setTotalVisits(customer.getTotalVisits() + 1);
            customer.setTotalSpent(customer.getTotalSpent().add(bill.getTotalAmount()));
            customerRepository.save(customer);
        }

        log.info("Bill settled: {} — Paid: {}, Change: {}",
                bill.getBillNumber(), totalPaid, change);
        return toBillDto(saved, orderItemRepository.findByOrderId(order.getId()));
    }

    // ─── VOID BILL ─────────────────────────────────────
    @Transactional
    public BillDto voidBill(Long billId, Long userId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", billId));

        if (bill.getBillStatus() == BillStatus.PAID) {
            throw new BusinessException(
                    "Cannot void a paid bill. Use refund instead.", "BILL_ALREADY_PAID");
        }

        bill.setBillStatus(BillStatus.VOIDED);
        bill.setDeletedAt(LocalDateTime.now());
        if (userId != null) {
            userRepository.findById(userId).ifPresent(bill::setDeletedByUser);
        }
        // Reset order so it can be re-billed
        CustomerOrder order = bill.getOrder();
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        orderRepository.save(order);

        log.info("Bill voided: {}", bill.getBillNumber());
        return toBillDto(billRepository.save(bill),
                orderItemRepository.findByOrderId(bill.getOrder().getId()));
    }

    // ─── MAPPER ────────────────────────────────────────
    private BillDto toBillDto(Bill bill, List<OrderItem> orderItems) {
        List<BillItemDto> items = orderItems.stream()
                .filter(i -> i.getItemStatus() != OrderStatus.CANCELLED)
                .map(i -> BillItemDto.builder()
                        .orderItemId(i.getId())
                        .itemName(i.getItemNameSnapshot())
                        .variantName(i.getVariant() != null
                                ? i.getVariant().getVariantName() : null)
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPriceSnapshot())
                        .discount(i.getDiscountAmount())
                        .totalPrice(i.getIsComplimentary() ? BigDecimal.ZERO
                                : i.getUnitPriceSnapshot()
                                        .multiply(BigDecimal.valueOf(i.getQuantity()))
                                        .subtract(i.getDiscountAmount()))
                        .isComplimentary(i.getIsComplimentary())
                        .build())
                .collect(Collectors.toList());

        List<PaymentDto> payments = paymentRepository.findByBillId(bill.getId())
                .stream()
                .map(p -> PaymentDto.builder()
                        .id(p.getId()).billId(bill.getId())
                        .paymentMethod(p.getPaymentMethod())
                        .amount(p.getAmount())
                        .referenceNumber(p.getReferenceNumber())
                        .paymentStatus(p.getPaymentStatus())
                        .paidAt(p.getPaidAt()).remarks(p.getRemarks())
                        .build())
                .collect(Collectors.toList());

        return BillDto.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .orderId(bill.getOrder().getId())
                .orderNumber(bill.getOrder().getOrderNumber())
                .branchId(bill.getBranch().getId())
                .customerId(bill.getCustomer() != null ? bill.getCustomer().getId() : null)
                .customerName(bill.getCustomer() != null ? bill.getCustomer().getName() : null)
                .customerPhone(bill.getCustomer() != null ? bill.getCustomer().getPhone() : null)
                .subtotal(bill.getSubtotal())
                .itemDiscount(bill.getItemDiscount())
                .orderDiscount(bill.getOrderDiscount())
                .discountPct(bill.getDiscountPct())              // [NEW]
                .discountReason(bill.getDiscountReason())
                .cgstAmount(bill.getCgstAmount())
                .sgstAmount(bill.getSgstAmount())
                .igstAmount(bill.getIgstAmount())
                .cessAmount(bill.getCessAmount())
                .serviceCharge(bill.getServiceCharge())
                .packagingCharge(bill.getPackagingCharge())
                .roundOff(bill.getRoundOff())
                .totalAmount(bill.getTotalAmount())
                .paidAmount(bill.getPaidAmount())
                .changeReturned(bill.getChangeReturned())
                .billStatus(bill.getBillStatus())
                .isGstInvoice(bill.getIsGstInvoice())
                .loyaltyPointsUsed(bill.getLoyaltyPointsUsed())
                .loyaltyPointsEarned(bill.getLoyaltyPointsEarned())
                .printedAt(bill.getPrintedAt())
                .paidAt(bill.getPaidAt())
                .createdAt(bill.getCreatedAt())
                .items(items)
                .payments(payments)
                .build();
    }

    // ─── INNER RECORD — calculation result ─────────────
    private record BillCalculation(
            BigDecimal subtotal,
            BigDecimal orderDiscount,
            BigDecimal totalTaxable,
            BigDecimal cgstTotal,
            BigDecimal sgstTotal,
            BigDecimal igstTotal,
            BigDecimal cessTotal,
            BigDecimal serviceCharge,
            BigDecimal roundOff,
            BigDecimal totalAmount
    ) {}
}
