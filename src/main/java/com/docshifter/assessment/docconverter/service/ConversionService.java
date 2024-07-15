package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.converter.PdfToTextConverter;
import com.docshifter.assessment.docconverter.converter.PdfToWordConverter;
import com.docshifter.assessment.docconverter.converter.WordToPdfConverter;
import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.model.DocumentStatus;
import com.docshifter.assessment.docconverter.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class ConversionService {
    private final Path uploadDir = Paths.get("uploads");
    private final Path convertedDir = Paths.get("converted");


    private final DocumentRepository documentRepository;
    private final PdfToTextConverter pdfToTextConverter;
    private final WordToPdfConverter wordToPdfConverter;
    private final PdfToWordConverter pdfToWordConverter;

    public ConversionService(PdfToTextConverter pdfToTextConverter, WordToPdfConverter wordToPdfConverter, PdfToWordConverter pdfToWordConverter, DocumentRepository documentRepository) {
        this.pdfToTextConverter = pdfToTextConverter;
        this.wordToPdfConverter = wordToPdfConverter;
        this.pdfToWordConverter = pdfToWordConverter;
        this.documentRepository = documentRepository;
    }

    @Async
    @Transactional
    public void convertPdfToText(String fileName, String conversionId) {
        Path inputFile = uploadDir.resolve(fileName);
        Path outputFile = convertedDir.resolve(fileName.replace(".pdf", ".docx"));
        updateDocumentStatus(conversionId, DocumentStatus.IN_PROGRESS);
        pdfToTextConverter.convert(inputFile.toFile(), outputFile.toFile());
        updateDocumentStatus(conversionId, DocumentStatus.COMPLETED, outputFile.getFileName().toString(), LocalDateTime.now(), outputFile.toString());
    }

    @Async
    @Transactional
    public void convertWordToPdf(String fileName, String conversionId) {
        Path inputFile = uploadDir.resolve(fileName);
        Path outputFile = convertedDir.resolve(fileName.replace(".docx", ".pdf"));
        updateDocumentStatus(conversionId, DocumentStatus.IN_PROGRESS);
        wordToPdfConverter.convert(inputFile.toFile(), outputFile.toFile());
        updateDocumentStatus(conversionId, DocumentStatus.COMPLETED, outputFile.getFileName().toString(), LocalDateTime.now(), outputFile.toString());
    }

    @Async
    @Transactional
    public void convertPdfToDocx(String fileName, String conversionId) {
        Path inputFile = uploadDir.resolve(fileName);
        Path outputFile = convertedDir.resolve(fileName.replace(".pdf", ".docx"));
        log.info("Converting to Doc: {}", inputFile.toFile().getAbsolutePath());
        updateDocumentStatus(conversionId, DocumentStatus.IN_PROGRESS);
        pdfToWordConverter.convert(inputFile.toFile(), outputFile.toFile());
        updateDocumentStatus(conversionId, DocumentStatus.COMPLETED, outputFile.getFileName().toString(), LocalDateTime.now(), outputFile.toString());
    }

    private void updateDocumentStatus(String conversionId, DocumentStatus status) {
        Optional<Document> documentOptional = documentRepository.findByConversionId(conversionId);
        documentOptional.ifPresent(document -> {
            document.setStatus(status);
            documentRepository.save(document);
        });
    }

    private void updateDocumentStatus(String conversionId, DocumentStatus status, String convertedName, LocalDateTime convertedAt, String convertedFilePath) {
        Optional<Document> documentOptional = documentRepository.findByConversionId(conversionId);
        documentOptional.ifPresent(document -> {
            document.setStatus(status);
            document.setConvertedName(convertedName);
            document.setConvertedAt(convertedAt);
            document.setConvertedFilePath(convertedFilePath);
            documentRepository.save(document);
        });
    }

    public DocumentStatus getConversionStatus(String conversionId) {
        Optional<Document> documentOptional = documentRepository.findByConversionId(conversionId);
        return documentOptional.map(Document::getStatus).orElse(DocumentStatus.UNKNOWN);
    }

    public Optional<Document> getDocumentWithConvertedFilePath(String conversionId) {
        Optional<Document> documentOptional = documentRepository.findByConversionId(conversionId);
        return documentOptional.filter(document -> DocumentStatus.COMPLETED.equals(document.getStatus()));
    }

}
