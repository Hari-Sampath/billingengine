package com.example.billingengine.controller;

import com.example.billingengine.dto.LoginRequest;
import com.example.billingengine.entity.AdminUser;
import com.example.billingengine.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // In-memory valid-token store — fine for a single-admin MVP; resets on restart
    private static final Set<String> validTokens = ConcurrentHashMap.newKeySet();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<AdminUser> admin = adminUserRepository.findByEmail(request.getEmail());

        if (admin.isEmpty() || !passwordEncoder.matches(request.getPassword(), admin.get().getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        String token = UUID.randomUUID().toString();
        validTokens.add(token);
        return ResponseEntity.ok(Map.of("token", token));
    }

    public static boolean isValidToken(String token) {
        return token != null && validTokens.contains(token);
    }
}