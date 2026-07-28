package com.example.billingengine.config;

import com.example.billingengine.entity.AdminUser;
import com.example.billingengine.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String seedEmail;

    @Value("${admin.password}")
    private String seedPassword;

    @Override
    public void run(String... args) {
        if (adminUserRepository.findByEmail(seedEmail).isEmpty()) {
            AdminUser admin = new AdminUser();
            admin.setEmail(seedEmail);
            admin.setPasswordHash(passwordEncoder.encode(seedPassword));
            adminUserRepository.save(admin);
            System.out.println("Seeded admin user: " + seedEmail);
        }
    }
}