package com.atozshop.service;

import com.atozshop.dto.request.CreateCustomerRequest;
import com.atozshop.dto.request.UpdateCustomerRequest;
import com.atozshop.dto.response.CustomerPurchaseHistoryResponse;
import com.atozshop.dto.response.CustomerResponse;
import com.atozshop.entity.Customer;
import com.atozshop.exception.ResourceNotFoundException;
import com.atozshop.repository.BillRepository;
import com.atozshop.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BillRepository billRepository;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        // Check if phone already exists
        if (customerRepository.existsByPhoneAndTenantId(request.getPhone(), request.getTenantId())) {
            throw new IllegalArgumentException("Customer with phone " + request.getPhone() + " already exists");
        }

        // Generate customer code: CUST-YYYYMMDD-XXX
        String customerCode = generateCustomerCode(request.getTenantId());

        Customer customer = Customer.builder()
            .tenantId(request.getTenantId())
            .customerCode(customerCode)
            .name(request.getName())
            .phone(request.getPhone())
            .email(request.getEmail())
            .address(request.getAddress())
            .city(request.getCity())
            .state(request.getState())
            .postalCode(request.getPostalCode())
            .gstin(request.getGstin())
            .loyaltyPoints(0)
            .totalPurchases(BigDecimal.ZERO)
            .isActive(true)
            .build();

        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id, Long tenantId) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        if (!customer.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Customer does not belong to this tenant");
        }

        return mapToResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByPhone(String phone, Long tenantId) {
        Customer customer = customerRepository.findByPhoneAndTenantId(phone, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with phone: " + phone));

        return mapToResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers(Long tenantId) {
        return customerRepository.findByTenantIdAndIsActive(tenantId, true)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> searchCustomers(String keyword, Long tenantId) {
        return customerRepository.searchByNameOrPhone(tenantId, keyword)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request, Long tenantId) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        if (!customer.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Customer does not belong to this tenant");
        }

        if (request.getName() != null) customer.setName(request.getName());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getEmail() != null) customer.setEmail(request.getEmail());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        if (request.getCity() != null) customer.setCity(request.getCity());
        if (request.getState() != null) customer.setState(request.getState());
        if (request.getPostalCode() != null) customer.setPostalCode(request.getPostalCode());
        if (request.getGstin() != null) customer.setGstin(request.getGstin());
        if (request.getIsActive() != null) customer.setIsActive(request.getIsActive());

        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    @Transactional
    public void deleteCustomer(Long id, Long tenantId) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        if (!customer.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Customer does not belong to this tenant");
        }

        customer.setIsActive(false);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public CustomerPurchaseHistoryResponse getCustomerPurchaseHistory(Long customerId, Long tenantId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        if (!customer.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Customer does not belong to this tenant");
        }

        var bills = billRepository.findByCustomerIdAndTenantId(customerId, tenantId);
        var totalBills = (long) bills.size();
        var totalAmount = customer.getTotalPurchases();
        var avgBillValue = totalBills > 0 ? totalAmount.divide(BigDecimal.valueOf(totalBills), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

        return CustomerPurchaseHistoryResponse.builder()
            .customer(mapToResponse(customer))
            .recentBills(bills.stream().limit(10).map(bill -> com.atozshop.dto.response.BillSummaryResponse.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .billDate(bill.getBillDate())
                .billType(bill.getBillType().name())
                .status(bill.getStatus().name())
                .paymentStatus(bill.getPaymentStatus().name())
                .totalItems(bill.getTotalItems())
                .totalQuantity(bill.getTotalQuantity())
                .totalAmount(bill.getTotalAmount())
                .paidAmount(bill.getPaidAmount())
                .balanceAmount(bill.getBalanceAmount())
                .build()).collect(Collectors.toList()))
            .totalBills(totalBills)
            .totalPurchaseAmount(totalAmount)
            .averageBillValue(avgBillValue)
            .loyaltyPoints(customer.getLoyaltyPoints())
            .build();
    }

    private String generateCustomerCode(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "CUST-" + dateStr + "-";

        String lastCode = customerRepository.findLastCustomerCodeForDate(tenantId, prefix)
            .orElse(null);

        int sequence = 1;
        if (lastCode != null) {
            sequence = Integer.parseInt(lastCode.substring(14)) + 1;
        }

        return prefix + String.format("%03d", sequence);
    }

    /**
     * Get or create customer record for the current logged-in user
     * This is used for online ordering where users need customer records
     */
    @Transactional
    public CustomerResponse getOrCreateCustomerForUser(String userEmail, Long tenantId, String userName, String userPhone) {
        // Try to find existing customer by email
        var existingCustomer = customerRepository.findByEmailAndTenantId(userEmail, tenantId);
        if (existingCustomer.isPresent()) {
            return mapToResponse(existingCustomer.get());
        }

        // Create new customer record for this user
        String customerCode = generateCustomerCode(tenantId);

        Customer customer = Customer.builder()
            .tenantId(tenantId)
            .customerCode(customerCode)
            .name(userName != null ? userName : "Online Customer")
            .email(userEmail)
            .phone(userPhone)
            .loyaltyPoints(0)
            .totalPurchases(BigDecimal.ZERO)
            .isActive(true)
            .build();

        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
            .id(customer.getId())
            .tenantId(customer.getTenantId())
            .customerCode(customer.getCustomerCode())
            .name(customer.getName())
            .phone(customer.getPhone())
            .email(customer.getEmail())
            .address(customer.getAddress())
            .city(customer.getCity())
            .state(customer.getState())
            .postalCode(customer.getPostalCode())
            .gstin(customer.getGstin())
            .loyaltyPoints(customer.getLoyaltyPoints())
            .totalPurchases(customer.getTotalPurchases())
            .isActive(customer.getIsActive())
            .createdAt(customer.getCreatedAt())
            .updatedAt(customer.getUpdatedAt())
            .build();
    }
}
