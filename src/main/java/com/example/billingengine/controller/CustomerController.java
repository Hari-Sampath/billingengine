package com.example.billingengine.controller;

import com.example.billingengine.dto.ChargeRequest;
import com.example.billingengine.dto.CustomerSummaryResponse;
import com.example.billingengine.entity.Customer;
import com.example.billingengine.entity.Transaction;
import com.example.billingengine.entity.TransactionStatus;
import com.example.billingengine.repository.CustomerRepository;
import com.example.billingengine.repository.TransactionRepository;
import com.example.billingengine.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BillingService billingService;

    @GetMapping
    public List<CustomerSummaryResponse> getAllCustomers() {
        return customerRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(c -> {
                    List<Transaction> txs = transactionRepository.findByCustomerIdOrderByCreatedAtDesc(c.getId());
                    boolean hasFailed = txs.stream().anyMatch(t -> t.getStatus() == TransactionStatus.FAILED);
                    return new CustomerSummaryResponse(c.getId(), c.getEmail(), txs.size(), hasFailed);
                })
                .toList();
    }

    @GetMapping("/{id}/transactions")
    public List<Transaction> getCustomerTransactions(@PathVariable Long id) {
        return transactionRepository.findByCustomerIdOrderByCreatedAtDesc(id);
    }

    @PostMapping("/{id}/charge")
    public ResponseEntity<Transaction> chargeCustomer(@PathVariable Long id, @RequestBody ChargeRequest request) {
        Transaction tx = billingService.chargeExistingCustomer(id, request.getAmountInCents(), request.getCurrency(), request.getTestToken());
        return ResponseEntity.ok(tx);
    }
}