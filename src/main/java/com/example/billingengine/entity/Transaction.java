package com.example.billingengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerEmail;
    private Long amountInCents;
    private String currency;
    private String stripeCustomerId;
    private String stripeChargeId;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private Instant createdAt;

    private boolean failureEmailSent = false;
}