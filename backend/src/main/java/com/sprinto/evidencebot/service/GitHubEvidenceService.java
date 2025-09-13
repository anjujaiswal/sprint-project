package com.sprinto.evidencebot.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class GitHubEvidenceService {

    @Value("${github.api.token}")
    private String githubToken;

    @Value("${github.api.base-url}")
    private String baseUrl;

    private final WebClient webClient = WebClient.builder().build();

    public Map<String, Object> getEvidence(String owner, String repo, String query, String type) {
        return getEvidenceWithParams(owner, repo, query, type, null, null, null);
    }

    public Map<String, Object> getEvidenceWithParams(String owner, String repo, String query, String type, 
                                                   String since, String until, String author) {
        switch (type.toLowerCase()) {
            case "commits": return getCommitsByQuery(owner, repo, query, since, until, author);
            case "prs": return getPullRequests(owner, repo, query, since, until, author);
            case "issues": return getIssues(owner, repo, query, since, until, author);
            default: return getCommitsByQuery(owner, repo, query, since, until, author);
        }
    }

    private Map<String, Object> getCommitsByQuery(String owner, String repo, String query, 
                                                 String since, String until, String author) {
        String uri = baseUrl + "/repos/" + owner + "/" + repo + "/commits?per_page=20";
        try {
            JsonNode commits = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<Map<String, Object>> evidence = new ArrayList<>();
            
            if (commits != null && commits.isArray()) {
                for (JsonNode commit : commits) {
                    String message = commit.get("commit").get("message").asText();
                    if (query == null || message.toLowerCase().contains(query.toLowerCase())) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("sha", commit.get("sha").asText().substring(0, 8));
                        data.put("message", message.split("\n")[0]);
                        data.put("author", commit.get("commit").get("author").get("name").asText());
                        data.put("date", commit.get("commit").get("author").get("date").asText());
                        data.put("url", commit.get("html_url").asText());
                        evidence.add(data);
                    }
                }
            }

            return Map.of(
                "source", "GitHub Commits",
                "repository", owner + "/" + repo,
                "evidence", evidence,
                "summary", "Found " + evidence.size() + " commits"
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve commits: " + e.getMessage() + ". API URL: " + uri);
        }
    }

    private Map<String, Object> getPullRequests(String owner, String repo, String query, 
                                              String since, String until, String author) {
        String uri = baseUrl + "/repos/" + owner + "/" + repo + "/pulls?state=all&per_page=20";
        try {
            JsonNode prs = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<Map<String, Object>> evidence = new ArrayList<>();
            if (prs != null && prs.isArray()) {
                for (JsonNode pr : prs) {
                    if (query == null || pr.get("title").asText().toLowerCase().contains(query.toLowerCase())) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("number", pr.get("number").asInt());
                        data.put("title", pr.get("title").asText());
                        data.put("state", pr.get("state").asText());
                        data.put("author", pr.get("user").get("login").asText());
                        data.put("created_at", pr.get("created_at").asText());
                        data.put("url", pr.get("html_url").asText());
                        evidence.add(data);
                    }
                }
            }

            return Map.of(
                "source", "GitHub Pull Requests",
                "repository", owner + "/" + repo,
                "evidence", evidence,
                "summary", "Found " + evidence.size() + " pull requests"
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve PRs: " + e.getMessage() + ". API URL: " + uri);
        }
    }

    private Map<String, Object> getIssues(String owner, String repo, String query, 
                                         String since, String until, String author) {
        String uri = baseUrl + "/repos/" + owner + "/" + repo + "/issues?state=all&per_page=20";
        try {
            JsonNode issues = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<Map<String, Object>> evidence = new ArrayList<>();
            if (issues != null && issues.isArray()) {
                for (JsonNode issue : issues) {
                    if (!issue.has("pull_request") && (query == null || issue.get("title").asText().toLowerCase().contains(query.toLowerCase()))) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("number", issue.get("number").asInt());
                        data.put("title", issue.get("title").asText());
                        data.put("state", issue.get("state").asText());
                        data.put("author", issue.get("user").get("login").asText());
                        data.put("created_at", issue.get("created_at").asText());
                        data.put("url", issue.get("html_url").asText());
                        evidence.add(data);
                    }
                }
            }

            return Map.of(
                "source", "GitHub Issues",
                "repository", owner + "/" + repo,
                "evidence", evidence,
                "summary", "Found " + evidence.size() + " issues"
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve issues: " + e.getMessage() + ". API URL: " + uri);
        }
    }

    public Map<String, Object> getPRsWithoutApproval(String owner, String repo) {
        String uri = baseUrl + "/repos/" + owner + "/" + repo + "/pulls?state=closed&per_page=50";
        try {
            JsonNode prs = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<Map<String, Object>> evidence = new ArrayList<>();
            int count = 0;
            
            if (prs != null && prs.isArray()) {
                for (JsonNode pr : prs) {
                    if (pr.get("merged_at") != null && !pr.get("merged_at").isNull()) {
                        JsonNode reviews = getReviews(owner, repo, pr.get("number").asInt());
                        boolean hasApproval = false;
                        
                        if (reviews != null && reviews.isArray()) {
                            for (JsonNode review : reviews) {
                                if ("APPROVED".equals(review.get("state").asText())) {
                                    hasApproval = true;
                                    break;
                                }
                            }
                        }
                        
                        if (!hasApproval) {
                            count++;
                            Map<String, Object> data = new HashMap<>();
                            data.put("pr_id", pr.get("number").asInt());
                            data.put("title", pr.get("title").asText());
                            JsonNode mergedBy = pr.get("merged_by");
                            data.put("merged_by", (mergedBy != null && !mergedBy.isNull()) ? mergedBy.get("login").asText() : "Unknown");
                            data.put("reviews", new ArrayList<>());
                            data.put("merged_at", pr.get("merged_at").asText());
                            evidence.add(data);
                        }
                    }
                }
            }
            
            return Map.of(
                "source", "PRs Without Approval",
                "repository", owner + "/" + repo,
                "evidence", evidence,
                "count", count,
                "summary", "Found " + count + " PRs merged without approval"
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve PRs: " + e.getMessage());
        }
    }

    public Map<String, Object> getPRsReviewedBy(String owner, String repo, String reviewer) {
        String uri = baseUrl + "/repos/" + owner + "/" + repo + "/pulls?state=all&per_page=50";
        try {
            JsonNode prs = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<Map<String, Object>> evidence = new ArrayList<>();
            
            if (prs != null && prs.isArray()) {
                for (JsonNode pr : prs) {
                    JsonNode reviews = getReviews(owner, repo, pr.get("number").asInt());
                    
                    if (reviews != null && reviews.isArray()) {
                        for (JsonNode review : reviews) {
                            JsonNode user = review.get("user");
                            if (user != null && reviewer.equals(user.get("login").asText())) {
                                Map<String, Object> data = new HashMap<>();
                                data.put("pr_id", pr.get("number").asInt());
                                data.put("title", pr.get("title").asText());
                                data.put("reviewer", reviewer);
                                data.put("decision", review.get("state").asText());
                                data.put("date", review.get("submitted_at").asText());
                                evidence.add(data);
                                break;
                            }
                        }
                    }
                }
            }
            
            return Map.of(
                "source", "PRs Reviewed by " + reviewer,
                "repository", owner + "/" + repo,
                "evidence", evidence,
                "summary", "Found " + evidence.size() + " PRs reviewed by " + reviewer
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve PR reviews: " + e.getMessage());
        }
    }

    public Map<String, Object> getPRsWaitingForReview(String owner, String repo) {
        String uri = baseUrl + "/repos/" + owner + "/" + repo + "/pulls?state=open&per_page=50";
        try {
            JsonNode prs = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<Map<String, Object>> evidence = new ArrayList<>();
            long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
            
            if (prs != null && prs.isArray()) {
                for (JsonNode pr : prs) {
                    String createdAt = pr.get("created_at").asText();
                    long createdTime = java.time.Instant.parse(createdAt).toEpochMilli();
                    
                    if (createdTime < oneDayAgo) {
                        JsonNode reviews = getReviews(owner, repo, pr.get("number").asInt());
                        boolean hasReviews = reviews != null && reviews.isArray() && reviews.size() > 0;
                        
                        if (!hasReviews) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("pr_id", pr.get("number").asInt());
                            data.put("title", pr.get("title").asText());
                            data.put("created_at", createdAt);
                            data.put("review_requested", false);
                            data.put("waiting_time", (System.currentTimeMillis() - createdTime) / (1000 * 60 * 60) + " hours");
                            evidence.add(data);
                        }
                    }
                }
            }
            
            return Map.of(
                "source", "PRs Waiting for Review",
                "repository", owner + "/" + repo,
                "evidence", evidence,
                "summary", "Found " + evidence.size() + " PRs waiting for review > 24 hours"
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve waiting PRs: " + e.getMessage());
        }
    }

    public Map<String, Object> getRecentlyMergedPRs(String owner, String repo) {
        String uri = baseUrl + "/repos/" + owner + "/" + repo + "/pulls?state=closed&per_page=50";
        try {
            JsonNode prs = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<Map<String, Object>> evidence = new ArrayList<>();
            long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
            
            if (prs != null && prs.isArray()) {
                for (JsonNode pr : prs) {
                    if (pr.get("merged_at") != null && !pr.get("merged_at").isNull()) {
                        String mergedAt = pr.get("merged_at").asText();
                        long mergedTime = java.time.Instant.parse(mergedAt).toEpochMilli();
                        
                        if (mergedTime > sevenDaysAgo) {
                            JsonNode reviews = getReviews(owner, repo, pr.get("number").asInt());
                            List<String> approvers = new ArrayList<>();
                            
                            if (reviews != null && reviews.isArray()) {
                                for (JsonNode review : reviews) {
                                    if ("APPROVED".equals(review.get("state").asText())) {
                                        JsonNode user = review.get("user");
                                        if (user != null) {
                                            approvers.add(user.get("login").asText());
                                        }
                                    }
                                }
                            }
                            
                            Map<String, Object> data = new HashMap<>();
                            data.put("pr_id", pr.get("number").asInt());
                            data.put("title", pr.get("title").asText());
                            data.put("merged_at", mergedAt);
                            data.put("approvers", approvers);
                            evidence.add(data);
                        }
                    }
                }
            }
            
            return Map.of(
                "source", "Recently Merged PRs (Last 7 Days)",
                "repository", owner + "/" + repo,
                "evidence", evidence,
                "summary", "Found " + evidence.size() + " PRs merged in the last 7 days"
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve recent PRs: " + e.getMessage());
        }
    }

    private JsonNode getReviews(String owner, String repo, int prNumber) {
        String uri = baseUrl + "/repos/" + owner + "/" + repo + "/pulls/" + prNumber + "/reviews";
        try {
            return webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] generateCSVReport(String owner, String repo, String type) {
        Map<String, Object> evidence = getEvidence(owner, repo, null, type);
        StringBuilder csv = new StringBuilder();
        
        switch (type.toLowerCase()) {
            case "commits":
                csv.append("SHA,Message,Author,Date,URL\n");
                break;
            case "prs":
                csv.append("Number,Title,State,Author,Created,URL\n");
                break;
            case "issues":
                csv.append("Number,Title,State,Author,Created,URL\n");
                break;
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) evidence.get("evidence");
        if (items != null) {
            for (Map<String, Object> item : items) {
                switch (type.toLowerCase()) {
                    case "commits":
                        csv.append(String.format("%s,\"%s\",\"%s\",%s,%s\n",
                            item.get("sha"), item.get("message"), item.get("author"),
                            item.get("date"), item.get("url")));
                        break;
                    case "prs":
                    case "issues":
                        csv.append(String.format("%s,\"%s\",%s,\"%s\",%s,%s\n",
                            item.get("number"), item.get("title"), item.get("state"),
                            item.get("author"), item.get("created_at"), item.get("url")));
                        break;
                }
            }
        }
        
        return csv.toString().getBytes();
    }
}