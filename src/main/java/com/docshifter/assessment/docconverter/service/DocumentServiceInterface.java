package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.model.DocumentStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DocumentServiceInterface {
    Document uploadDocument(MultipartFile file) throws IOException;

    boolean deleteFile(String fileName) throws IOException;

    void deleteAllFiles() throws IOException;

    Document getDocumentById(Long id);

    String createRequestedDocument(Long fileId);

    List<Document> getAllFiles();

    Document getDocumentByOriginalName(String originalName);

    List<String> getAllUploadedFileNames();

    Optional<Document> getDocumentByConversionId(String conversionId);

    Optional<Document> getCompletedDocumentByConversionID(String conversionId);

    void updateDocumentStatus(String conversionId, DocumentStatus status);

    void updateDocumentStatus(String conversionId, DocumentStatus status, String convertedName, LocalDateTime convertedAt, String convertedFilePath);

    DocumentStatus getConversionStatus(String conversionId);
}
