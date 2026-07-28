package com.example.billingengine.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.ChargeCollection;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.ChargeListParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
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
                .setSource(testToken)
                .build();
        return Charge.create(params);
    }

    // NEW — pulls every customer straight from your Stripe test account
    public CustomerCollection listCustomers() throws StripeException {
        CustomerListParams params = CustomerListParams.builder().setLimit(100L).build();
        return Customer.list(params);
    }

    // NEW — pulls every charge belonging to one Stripe customer
    public ChargeCollection listChargesForCustomer(String stripeCustomerId) throws StripeException {
        ChargeListParams params = ChargeListParams.builder()
                .setCustomer(stripeCustomerId)
                .setLimit(100L)
                .build();
        return Charge.list(params);
    }
}