package com.example.billingengine.controller;

import com.example.billingengine.dto.ChargeRequest;
import com.example.billingengine.dto.StripeCustomerSummary;
import com.example.billingengine.dto.StripeTransactionResponse;
import com.example.billingengine.entity.Transaction;
import com.example.billingengine.service.BillingService;
import com.example.billingengine.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private BillingService billingService;

    // Now sourced live from Stripe, not the local `customers` table
    @GetMapping
    public List<StripeCustomerSummary> getAllCustomers() throws StripeException {
        List<Customer> stripeCustomers = stripeService.listCustomers().getData();

        return stripeCustomers.stream()
                .map(c -> {
                    try {
                        List<Charge> charges = stripeService.listChargesForCustomer(c.getId()).getData();
                        boolean hasFailed = charges.stream().anyMatch(ch -> !"succeeded".equals(ch.getStatus()));
                        return new StripeCustomerSummary(c.getId(), c.getEmail(), charges.size(), hasFailed);
                    } catch (StripeException e) {
                        return new StripeCustomerSummary(c.getId(), c.getEmail(), 0, false);
                    }
                })
                .toList();
    }

    // Now sourced live from Stripe's Charges, not the local `transactions` table
    @GetMapping("/{stripeCustomerId}/transactions")
    public List<StripeTransactionResponse> getCustomerTransactions(@PathVariable String stripeCustomerId) throws StripeException {
        List<Charge> charges = stripeService.listChargesForCustomer(stripeCustomerId).getData();

        return charges.stream()
                .map(ch -> new StripeTransactionResponse(
                        ch.getId(),
                        ch.getCustomer(),
                        ch.getAmount(),
                        ch.getCurrency(),
                        "succeeded".equals(ch.getStatus()) ? "SUCCEEDED" : "FAILED",
                        Instant.ofEpochSecond(ch.getCreated()).toString()
                ))
                .toList();
    }

    // Charging still writes locally too — the scheduler/email feature depends on that
    @PostMapping("/{stripeCustomerId}/charge")
    public ResponseEntity<Transaction> chargeCustomer(@PathVariable String stripeCustomerId, @RequestBody ChargeRequest request) {
        Transaction tx = billingService.chargeExistingStripeCustomer(stripeCustomerId, request.getAmountInCents(), request.getCurrency(), request.getTestToken());
        return ResponseEntity.ok(tx);
    }
}