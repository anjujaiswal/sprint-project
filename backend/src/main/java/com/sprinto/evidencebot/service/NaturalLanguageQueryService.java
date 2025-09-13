package com.sprinto.evidencebot.service;

import com.sprinto.evidencebot.model.AssetRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NaturalLanguageQueryService {
    
    @Autowired
    private AssetService assetService;
    
    @Autowired
    private EscalationService escalationService;
    
    public Map<String, Object> processQuery(String query) {
        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("queryType", detectQueryType(query));
        
        String queryType = (String) response.get("queryType");
        
        switch (queryType) {
            case "GITHUB_PR":
                return processGitHubPRQuery(query);
            case "ASSET_SEARCH":
                return processAssetQuery(query);
            default:
                return processGeneralQuery(query);
        }
    }
    
    private String detectQueryType(String query) {
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("pr #") || lowerQuery.contains("pull request") || lowerQuery.contains("merged")) {
            return "GITHUB_PR";
        }
        return "ASSET_SEARCH";
    }
    
    private Map<String, Object> processGitHubPRQuery(String query) {
        Map<String, Object> response = new HashMap<>();
        
        Pattern prPattern = Pattern.compile("pr #(\\d+)|pull request #?(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = prPattern.matcher(query);
        
        if (matcher.find()) {
            String prNumber = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            response.put("prNumber", prNumber);
            response.put("answer", "PR #" + prNumber + " analysis requires repository context. Please specify the repository (owner/repo) to investigate this pull request.");
        } else {
            response.put("answer", "Could not identify specific PR number. Please specify the PR number (e.g., 'PR #456').");
        }
        
        response.put("queryType", "GITHUB_PR");
        return response;
    }
    
    private Map<String, Object> processAssetQuery(String query) {
        Map<String, Object> response = new HashMap<>();
        
        List<AssetRecord> allAssets = assetService.getAllAssets();
        List<AssetRecord> results = assetService.searchAssets(query);
        
        response.put("queryType", "ASSET_SEARCH");
        response.put("searchTerm", query);
        response.put("resultsCount", results.size());
        response.put("assets", results);
        
        if (results.isEmpty()) {
            response.put("answer", "No assets found matching '" + query + "'. Consider uploading asset register or checking spelling.");
            Map<String, Object> escalation = escalationService.createEscalation(query, "Asset not found in current repository");
            response.put("escalation", escalation);
        } else {
            response.put("answer", "Found " + results.size() + " asset(s) matching '" + query + "'.");
        }
        
        return response;
    }
    
    private Map<String, Object> processGeneralQuery(String query) {
        Map<String, Object> response = new HashMap<>();
        response.put("queryType", "GENERAL");
        response.put("answer", "I can help you with GitHub PR analysis or asset searches. Please be more specific.");
        return response;
    }
}