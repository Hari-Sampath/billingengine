package com.example.billingengine.scheduler;

import com.example.billingengine.entity.Transaction;
import com.example.billingengine.entity.TransactionStatus;
import com.example.billingengine.repository.TransactionRepository;
import com.example.billingengine.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FailedPaymentScheduler {

    private final TransactionRepository transactionRepository;
    private final EmailService emailService;

    // Runs every day at 9:00 AM server time
    @Scheduled(cron = "*/30 * * * * *")
    public void checkFailedPayments() {
        List<Transaction> failedUnnotified =
                transactionRepository.findByStatusAndFailureEmailSentFalse(TransactionStatus.FAILED);

        for (Transaction tx : failedUnnotified) {
            emailService.sendFailureAlert(tx);
            tx.setFailureEmailSent(true);
            transactionRepository.save(tx);
        }
    }
}