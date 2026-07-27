package com.example.billingengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StripeCustomerSummary {
    private String id;       // Stripe's cus_... ID — this becomes the frontend's "id" now
    private String email;
    private long transactionCount;
    private boolean hasFailedTransaction;
}