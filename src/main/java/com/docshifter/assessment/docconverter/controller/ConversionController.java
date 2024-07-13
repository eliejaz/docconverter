package com.docshifter.assessment.docconverter.controller;

import com.docshifter.assessment.docconverter.service.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversions")
public class ConversionController {
    private final ConversionService conversionService;

    public ConversionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @PostMapping("/convert/pdf-to-word")
    public ResponseEntity<String> convertPdfToWord(@RequestParam("fileName") String fileName) {
        try {
            String outputFilePath = conversionService.convertPdfToDocx(fileName);
            return ResponseEntity.ok(outputFilePath);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Conversion failed: " + e.getMessage());
        }
    }

    @PostMapping("/convert/word-to-pdf")
    public ResponseEntity<String> convertWordToPdf(@RequestParam("fileName") String fileName) {
        try {
            String outputFilePath = conversionService.convertWordToPdf(fileName);
            return ResponseEntity.ok(outputFilePath);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Conversion failed: " + e.getMessage());
        }
    }
}
