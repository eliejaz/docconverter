package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final Path uploadDir = Paths.get("uploads");


    public DocumentService(DocumentRepository documentRepository) throws IOException {
        this.documentRepository = documentRepository;
        Files.createDirectories(uploadDir);
        Path convertedDir = Paths.get("converted");
        Files.createDirectories(convertedDir);
        log.info("Upload directory created: {}", uploadDir.toAbsolutePath());
        log.info("Converted directory created: {}", convertedDir.toAbsolutePath());

    }

    public Document uploadDocument(MultipartFile file) throws IOException {
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        Path filePath = uploadDir.resolve(originalFilename);
        Files.write(filePath, file.getBytes());
        log.info("File uploaded: {}", filePath.toAbsolutePath());

        Document document = new Document();
        document.setOriginalName(originalFilename);
        document.setStatus("Uploaded");
        document.setUploadedAt(LocalDateTime.now());
        document.setFilePath(filePath.toString());

        Document savedDocument = documentRepository.save(document);
        log.info("Document saved to database: {}", savedDocument);

        return savedDocument;
    }

    public List<String> getAllUploadedFileNames() {
        List<String> fileNames = documentRepository.findAll().stream()
                .filter(d -> d.getStatus().equals("Uploaded"))
                .map(Document::getOriginalName)
                .collect(Collectors.toList());
        log.info("Retrieved all uploaded file names: {}", fileNames);
        return fileNames;
    }

    @Transactional
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

    public Path getFilePath(String fileName) {
        Path filePath = uploadDir.resolve(fileName);
        log.info("Retrieved file path: {}", filePath.toAbsolutePath());
        return filePath;
    }

    public Document getDocumentById(Long id) {
        Optional<Document> document = documentRepository.findById(id);
        if (document.isPresent()) {
            log.info("Document retrieved: {}", document.get());
        } else {
            log.warn("Document with id '{}' not found", id);
        }
        return document.orElse(null);
    }

    public List<Document> getAllFiles() {
        return documentRepository.findAll();
    }
}
