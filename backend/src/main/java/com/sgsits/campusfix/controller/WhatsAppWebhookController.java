package com.sgsits.campusfix.controller;

import com.sgsits.campusfix.model.User;
import com.sgsits.campusfix.repository.ComplaintRepository;
import com.sgsits.campusfix.repository.UserRepository;
import com.sgsits.campusfix.service.ComplaintService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WhatsApp Webhook — WATI/Twilio POST here when staff replies.
 *
 * FIXES applied in v7:
 *   FIX 1: updateStatus() now takes explicit actor — no auth.me() = no NPE
 *   FIX 2: Parses #REF from reply (e.g. "DONE #EL-1001") so correct complaint updated
 *   FIX 3: Phone number normalized before lookup (strips +91, spaces, etc.)
 *   FIX 4: Webhook token validation — rejects requests without correct token
 *
 * Staff reply format (after v7):
 *   "YES #EL-1001"   → marks complaint EL-1001 as in_progress
 *   "DONE #EL-1001"  → marks complaint EL-1001 as resolved
 *   "YES"            → falls back to most recent open complaint (old behaviour)
 *   "DONE"           → falls back to most recent open complaint
 *
 * Auto-route logic (set in ComplaintService):
 *   electrical / plumbing / technical / sanitation → WhatsApp sent directly on submit
 *   furniture / infrastructure / other            → Admin gets notification, routes manually
 */
@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppWebhookController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WhatsAppWebhookController.class);
    private final com.sgsits.campusfix.service.ComplaintService complaintSvc;
    private final com.sgsits.campusfix.repository.UserRepository userRepo;
    public WhatsAppWebhookController(com.sgsits.campusfix.service.ComplaintService complaintSvc,
        com.sgsits.campusfix.repository.UserRepository userRepo,
        com.sgsits.campusfix.repository.ComplaintRepository complaintRepo){
        this.complaintSvc=complaintSvc; this.userRepo=userRepo; this.complaintRepo=complaintRepo;
    }



    @Value("${app.whatsapp.webhook-verify-token:campusfix_webhook_secret_2026}")
    private String webhookVerifyToken;

    private static final Pattern REF_PATTERN = Pattern.compile("#([A-Z]{2}-\\d+)", Pattern.CASE_INSENSITIVE);

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestHeader(value = "x-wati-token", required = false) String watiHeader,
            @RequestParam(required = false) String Body,
            @RequestParam(required = false) String From,
            @RequestBody(required = false) String rawBody) {

        // FIX 4: Token validation — skip in dev (token = default placeholder)
        if (!webhookVerifyToken.equals("campusfix_webhook_secret_2026")) {
            if (watiHeader == null || !watiHeader.equals(webhookVerifyToken)) {
                log.warn("WhatsApp webhook: invalid token rejected");
                return ResponseEntity.status(403).body("Forbidden");
            }
        }

        String text = (Body != null) ? Body.trim() : "";
        String from = (From != null) ? From.replace("whatsapp:", "").replace("+", "").trim() : "";

        // Parse WATI JSON body format
        if (text.isEmpty() && rawBody != null && rawBody.contains("text")) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                var node = om.readTree(rawBody);
                if (node.has("text")) text = node.get("text").asText("").trim();
                if (node.has("waId")) from = node.get("waId").asText("").trim();
            } catch (Exception e) {
                log.warn("WhatsApp webhook parse error: {}", e.getMessage());
            }
        }

        // FIX 3: Normalize phone number
        final String normalizedFrom = from.replaceAll("[^0-9]", "");
        final String upperText = text.toUpperCase().trim();

        log.info("WhatsApp webhook: from={} text={}", normalizedFrom, upperText);

        // Only act on YES or DONE keywords
        if (!upperText.startsWith("YES") && !upperText.startsWith("DONE")) {
            return ResponseEntity.ok("OK — not a tracked keyword");
        }

        String newStatus = upperText.startsWith("YES") ? "in_progress" : "resolved";

        // FIX 2: Try to extract #REF from message (e.g. "DONE #EL-1001")
        Matcher matcher = REF_PATTERN.matcher(upperText);
        final Optional<String> refFromMessage = matcher.find()
            ? Optional.of("#" + matcher.group(1).toUpperCase())
            : Optional.empty();

        // FIX 3: Try lookup by normalized number (12-digit, then 10-digit fallback)
        Optional<User> staffOpt = userRepo.findByWhatsappNumber(normalizedFrom);
        if (staffOpt.isEmpty() && normalizedFrom.startsWith("91") && normalizedFrom.length() == 12) {
            staffOpt = userRepo.findByWhatsappNumber(normalizedFrom.substring(2)); // try 10-digit
        }

        staffOpt.ifPresentOrElse(staff -> {
            // FIX 2: If #REF provided, find that specific complaint. Else use recency fallback.
            var complaintOpt = refFromMessage
                .flatMap(ref -> complaintRepo.findByComplaintNo(ref))
                .or(() -> complaintRepo.findTopByAssignedToAndStatusNotInOrderByCreatedAtDesc(
                    staff.getId(), List.of("closed", "rejected")));

            complaintOpt.ifPresentOrElse(complaint -> {
                // FIX 1: Pass staff as actor — no SecurityContext needed
                complaintSvc.updateStatus(complaint.getId(), newStatus,
                    "Staff WhatsApp reply: " + upperText, staff);
                log.info("✅ Complaint {} → {} via WhatsApp from {}",
                    complaint.getComplaintNo(), newStatus, normalizedFrom);
            }, () -> log.warn("No open complaint found for staff: {}", normalizedFrom));

        }, () -> log.warn("No staff with WhatsApp number: {} — admin should add it in Users tab", normalizedFrom));

        return ResponseEntity.ok("OK");
    }

    /** WATI webhook verification handshake */
    @GetMapping("/webhook")
    public ResponseEntity<String> verify(
            @RequestParam(required = false, name = "hub.verify_token") String token,
            @RequestParam(required = false, name = "hub.challenge") String challenge) {
        if (webhookVerifyToken.equals(token) && challenge != null) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.ok("CampusFix WhatsApp webhook active — v7");
    }
}
