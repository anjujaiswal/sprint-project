package com.sprinto.evidencebot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String filename;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "file_type")
    private String fileType;
    
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;
    
    @Column(name = "file_size")
    private Long fileSize;

    public Document() {}

    public Document(String filename, String content, String fileType, Long fileSize) {
        this.filename = filename;
        this.content = content;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}