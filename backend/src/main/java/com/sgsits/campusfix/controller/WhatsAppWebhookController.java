package com.sgsits.campusfix.controller;

import com.sgsits.campusfix.model.Complaint;
import com.sgsits.campusfix.model.User;
import com.sgsits.campusfix.repository.ComplaintRepository;
import com.sgsits.campusfix.repository.UserRepository;
import com.sgsits.campusfix.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppWebhookController {

    private static final Logger log = Logger.getLogger(WhatsAppWebhookController.class.getName());
    private static final Pattern REF_PATTERN = Pattern.compile("#([A-Z]{2}-\\d+)", Pattern.CASE_INSENSITIVE);

    @Autowired private ComplaintService complaintSvc;
    @Autowired private UserRepository userRepo;
    @Autowired private ComplaintRepository complaintRepo;

    @Value("${app.whatsapp.webhook-verify-token:campusfix_webhook_secret_2026}")
    private String webhookVerifyToken;

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestHeader(value="x-wati-token", required=false) String watiHeader,
            @RequestParam(required=false) String Body,
            @RequestParam(required=false) String From,
            @RequestBody(required=false) String rawBody) {

        // Token validation
        if (!webhookVerifyToken.equals("campusfix_webhook_secret_2026")) {
            if (watiHeader == null || !watiHeader.equals(webhookVerifyToken)) {
                log.warning("WhatsApp webhook: invalid token");
                return ResponseEntity.status(403).body("Forbidden");
            }
        }

        String text = (Body != null) ? Body.trim() : "";
        String from = (From != null) ? From.replace("whatsapp:", "").replace("+", "").trim() : "";

        // Parse WATI JSON body format
        if (text.isEmpty() && rawBody != null && rawBody.contains("text")) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = om.readTree(rawBody);
                if (node.has("text")) text = node.get("text").asText("").trim();
                if (node.has("waId")) from = node.get("waId").asText("").trim();
            } catch (Exception e) {
                log.warning("Webhook parse error: " + e.getMessage());
            }
        }

        final String normalizedFrom = from.replaceAll("[^0-9]", "");
        final String upperText = text.toUpperCase().trim();

        log.info("WhatsApp webhook: from=" + normalizedFrom + " text=" + upperText);

        if (!upperText.startsWith("YES") && !upperText.startsWith("DONE")) {
            return ResponseEntity.ok("OK - not a tracked keyword");
        }

        String newStatus = upperText.startsWith("YES") ? "in_progress" : "resolved";

        // Parse #REF from message e.g. "DONE #EL-1001"
        Matcher matcher = REF_PATTERN.matcher(upperText);
        String refFromMessage = matcher.find() ? "#" + matcher.group(1).toUpperCase() : null;

        // Find staff by phone number
        Optional<User> staffOpt = userRepo.findByWhatsappNumber(normalizedFrom);
        if (staffOpt.isEmpty() && normalizedFrom.startsWith("91") && normalizedFrom.length() == 12) {
            staffOpt = userRepo.findByWhatsappNumber(normalizedFrom.substring(2));
        }

        if (staffOpt.isPresent()) {
            User staff = staffOpt.get();

            // Find complaint by #REF or fall back to most recent
            Optional<Complaint> complaintOpt;
            if (refFromMessage != null) {
                complaintOpt = complaintRepo.findByComplaintNo(refFromMessage);
            } else {
                complaintOpt = complaintRepo.findTopByAssignedToAndStatusNotInOrderByCreatedAtDesc(
                    staff.getId(), List.of("closed", "rejected"));
            }

            if (complaintOpt.isPresent()) {
                Complaint complaint = complaintOpt.get();
                complaintSvc.updateStatus(complaint.getId(), newStatus,
                    "Staff WhatsApp reply: " + upperText, staff);
                log.info("Complaint " + complaint.getComplaintNo() + " → " + newStatus);
            } else {
                log.warning("No open complaint found for staff: " + normalizedFrom);
            }
        } else {
            log.warning("No staff found with WhatsApp: " + normalizedFrom);
        }

        return ResponseEntity.ok("OK");
    }

    @GetMapping("/webhook")
    public ResponseEntity<String> verify(
            @RequestParam(required=false, name="hub.verify_token") String token,
            @RequestParam(required=false, name="hub.challenge") String challenge) {
        if (webhookVerifyToken.equals(token) && challenge != null) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.ok("CampusFix WhatsApp webhook active v7");
    }
}
