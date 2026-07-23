package com.example.billingengine.repository;

import com.example.billingengine.entity.Transaction;
import com.example.billingengine.entity .TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Spring generates the SQL for this just from the method name
    List<Transaction> findByStatusAndFailureEmailSentFalse(TransactionStatus status);
}