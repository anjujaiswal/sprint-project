package com.sprinto.evidencebot.controller;

import com.sprinto.evidencebot.model.AssetRecord;
import com.sprinto.evidencebot.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadAssetRegister(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(assetService.uploadAssetRegister(file));
    }

    @GetMapping
    public ResponseEntity<List<AssetRecord>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    @GetMapping("/search")
    public ResponseEntity<List<AssetRecord>> searchAssets(@RequestParam String query) {
        return ResponseEntity.ok(assetService.searchAssets(query));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportToCsv(@RequestParam(required = false) String filter) {
        byte[] csvData = assetService.exportToCsv(filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=assets.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(@RequestParam(required = false) String filter) {
        byte[] excelData = assetService.exportToExcel(filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=assets.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    // Internal method for service layer
    public List<AssetRecord> getAllAssetsInternal() {
        return assetService.getAllAssets();
    }
}