package com.example.billingengine.entity;

import lombok.Data;

@Data
public class ChargeRequest {
    private String email;
    private long amountInCents;
    private String currency;
    private String testToken; // e.g. "tok_visa" or "tok_chargeDeclined"
}