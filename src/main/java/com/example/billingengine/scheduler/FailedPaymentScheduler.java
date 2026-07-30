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

    @Scheduled(cron = "*/30 * * * * *")
    public void checkFailedPayments() {
        List<Transaction> failedUnnotified =
                transactionRepository.findByStatusAndFailureEmailSentFalse(TransactionStatus.FAILED);
        for (Transaction tx : failedUnnotified) {
            try {
                emailService.sendFailureAlert(tx);
                tx.setFailureEmailSent(true);
                transactionRepository.save(tx);
            } catch (Exception e) {
                System.out.println("Failed to send alert for transaction " + tx.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}