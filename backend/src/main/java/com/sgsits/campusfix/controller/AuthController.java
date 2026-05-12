package com.sgsits.campusfix.controller;

import com.sgsits.campusfix.model.User;
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

    /** Step 0: Lookup enrollment ID → returns name + masked email (no auth needed) */
    @PostMapping("/lookup")
    public ResponseEntity<Map<String, Object>> lookup(@RequestBody Map<String, String> b) {
        return ResponseEntity.ok(auth.lookupUser(b.get("enrollmentId")));
    }

    /** Step 1: Enrollment ID → send OTP to registered email */
    @PostMapping("/request-otp")
    public ResponseEntity<Map<String, Object>> requestOtp(@RequestBody Map<String, String> b) {
        return ResponseEntity.ok(auth.requestOtp(b.get("enrollmentId")));
    }

    /** Step 2: Submit OTP → get JWT token + full user object */
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> b) {
        return ResponseEntity.ok(auth.verifyOtp(b.get("enrollmentId"), b.get("otp")));
    }

    @GetMapping("/me")
    public ResponseEntity<User> me() { return ResponseEntity.ok(auth.me()); }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
