package com.oneness.dine.billing.service;

import com.oneness.dine.auth.repository.UserRepository;
import com.oneness.dine.billing.dto.*;
import com.oneness.dine.billing.entity.Bill;
import com.oneness.dine.billing.entity.BillPayment;
import com.oneness.dine.billing.repository.BillPaymentRepository;
import com.oneness.dine.billing.repository.BillRepository;
import com.oneness.dine.billing.repository.BillSplitRepository;
import com.oneness.dine.core.enums.BillStatus;
import com.oneness.dine.core.enums.OrderStatus;
import com.oneness.dine.core.enums.PaymentStatus;
import com.oneness.dine.core.enums.TableStatus;
import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.customer.entity.Customer;
import com.oneness.dine.customer.repository.CustomerRepository;
import com.oneness.dine.order.entity.CustomerOrder;
import com.oneness.dine.order.entity.OrderItem;
import com.oneness.dine.order.repository.CustomerOrderRepository;
import com.oneness.dine.order.repository.OrderItemRepository;
import com.oneness.dine.restaurant.repository.BranchSettingsRepository;
import com.oneness.dine.table.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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
    // 1 point per 100 rupees spent
    private Integer calculateLoyaltyPoints(BigDecimal totalAmount) {
        return totalAmount.divide(
                BigDecimal.valueOf(100), 0, RoundingMode.FLOOR).intValue();
    }

    // ─── GENERATE BILL ─────────────────────────────────
    @Transactional
    public BillDto generateBill(BillRequest request) {

        if (billRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new BusinessException(
                    "Bill already generated for this order",
                    "BILL_ALREADY_EXISTS");
        }

        CustomerOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", request.getOrderId()));

        if (order.getIsSettled()) {
            throw new BusinessException(
                    "Order is already settled", "ORDER_SETTLED");
        }

        // Performance: single query, filter in memory
        List<OrderItem> items = orderItemRepository
                .findByOrderId(order.getId())
                .stream()
                .filter(i -> i.getItemStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            throw new BusinessException(
                    "No items found for billing", "NO_ITEMS");
        }

        // ── Calculate subtotal ──────────────────────────
        BigDecimal subtotal = items.stream()
                .filter(i -> !i.getIsComplimentary())
                .map(i -> i.getUnitPriceSnapshot()
                        .multiply(BigDecimal.valueOf(i.getQuantity()))
                        .subtract(i.getDiscountAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal itemDiscount = items.stream()
                .map(OrderItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal orderDiscount = request.getOrderDiscount() != null ?
                request.getOrderDiscount() : BigDecimal.ZERO;

        // ── Service charge from branch settings ─────────
        BigDecimal serviceChargePct = branchSettingsRepository
                .findByBranchId(order.getBranch().getId())
                .map(s -> s.getServiceChargePct())
                .orElse(BigDecimal.ZERO);

        BigDecimal taxableAmount = subtotal.subtract(orderDiscount);

        BigDecimal serviceCharge = taxableAmount
                .multiply(serviceChargePct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // ── Calculate GST per item tax group ────────────
        BigDecimal cgstTotal = BigDecimal.ZERO;
        BigDecimal sgstTotal = BigDecimal.ZERO;
        BigDecimal igstTotal = BigDecimal.ZERO;
        BigDecimal cessTotal = BigDecimal.ZERO;

        for (OrderItem item : items) {
            if (item.getIsComplimentary()) continue;
            if (item.getItem().getTaxGroup() == null) continue;

            BigDecimal itemTotal = item.getUnitPriceSnapshot()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .subtract(item.getDiscountAmount());

            var tg = item.getItem().getTaxGroup();
            cgstTotal = cgstTotal.add(itemTotal.multiply(tg.getCgstPct())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            sgstTotal = sgstTotal.add(itemTotal.multiply(tg.getSgstPct())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            igstTotal = igstTotal.add(itemTotal.multiply(tg.getIgstPct())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            cessTotal = cessTotal.add(itemTotal.multiply(tg.getCessPct())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        }

        BigDecimal packagingCharge = request.getPackagingCharge() != null ?
                request.getPackagingCharge() : BigDecimal.ZERO;

        // ── Round off ───────────────────────────────────
        BigDecimal totalBeforeRoundOff = subtotal
                .subtract(orderDiscount)
                .add(cgstTotal).add(sgstTotal)
                .add(igstTotal).add(cessTotal)
                .add(serviceCharge).add(packagingCharge);

        BigDecimal roundedTotal = totalBeforeRoundOff
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal roundOff = roundedTotal.subtract(totalBeforeRoundOff);

        Integer pointsEarned = calculateLoyaltyPoints(roundedTotal);

        // ── Build and save bill ─────────────────────────
        Bill bill = new Bill();
        bill.setOrder(order);
        bill.setBranch(order.getBranch());
        bill.setBillNumber(generateBillNumber(order.getBranch().getId()));
        bill.setSubtotal(subtotal);
        bill.setItemDiscount(itemDiscount);
        bill.setOrderDiscount(orderDiscount);
        bill.setDiscountReason(request.getDiscountReason());
        bill.setCgstAmount(cgstTotal);
        bill.setSgstAmount(sgstTotal);
        bill.setIgstAmount(igstTotal);
        bill.setCessAmount(cessTotal);
        bill.setServiceCharge(serviceCharge);
        bill.setPackagingCharge(packagingCharge);
        bill.setRoundOff(roundOff);
        bill.setTotalAmount(roundedTotal);
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

        // Update order status
        order.setIsPrinted(true);
        order.setSaveAndPrintAt(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.BILLED);
        orderRepository.save(order);

        log.info("Bill generated: {} for order: {}",
                savedBill.getBillNumber(), order.getOrderNumber());
        return toBillDto(savedBill, items);
    }

    // ─── GET BILL ──────────────────────────────────────
    @Transactional(readOnly = true)
    public BillDto getBillById(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill", "id", billId));
        return toBillDto(bill,
                orderItemRepository.findByOrderId(bill.getOrder().getId()));
    }

    @Transactional(readOnly = true)
    public BillDto getBillByOrder(Long orderId) {
        Bill bill = billRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill", "orderId", orderId));
        return toBillDto(bill, orderItemRepository.findByOrderId(orderId));
    }

    @Transactional(readOnly = true)
    public List<BillDto> getTodaysBills(Long branchId) {
        LocalDateTime startOfDay = LocalDateTime.of(
                LocalDate.now(), LocalTime.MIDNIGHT);
        return billRepository.findTodaysBills(branchId, startOfDay)
                .stream()
                .map(bill -> toBillDto(bill,
                        orderItemRepository.findByOrderId(
                                bill.getOrder().getId())))
                .collect(Collectors.toList());
    }

    // ─── PRINT BILL ────────────────────────────────────
    @Transactional
    public BillDto printBill(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill", "id", billId));
        bill.setBillStatus(BillStatus.PRINTED);
        bill.setPrintedAt(LocalDateTime.now());
        log.info("Bill printed: {}", bill.getBillNumber());
        return toBillDto(billRepository.save(bill),
                orderItemRepository.findByOrderId(bill.getOrder().getId()));
    }

    // ─── APPLY DISCOUNT ────────────────────────────────
    @Transactional
    public BillDto applyDiscount(Long billId, ApplyDiscountRequest request) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill", "id", billId));

        if (bill.getBillStatus() == BillStatus.PAID) {
            throw new BusinessException(
                    "Cannot apply discount to paid bill",
                    "BILL_ALREADY_PAID");
        }

        bill.setOrderDiscount(request.getDiscountAmount());
        bill.setDiscountReason(request.getDiscountReason());

        BigDecimal newTotal = bill.getSubtotal()
                .subtract(request.getDiscountAmount())
                .add(bill.getCgstAmount()).add(bill.getSgstAmount())
                .add(bill.getIgstAmount()).add(bill.getCessAmount())
                .add(bill.getServiceCharge()).add(bill.getPackagingCharge());

        BigDecimal roundedTotal = newTotal.setScale(0, RoundingMode.HALF_UP);
        bill.setRoundOff(roundedTotal.subtract(newTotal));
        bill.setTotalAmount(roundedTotal);

        log.info("Discount applied to bill: {}", bill.getBillNumber());
        return toBillDto(billRepository.save(bill),
                orderItemRepository.findByOrderId(bill.getOrder().getId()));
    }

    // ─── SETTLE BILL ───────────────────────────────────
    @Transactional
    public BillDto settleBill(Long billId, SettleRequest request) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill", "id", billId));

        if (bill.getBillStatus() == BillStatus.PAID) {
            throw new BusinessException(
                    "Bill is already paid", "BILL_ALREADY_PAID");
        }

        // Validate total payment
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

        // Save payment records
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

        Integer pointsUsed = request.getLoyaltyPointsToUse() != null ?
                request.getLoyaltyPointsToUse() : 0;

        bill.setPaidAmount(totalPaid);
        bill.setChangeReturned(change);
        bill.setBillStatus(BillStatus.PAID);
        bill.setPaidAt(LocalDateTime.now());
        bill.setLoyaltyPointsUsed(pointsUsed);
        Bill saved = billRepository.save(bill);

        // Settle order
        CustomerOrder order = bill.getOrder();
        order.setIsSettled(true);
        order.setOrderStatus(OrderStatus.BILLED);
        orderRepository.save(order);

        // Free table if no more active orders
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

        // Update customer stats
        if (bill.getCustomer() != null) {
            Customer customer = bill.getCustomer();
            customer.setLoyaltyPointsBalance(
                    customer.getLoyaltyPointsBalance()
                    + bill.getLoyaltyPointsEarned() - pointsUsed);
            customer.setTotalVisits(customer.getTotalVisits() + 1);
            customer.setTotalSpent(
                    customer.getTotalSpent().add(bill.getTotalAmount()));
            customerRepository.save(customer);
        }

        log.info("Bill settled: {} — Paid: {}, Change: {}",
                bill.getBillNumber(), totalPaid, change);
        return toBillDto(saved,
                orderItemRepository.findByOrderId(order.getId()));
    }

    // ─── VOID BILL ─────────────────────────────────────
    @Transactional
    public BillDto voidBill(Long billId, Long userId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill", "id", billId));

        if (bill.getBillStatus() == BillStatus.PAID) {
            throw new BusinessException(
                    "Cannot void a paid bill. Use refund instead.",
                    "BILL_ALREADY_PAID");
        }

        bill.setBillStatus(BillStatus.VOIDED);
        bill.setDeletedAt(LocalDateTime.now());
        if (userId != null) {
            userRepository.findById(userId).ifPresent(bill::setDeletedByUser);
        }

        log.info("Bill voided: {}", bill.getBillNumber());
        return toBillDto(billRepository.save(bill),
                orderItemRepository.findByOrderId(bill.getOrder().getId()));
    }

    // ─── MAPPER ────────────────────────────────────────
    private BillDto toBillDto(Bill bill, List<OrderItem> orderItems) {
        List<BillItemDto> items = orderItems.stream()
                .map(i -> BillItemDto.builder()
                        .orderItemId(i.getId())
                        .itemName(i.getItemNameSnapshot())
                        .variantName(i.getVariant() != null ?
                                i.getVariant().getVariantName() : null)
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPriceSnapshot())
                        .discount(i.getDiscountAmount())
                        .totalPrice(i.getIsComplimentary() ? BigDecimal.ZERO :
                                i.getUnitPriceSnapshot()
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
                .customerId(bill.getCustomer() != null ?
                        bill.getCustomer().getId() : null)
                .customerName(bill.getCustomer() != null ?
                        bill.getCustomer().getName() : null)
                .customerPhone(bill.getCustomer() != null ?
                        bill.getCustomer().getPhone() : null)
                .subtotal(bill.getSubtotal())
                .itemDiscount(bill.getItemDiscount())
                .orderDiscount(bill.getOrderDiscount())
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
}
