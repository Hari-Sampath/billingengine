package com.example.billingengine.controller;

import com.example.billingengine.dto.DashboardSummaryResponse;
import com.example.billingengine.entity.Transaction;
import com.example.billingengine.entity.TransactionStatus;
import com.example.billingengine.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        List<Transaction> all = transactionRepository.findAll();

        long successCount = all.stream().filter(t -> t.getStatus() == TransactionStatus.SUCCEEDED).count();
        long failedCount = all.stream().filter(t -> t.getStatus() == TransactionStatus.FAILED).count();
        long totalRevenue = all.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCEEDED)
                .mapToLong(Transaction::getAmountInCents)
                .sum();

        long total = successCount + failedCount;
        double successRate = total == 0 ? 0 : (successCount * 100.0 / total);

        Set<String> uniqueEmails = all.stream().map(Transaction::getCustomerEmail).collect(Collectors.toSet());

        return new DashboardSummaryResponse(totalRevenue, successCount, failedCount, successRate, uniqueEmails.size());
    }

    @GetMapping("/recent")
    public List<Transaction> getRecentActivity() {
        return transactionRepository.findTop10ByOrderByCreatedAtDesc();
    }
}