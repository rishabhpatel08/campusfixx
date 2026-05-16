package com.sgsits.campusfix.controller;

import com.sgsits.campusfix.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/lookup")
    public ResponseEntity<Map<String, Object>> lookup(@RequestBody Map<String, String> b) {
        return ResponseEntity.ok(auth.lookupUser(b.get("enrollmentId")));
    }

    @PostMapping("/request-otp")
    public ResponseEntity<Map<String, Object>> requestOtp(@RequestBody Map<String, String> b) {
        return ResponseEntity.ok(auth.requestOtp(b.get("enrollmentId")));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> b) {
        return ResponseEntity.ok(auth.verifyOtp(b.get("enrollmentId"), b.get("otp")));
    }

    /*
     * FIX: was ResponseEntity<User> returning the raw entity.
     * Raw entity exposes internal fields: dataSource, streakDays, active flag,
     * createdAt, updatedAt, lastLogin — none of which the frontend needs,
     * and some of which are operationally sensitive.
     * safeUser() returns only the 10 fields the frontend actually uses.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        return ResponseEntity.ok(auth.safeUser(auth.me()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
