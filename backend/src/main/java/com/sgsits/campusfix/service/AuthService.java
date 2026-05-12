package com.sgsits.campusfix.service;

import com.sgsits.campusfix.model.*;
import com.sgsits.campusfix.repository.*;
import com.sgsits.campusfix.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ═══════════════════════════════════════════════════════════════
 * AUTH SERVICE — Enrollment-Only OTP Authentication (v4)
 *
 * Flow:
 *  1. /auth/lookup  → user enters enrollmentId → return name + masked email
 *  2. /auth/request-otp → send 6-digit OTP to their registered email
 *  3. /auth/verify-otp  → verify OTP → issue JWT
 *
 * NO password. NO date of birth. Zero friction.
 * Only data needed: enrollmentId (user knows) + email (we store internally).
 * ═══════════════════════════════════════════════════════════════
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    private final UserRepository        userRepo;
    private final OtpTokenRepository    otpRepo;
    private final NotificationRepository notifRepo;
    private final BCryptPasswordEncoder  encoder;
    private final JwtUtil                jwt;
    private final JavaMailSender         mailer;

    @Value("${app.otp.expiry-minutes:10}") private int otpExpiry;
    @Value("${app.mail.from}")             private String mailFrom;

    // ── STEP 0: Lookup enrollment → return name + masked email ──
    public Map<String,Object> lookupUser(String enrollmentId) {
        User user = userRepo.findByEnrollmentId(enrollmentId.trim().toUpperCase())
            .orElseThrow(() -> new RuntimeException(
                "Enrollment ID not found. Please check your ID or contact the IT Cell."));

        if (!Boolean.TRUE.equals(user.getActive()))
            throw new RuntimeException("Account is deactivated. Contact administration.");

        return Map.of(
            "name",         user.getName(),
            "email_masked", maskEmail(user.getEmail()),
            "department",   user.getDepartment() != null ? user.getDepartment() : "",
            "role",         user.getRole()
        );
    }

    // ── STEP 1: Send OTP to registered email ────────────────────
    @Transactional
    public Map<String,Object> requestOtp(String enrollmentId) {
        User user = userRepo.findByEnrollmentId(enrollmentId.trim().toUpperCase())
            .orElseThrow(() -> new RuntimeException("Enrollment ID not found."));

        if (!Boolean.TRUE.equals(user.getActive()))
            throw new RuntimeException("Account deactivated. Contact administration.");

        // Invalidate any existing OTPs for this user
        otpRepo.invalidateAll(user.getEmail(), "login");

        // Generate 6-digit OTP
        String rawOtp  = String.format("%06d", new Random().nextInt(999999));
        String otpHash = encoder.encode(rawOtp);

        // Store hashed OTP
        otpRepo.save(OtpToken.builder()
            .userId(user.getId()).email(user.getEmail())
            .tokenHash(otpHash).purpose("login")
            .expiresAt(LocalDateTime.now().plusMinutes(otpExpiry))
            .build());

        // Send email async
        sendOtpEmail(user.getEmail(), user.getName(), rawOtp);

        log.info("OTP sent to {} for enrollment {}", user.getEmail(), enrollmentId);
        return Map.of(
            "message",          "OTP sent to " + maskEmail(user.getEmail()),
            "email_masked",     maskEmail(user.getEmail()),
            "expires_in_minutes", otpExpiry
        );
    }

    // ── STEP 2: Verify OTP → issue JWT ──────────────────────────
    @Transactional
    public Map<String,Object> verifyOtp(String enrollmentId, String otp) {
        User user = userRepo.findByEnrollmentId(enrollmentId.trim().toUpperCase())
            .orElseThrow(() -> new RuntimeException("User not found."));

        List<OtpToken> tokens = otpRepo
            .findByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(user.getEmail(), "login");

        OtpToken valid = tokens.stream()
            .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
            .filter(t -> encoder.matches(otp.trim(), t.getTokenHash()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Please request a new one."));

        valid.setUsed(true);
        otpRepo.save(valid);

        user.setLastLogin(LocalDateTime.now());
        userRepo.save(user);

        String token = jwt.generate(user);
        long unread  = notifRepo.countByUserIdAndReadFalse(user.getId());

        log.info("Login successful: {} ({})", user.getEmail(), user.getRole());
        return Map.of("token", token, "user", safeUser(user), "unread", unread);
    }

    // ── GET CURRENT USER ─────────────────────────────────────────
    public User me() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Session expired. Please login again."));
    }

    public Long myId() { return me().getId(); }

    // ── ADMIN: Add user ──────────────────────────────────────────
    @Transactional
    public User addUser(String enrollmentId, String name, String email,
                        String role, String dept, String phone) {
        if (userRepo.existsByEnrollmentId(enrollmentId))
            throw new RuntimeException("Enrollment ID already exists.");
        return userRepo.save(User.builder()
            .enrollmentId(enrollmentId.toUpperCase()).name(name).email(email)
            .role(role).department(dept).phone(phone)
            .active(true).dataSource("admin_manual").build());
    }

    // ── Points ───────────────────────────────────────────────────
    @Transactional
    public void addPoints(Long uid, int pts) {
        userRepo.findById(uid).ifPresent(u -> { u.addPoints(pts); userRepo.save(u); });
    }

    // ── Helpers ──────────────────────────────────────────────────
    public Map<String,Object> safeUser(User u) {
        return Map.of(
            "id",          u.getId(),
            "name",        u.getName(),
            "email",       u.getEmail(),
            "role",        u.getRole(),
            "enrollmentId",u.getEnrollmentId(),
            "department",  u.getDepartment() != null ? u.getDepartment() : "",
            "branch",      u.getBranch()     != null ? u.getBranch()     : "",
            "yearSection", u.getYearSection()!= null ? u.getYearSection(): "",
            "points",      u.getPoints(),
            "badge",       u.getBadge()      != null ? u.getBadge()      : "Newcomer"
        );
    }

    private String maskEmail(String e) {
        int at = e.indexOf('@'); if(at <= 2) return e;
        return e.charAt(0) + "***" + e.charAt(at-1) + e.substring(at);
    }

    @Async
    void sendOtpEmail(String to, String name, String otp) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom); msg.setTo(to);
            msg.setSubject("CampusFix — Your Login OTP | SGSITS Indore");
            msg.setText(String.format("""
                Dear %s,

                Your CampusFix login OTP is: %s

                Valid for %d minutes. Do not share it with anyone.

                — CampusFix | SGSITS Indore
                Vallabh Nagar, Indore — 452003
                """, name, otp, otpExpiry));
            mailer.send(msg);
            log.info("OTP email sent to {}", to);
        } catch (Exception e) {
            // Dev mode: print OTP to console so you can test without SMTP
            log.warn("Email send failed (dev/no-SMTP). OTP for {} is: {}", to, otp);
        }
    }
}
