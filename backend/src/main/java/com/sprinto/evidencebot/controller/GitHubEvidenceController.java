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

    // Use Case 1: PRs merged without approval
    @GetMapping("/prs-without-approval/{owner}/{repo}")
    public ResponseEntity<Map<String, Object>> getPRsWithoutApproval(
            @PathVariable String owner,
            @PathVariable String repo) {
        Map<String, Object> result = gitHubEvidenceService.getPRsWithoutApproval(owner, repo);
        return ResponseEntity.ok(result);
    }

    // Use Case 2: PRs reviewed by specific user
    @GetMapping("/prs-reviewed-by/{owner}/{repo}/{reviewer}")
    public ResponseEntity<Map<String, Object>> getPRsReviewedBy(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String reviewer) {
        Map<String, Object> result = gitHubEvidenceService.getPRsReviewedBy(owner, repo, reviewer);
        return ResponseEntity.ok(result);
    }

    // Use Case 3: PRs waiting for review > 24 hours
    @GetMapping("/prs-waiting-review/{owner}/{repo}")
    public ResponseEntity<Map<String, Object>> getPRsWaitingForReview(
            @PathVariable String owner,
            @PathVariable String repo) {
        Map<String, Object> result = gitHubEvidenceService.getPRsWaitingForReview(owner, repo);
        return ResponseEntity.ok(result);
    }

    // Use Case 4: PRs merged in last 7 days with approvers
    @GetMapping("/recent-merged-prs/{owner}/{repo}")
    public ResponseEntity<Map<String, Object>> getRecentlyMergedPRs(
            @PathVariable String owner,
            @PathVariable String repo) {
        Map<String, Object> result = gitHubEvidenceService.getRecentlyMergedPRs(owner, repo);
        return ResponseEntity.ok(result);
    }
}