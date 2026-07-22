package com.example.billingengine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
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
    private com.example.billingengine.model.TransactionStatus status;

    private Instant createdAt;

    private boolean failureEmailSent = false;
}