package com.oneness.dine.customer.service;

import com.oneness.dine.auth.repository.UserRepository;
import com.oneness.dine.billing.entity.Bill;
import com.oneness.dine.billing.repository.BillRepository;
import com.oneness.dine.core.enums.BillStatus;
import com.oneness.dine.core.enums.CreditTxnType;
import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.customer.dto.CollectCreditRequest;
import com.oneness.dine.customer.dto.CreditLedgerEntryDto;
import com.oneness.dine.customer.dto.DueCustomerDto;
import com.oneness.dine.customer.entity.Customer;
import com.oneness.dine.customer.entity.CustomerCreditLedger;
import com.oneness.dine.customer.repository.CustomerCreditLedgerRepository;
import com.oneness.dine.customer.repository.CustomerRepository;
import com.oneness.dine.restaurant.entity.Branch;
import com.oneness.dine.restaurant.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditLedgerService {

    private final CustomerCreditLedgerRepository ledgerRepository;
    private final CustomerRepository             customerRepository;
    private final BillRepository                 billRepository;
    private final BranchRepository               branchRepository;
    private final UserRepository                 userRepository;

    // ─── CALLED BY BillingService on credit bill settle ───────────────────
    /**
     * Creates a DEBIT ledger entry when a bill is settled with CREDIT payment.
     * Called inside BillingService.settleBill() within the same transaction.
     *
     * @param bill        the bill being settled
     * @param creditAmount the portion paid by CREDIT method
     */
    @Transactional
    public void recordCreditDebit(Bill bill, BigDecimal creditAmount) {
        Customer customer = bill.getCustomer();
        if (customer == null) {
            log.warn("Cannot record credit DEBIT: bill {} has no customer", bill.getBillNumber());
            return;
        }

        BigDecimal previousBalance = ledgerRepository.findCurrentBalance(customer.getId());
        if (previousBalance == null) previousBalance = BigDecimal.ZERO;

        BigDecimal newBalance = previousBalance.add(creditAmount);

        CustomerCreditLedger entry = new CustomerCreditLedger();
        entry.setCustomer(customer);
        entry.setBranch(bill.getBranch());
        entry.setBill(bill);
        entry.setTxnType(CreditTxnType.DEBIT);
        entry.setAmount(creditAmount);
        entry.setRemainingCredit(creditAmount);   // tracks uncollected portion; decremented by adjustBillStatuses
        entry.setBalanceAfter(newBalance);
        entry.setRemarks("Bill " + bill.getBillNumber() + " — credit payment");

        ledgerRepository.save(entry);
        log.info("Credit DEBIT recorded: customer={} bill={} amount={} balance={}",
                customer.getId(), bill.getBillNumber(), creditAmount, newBalance);
    }

    // ─── COLLECT PAYMENT ──────────────────────────────────────────────────
    /**
     * Collects a payment from a customer against their outstanding credit balance.
     * Automatically adjusts the oldest unpaid bills first (CREDIT_DUE → PAID / PARTIAL_CREDIT).
     */
    @Transactional
    public CreditLedgerEntryDto collectPayment(Long branchId, CollectCreditRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "id", request.getCustomerId()));

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));

        BigDecimal previousBalance = ledgerRepository.findCurrentBalance(customer.getId());
        if (previousBalance == null) previousBalance = BigDecimal.ZERO;

        if (previousBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "Customer has no outstanding balance to collect", "NO_CREDIT_DUE");
        }

        BigDecimal collectAmount = request.getAmount();
//        if (collectAmount.compareTo(previousBalance) > 0) {
//            throw new BusinessException(
//                    String.format("Collection amount (%.2f) exceeds outstanding balance (%.2f)",
//                            collectAmount, previousBalance),
//                    "EXCEEDS_BALANCE");
//        }

        BigDecimal newBalance = previousBalance.subtract(collectAmount);

        // Create the CREDIT ledger entry
        CustomerCreditLedger entry = new CustomerCreditLedger();
        entry.setCustomer(customer);
        entry.setBranch(branch);
        entry.setBill(null); // collection is account-level, not tied to one bill
        entry.setTxnType(CreditTxnType.CREDIT);
        entry.setAmount(collectAmount);
        entry.setBalanceAfter(newBalance);
        entry.setRemarks(request.getRemarks() != null && !request.getRemarks().isBlank()
                ? request.getRemarks() : "Credit payment collected");

        if (request.getCollectedByUserId() != null) {
            userRepository.findById(request.getCollectedByUserId())
                    .ifPresent(entry::setCollectedBy);
        }

        CustomerCreditLedger saved = ledgerRepository.save(entry);

        // Auto-adjust oldest unpaid bills: mark them PAID or PARTIAL_CREDIT
        adjustBillStatuses(customer, collectAmount);

        log.info("Credit COLLECTED: customer={} amount={} newBalance={}",
                customer.getId(), collectAmount, newBalance);

        return toDto(saved);
    }

    // ─── GET CUSTOMER LEDGER ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<CreditLedgerEntryDto> getCustomerLedger(Long customerId, String from, String to) {
        LocalDateTime fromDt = LocalDate.parse(from).atStartOfDay();
        LocalDateTime toDt   = LocalDate.parse(to).atTime(LocalTime.MAX);

        return ledgerRepository
                .findByCustomerIdAndDateRange(customerId, fromDt, toDt)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ─── GET OUTSTANDING BALANCE ──────────────────────────────────────────
    @Transactional(readOnly = true)
    public BigDecimal getOutstandingBalance(Long customerId) {
        BigDecimal balance = ledgerRepository.findCurrentBalance(customerId);
        return balance != null ? balance : BigDecimal.ZERO;
    }

    // ─── GET ALL CUSTOMERS WITH LEDGER ACTIVITY ───────────────────────────
    @Transactional(readOnly = true)
    public List<DueCustomerDto> getDueCustomers(Long branchId, String from, String to) {
        LocalDateTime fromDt = LocalDate.parse(from).atStartOfDay();
        LocalDateTime toDt   = LocalDate.parse(to).atTime(LocalTime.MAX);

        return ledgerRepository.findDueCustomerSummary(branchId, fromDt, toDt)
                .stream()
                .map(row -> DueCustomerDto.builder()
                        .customerId(((Number) row[0]).longValue())
                        .customerName((String) row[1])
                        .customerPhone((String) row[2])
                        .outstandingBalance(new BigDecimal(row[3].toString()))
                        .billCount(((Number) row[4]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── INTERNAL: auto-adjust bill statuses ──────────────────────────────
    /**
     * After a collection, walk oldest CREDIT_DUE / PARTIAL_CREDIT bills for this customer
     * and flip them to PAID once the collected amount covers them, or PARTIAL_CREDIT if partial.
     */
    private void adjustBillStatuses(Customer customer, BigDecimal collected) {
        List<CustomerCreditLedger> debitEntries =
                ledgerRepository.findUnpaidDebitEntriesOldestFirst(customer.getId());

        BigDecimal remaining = collected;

        for (CustomerCreditLedger debit : debitEntries) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            Bill bill = debit.getBill();
            if (bill == null) continue;

            // Reload fresh from DB to get current status
            bill = billRepository.findById(bill.getId()).orElse(null);
            if (bill == null) continue;

            if (bill.getBillStatus() != BillStatus.CREDIT_DUE
                    && bill.getBillStatus() != BillStatus.PARTIAL_CREDIT) {
                continue;
            }

            // Credit amount for this bill = the debit entry amount
            // NEW:
            BigDecimal billCreditAmount = debit.getRemainingCredit() != null
                    ? debit.getRemainingCredit()
                    : debit.getAmount();

            if (remaining.compareTo(billCreditAmount) >= 0) {
                bill.setBillStatus(BillStatus.PAID);
                debit.setRemainingCredit(BigDecimal.ZERO);
                remaining = remaining.subtract(billCreditAmount);
            } else {
                bill.setBillStatus(BillStatus.PARTIAL_CREDIT);
                debit.setRemainingCredit(billCreditAmount.subtract(remaining));
                remaining = BigDecimal.ZERO;
            }

            ledgerRepository.save(debit);
            billRepository.save(bill);
        }
    }

    // ─── MAPPER ───────────────────────────────────────────────────────────
    private CreditLedgerEntryDto toDto(CustomerCreditLedger e) {
        return CreditLedgerEntryDto.builder()
                .id(e.getId())
                .txnType(e.getTxnType())
                .amount(e.getAmount())
                .balanceAfter(e.getBalanceAfter())
                .billId(e.getBill() != null ? e.getBill().getId() : null)
                .billNumber(e.getBill() != null ? e.getBill().getBillNumber() : null)
                .collectedByName(e.getCollectedBy() != null ? e.getCollectedBy().getFullName() : null)
                .remarks(e.getRemarks())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
