package com.example.billingengine.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.CustomerCreateParams;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    public Customer createCustomer(String email) throws StripeException {
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(email)
                .build();
        return Customer.create(params);
    }

    public Charge createCharge(String customerId, long amountInCents, String currency, String testToken) throws StripeException {
        ChargeCreateParams params = ChargeCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency)
                .setCustomer(customerId)
                .setSource(testToken) // e.g. "tok_visa" (succeeds) or "tok_chargeDeclined" (fails)
                .build();
        return Charge.create(params);
    }
}