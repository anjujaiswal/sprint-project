package com.sprinto.evidencebot.service;

import com.sprinto.evidencebot.model.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class EvidenceService {

    @Autowired
    private DocumentService documentService;
    
    @Autowired
    private OpenAIService openAIService;

    public Map<String, Object> generateEvidence(String query) {
        List<Document> relevantDocs = documentService.searchDocuments(query);
        
        Map<String, Object> evidence = new HashMap<>();
        evidence.put("query", query);
        evidence.put("documentsFound", relevantDocs.size());
        evidence.put("documents", relevantDocs);
        
        // Generate AI-powered summary
        String aiSummary = generateAISummary(relevantDocs, query);
        evidence.put("summary", aiSummary);
        evidence.put("basicSummary", generateSummary(relevantDocs, query));
        
        return evidence;
    }
    
    private String generateAISummary(List<Document> documents, String query) {
        if (documents.isEmpty()) {
            return "No documents found for the query: " + query;
        }
        
        List<String> documentContents = documents.stream()
                .map(Document::getContent)
                .filter(content -> content != null && !content.trim().isEmpty())
                .collect(Collectors.toList());
        
        if (documentContents.isEmpty()) {
            return "No readable content found in the documents for analysis.";
        }
        
        return openAIService.generateEvidenceSummary(query, documentContents);
    }

    private String generateSummary(List<Document> documents, String query) {
        if (documents.isEmpty()) {
            return "No evidence found for the query: " + query;
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Found ").append(documents.size()).append(" document(s) related to '").append(query).append("':\n\n");
        
        for (Document doc : documents) {
            summary.append("• ").append(doc.getFilename()).append(" (").append(doc.getFileType()).append(")\n");
            
            String content = doc.getContent();
            if (content != null && content.length() > 200) {
                summary.append("  Preview: ").append(content.substring(0, 200)).append("...\n\n");
            } else if (content != null) {
                summary.append("  Content: ").append(content).append("\n\n");
            }
        }
        
        return summary.toString();
    }

    public Map<String, Object> getComplianceReport(String domain) {
        List<Document> allDocs = documentService.getAllDocuments();
        
        Map<String, Object> report = new HashMap<>();
        report.put("domain", domain);
        report.put("totalDocuments", allDocs.size());
        report.put("lastUpdated", java.time.LocalDateTime.now());
        
        // Basic compliance check based on document types
        long policyDocs = allDocs.stream()
            .filter(doc -> doc.getFilename().toLowerCase().contains("policy") || 
                          doc.getContent().toLowerCase().contains("policy"))
            .count();
        
        long procedureDocs = allDocs.stream()
            .filter(doc -> doc.getFilename().toLowerCase().contains("procedure") || 
                          doc.getContent().toLowerCase().contains("procedure"))
            .count();
        
        report.put("policyDocuments", policyDocs);
        report.put("procedureDocuments", procedureDocs);
        report.put("complianceScore", calculateComplianceScore(policyDocs, procedureDocs, allDocs.size()));
        
        // Add AI-powered compliance analysis
        if (!allDocs.isEmpty()) {
            List<String> documentContents = allDocs.stream()
                    .map(Document::getContent)
                    .filter(content -> content != null && !content.trim().isEmpty())
                    .collect(Collectors.toList());
            
            if (!documentContents.isEmpty()) {
                String aiAnalysis = openAIService.analyzeComplianceGaps(domain, documentContents);
                report.put("aiAnalysis", aiAnalysis);
            }
        }
        
        return report;
    }

    private double calculateComplianceScore(long policies, long procedures, long total) {
        if (total == 0) return 0.0;
        double score = ((policies + procedures) / (double) total) * 100;
        return Math.min(score, 100.0);
    }
}