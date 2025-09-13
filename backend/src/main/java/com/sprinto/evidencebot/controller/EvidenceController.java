package com.sprinto.evidencebot.controller;

import com.sprinto.evidencebot.service.EvidenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/evidence")
@CrossOrigin(origins = "*")
public class EvidenceController {

    @Autowired
    private EvidenceService evidenceService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateEvidence(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query is required"));
        }
        
        Map<String, Object> evidence = evidenceService.generateEvidence(query);
        return ResponseEntity.ok(evidence);
    }

    @GetMapping("/compliance-report")
    public ResponseEntity<Map<String, Object>> getComplianceReport(@RequestParam(defaultValue = "general") String domain) {
        Map<String, Object> report = evidenceService.getComplianceReport(domain);
        return ResponseEntity.ok(report);
    }
}