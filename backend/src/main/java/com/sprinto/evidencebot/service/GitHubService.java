package com.sprinto.evidencebot.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class GitHubService {

    @Value("${github.api.token}")
    private String githubToken;

    @Value("${github.api.base-url}")
    private String baseUrl;

    private final WebClient webClient;

    public GitHubService() {
        this.webClient = WebClient.builder().build();
    }

    public Map<String, Object> getCommitEvidence(String owner, String repo, String query) {
        try {
            JsonNode commits = webClient.get()
                    .uri(baseUrl + "/repos/{owner}/{repo}/commits?q={query}&per_page=10", owner, repo, query)
                    .header("Authorization", "token " + githubToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<Map<String, Object>> evidence = new ArrayList<>();
            if (commits != null && commits.isArray()) {
                for (JsonNode commit : commits) {
                    Map<String, Object> commitData = new HashMap<>();
                    commitData.put("sha", commit.get("sha").asText());
                    commitData.put("message", commit.get("commit").get("message").asText());
                    commitData.put("author", commit.get("commit").get("author").get("name").asText());
                    commitData.put("date", commit.get("commit").get("author").get("date").asText());
                    commitData.put("url", commit.get("html_url").asText());
                    evidence.add(commitData);
                }
            }

            return Map.of(
                    "source", "GitHub Commits",
                    "repository", owner + "/" + repo,
                    "query", query,
                    "evidence", evidence,
                    "summary", "Found " + evidence.size() + " commits related to: " + query
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve GitHub evidence: " + e.getMessage());
        }
    }

    public Map<String, Object> getPullRequestEvidence(String owner, String repo, String state) {
        try {
            JsonNode prs = webClient.get()
                    .uri(baseUrl + "/repos/{owner}/{repo}/pulls?state={state}&per_page=10", owner, repo, state)
                    .header("Authorization", "token " + githubToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<Map<String, Object>> evidence = new ArrayList<>();
            if (prs != null && prs.isArray()) {
                for (JsonNode pr : prs) {
                    Map<String, Object> prData = new HashMap<>();
                    prData.put("number", pr.get("number").asInt());
                    prData.put("title", pr.get("title").asText());
                    prData.put("state", pr.get("state").asText());
                    prData.put("author", pr.get("user").get("login").asText());
                    prData.put("created_at", pr.get("created_at").asText());
                    prData.put("url", pr.get("html_url").asText());
                    evidence.add(prData);
                }
            }

            return Map.of(
                    "source", "GitHub Pull Requests",
                    "repository", owner + "/" + repo,
                    "state", state,
                    "evidence", evidence,
                    "summary", "Found " + evidence.size() + " " + state + " pull requests"
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve GitHub PR evidence: " + e.getMessage());
        }
    }
}