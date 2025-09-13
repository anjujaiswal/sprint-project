package com.sprinto.evidencebot.service;

import com.sprinto.evidencebot.model.AssetRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssetService {
    
    private List<AssetRecord> assets = new ArrayList<>();

    public Map<String, Object> uploadAssetRegister(MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.getOriginalFilename().endsWith(".csv")) {
                parseCSV(file.getInputStream());
            } else if (file.getOriginalFilename().endsWith(".xlsx") || file.getOriginalFilename().endsWith(".xls")) {
                parseExcel(file.getInputStream());
            }
            response.put("success", true);
            response.put("assetsLoaded", assets.size());
            response.put("message", "Asset register uploaded successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    private void parseCSV(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        boolean isHeader = true;
        
        while ((line = reader.readLine()) != null) {
            if (isHeader) {
                isHeader = false;
                continue;
            }
            String[] parts = line.split(",");
            if (parts.length >= 4) {
                AssetRecord asset = new AssetRecord();
                asset.setAssetId(parts[0].trim());
                asset.setAssetType(parts[1].trim());
                asset.setLocation(parts[2].trim());
                asset.setOwner(parts[3].trim());
                asset.setStatus(parts.length > 4 ? parts[4].trim() : "Active");
                assets.add(asset);
            }
        }
    }

    private void parseExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                AssetRecord asset = new AssetRecord();
                asset.setAssetId(getCellValue(row.getCell(0)));
                asset.setAssetType(getCellValue(row.getCell(1)));
                asset.setLocation(getCellValue(row.getCell(2)));
                asset.setOwner(getCellValue(row.getCell(3)));
                asset.setStatus(getCellValue(row.getCell(4)));
                assets.add(asset);
            }
        }
        workbook.close();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf(cell.getNumericCellValue());
    }

    public List<AssetRecord> getAllAssets() {
        return new ArrayList<>(assets);
    }

    public List<AssetRecord> searchAssets(String query) {
        String lowerQuery = query.toLowerCase();
        return assets.stream()
                .filter(asset -> 
                    asset.getAssetType().toLowerCase().contains(lowerQuery) ||
                    asset.getLocation().toLowerCase().contains(lowerQuery) ||
                    asset.getOwner().toLowerCase().contains(lowerQuery) ||
                    asset.getAssetId().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    public byte[] exportToCsv(String filter) {
        List<AssetRecord> filteredAssets = filter != null ? searchAssets(filter) : assets;
        StringBuilder csv = new StringBuilder();
        csv.append("Asset ID,Asset Type,Location,Owner,Status\n");
        
        for (AssetRecord asset : filteredAssets) {
            csv.append(String.format("%s,%s,%s,%s,%s\n",
                asset.getAssetId(), asset.getAssetType(), asset.getLocation(), 
                asset.getOwner(), asset.getStatus()));
        }
        
        return csv.toString().getBytes();
    }

    public byte[] exportToExcel(String filter) {
        List<AssetRecord> filteredAssets = filter != null ? searchAssets(filter) : assets;
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Assets");
            
            // Header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Asset ID");
            header.createCell(1).setCellValue("Asset Type");
            header.createCell(2).setCellValue("Location");
            header.createCell(3).setCellValue("Owner");
            header.createCell(4).setCellValue("Status");
            
            // Data
            for (int i = 0; i < filteredAssets.size(); i++) {
                AssetRecord asset = filteredAssets.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(asset.getAssetId());
                row.createCell(1).setCellValue(asset.getAssetType());
                row.createCell(2).setCellValue(asset.getLocation());
                row.createCell(3).setCellValue(asset.getOwner());
                row.createCell(4).setCellValue(asset.getStatus());
            }
            
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error creating Excel file", e);
        }
    }
}