package com.example.billingengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StripeTransactionResponse {
    private String stripeChargeId;
    private String stripeCustomerId;
    private long amountInCents;
    private String currency;
    private String status;     // "SUCCEEDED" or "FAILED"
    private String createdAt;  // ISO timestamp, converted from Stripe's Unix seconds
}