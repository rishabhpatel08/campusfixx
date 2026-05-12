package com.sgsits.campusfix.controller;

import com.sgsits.campusfix.model.*;
import com.sgsits.campusfix.repository.NotificationRepository;
import com.sgsits.campusfix.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository repo;
    private final AuthService auth;

    @GetMapping
    public ResponseEntity<List<Notification>> all() {
        return ResponseEntity.ok(repo.findByUserIdOrderByCreatedAtDesc(auth.myId()));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, String>> readAll() {
        repo.markAllRead(auth.myId());
        return ResponseEntity.ok(Map.of("message", "Done"));
    }
}
