package com.example.billingengine.service;

import com.example.billingengine.entity.Transaction;
import com.example.billingengine.entity.TransactionStatus;
import com.example.billingengine.repository.TransactionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BillingService {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction processCharge(String email, long amountInCents, String currency, String testToken) {
        Transaction tx = new Transaction();
        tx.setCustomerEmail(email);
        tx.setAmountInCents(amountInCents);
        tx.setCurrency(currency);
        tx.setCreatedAt(Instant.now());

        try {
            Customer customer = stripeService.createCustomer(email);
            tx.setStripeCustomerId(customer.getId());

            Charge charge = stripeService.createCharge(customer.getId(), amountInCents, currency, testToken);
            tx.setStripeChargeId(charge.getId());
            tx.setStatus(TransactionStatus.SUCCEEDED);

        } catch (StripeException e) {
            // Card declined, or any other Stripe-side failure — we still save the attempt
            tx.setStatus(TransactionStatus.FAILED);
        }

        return transactionRepository.save(tx);
    }
}