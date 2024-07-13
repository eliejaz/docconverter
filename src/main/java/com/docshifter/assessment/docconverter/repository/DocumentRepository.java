package com.docshifter.assessment.docconverter.repository;

import com.docshifter.assessment.docconverter.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    void deleteByOriginalName(String originalName);

}