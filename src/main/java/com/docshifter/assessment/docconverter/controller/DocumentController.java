package com.docshifter.assessment.docconverter.controller;

import com.docshifter.assessment.docconverter.annotation.RateLimited;
import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.service.DocumentService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @RateLimited
    @PostMapping(path = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            Document document = documentService.uploadDocument(file);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/files")
    public ResponseEntity<List<Document>> getAllUploadedFiles() {
        List<Document> files = documentService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/files/names")
    public ResponseEntity<List<String>> getAllUploadedFileNames() {
        List<String> fileNames = documentService.getAllUploadedFileNames();
        return ResponseEntity.ok(fileNames);
    }


    @DeleteMapping("/file")
    public ResponseEntity<String> deleteFile(@RequestParam("fileName") String fileName) {
        try {
            boolean deleted = documentService.deleteFile(fileName);
            if (deleted) {
                return ResponseEntity.ok("File deleted successfully.");
            } else {
                return ResponseEntity.status(404).body("File not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/files")
    public ResponseEntity<String> deleteAllFiles() {
        try {
            documentService.deleteAllFiles();
            return ResponseEntity.ok("All files deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/file")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("fileName") String fileName) {
        try {
            Path filePath = documentService.getFilePath(fileName);
            if (filePath != null && Files.exists(filePath)) {
                InputStreamResource resource = new InputStreamResource(new FileInputStream(filePath.toFile()));
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .contentLength(Files.size(filePath))
                        .body(resource);
            } else {
                return ResponseEntity.status(404).body(null);
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentDetails(@PathVariable Long id) {
        try {
            Document document = documentService.getDocumentById(id);
            if (document != null) {
                return ResponseEntity.ok(document);
            } else {
                return ResponseEntity.status(404).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
