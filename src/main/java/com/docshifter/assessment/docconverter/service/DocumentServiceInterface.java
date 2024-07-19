package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.model.DocumentStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DocumentServiceInterface {
    public Document uploadDocument(MultipartFile file) throws IOException;
    public boolean deleteFile(String fileName) throws IOException;
    public void deleteAllFiles() throws IOException;
    public Document getDocumentById(Long id);
    public String createRequestedDocument(Long fileId);
    public List<Document> getAllFiles();
    public Document getDocumentByOriginalName(String originalName);
    public List<String> getAllUploadedFileNames();
    public Optional<Document> getDocumentByConversionId(String conversionId);
    public Optional<Document> getCompletedDocumentByConversionID(String conversionId);

    public void updateDocumentStatus(String conversionId, DocumentStatus status);
    public void updateDocumentStatus(String conversionId, DocumentStatus status, String convertedName, LocalDateTime convertedAt, String convertedFilePath);

    public DocumentStatus getConversionStatus(String conversionId);
}
