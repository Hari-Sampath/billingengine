package com.example.billingengine.controller;

import com.example.billingengine.dto.ChargeRequest;
import com.example.billingengine.dto.StripeCustomerSummary;
import com.example.billingengine.entity.Transaction;
import com.example.billingengine.entity.TransactionStatus;
import com.example.billingengine.repository.TransactionRepository;
import com.example.billingengine.service.BillingService;
import com.example.billingengine.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BillingService billingService;

    @GetMapping
    public List<StripeCustomerSummary> getAllCustomers() throws StripeException {
        List<Customer> stripeCustomers = stripeService.listCustomers().getData();
        List<Transaction> allTransactions = transactionRepository.findAll();

        // Dedupe: keep only the first Stripe customer record seen per email
        Map<String, Customer> uniqueByEmail = new LinkedHashMap<>();
        for (Customer c : stripeCustomers) {
            uniqueByEmail.putIfAbsent(c.getEmail(), c);
        }

        return uniqueByEmail.values().stream()
                .map(c -> {
                    List<Transaction> txs = allTransactions.stream()
                            .filter(t -> c.getEmail().equalsIgnoreCase(t.getCustomerEmail()))
                            .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                            .toList();
                    boolean hasFailed = !txs.isEmpty() && txs.get(0).getStatus() == TransactionStatus.FAILED;
                    return new StripeCustomerSummary(c.getId(), c.getEmail(), txs.size(), hasFailed);
                })
                .toList();
    }

    @GetMapping("/{stripeCustomerId}/transactions")
    public List<Transaction> getCustomerTransactions(@PathVariable String stripeCustomerId) throws StripeException {
        Customer stripeCustomer = Customer.retrieve(stripeCustomerId);
        return transactionRepository.findByCustomerEmailOrderByCreatedAtDesc(stripeCustomer.getEmail());
    }

    @PostMapping("/{stripeCustomerId}/charge")
    public ResponseEntity<Transaction> chargeCustomer(@PathVariable String stripeCustomerId, @RequestBody ChargeRequest request) {
        Transaction tx = billingService.chargeExistingStripeCustomer(stripeCustomerId, request.getAmountInCents(), request.getCurrency(), request.getTestToken());
        return ResponseEntity.ok(tx);
    }
}