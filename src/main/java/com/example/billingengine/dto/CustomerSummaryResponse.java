package com.example.billingengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerSummaryResponse {
    private Long id;
    private String email;
    private long transactionCount;
    private boolean hasFailedTransaction;
}