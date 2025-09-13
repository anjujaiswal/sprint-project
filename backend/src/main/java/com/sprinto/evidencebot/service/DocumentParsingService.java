package com.sprinto.evidencebot.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class DocumentParsingService {

    @Autowired
    private EscalationService escalationService;

    public Map<String, Object> parseAndExtractEvidence(String query, List<String> filePaths) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> extractedData = new ArrayList<>();
        
        for (String filePath : filePaths) {
            try {
                Map<String, Object> fileData = parseFile(filePath, query);
                if (fileData != null && !fileData.isEmpty()) {
                    extractedData.add(fileData);
                }
            } catch (Exception e) {
                System.err.println("Error parsing file: " + filePath + " - " + e.getMessage());
            }
        }
        
        if (extractedData.isEmpty()) {
            Map<String, Object> escalation = escalationService.createEscalation(query, 
                "No relevant information found in document repository");
            result.put("escalation", escalation);
            result.put("found", false);
        } else {
            result.put("found", true);
            result.put("data", extractedData);
        }
        
        result.put("query", query);
        result.put("filesProcessed", filePaths.size());
        result.put("summary", generateSummary(extractedData, query));
        
        return result;
    }

    private Map<String, Object> parseFile(String filePath, String query) throws IOException {
        String extension = getFileExtension(filePath);
        
        switch (extension.toLowerCase()) {
            case "xlsx":
            case "xls": return parseExcel(filePath, query);
            case "csv": return parseCSV(filePath, query);
            default: return null;
        }
    }



    private Map<String, Object> parseExcel(String filePath, String query) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath))) {
            List<Map<String, Object>> relevantRows = new ArrayList<>();
            
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                
                for (Row row : sheet) {
                    StringBuilder rowText = new StringBuilder();
                    for (Cell cell : row) {
                        rowText.append(getCellValue(cell)).append(" ");
                    }
                    
                    if (containsQuery(rowText.toString(), query)) {
                        Map<String, Object> rowData = new HashMap<>();
                        List<String> cellValues = new ArrayList<>();
                        for (Cell cell : row) {
                            cellValues.add(getCellValue(cell));
                        }
                        rowData.put("rowNumber", row.getRowNum() + 1);
                        rowData.put("sheetName", sheet.getSheetName());
                        rowData.put("data", cellValues);
                        relevantRows.add(rowData);
                    }
                }
            }
            
            if (!relevantRows.isEmpty()) {
                Map<String, Object> data = new HashMap<>();
                data.put("fileName", new File(filePath).getName());
                data.put("fileType", "Excel");
                data.put("relevantRows", relevantRows);
                return data;
            }
        }
        return null;
    }

    private Map<String, Object> parseCSV(String filePath, String query) throws IOException {
        List<Map<String, Object>> relevantRows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int rowNumber = 0;
            String[] headers = null;
            
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                String[] values = line.split(",");
                
                if (rowNumber == 1) {
                    headers = values;
                    continue;
                }
                
                if (containsQuery(line, query)) {
                    Map<String, Object> rowData = new HashMap<>();
                    Map<String, String> fieldData = new HashMap<>();
                    
                    for (int i = 0; i < values.length && i < headers.length; i++) {
                        fieldData.put(headers[i].trim(), values[i].trim());
                    }
                    
                    rowData.put("rowNumber", rowNumber);
                    rowData.put("data", fieldData);
                    relevantRows.add(rowData);
                }
            }
        }
        
        if (!relevantRows.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("fileName", new File(filePath).getName());
            data.put("fileType", "CSV");
            data.put("relevantRows", relevantRows);
            return data;
        }
        return null;
    }

    private List<String> findRelevantContent(String text, String query) {
        List<String> relevantLines = new ArrayList<>();
        String[] lines = text.split("\n");
        
        for (String line : lines) {
            if (containsQuery(line, query)) {
                relevantLines.add(line.trim());
            }
        }
        return relevantLines;
    }

    private boolean containsQuery(String text, String query) {
        String[] queryTerms = query.toLowerCase().split("\\s+");
        String lowerText = text.toLowerCase();
        
        for (String term : queryTerms) {
            if (lowerText.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    private String getFileExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        return lastDot > 0 ? filePath.substring(lastDot + 1) : "";
    }

    private String generateSummary(List<Map<String, Object>> extractedData, String query) {
        if (extractedData.isEmpty()) {
            return "No relevant information found for query: " + query;
        }
        
        int totalFiles = extractedData.size();
        int totalMatches = extractedData.stream()
            .mapToInt(data -> {
                Object content = data.get("relevantContent");
                Object rows = data.get("relevantRows");
                if (content instanceof List) return ((List<?>) content).size();
                if (rows instanceof List) return ((List<?>) rows).size();
                return 1;
            })
            .sum();
            
        return String.format("Found %d matches across %d files for query: %s", 
            totalMatches, totalFiles, query);
    }
}