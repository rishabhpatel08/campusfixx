package com.sgsits.campusfix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgsits.campusfix.model.User;
import com.sgsits.campusfix.repository.ComplaintRepository;
import com.sgsits.campusfix.repository.UserRepository;
import com.sgsits.campusfix.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    private final ComplaintService    complaintSvc;
    private final UserRepository      userRepo;
    private final ComplaintRepository complaintRepo;
    /*
     * FIX: inject Spring's shared ObjectMapper instead of creating new ObjectMapper()
     * per request. ObjectMapper construction is expensive (~5ms, allocates thread
     * locals, parser factories, deserializer caches). Under webhook load this adds
     * up fast. Spring Boot auto-configures a singleton ObjectMapper bean — use it.
     */
    private final ObjectMapper        objectMapper;

    @Value("${app.whatsapp.webhook-verify-token:campusfix_webhook_secret_2026}")
    private String webhookVerifyToken;

    private static final Pattern REF_PATTERN =
        Pattern.compile("#([A-Z]{2}-\\d+)", Pattern.CASE_INSENSITIVE);

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestHeader(value = "x-wati-token", required = false) String watiHeader,
            @RequestParam(required = false) String Body,
            @RequestParam(required = false) String From,
            @RequestBody(required = false) String rawBody) {

        if (!webhookVerifyToken.equals("campusfix_webhook_secret_2026")) {
            if (watiHeader == null || !watiHeader.equals(webhookVerifyToken)) {
                log.warn("WhatsApp webhook: invalid token rejected");
                return ResponseEntity.status(403).body("Forbidden");
            }
        }

        String text = (Body != null) ? Body.trim() : "";
        String from = (From != null) ? From.replace("whatsapp:", "").replace("+", "").trim() : "";

        if (text.isEmpty() && rawBody != null && rawBody.contains("text")) {
            try {
                var node = objectMapper.readTree(rawBody);
                if (node.has("text")) text = node.get("text").asText("").trim();
                if (node.has("waId"))  from = node.get("waId").asText("").trim();
            } catch (Exception e) {
                log.warn("WhatsApp webhook parse error: {}", e.getMessage());
            }
        }

        final String normalizedFrom = from.replaceAll("[^0-9]", "");
        final String upperText      = text.toUpperCase().trim();

        log.info("WhatsApp webhook: from={} text={}", normalizedFrom, upperText);

        if (!upperText.startsWith("YES") && !upperText.startsWith("DONE")) {
            return ResponseEntity.ok("OK — not a tracked keyword");
        }

        String newStatus = upperText.startsWith("YES") ? "in_progress" : "resolved";

        Matcher matcher = REF_PATTERN.matcher(upperText);
        final Optional<String> refFromMessage = matcher.find()
            ? Optional.of("#" + matcher.group(1).toUpperCase())
            : Optional.empty();

        Optional<User> staffOpt = userRepo.findByWhatsappNumber(normalizedFrom);
        if (staffOpt.isEmpty() && normalizedFrom.startsWith("91") && normalizedFrom.length() == 12) {
            staffOpt = userRepo.findByWhatsappNumber(normalizedFrom.substring(2));
        }

        staffOpt.ifPresentOrElse(staff -> {
            var complaintOpt = refFromMessage
                .flatMap(ref -> complaintRepo.findByComplaintNo(ref))
                .or(() -> complaintRepo.findTopByAssignedToAndStatusNotInOrderByCreatedAtDesc(
                    staff.getId(), List.of("closed", "rejected")));

            complaintOpt.ifPresentOrElse(complaint -> {
                complaintSvc.updateStatus(complaint.getId(), newStatus,
                    "Staff WhatsApp reply: " + upperText, staff);
                log.info("✅ Complaint {} → {} via WhatsApp from {}",
                    complaint.getComplaintNo(), newStatus, normalizedFrom);
            }, () -> log.warn("No open complaint found for staff: {}", normalizedFrom));

        }, () -> log.warn("No staff with WhatsApp number: {}", normalizedFrom));

        return ResponseEntity.ok("OK");
    }

    @GetMapping("/webhook")
    public ResponseEntity<String> verify(
            @RequestParam(required = false, name = "hub.verify_token") String token,
            @RequestParam(required = false, name = "hub.challenge")    String challenge) {
        if (webhookVerifyToken.equals(token) && challenge != null) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.ok("CampusFix WhatsApp webhook active — v7");
    }
}
