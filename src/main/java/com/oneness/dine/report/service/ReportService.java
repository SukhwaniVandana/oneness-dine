package com.oneness.dine.report.service;

import com.oneness.dine.billing.entity.Bill;
import com.oneness.dine.billing.entity.BillPayment;
import com.oneness.dine.billing.repository.BillPaymentRepository;
import com.oneness.dine.billing.repository.BillRepository;
import com.oneness.dine.core.enums.BillStatus;
import com.oneness.dine.core.enums.OrderStatus;
import com.oneness.dine.core.enums.PaymentMethod;
import com.oneness.dine.order.entity.CustomerOrder;
import com.oneness.dine.order.entity.OrderItem;
import com.oneness.dine.order.repository.CustomerOrderRepository;
import com.oneness.dine.order.repository.OrderItemRepository;
import com.oneness.dine.report.dto.*;
import com.oneness.dine.report.entity.ActivityLog;
import com.oneness.dine.report.entity.DailySummary;
import com.oneness.dine.report.repository.ActivityLogRepository;
import com.oneness.dine.report.repository.DailySummaryRepository;
import com.oneness.dine.restaurant.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final DailySummaryRepository  summaryRepository;
    private final ActivityLogRepository   activityLogRepository;
    private final BillRepository          billRepository;
    private final BillPaymentRepository   paymentRepository;
    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository     orderItemRepository;
    private final BranchRepository        branchRepository;

    // ─── DAILY SUMMARY ───────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DailySummaryDto generateDailySummary(Long branchId, LocalDate date) {
        LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIDNIGHT);
        LocalDateTime endOfDay   = LocalDateTime.of(date, LocalTime.MAX);

        // Performance: fetch bills once and compute everything in memory
        List<Bill> bills = billRepository.findByBranchIdAndCreatedAtBetween(branchId, startOfDay, endOfDay)
                .stream()
                .filter(b -> b.getBillStatus() == BillStatus.PAID)
                .collect(Collectors.toList());

        List<CustomerOrder> orders = orderRepository
                .findByBranchIdAndCreatedAtBetween(branchId, startOfDay, endOfDay);

        // Calculate totals
        BigDecimal totalRevenue  = bills.stream()
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscount = bills.stream()
                .map(b -> b.getItemDiscount().add(b.getOrderDiscount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTax      = bills.stream()
                .map(b -> b.getCgstAmount().add(b.getSgstAmount())
                        .add(b.getIgstAmount()).add(b.getCessAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Payment method breakdown
        // Performance: one query per bill avoided by fetching all payments
        List<BillPayment> allPayments = bills.stream()
                .flatMap(b -> paymentRepository.findByBillId(b.getId()).stream())
                .collect(Collectors.toList());

        BigDecimal cashCollected = sumByMethod(allPayments, PaymentMethod.CASH);
        BigDecimal cardCollected = sumByMethod(allPayments, PaymentMethod.CARD);
        BigDecimal upiCollected  = sumByMethod(allPayments, PaymentMethod.UPI);

        // Order type breakdown
        long dineIn    = orders.stream().filter(o ->
                o.getOrderType().name().equals("DINE_IN")).count();
        long takeaway  = orders.stream().filter(o ->
                o.getOrderType().name().equals("TAKEAWAY")).count();
        long online    = orders.stream().filter(o ->
                o.getOrderType().name().equals("ONLINE")).count();
        long cancelled = orders.stream().filter(o ->
                o.getOrderStatus() == OrderStatus.CANCELLED).count();

        BigDecimal avgOrderValue = bills.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(
                        BigDecimal.valueOf(bills.size()),
                        2, RoundingMode.HALF_UP);

        // Upsert daily summary
        DailySummary summary = summaryRepository
                .findByBranchIdAndSummaryDate(branchId, date)
                .orElse(new DailySummary());

        summary.setBranch(branchRepository.findById(branchId)
                .orElseThrow());
        summary.setSummaryDate(date);
        summary.setTotalOrders((int) orders.stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                .count());
        summary.setDineInOrders((int) dineIn);
        summary.setTakeawayOrders((int) takeaway);
        summary.setOnlineOrders((int) online);
        summary.setTotalRevenue(totalRevenue);
        summary.setCashCollected(cashCollected);
        summary.setCardCollected(cardCollected);
        summary.setUpiCollected(upiCollected);
        summary.setTotalDiscount(totalDiscount);
        summary.setTotalTax(totalTax);
        summary.setCancelledOrders((int) cancelled);
        summary.setAvgOrderValue(avgOrderValue);

        DailySummary saved = summaryRepository.save(summary);
        log.info("Daily summary generated for branch {} date {}",
                branchId, date);
        return toSummaryDto(saved);
    }

    @Transactional
    public DailySummaryDto getDailySummary(Long branchId, LocalDate date) {
        // Always regenerate for today — data changes throughout the day
        // For past dates — return existing record if available (immutable)
        if (date.equals(LocalDate.now())) {
            return generateDailySummary(branchId, date); // always fresh for today
        }
        return summaryRepository.findByBranchIdAndSummaryDate(branchId, date)
                .map(this::toSummaryDto)
                .orElseGet(() -> generateDailySummary(branchId, date));
    }

    // ─── SALES REPORT ────────────────────────────────

    @Transactional
    public SalesReportDto getSalesReport(Long branchId,
                                          LocalDate from, LocalDate to) {
        // Backfill any missing daily summaries in the range
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            final LocalDate d = cursor;
            boolean exists = summaryRepository
                    .findByBranchIdAndSummaryDate(branchId, d).isPresent();
            if (!exists || d.equals(LocalDate.now())) {
                generateDailySummary(branchId, d);
            }
            cursor = cursor.plusDays(1);
        }
        List<DailySummary> dailies = summaryRepository
                .findByBranchIdAndDateRange(branchId, from, to);

        // Performance: aggregate in memory from already fetched data
        BigDecimal totalRevenue  = dailies.stream()
                .map(DailySummary::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscount = dailies.stream()
                .map(DailySummary::getTotalDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTax      = dailies.stream()
                .map(DailySummary::getTotalTax)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalOrders          = dailies.stream()
                .mapToInt(DailySummary::getTotalOrders).sum();
        int cancelledOrders      = dailies.stream()
                .mapToInt(DailySummary::getCancelledOrders).sum();

        BigDecimal avgOrderValue = totalOrders == 0 ? BigDecimal.ZERO :
                totalRevenue.divide(
                        BigDecimal.valueOf(totalOrders),
                        2, RoundingMode.HALF_UP);

        return SalesReportDto.builder()
                .branchId(branchId)
                .from(from)
                .to(to)
                .totalDays(dailies.size())
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalDiscount(totalDiscount)
                .totalTax(totalTax)
                .avgOrderValue(avgOrderValue)
                .cancelledOrders(cancelledOrders)
                .dailyBreakdown(dailies.stream()
                        .map(this::toSummaryDto)
                        .collect(Collectors.toList()))
                .build();
    }

    // ─── ITEM WISE SALES ─────────────────────────────

    @Transactional(readOnly = true)
    public List<ItemSalesDto> getItemWiseSales(Long branchId,
                                                LocalDate from, LocalDate to) {
        LocalDateTime start = LocalDateTime.of(from, LocalTime.MIDNIGHT);
        LocalDateTime end   = LocalDateTime.of(to, LocalTime.MAX);

        // Performance: single query joining orders and items
        List<CustomerOrder> orders = orderRepository
                .findByBranchIdAndCreatedAtBetween(branchId, start, end);

        // Group order items by item and aggregate
        Map<Long, List<OrderItem>> itemGroups = orders.stream()
                .flatMap(o -> orderItemRepository
                        .findByOrderId(o.getId()).stream())
                .filter(i -> i.getItemStatus() != OrderStatus.CANCELLED
                        && !i.getIsComplimentary())
                .collect(Collectors.groupingBy(i -> i.getItem().getId()));

        return itemGroups.entrySet().stream().map(entry -> {
            List<OrderItem> items = entry.getValue();
            OrderItem first       = items.get(0);
            int totalQty          = items.stream()
                    .mapToInt(OrderItem::getQuantity).sum();
            BigDecimal totalRev   = items.stream()
                    .map(i -> i.getUnitPriceSnapshot()
                            .multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgPrice   = totalQty == 0 ? BigDecimal.ZERO :
                    totalRev.divide(BigDecimal.valueOf(totalQty),
                            2, RoundingMode.HALF_UP);

            return ItemSalesDto.builder()
                    .itemId(entry.getKey())
                    .itemName(first.getItemNameSnapshot())
                    .categoryName(first.getItem().getCategory().getName())
                    .quantitySold(totalQty)
                    .totalRevenue(totalRev)
                    .avgPrice(avgPrice)
                    .build();
        })
        .sorted(Comparator.comparing(
                ItemSalesDto::getTotalRevenue).reversed())
        .collect(Collectors.toList());
    }

    // ─── PAYMENT REPORT ──────────────────────────────

    @Transactional(readOnly = true)
    public PaymentReportDto getPaymentReport(Long branchId, LocalDate from, LocalDate to) {
        LocalDateTime startOfDay = LocalDateTime.of(from, LocalTime.MIDNIGHT);
        LocalDateTime endOfDay   = LocalDateTime.of(to, LocalTime.MAX);

        List<Bill> bills = billRepository
                .findByBranchIdAndCreatedAtBetween(branchId, startOfDay, endOfDay)
                .stream()
                .filter(b -> b.getBillStatus() == BillStatus.PAID)
                .collect(Collectors.toList());

        List<BillPayment> payments = bills.stream()
                .flatMap(b -> paymentRepository.findByBillId(b.getId()).stream())
                .collect(Collectors.toList());

        BigDecimal cash   = sumByMethod(payments, PaymentMethod.CASH);
        BigDecimal card   = sumByMethod(payments, PaymentMethod.CARD);
        BigDecimal upi    = sumByMethod(payments, PaymentMethod.UPI);
        BigDecimal wallet = sumByMethod(payments, PaymentMethod.WALLET);
        BigDecimal other  = payments.stream()
                .filter(p -> !List.of(PaymentMethod.CASH,
                        PaymentMethod.CARD, PaymentMethod.UPI,
                        PaymentMethod.WALLET).contains(p.getPaymentMethod()))
                .map(BillPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PaymentReportDto.builder()
                .branchId(branchId)
                .fromDate(from)
                .toDate(to)
                .cashTotal(cash)
                .cardTotal(card)
                .upiTotal(upi)
                .walletTotal(wallet)
                .otherTotal(other)
                .grandTotal(cash.add(card).add(upi).add(wallet).add(other))
                .totalTransactions(payments.size())
                .build();
    }

    // ─── ACTIVITY LOG ────────────────────────────────

    @Transactional(readOnly = true)
    public List<ActivityLogDto> getActivityLog(Long branchId,
                                                LocalDate from,
                                                LocalDate to,
                                                int page) {
        LocalDateTime start = LocalDateTime.of(from, LocalTime.MIDNIGHT);
        LocalDateTime end   = LocalDateTime.of(to, LocalTime.MAX);

        return activityLogRepository.findByBranchAndDateRange(
                branchId, start, end, PageRequest.of(page, 50))
                .stream().map(this::toActivityDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void logActivity(Long branchId, Long userId, Long sessionId,
                             String action, String entityType,
                             Long entityId, String oldValue,
                             String newValue, String ipAddress) {
        ActivityLog log = new ActivityLog();
        if (branchId != null) {
            branchRepository.findById(branchId)
                    .ifPresent(log::setBranch);
        }
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        activityLogRepository.save(log);
    }

    // ─── CANCELLED ORDERS ────────────────────────────

    @Transactional(readOnly = true)
    public List<CancelledOrderDto> getCancelledOrders(Long branchId,
                                                       LocalDate date) {
        LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIDNIGHT);
        return orderRepository.findTodaysOrders(branchId, startOfDay)
                .stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.CANCELLED)
                .map(o -> CancelledOrderDto.builder()
                        .orderId(o.getId())
                        .orderNumber(o.getOrderNumber())
                        .tableNumber(o.getTable() != null ?
                                o.getTable().getTableNumber() : "N/A")
                        .cancelReason(o.getCancelReason())
                        .cancelledAt(o.getCancelledAt())
                        .cancelledBy(o.getCancelledByUser() != null ?
                                o.getCancelledByUser().getFullName() : null)
                        .build())
                .collect(Collectors.toList());
    }

    // ─── HELPER ──────────────────────────────────────

    private BigDecimal sumByMethod(List<BillPayment> payments,
                                    PaymentMethod method) {
        return payments.stream()
                .filter(p -> p.getPaymentMethod() == method)
                .map(BillPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ─── MAPPERS ─────────────────────────────────────

    private DailySummaryDto toSummaryDto(DailySummary s) {
        return DailySummaryDto.builder()
                .id(s.getId())
                .branchId(s.getBranch().getId())
                .summaryDate(s.getSummaryDate())
                .totalOrders(s.getTotalOrders())
                .dineInOrders(s.getDineInOrders())
                .takeawayOrders(s.getTakeawayOrders())
                .onlineOrders(s.getOnlineOrders())
                .totalRevenue(s.getTotalRevenue())
                .cashCollected(s.getCashCollected())
                .cardCollected(s.getCardCollected())
                .upiCollected(s.getUpiCollected())
                .totalDiscount(s.getTotalDiscount())
                .totalTax(s.getTotalTax())
                .cancelledOrders(s.getCancelledOrders())
                .newCustomers(s.getNewCustomers())
                .avgOrderValue(s.getAvgOrderValue())
                .generatedAt(s.getGeneratedAt())
                .build();
    }

    private ActivityLogDto toActivityDto(ActivityLog a) {
        return ActivityLogDto.builder()
                .id(a.getId())
                .branchId(a.getBranch() != null ? a.getBranch().getId() : null)
                .userId(a.getUser() != null ? a.getUser().getId() : null)
                .userName(a.getUser() != null ? a.getUser().getFullName() : null)
                .sessionId(a.getSession() != null ? a.getSession().getId() : null)
                .action(a.getAction())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .oldValue(a.getOldValue())
                .newValue(a.getNewValue())
                .ipAddress(a.getIpAddress())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
