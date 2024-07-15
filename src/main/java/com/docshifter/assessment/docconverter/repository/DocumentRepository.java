package com.docshifter.assessment.docconverter.repository;

import com.docshifter.assessment.docconverter.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    void deleteByOriginalName(String originalName);

    Optional<Document> findByConversionId(String conversionId);

    Document findByOriginalName(String originalName);
}