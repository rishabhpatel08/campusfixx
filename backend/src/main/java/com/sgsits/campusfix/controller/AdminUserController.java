package com.sgsits.campusfix.controller;

import com.sgsits.campusfix.model.User;
import com.sgsits.campusfix.repository.UserRepository;
import com.sgsits.campusfix.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * Admin User Management Controller
 *
 * FIX #2: All @PreAuthorize annotations changed from hasAnyRole('admin','super_admin')
 * to hasAnyRole('ADMIN','SUPER_ADMIN').
 *
 * Spring Security stores roles as ROLE_ADMIN internally (UPPERCASE). The hasAnyRole()
 * helper automatically prepends "ROLE_" — so 'ADMIN' matches ROLE_ADMIN.
 * Using lowercase 'admin' would try to match ROLE_admin, which never exists → always 403.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserRepository userRepo;
    private final AuthService auth;

    /** List all users — supports optional role filter and search query */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")  // FIX: was 'admin','super_admin'
    public ResponseEntity<List<User>> listAll(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String q) {
        List<User> all = (role != null && !role.isEmpty())
            ? userRepo.findByRole(role)
            : userRepo.findAll();
        if (q != null && !q.isEmpty()) {
            String lq = q.toLowerCase();
            all = all.stream().filter(u ->
                (u.getName()         != null && u.getName().toLowerCase().contains(lq)) ||
                (u.getEmail()        != null && u.getEmail().toLowerCase().contains(lq)) ||
                (u.getEnrollmentId() != null && u.getEnrollmentId().toLowerCase().contains(lq))
            ).toList();
        }
        return ResponseEntity.ok(all);
    }

    /** Add a single user manually via admin form */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")  // FIX: was 'admin','super_admin'
    public ResponseEntity<Map<String, Object>> addUser(@RequestBody Map<String, String> body) {
        String enrollmentId = body.getOrDefault("enrollmentId", "").trim();
        String email        = body.getOrDefault("email", "").trim();
        String name         = body.getOrDefault("name", "").trim();

        if (enrollmentId.isEmpty() || email.isEmpty() || name.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "name, enrollmentId and email are required"));
        if (userRepo.findByEnrollmentId(enrollmentId).isPresent())
            return ResponseEntity.badRequest().body(Map.of("error", "Enrollment ID already exists: " + enrollmentId));
        if (userRepo.findByEmail(email).isPresent())
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists: " + email));

        User u = User.builder()
            .enrollmentId(enrollmentId)
            .name(name)
            .email(email)
            .role(body.getOrDefault("role", "student"))
            .department(body.getOrDefault("department", ""))
            .branch(body.getOrDefault("branch", ""))
            .yearSection(body.getOrDefault("yearSection", ""))
            .phone(body.getOrDefault("phone", ""))
            .whatsappNumber(body.getOrDefault("whatsappNumber", ""))
            .points(0)
            .badge("Newcomer")
            .active(true)
            .dataSource("admin_manual")
            .build();
        userRepo.save(u);
        return ResponseEntity.ok(Map.of(
            "message", "User added successfully",
            "id", u.getId(),
            "enrollmentId", u.getEnrollmentId()
        ));
    }

    /** Bulk import users from JSON array (parsed from CSV on the frontend) */
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")  // FIX: was 'admin','super_admin'
    public ResponseEntity<Map<String, Object>> bulkImport(@RequestBody List<Map<String, String>> rows) {
        int added = 0, skipped = 0;
        List<String> errors = new ArrayList<>();
        for (Map<String, String> row : rows) {
            String eid   = row.getOrDefault("enrollmentId", row.getOrDefault("enrollment_id", "")).trim();
            String email = row.getOrDefault("email", "").trim();
            String name  = row.getOrDefault("name", "").trim();
            if (eid.isEmpty() || email.isEmpty() || name.isEmpty()) { skipped++; continue; }
            if (userRepo.findByEnrollmentId(eid).isPresent() || userRepo.findByEmail(email).isPresent()) {
                skipped++; continue;
            }
            try {
                User u = User.builder()
                    .enrollmentId(eid).name(name).email(email)
                    .role(row.getOrDefault("role", "student"))
                    .department(row.getOrDefault("department", ""))
                    .branch(row.getOrDefault("branch", ""))
                    .yearSection(row.getOrDefault("yearSection", row.getOrDefault("year_section", "")))
                    .phone(row.getOrDefault("phone", ""))
                    .whatsappNumber(row.getOrDefault("whatsappNumber", row.getOrDefault("whatsapp_number", "")))
                    .points(0).badge("Newcomer").active(true).dataSource("csv_import")
                    .build();
                userRepo.save(u);
                added++;
            } catch (Exception e) {
                errors.add(eid + ": " + e.getMessage());
                skipped++;
            }
        }
        return ResponseEntity.ok(Map.of("added", added, "skipped", skipped, "errors", errors));
    }

    /** Soft-delete (deactivate) a user */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")  // FIX: was 'admin','super_admin'
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable Long id) {
        userRepo.findById(id).ifPresent(u -> { u.setActive(false); userRepo.save(u); });
        return ResponseEntity.ok(Map.of("message", "User deactivated"));
    }

    /** Re-activate a previously deactivated user */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")  // FIX: was 'admin','super_admin'
    public ResponseEntity<Map<String, String>> activate(@PathVariable Long id) {
        userRepo.findById(id).ifPresent(u -> { u.setActive(true); userRepo.save(u); });
        return ResponseEntity.ok(Map.of("message", "User activated"));
    }
}
