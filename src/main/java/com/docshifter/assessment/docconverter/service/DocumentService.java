package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.repository.DocumentRepository;
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
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final Path uploadDir = Paths.get("uploads");

    public DocumentService(DocumentRepository documentRepository) throws IOException {
        this.documentRepository = documentRepository;
        Files.createDirectories(uploadDir);
    }

    public Document uploadDocument(MultipartFile file) throws IOException {
        Path filePath = uploadDir.resolve(Objects.requireNonNull(file.getOriginalFilename()));
        Files.write(filePath, file.getBytes());

        Document document = new Document();
        document.setOriginalName(file.getOriginalFilename());
        document.setStatus("Uploaded");
        document.setUploadedAt(LocalDateTime.now());
        document.setFilePath(filePath.toString());

        return documentRepository.save(document);
    }

    public List<String> getAllUploadedFileNames() {
        return documentRepository.findAll().stream()
                .map(Document::getOriginalName)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean deleteFile(String fileName) throws IOException {
        Path filePath = uploadDir.resolve(fileName);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            documentRepository.deleteByOriginalName(fileName);
            return true;
        }
        return false;
    }

    public void deleteAllFiles() throws IOException {
        List<Document> documents = documentRepository.findAll();
        for (Document document : documents) {
            Path filePath = uploadDir.resolve(document.getOriginalName());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        }
        documentRepository.deleteAll();
    }

    public Path getFilePath(String fileName) {
        return uploadDir.resolve(fileName);
    }

    public Document getDocumentById(Long id) {
        Optional<Document> document = documentRepository.findById(id);
        return document.orElse(null);
    }
}
