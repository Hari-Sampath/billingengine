package com.example.billingengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BillingengineApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingengineApplication.class, args);
	}

}
