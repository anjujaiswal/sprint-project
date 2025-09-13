package com.sprinto.evidencebot.repository;

import com.sprinto.evidencebot.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    @Query("SELECT d FROM Document d WHERE LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Document> findByContentContainingIgnoreCase(@Param("keyword") String keyword);
    
    @Query("SELECT d FROM Document d WHERE LOWER(d.filename) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Document> findByFilenameContainingIgnoreCase(@Param("keyword") String keyword);
    
    List<Document> findByFileType(String fileType);
}