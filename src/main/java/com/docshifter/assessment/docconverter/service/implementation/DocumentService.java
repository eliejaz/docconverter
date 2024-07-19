package com.docshifter.assessment.docconverter.service.implementation;

import com.docshifter.assessment.docconverter.dto.StatusChangeNotification;
import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.model.DocumentStatus;
import com.docshifter.assessment.docconverter.repository.DocumentRepository;
import com.docshifter.assessment.docconverter.service.DocumentServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentService implements DocumentServiceInterface {
    private final Path uploadDir = Paths.get("uploads");

    private final SimpMessagingTemplate template;
    private final DocumentRepository documentRepository;


    public DocumentService(DocumentRepository documentRepository, SimpMessagingTemplate template) throws IOException {
        this.documentRepository = documentRepository;
        this.template = template;

        Files.createDirectories(uploadDir);
        Path convertedDir = Paths.get("converted");
        Files.createDirectories(convertedDir);
        log.info("Upload directory created: {}", uploadDir.toAbsolutePath());
        log.info("Converted directory created: {}", convertedDir.toAbsolutePath());

    }

    @CacheEvict(value = "documents", allEntries = true)
    public Document uploadDocument(MultipartFile file) throws IOException {
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        Path filePath = uploadDir.resolve(originalFilename);
        Files.write(filePath, file.getBytes());
        log.info("File uploaded: {}", filePath.toAbsolutePath());

        Document document = new Document();
        document.setOriginalName(originalFilename);
        document.setStatus(DocumentStatus.UPLOADED);
        document.setUploadedAt(LocalDateTime.now());
        document.setFilePath(filePath.toString());

        Document savedDocument = documentRepository.save(document);
        log.info("Document saved to database: {}", savedDocument);

        return savedDocument;
    }

    @Cacheable("documents")
    public List<String> getAllUploadedFileNames() {
        List<String> fileNames = documentRepository.findAll().stream()
                .filter(d -> d.getStatus().equals(DocumentStatus.UPLOADED))
                .map(Document::getOriginalName)
                .collect(Collectors.toList());
        log.info("Retrieved all uploaded file names: {}", fileNames);
        return fileNames;
    }

    @Transactional
    @CacheEvict(value = "documents", allEntries = true)
    public boolean deleteFile(String fileName) throws IOException {
        Path filePath = uploadDir.resolve(fileName);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("File deleted: {}", filePath.toAbsolutePath());
            documentRepository.deleteByOriginalName(fileName);
            log.info("Document with original name '{}' deleted from database", fileName);
            return true;
        }
        log.warn("File not found: {}", filePath.toAbsolutePath());
        return false;
    }

    @CacheEvict(value = "documents", allEntries = true)
    public void deleteAllFiles() throws IOException {
        List<Document> documents = documentRepository.findAll();
        for (Document document : documents) {
            Path filePath = uploadDir.resolve(document.getOriginalName());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted: {}", filePath.toAbsolutePath());
            } else {
                log.warn("File not found: {}", filePath.toAbsolutePath());
            }
        }
        documentRepository.deleteAll();
        log.info("All documents deleted from database");
    }

    @Cacheable(value = "document", key = "#id")
    public Document getDocumentById(Long id) {
        Optional<Document> document = documentRepository.findById(id);
        if (document.isPresent()) {
            log.info("Document retrieved: {}", document.get());
        } else {
            log.error("Document with id '{}' not found", id);
            throw  new RuntimeException("Document with id" + id + " not found");

        }
        return document.orElse(null);
    }

    @CacheEvict(value = "documents", allEntries = true)
    public String createRequestedDocument(Long fileId){
        Document orignalDocument = getDocumentById(fileId);

        String conversionId = UUID.randomUUID().toString();
        Document document = new Document();
        document.setOriginalName(orignalDocument.getOriginalName());
        document.setConversionId(conversionId);
        document.setStatus(DocumentStatus.REQUESTED);
        document.setUploadedAt(LocalDateTime.now());
        documentRepository.save(document);
        return conversionId;
    }

    @Cacheable("documents")
    public List<Document> getAllFiles() {
        return documentRepository.findAll();
    }

    public Document getDocumentByOriginalName(String originalName) {
        return documentRepository.findByOriginalName(originalName);
    }

    @CacheEvict(value = "documents", allEntries = true)
    public void updateDocumentStatus(String conversionId, DocumentStatus status) {
        Optional<Document> documentOptional = getDocumentByConversionId(conversionId);
        documentOptional.ifPresent(document -> {
            document.setStatus(status);
            documentRepository.save(document);
            sendNotificationStatusChange(document);
        });
    }

    public Optional<Document> getDocumentByConversionId(String conversionId) {
        return  documentRepository.findByConversionId(conversionId);
    }

    @CacheEvict(value = "documents", allEntries = true)
    public void updateDocumentStatus(String conversionId, DocumentStatus status, String convertedName, LocalDateTime convertedAt, String convertedFilePath) {
        Optional<Document> documentOptional = getDocumentByConversionId(conversionId);
        documentOptional.ifPresent(document -> {
            document.setStatus(status);
            document.setConvertedName(convertedName);
            document.setConvertedAt(convertedAt);
            document.setConvertedFilePath(convertedFilePath);
            documentRepository.save(document);

            sendNotificationStatusChange(document);
        });
    }

    public DocumentStatus getConversionStatus(String conversionId) {
        Optional<Document> documentOptional = documentRepository.findByConversionId(conversionId);
        return documentOptional.map(Document::getStatus).orElse(DocumentStatus.UNKNOWN);
    }

    public Optional<Document> getCompletedDocumentByConversionID(String conversionId) {
        Optional<Document> documentOptional = documentRepository.findByConversionId(conversionId);
        return documentOptional.filter(document -> DocumentStatus.COMPLETED.equals(document.getStatus()));
    }


    private void sendNotificationStatusChange(Document document) {
        StatusChangeNotification statusChangeNotification = new StatusChangeNotification();
        statusChangeNotification.setFileId(document.getId());
        statusChangeNotification.setConvertedAt(document.getConvertedAt());
        statusChangeNotification.setConvertedFilePath(document.getConvertedFilePath());
        statusChangeNotification.setConvertedName(document.getConvertedName());
        statusChangeNotification.setNewDocumentStatus(document.getStatus());
        log.info("Sending to websocket...");
        template.convertAndSend("/topic/notification", statusChangeNotification);
    }

}
