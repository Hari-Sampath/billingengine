package com.example.billingengine.controller;

import com.example.billingengine.dto.LoginRequest;
import com.example.billingengine.entity.AdminUser;
import com.example.billingengine.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Map<String, Instant> tokenExpiry = new ConcurrentHashMap<>();
    private static final long TOKEN_TTL_HOURS = 24;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<AdminUser> admin = adminUserRepository.findByEmail(request.getEmail());

        if (admin.isEmpty() || !passwordEncoder.matches(request.getPassword(), admin.get().getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        String token = UUID.randomUUID().toString();
        tokenExpiry.put(token, Instant.now().plusSeconds(TOKEN_TTL_HOURS * 3600));
        return ResponseEntity.ok(Map.of("token", token));
    }

    public static boolean isValidToken(String token) {
        if (token == null) return false;
        Instant expiry = tokenExpiry.get(token);
        if (expiry == null) return false;
        if (Instant.now().isAfter(expiry)) {
            tokenExpiry.remove(token); // expired — drop it immediately
            return false;
        }
        return true;
    }

    public static void purgeExpiredTokens() {
        Instant now = Instant.now();
        tokenExpiry.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }
}