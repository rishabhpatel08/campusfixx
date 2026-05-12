package com.sgsits.campusfix.controller;

import com.sgsits.campusfix.model.*;
import com.sgsits.campusfix.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

// ── Complaints ──────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {
    private final ComplaintService svc;

    @GetMapping
    public ResponseEntity<List<Complaint>> all(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        return ResponseEntity.ok(svc.all(category, status, priority));
    }

    @GetMapping("/mine")   public ResponseEntity<List<Complaint>> mine()   { return ResponseEntity.ok(svc.mine()); }
    @GetMapping("/stats")  public ResponseEntity<Map<String,Object>> stats() { return ResponseEntity.ok(svc.stats()); }
    @GetMapping("/leaderboard") public ResponseEntity<List<Map<String,Object>>> lb() { return ResponseEntity.ok(svc.leaderboard()); }
    @GetMapping("/dept-load")   public ResponseEntity<List<Map<String,Object>>> deptLoad() { return ResponseEntity.ok(svc.deptLoad()); }
    @GetMapping("/{id}")         public ResponseEntity<Complaint> get(@PathVariable Long id) { return ResponseEntity.ok(svc.get(id)); }
    @GetMapping("/{id}/logs")    public ResponseEntity<List<ComplaintLog>> logs(@PathVariable Long id) { return ResponseEntity.ok(svc.logs(id)); }
    @GetMapping("/{id}/comments") public ResponseEntity<List<Comment>> comments(@PathVariable Long id) { return ResponseEntity.ok(svc.comments(id)); }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> submit(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String locationText,
            @RequestParam(required = false) Double gpsLat,
            @RequestParam(required = false) Double gpsLng,
            @RequestParam(required = false) String aiCategory,
            @RequestParam(required = false) Double aiConfidence,
            @RequestPart(required = false) MultipartFile photo) {
        return ResponseEntity.ok(svc.submit(title, description, category, subcategory,
            priority, locationId, locationText, gpsLat, gpsLng, aiCategory, aiConfidence, photo));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Complaint> status(@PathVariable Long id, @RequestBody Map<String, String> b) {
        return ResponseEntity.ok(svc.updateStatus(id, b.get("status"), b.get("reason")));
    }

    @PostMapping("/{id}/confirm-closure")
    public ResponseEntity<Map<String, Object>> confirmClosure(@PathVariable Long id, @RequestBody Map<String, String> b) {
        return ResponseEntity.ok(svc.confirmClosure(id, b.get("otp")));
    }

    @PostMapping("/{id}/remind")
    public ResponseEntity<Map<String, Object>> remind(@PathVariable Long id) {
        return ResponseEntity.ok(svc.sendReminder(id));
    }

    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Complaint> escalate(@PathVariable Long id, @RequestBody(required = false) Map<String, String> b) {
        return ResponseEntity.ok(svc.escalate(id, b != null ? b.get("reason") : "Manually escalated"));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Complaint> assign(@PathVariable Long id, @RequestBody Map<String, Long> b) {
        return ResponseEntity.ok(svc.assign(id, b.get("staffId")));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> comment(@PathVariable Long id, @RequestBody Map<String, String> b) {
        return ResponseEntity.ok(svc.postComment(id, b.get("message")));
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Map<String, Object>> rate(@PathVariable Long id, @RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(svc.rate(id, (Integer) b.get("stars"), (String) b.getOrDefault("comment", "")));
    }

    // ── NEW: Repair proof upload (staff/admin uploads photo after fixing) ──
    @PostMapping(value = "/{id}/proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF','FACULTY','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadProof(
            @PathVariable Long id,
            @RequestPart("proof") MultipartFile proofFile) {
        return ResponseEntity.ok(svc.uploadProof(id, proofFile));
    }

    // ── NEW: Student reopens a complaint they're not satisfied with ──
    @PostMapping("/{id}/reopen")
    public ResponseEntity<Map<String, Object>> reopen(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> b) {
        return ResponseEntity.ok(svc.reopenComplaint(id, b != null ? b.get("reason") : null));
    }

    // ── NEW: Location hotspot analytics for dashboard ──
    @GetMapping("/hotspots")
    public ResponseEntity<List<Map<String, Object>>> hotspots() {
        return ResponseEntity.ok(svc.hotspots());
    }
}
