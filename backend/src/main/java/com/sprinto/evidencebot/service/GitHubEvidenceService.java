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
        try {
            StringBuilder uriBuilder = new StringBuilder(baseUrl + "/repos/" + owner + "/" + repo + "/commits?per_page=50");
            if (since != null) uriBuilder.append("&since=").append(since);
            if (until != null) uriBuilder.append("&until=").append(until);
            if (author != null) uriBuilder.append("&author=").append(author);

            JsonNode commits = webClient.get()
                    .uri(uriBuilder.toString())
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
                        data.put("message", message.split("\n")[0]); // First line only
                        data.put("author", commit.get("commit").get("author").get("name").asText());
                        data.put("date", formatDate(commit.get("commit").get("author").get("date").asText()));
                        data.put("url", commit.get("html_url").asText());
                        data.put("files_changed", getFilesChanged(commit));
                        evidence.add(data);
                    }
                }
            }

            return Map.of(
                "source", "GitHub Commits",
                "repository", owner + "/" + repo,
                "query_params", buildQuerySummary(query, since, until, author),
                "evidence", evidence,
                "summary", generateCommitSummary(evidence, query),
                "human_readable", generateHumanReadableCommitSummary(evidence, owner, repo)
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve commits: " + e.getMessage());
        }
    }

    private Map<String, Object> getPullRequests(String owner, String repo, String query, 
                                              String since, String until, String author) {
        try {
            String uri = baseUrl + "/repos/" + owner + "/" + repo + "/pulls?state=all&per_page=50";
            
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
                    if (matchesFilters(pr, query, since, until, author, "title", "user", "created_at")) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("number", pr.get("number").asInt());
                        data.put("title", pr.get("title").asText());
                        data.put("state", pr.get("state").asText());
                        data.put("author", pr.get("user").get("login").asText());
                        data.put("created_at", formatDate(pr.get("created_at").asText()));
                        data.put("merged_at", pr.has("merged_at") && !pr.get("merged_at").isNull() ? 
                                formatDate(pr.get("merged_at").asText()) : "Not merged");
                        data.put("url", pr.get("html_url").asText());
                        evidence.add(data);
                    }
                }
            }

            return Map.of(
                "source", "GitHub Pull Requests",
                "repository", owner + "/" + repo,
                "query_params", buildQuerySummary(query, since, until, author),
                "evidence", evidence,
                "summary", "Found " + evidence.size() + " pull requests",
                "human_readable", generateHumanReadablePRSummary(evidence, owner, repo)
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve PRs: " + e.getMessage());
        }
    }

    private Map<String, Object> getIssues(String owner, String repo, String query, 
                                         String since, String until, String author) {
        try {
            String uri = baseUrl + "/repos/" + owner + "/" + repo + "/issues?state=all&per_page=50";
            
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
                    if (!issue.has("pull_request") && matchesFilters(issue, query, since, until, author, "title", "user", "created_at")) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("number", issue.get("number").asInt());
                        data.put("title", issue.get("title").asText());
                        data.put("state", issue.get("state").asText());
                        data.put("author", issue.get("user").get("login").asText());
                        data.put("created_at", formatDate(issue.get("created_at").asText()));
                        data.put("closed_at", issue.has("closed_at") && !issue.get("closed_at").isNull() ? 
                                formatDate(issue.get("closed_at").asText()) : "Open");
                        data.put("labels", getLabels(issue));
                        data.put("url", issue.get("html_url").asText());
                        evidence.add(data);
                    }
                }
            }

            return Map.of(
                "source", "GitHub Issues",
                "repository", owner + "/" + repo,
                "query_params", buildQuerySummary(query, since, until, author),
                "evidence", evidence,
                "summary", "Found " + evidence.size() + " issues",
                "human_readable", generateHumanReadableIssueSummary(evidence, owner, repo)
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve issues: " + e.getMessage());
        }
    }

    public byte[] generateCSVReport(String owner, String repo, String type) {
        Map<String, Object> evidence = getEvidence(owner, repo, null, type);
        StringBuilder csv = new StringBuilder();
        
        if (type.equals("commits")) {
            csv.append("SHA,Message,Author,Date,Files Changed,URL\n");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> commits = (List<Map<String, Object>>) evidence.get("evidence");
            for (Map<String, Object> commit : commits) {
                csv.append(String.format("%s,\"%s\",%s,%s,%s,%s\n",
                    commit.get("sha"), commit.get("message"), commit.get("author"),
                    commit.get("date"), commit.get("files_changed"), commit.get("url")));
            }
        } else if (type.equals("prs")) {
            csv.append("Number,Title,State,Author,Created,Merged,URL\n");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) evidence.get("evidence");
            for (Map<String, Object> item : items) {
                csv.append(String.format("%s,\"%s\",%s,%s,%s,%s,%s\n",
                    item.get("number"), item.get("title"), item.get("state"),
                    item.get("author"), item.get("created_at"), item.get("merged_at"), item.get("url")));
            }
        } else {
            csv.append("Number,Title,State,Author,Created,Closed,Labels,URL\n");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) evidence.get("evidence");
            for (Map<String, Object> item : items) {
                csv.append(String.format("%s,\"%s\",%s,%s,%s,%s,\"%s\",%s\n",
                    item.get("number"), item.get("title"), item.get("state"),
                    item.get("author"), item.get("created_at"), item.get("closed_at"),
                    item.get("labels"), item.get("url")));
            }
        }
        
        return csv.toString().getBytes();
    }

    private String formatDate(String isoDate) {
        return isoDate.substring(0, 10); // YYYY-MM-DD format
    }

    private String buildQuerySummary(String query, String since, String until, String author) {
        List<String> params = new ArrayList<>();
        if (query != null) params.add("query: " + query);
        if (since != null) params.add("since: " + since);
        if (until != null) params.add("until: " + until);
        if (author != null) params.add("author: " + author);
        return params.isEmpty() ? "all records" : String.join(", ", params);
    }

    private boolean matchesFilters(JsonNode item, String query, String since, String until, String author, 
                                 String titleField, String userField, String dateField) {
        if (query != null && !item.get(titleField).asText().toLowerCase().contains(query.toLowerCase())) {
            return false;
        }
        if (author != null && !item.get(userField).get("login").asText().equalsIgnoreCase(author)) {
            return false;
        }
        // Date filtering would require more complex logic - simplified for now
        return true;
    }

    private String getFilesChanged(JsonNode commit) {
        return "N/A"; // Would require additional API call to get commit details
    }

    private String getLabels(JsonNode issue) {
        if (!issue.has("labels") || !issue.get("labels").isArray()) return "";
        List<String> labels = new ArrayList<>();
        for (JsonNode label : issue.get("labels")) {
            labels.add(label.get("name").asText());
        }
        return String.join(", ", labels);
    }

    private String generateCommitSummary(List<Map<String, Object>> evidence, String query) {
        if (evidence.isEmpty()) return "No commits found";
        return String.format("Found %d commits%s. Latest: %s by %s", 
            evidence.size(), 
            query != null ? " matching '" + query + "'" : "",
            evidence.get(0).get("message"),
            evidence.get(0).get("author"));
    }

    private String generateHumanReadableCommitSummary(List<Map<String, Object>> evidence, String owner, String repo) {
        if (evidence.isEmpty()) return "No commit activity found in " + owner + "/" + repo;
        
        Map<String, Long> authorCounts = evidence.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                commit -> (String) commit.get("author"),
                java.util.stream.Collectors.counting()));
        
        String topAuthor = authorCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Unknown");
        
        return String.format("Repository %s/%s shows %d commits. Most active contributor: %s (%d commits). " +
                           "Latest activity on %s.", 
            owner, repo, evidence.size(), topAuthor, authorCounts.get(topAuthor),
            evidence.get(0).get("date"));
    }

    private String generateHumanReadablePRSummary(List<Map<String, Object>> evidence, String owner, String repo) {
        if (evidence.isEmpty()) return "No pull request activity found in " + owner + "/" + repo;
        
        long merged = evidence.stream().mapToLong(pr -> 
            !"Not merged".equals(pr.get("merged_at")) ? 1 : 0).sum();
        long open = evidence.stream().mapToLong(pr -> 
            "open".equals(pr.get("state")) ? 1 : 0).sum();
        
        return String.format("Repository %s/%s has %d pull requests: %d merged, %d open. " +
                           "This indicates active code review and collaboration processes.",
            owner, repo, evidence.size(), merged, open);
    }

    private String generateHumanReadableIssueSummary(List<Map<String, Object>> evidence, String owner, String repo) {
        if (evidence.isEmpty()) return "No issues found in " + owner + "/" + repo;
        
        long open = evidence.stream().mapToLong(issue -> 
            "open".equals(issue.get("state")) ? 1 : 0).sum();
        long closed = evidence.size() - open;
        
        return String.format("Repository %s/%s has %d issues: %d open, %d closed. " +
                           "This shows project maintenance and issue tracking activity.",
            owner, repo, evidence.size(), open, closed);
    }

    public Map<String, Object> getRepositories(String owner) {
        try {
            String uri = baseUrl + "/users/" + owner + "/repos?per_page=30&sort=updated";
            
            JsonNode repos = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<Map<String, Object>> repositories = new ArrayList<>();
            if (repos != null && repos.isArray()) {
                for (JsonNode repo : repos) {
                    Map<String, Object> repoData = new HashMap<>();
                    repoData.put("name", repo.get("name").asText());
                    repoData.put("full_name", repo.get("full_name").asText());
                    repoData.put("description", repo.has("description") && !repo.get("description").isNull() ? 
                            repo.get("description").asText() : "No description");
                    repoData.put("private", repo.get("private").asBoolean());
                    repoData.put("updated_at", formatDate(repo.get("updated_at").asText()));
                    repoData.put("url", repo.get("html_url").asText());
                    repositories.add(repoData);
                }
            }

            return Map.of(
                "owner", owner,
                "repositories", repositories,
                "count", repositories.size(),
                "summary", "Found " + repositories.size() + " repositories for " + owner
            );
        } catch (Exception e) {
            return Map.of("error", "Failed to retrieve repositories: " + e.getMessage());
        }
    }
}