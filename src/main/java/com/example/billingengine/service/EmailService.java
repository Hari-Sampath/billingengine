package com.example.billingengine.service;

import com.example.billingengine.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendFailureAlert(Transaction tx) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(tx.getCustomerEmail());
        message.setSubject("Payment Failed - Action Required");
        message.setText(
                "Hi,\n\nYour payment of " + (tx.getAmountInCents() / 100.0) + " " + tx.getCurrency().toUpperCase()
                        + " could not be processed. Please update your payment method.\n\nThanks."
        );
        mailSender.send(message);
    }
}