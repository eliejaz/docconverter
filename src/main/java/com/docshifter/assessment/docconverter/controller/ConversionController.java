package com.docshifter.assessment.docconverter.controller;

import com.docshifter.assessment.docconverter.annotation.RateLimited;
import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.model.DocumentStatus;
import com.docshifter.assessment.docconverter.service.ConversionService;
import com.docshifter.assessment.docconverter.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/conversions")
public class ConversionController {
    private final ConversionService conversionService;
    private final DocumentService documentService;

    public ConversionController(ConversionService conversionService, DocumentService documentService) {
        this.conversionService = conversionService;
        this.documentService = documentService;
    }

    @RateLimited
    @PostMapping("/convert/pdf-to-word")
    public ResponseEntity<String> convertPdfToWord(@RequestParam("fileName") String fileName) {
        String conversionId = documentService.createRequestedDocument(fileName);

        conversionService.convertPdfToDocx(fileName, conversionId);
        return ResponseEntity.ok("Conversion started with ID: " + conversionId);
    }

    @RateLimited
    @PostMapping("/convert/pdf-to-word-text-only")
    public ResponseEntity<String> convertPdfToWordTextOnly(@RequestParam("fileName") String fileName) {
        String conversionId = documentService.createRequestedDocument(fileName);

        conversionService.convertPdfToText(fileName, conversionId);
        return ResponseEntity.ok("Conversion started with ID: " + conversionId);
    }

    @RateLimited
    @PostMapping("/convert/word-to-pdf")
    public ResponseEntity<String> convertWordToPdf(@RequestParam("fileName") String fileName) {
        String conversionId = documentService.createRequestedDocument(fileName);

        conversionService.convertWordToPdf(fileName, conversionId);
        return ResponseEntity.ok("Conversion started with ID: " + conversionId);
    }

    @GetMapping("/status/{conversionId}")
    public ResponseEntity<String> getConversionStatus(@PathVariable String conversionId) {
        DocumentStatus status = conversionService.getConversionStatus(conversionId);
        return ResponseEntity.ok("Conversion status: " + status);
    }

    @GetMapping("/download/{conversionId}")
    public ResponseEntity<?> downloadConvertedFile(@PathVariable String conversionId) {
        log.info("Request received to download file with conversion ID: {}", conversionId);
        Optional<Document> documentOptional = conversionService.getDocumentWithConvertedFilePath(conversionId);

        if (documentOptional.isEmpty()) {
            log.warn("Document not found or conversion not completed for conversion ID: {}", conversionId);
            return ResponseEntity.status(404).body("Document not found or conversion not completed.");
        }

        Document document = documentOptional.get();
        Path filePath = Paths.get(document.getConvertedFilePath());

        if (!Files.exists(filePath)) {
            log.warn("File not found at path: {}", filePath.toAbsolutePath());
            return ResponseEntity.status(404).body("File not found.");
        }

        try {
            log.info("File found for conversion ID: {}, path: {}", conversionId, filePath.toAbsolutePath());
            InputStreamResource resource = new InputStreamResource(new FileInputStream(filePath.toFile()));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filePath.getFileName().toString())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(Files.size(filePath))
                    .body(resource);
        } catch (IOException e) {
            log.error("Error occurred while processing the file download for conversion ID: {}", conversionId, e);
            return ResponseEntity.status(500).body("An error occurred while processing the file download.");
        }
    }

}
