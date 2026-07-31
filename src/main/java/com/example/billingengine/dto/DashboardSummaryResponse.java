package com.example.billingengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalRevenueCents;
    private long successCount;
    private long failedCount;
    private double successRate;
    private long customerCount;
}