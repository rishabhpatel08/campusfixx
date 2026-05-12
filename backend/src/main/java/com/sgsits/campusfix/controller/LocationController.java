package com.sgsits.campusfix.controller;

import com.sgsits.campusfix.model.Location;
import com.sgsits.campusfix.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationRepository repo;

    @GetMapping
    public ResponseEntity<List<Location>> all() { return ResponseEntity.ok(repo.findAll()); }

    /** QR URL builder — returns the full URL for a location's QR code */
    @GetMapping("/qr-url")
    public ResponseEntity<Map<String, String>> qrUrl(
            @RequestParam String loc,
            @RequestParam(required = false) String block,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) String zone,
            @RequestParam(defaultValue = "http://localhost:5500/frontend/pages/app.html") String baseUrl) {
        StringBuilder url = new StringBuilder(baseUrl)
            .append("?report=1&loc=").append(loc.replace(" ", "+"));
        if (block != null && !block.isEmpty()) url.append("&block=").append(block.replace(" ", "+"));
        if (floor != null && !floor.isEmpty()) url.append("&floor=").append(floor.replace(" ", "+"));
        if (zone  != null && !zone.isEmpty())  url.append("&zone=").append(zone.replace(" ", "+"));
        return ResponseEntity.ok(Map.of("url", url.toString(), "loc", loc));
    }
}
