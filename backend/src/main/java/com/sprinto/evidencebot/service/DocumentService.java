package com.sprinto.evidencebot.service;

import com.sprinto.evidencebot.model.Document;
import com.sprinto.evidencebot.repository.DocumentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    public Document uploadDocument(MultipartFile file) throws IOException {
        String content = extractTextFromFile(file);
        Document document = new Document(
            file.getOriginalFilename(),
            content,
            file.getContentType(),
            file.getSize()
        );
        return documentRepository.save(document);
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        
        if (contentType != null) {
            if (contentType.equals("application/pdf")) {
                return extractTextFromPDF(file);
            } else if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                return extractTextFromWord(file);
            } else if (contentType.startsWith("text/")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        }
        
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    private String extractTextFromPDF(MultipartFile file) throws IOException {
        try (PDDocument document = org.apache.pdfbox.Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromWord(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream());
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    public List<Document> searchDocuments(String query) {
        return documentRepository.findByContentContainingIgnoreCase(query);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
}