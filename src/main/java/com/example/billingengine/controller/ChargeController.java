package com.example.billingengine.controller;

import com.example.billingengine.dto.ChargeRequest;
import com.example.billingengine.entity.Transaction;
import com.example.billingengine.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/charges")
public class ChargeController {

    @Autowired
    private BillingService billingService;

    @PostMapping
    public ResponseEntity<Transaction> charge(@RequestBody ChargeRequest request) {
        Transaction tx = billingService.processCharge(
                request.getEmail(),
                request.getAmountInCents(),
                request.getCurrency(),
                request.getTestToken()
        );
        return ResponseEntity.ok(tx);
    }
}