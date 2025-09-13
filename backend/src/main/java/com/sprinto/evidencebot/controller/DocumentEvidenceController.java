package com.sprinto.evidencebot.controller;

import com.sprinto.evidencebot.service.DocumentParsingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/document-evidence")
@CrossOrigin(origins = "*")
public class DocumentEvidenceController {

    @Autowired
    private DocumentParsingService documentParsingService;

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchDocumentEvidence(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query is required"));
        }
        
        // Get files from docs directory
        List<String> filePaths = getDocumentFiles();
        
        Map<String, Object> result = documentParsingService.parseAndExtractEvidence(query, filePaths);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/available-documents")
    public ResponseEntity<Map<String, Object>> getAvailableDocuments() {
        List<String> filePaths = getDocumentFiles();
        List<Map<String, Object>> documents = new ArrayList<>();
        
        for (String filePath : filePaths) {
            File file = new File(filePath);
            Map<String, Object> docInfo = new HashMap<>();
            docInfo.put("name", file.getName());
            docInfo.put("path", filePath);
            docInfo.put("size", file.length());
            docInfo.put("type", getFileType(file.getName()));
            documents.add(docInfo);
        }
        
        return ResponseEntity.ok(Map.of(
            "documents", documents,
            "totalCount", documents.size()
        ));
    }

    private List<String> getDocumentFiles() {
        List<String> filePaths = new ArrayList<>();
        String docsPath = "docs/";
        
        File docsDir = new File(docsPath);
        if (docsDir.exists() && docsDir.isDirectory()) {
            File[] files = docsDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".pdf") ||
                name.toLowerCase().endsWith(".xlsx") ||
                name.toLowerCase().endsWith(".xls") ||
                name.toLowerCase().endsWith(".csv")
            );
            
            if (files != null) {
                for (File file : files) {
                    filePaths.add(file.getAbsolutePath());
                }
            }
        }
        
        return filePaths;
    }

    private String getFileType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "PDF Document";
            case "xlsx":
            case "xls": return "Excel Spreadsheet";
            case "csv": return "CSV File";
            default: return "Unknown";
        }
    }
}