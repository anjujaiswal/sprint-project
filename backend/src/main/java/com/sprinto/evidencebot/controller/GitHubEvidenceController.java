package com.sprinto.evidencebot.controller;

import com.sprinto.evidencebot.service.GitHubEvidenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/github")
@CrossOrigin(origins = "*")
public class GitHubEvidenceController {

    @Autowired
    private GitHubEvidenceService gitHubEvidenceService;

    @PostMapping("/evidence")
    public ResponseEntity<Map<String, Object>> getEvidence(@RequestBody Map<String, String> request) {
        String owner = request.get("owner");
        String repo = request.get("repo");
        String query = request.get("query");
        String type = request.getOrDefault("type", "commits");
        String since = request.get("since");
        String until = request.get("until");
        String author = request.get("author");
        
        if (owner == null || repo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Owner and repo are required"));
        }
        
        Map<String, Object> evidence = gitHubEvidenceService.getEvidenceWithParams(
            owner, repo, query, type, since, until, author);
        return ResponseEntity.ok(evidence);
    }

    @GetMapping("/evidence/{owner}/{repo}")
    public ResponseEntity<Map<String, Object>> getEvidenceByPath(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "commits") String type,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until,
            @RequestParam(required = false) String author) {
        
        Map<String, Object> evidence = gitHubEvidenceService.getEvidenceWithParams(
            owner, repo, query, type, since, until, author);
        return ResponseEntity.ok(evidence);
    }

    @GetMapping("/download/{owner}/{repo}")
    public ResponseEntity<byte[]> downloadEvidence(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "commits") String type) {
        
        byte[] csvData = gitHubEvidenceService.generateCSVReport(owner, repo, type);
        String filename = String.format("%s-%s-%s-evidence.csv", owner, repo, type);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", "text/csv")
                .body(csvData);
    }

    @GetMapping("/repositories/{owner}")
    public ResponseEntity<Map<String, Object>> getRepositories(@PathVariable String owner) {
        Map<String, Object> repos = gitHubEvidenceService.getRepositories(owner);
        return ResponseEntity.ok(repos);
    }
}