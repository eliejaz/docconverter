package com.docshifter.assessment.docconverter.controller;

import com.docshifter.assessment.docconverter.annotation.RateLimited;
import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.repository.DocumentRepository;
import com.docshifter.assessment.docconverter.service.ConversionService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversions")
public class ConversionController {
    private final ConversionService conversionService;
    private final DocumentRepository documentRepository;

    public ConversionController(ConversionService conversionService, DocumentRepository documentRepository) {
        this.conversionService = conversionService;
        this.documentRepository = documentRepository;
    }

    @RateLimited
    @PostMapping("/convert/pdf-to-word")
    public ResponseEntity<String> convertPdfToWord(@RequestParam("fileName") String fileName) {
        String conversionId = UUID.randomUUID().toString();
        Document document = new Document();
        document.setOriginalName(fileName);
        document.setConversionId(conversionId);
        document.setStatus("REQUESTED");
        document.setUploadedAt(LocalDateTime.now());
        documentRepository.save(document);

        conversionService.convertPdfToDocx(fileName, conversionId);
        return ResponseEntity.ok("Conversion started with ID: " + conversionId);
    }

    @RateLimited
    @PostMapping("/convert/pdf-to-word-text-only")
    public ResponseEntity<String> convertPdfToWordTextOnly(@RequestParam("fileName") String fileName) {
        String conversionId = UUID.randomUUID().toString();
        Document document = new Document();
        document.setOriginalName(fileName);
        document.setConversionId(conversionId);
        document.setStatus("REQUESTED");
        document.setUploadedAt(LocalDateTime.now());
        documentRepository.save(document);

        conversionService.convertPdfToText(fileName, conversionId);
        return ResponseEntity.ok("Conversion started with ID: " + conversionId);
    }

    @RateLimited
    @PostMapping("/convert/word-to-pdf")
    public ResponseEntity<String> convertWordToPdf(@RequestParam("fileName") String fileName) {
        String conversionId = UUID.randomUUID().toString();
        Document document = new Document();
        document.setOriginalName(fileName);
        document.setConversionId(conversionId);
        document.setStatus("REQUESTED");
        document.setUploadedAt(LocalDateTime.now());
        documentRepository.save(document);

        conversionService.convertWordToPdf(fileName, conversionId);
        return ResponseEntity.ok("Conversion started with ID: " + conversionId);
    }

    @GetMapping("/status/{conversionId}")
    public ResponseEntity<String> getConversionStatus(@PathVariable String conversionId) {
        String status = conversionService.getConversionStatus(conversionId);
        return ResponseEntity.ok("Conversion status: " + status);
    }

    @GetMapping("/download/{conversionId}")
    public ResponseEntity<InputStreamResource> downloadConvertedFile(@PathVariable String conversionId) {
        Path filePath = conversionService.getConvertedFilePath(conversionId);
        if (filePath == null || !Files.exists(filePath)) {
            return ResponseEntity.status(404).body(null);
        }

        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(filePath.toFile()));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filePath.getFileName().toString())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(Files.size(filePath))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
