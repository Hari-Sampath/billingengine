package com.example.billingengine.service;

import com.example.billingengine.entity.Customer;
import com.example.billingengine.entity.Transaction;
import com.example.billingengine.entity.TransactionStatus;
import com.example.billingengine.repository.CustomerRepository;
import com.example.billingengine.repository.TransactionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BillingService {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // Used by POST /api/charges — email might be new or existing
    public Transaction processCharge(String email, long amountInCents, String currency, String testToken) {
        Customer customer = getOrCreateCustomer(email);
        return chargeCustomer(customer, amountInCents, currency, testToken);
    }

    // Used by POST /api/customers/{id}/charge — customer already exists
    public Transaction chargeExistingCustomer(Long customerId, long amountInCents, String currency, String testToken) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return chargeCustomer(customer, amountInCents, currency, testToken);
    }

    private Customer getOrCreateCustomer(String email) {
        return customerRepository.findByEmail(email).orElseGet(() -> {
            try {
                com.stripe.model.Customer stripeCustomer = stripeService.createCustomer(email);

                Customer newCustomer = new Customer();
                newCustomer.setEmail(email);
                newCustomer.setStripeCustomerId(stripeCustomer.getId());
                newCustomer.setCreatedAt(Instant.now());
                return customerRepository.save(newCustomer);
            } catch (StripeException e) {
                throw new RuntimeException("Failed to create Stripe customer", e);
            }
        });
    }

    private Transaction chargeCustomer(Customer customer, long amountInCents, String currency, String testToken) {
        Transaction tx = new Transaction();
        tx.setCustomer(customer);
        tx.setCustomerEmail(customer.getEmail());
        tx.setAmountInCents(amountInCents);
        tx.setCurrency(currency);
        tx.setCreatedAt(Instant.now());

        try {
            Charge charge = stripeService.createCharge(customer.getStripeCustomerId(), amountInCents, currency, testToken);
            tx.setStripeChargeId(charge.getId());
            tx.setStatus(TransactionStatus.SUCCEEDED);
        } catch (StripeException e) {
            tx.setStatus(TransactionStatus.FAILED);
        }

        return transactionRepository.save(tx);
    }
    public Transaction chargeExistingStripeCustomer(String stripeCustomerId, long amountInCents, String currency, String testToken) {
        Customer localCustomer = getOrCreateLocalCustomerByStripeId(stripeCustomerId);
        return chargeCustomer(localCustomer, amountInCents, currency, testToken);
    }

    private Customer getOrCreateLocalCustomerByStripeId(String stripeCustomerId) {
        return customerRepository.findByStripeCustomerId(stripeCustomerId).orElseGet(() -> {
            try {
                com.stripe.model.Customer stripeCustomer = com.stripe.model.Customer.retrieve(stripeCustomerId);

                Customer newCustomer = new Customer();
                newCustomer.setEmail(stripeCustomer.getEmail());
                newCustomer.setStripeCustomerId(stripeCustomerId);
                newCustomer.setCreatedAt(Instant.now());
                return customerRepository.save(newCustomer);
            } catch (StripeException e) {
                throw new RuntimeException("Failed to retrieve Stripe customer", e);
            }
        });
    }
    public Customer createCustomerOnly(String email) {
        return getOrCreateCustomer(email);
    }
}