package com.example.billingengine.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.api.key}")
    private String apiKey;

    @PostConstruct // runs once, right after Spring builds this object
    public void init() {
        Stripe.apiKey = apiKey;
    }
}