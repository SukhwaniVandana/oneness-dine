package com.oneness.dine.customer.service;

import com.oneness.dine.core.enums.LoyaltyTxnType;
import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.customer.dto.*;
import com.oneness.dine.customer.entity.Customer;
import com.oneness.dine.customer.entity.LoyaltyTransaction;
import com.oneness.dine.customer.repository.CustomerRepository;
import com.oneness.dine.customer.repository.LoyaltyTransactionRepository;
import com.oneness.dine.restaurant.entity.Branch;
import com.oneness.dine.restaurant.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository          customerRepository;
    private final LoyaltyTransactionRepository loyaltyRepository;
    private final BranchRepository            branchRepository;

    private Branch getBranch(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", branchId));
    }

    // ─── SEARCH ──────────────────────────────────────
    @Transactional(readOnly = true)
    public CustomerDto searchByPhone(String phone) {
        return customerRepository.findByPhone(phone)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "phone", phone));
    }

    // ─── GET ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public CustomerDto getById(Long id) {
        return toDto(customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "id", id)));
    }

    // ─── GET ALL ─────────────────────────────────────
    @Transactional(readOnly = true)
    public List<CustomerDto> getAllByBranch(Long branchId) {
        return customerRepository
                .findByBranchIdAndIsActiveTrue(branchId)
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    // ─── CREATE ──────────────────────────────────────
    @Transactional
    public CustomerDto createCustomer(CustomerRequest request) {
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(
                    "Customer with this phone already exists",
                    "CUSTOMER_EXISTS");
        }

        Customer customer = new Customer();
        customer.setBranch(getBranch(request.getBranchId()));
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setGstin(request.getGstin());
        customer.setCompanyName(request.getCompanyName());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAnniversaryDate(request.getAnniversaryDate());
        customer.setCustomerTag(request.getCustomerTag());
        customer.setTotalVisits(0);
        customer.setTotalSpent(BigDecimal.ZERO);
        customer.setLoyaltyPointsBalance(0);
        customer.setIsActive(true);

        Customer saved = customerRepository.save(customer);
        log.info("Customer created: {}", saved.getPhone());
        return toDto(saved);
    }

    // ─── UPDATE ──────────────────────────────────────
    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "id", id));

        if (request.getName()            != null) customer.setName(request.getName());
        if (request.getEmail()           != null) customer.setEmail(request.getEmail());
        if (request.getGstin()           != null) customer.setGstin(request.getGstin());
        if (request.getCompanyName()     != null) customer.setCompanyName(request.getCompanyName());
        if (request.getAddress()         != null) customer.setAddress(request.getAddress());
        if (request.getDateOfBirth()     != null) customer.setDateOfBirth(request.getDateOfBirth());
        if (request.getAnniversaryDate() != null) customer.setAnniversaryDate(request.getAnniversaryDate());
        if (request.getCustomerTag()     != null) customer.setCustomerTag(request.getCustomerTag());

        return toDto(customerRepository.save(customer));
    }

    // ─── LOYALTY ─────────────────────────────────────
    @Transactional(readOnly = true)
    public List<LoyaltyTransactionDto> getLoyaltyHistory(Long customerId) {
        return loyaltyRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toLoyaltyDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoyaltyTransactionDto redeemPoints(Long customerId,
                                               RedeemLoyaltyRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "id", customerId));

        if (customer.getLoyaltyPointsBalance() < request.getPoints()) {
            throw new BusinessException(
                    String.format("Insufficient points. Available: %d, Requested: %d",
                            customer.getLoyaltyPointsBalance(), request.getPoints()),
                    "INSUFFICIENT_POINTS");
        }

        int newBalance = customer.getLoyaltyPointsBalance() - request.getPoints();
        customer.setLoyaltyPointsBalance(newBalance);
        customerRepository.save(customer);

        LoyaltyTransaction txn = new LoyaltyTransaction();
        txn.setCustomer(customer);
        txn.setTxnType(LoyaltyTxnType.REDEEMED);
        txn.setPoints(-request.getPoints());
        txn.setBalanceAfter(newBalance);
        txn.setRemarks(request.getRemarks() != null ?
                request.getRemarks() : "Points redeemed");

        LoyaltyTransaction saved = loyaltyRepository.save(txn);
        log.info("Loyalty points redeemed: {} points for customer {}",
                request.getPoints(), customer.getPhone());
        return toLoyaltyDto(saved);
    }

    // ─── MAPPERS ─────────────────────────────────────
    private CustomerDto toDto(Customer c) {
        return CustomerDto.builder()
                .id(c.getId())
                .branchId(c.getBranch().getId())
                .name(c.getName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .gstin(c.getGstin())
                .companyName(c.getCompanyName())
                .address(c.getAddress())
                .dateOfBirth(c.getDateOfBirth())
                .anniversaryDate(c.getAnniversaryDate())
                .totalVisits(c.getTotalVisits())
                .totalSpent(c.getTotalSpent())
                .loyaltyPointsBalance(c.getLoyaltyPointsBalance())
                .customerTag(c.getCustomerTag())
                .isActive(c.getIsActive())
                .build();
    }

    private LoyaltyTransactionDto toLoyaltyDto(LoyaltyTransaction t) {
        return LoyaltyTransactionDto.builder()
                .id(t.getId())
                .customerId(t.getCustomer().getId())
                .billId(t.getBill() != null ? t.getBill().getId() : null)
                .txnType(t.getTxnType())
                .points(t.getPoints())
                .balanceAfter(t.getBalanceAfter())
                .remarks(t.getRemarks())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
