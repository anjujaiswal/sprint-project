package com.sprinto.evidencebot.controller;

import com.sprinto.evidencebot.model.Document;
import com.sprinto.evidencebot.service.DocumentService;
import com.sprinto.evidencebot.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private DocumentService documentService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chatWithDocuments(@RequestBody Map<String, String> request) {
        String userQuery = request.get("query");
        String conversationHistory = request.get("history");

        if (userQuery == null || userQuery.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query is required"));
        }

        // Get all documents for context
        List<Document> allDocs = documentService.getAllDocuments();
        List<String> documentContents = allDocs.stream()
                .map(Document::getContent)
                .filter(content -> content != null && !content.trim().isEmpty())
                .collect(Collectors.toList());

        String response;
        if (documentContents.isEmpty()) {
            response = "I don't have access to any documents yet. Please upload some compliance documents first, and I'll be happy to help you analyze them!";
        } else {
            response = openAIService.chatWithDocuments(userQuery, documentContents, conversationHistory);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("response", response);
        result.put("documentsCount", allDocs.size());
        result.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/analyze-document")
    public ResponseEntity<Map<String, Object>> analyzeSpecificDocument(@RequestBody Map<String, Object> request) {
        Long documentId = Long.valueOf(request.get("documentId").toString());
        String query = (String) request.get("query");

        if (documentId == null || query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Document ID and query are required"));
        }

        Document document = documentService.getDocumentById(documentId);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }

        if (document.getContent() == null || document.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Document has no readable content"));
        }

        String analysis = openAIService.generateEvidenceSummary(query, List.of(document.getContent()));

        Map<String, Object> result = new HashMap<>();
        result.put("documentId", documentId);
        result.put("documentName", document.getFilename());
        result.put("query", query);
        result.put("analysis", analysis);
        result.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getQuerySuggestions() {
        List<String> suggestions = List.of(
                "What are our data privacy policies?",
                "Show me information about access controls",
                "What security procedures do we have?",
                "Are there any compliance gaps?",
                "What training materials are available?",
                "Show me our incident response procedures",
                "What are the requirements for GDPR compliance?",
                "How do we handle sensitive data?",
                "What audit evidence do we have?",
                "Are our policies up to date?"
        );

        Map<String, Object> result = new HashMap<>();
        result.put("suggestions", suggestions);
        result.put("totalDocuments", documentService.getAllDocuments().size());

        return ResponseEntity.ok(result);
    }
}