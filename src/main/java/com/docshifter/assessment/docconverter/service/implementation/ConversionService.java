package com.docshifter.assessment.docconverter.service.implementation;

import com.docshifter.assessment.docconverter.converter.DocumentConversionType;
import com.docshifter.assessment.docconverter.converter.DocumentConverterFactory;
import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.model.DocumentStatus;
import com.docshifter.assessment.docconverter.service.ConversionServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ConversionService implements ConversionServiceInterface {
    private final Path convertedDir = Paths.get("converted");

    private final DocumentService documentService;
    private final DocumentConverterFactory documentConverterFactory;

    public ConversionService(DocumentConverterFactory documentConverterFactory, DocumentService documentService) {
        this.documentConverterFactory = documentConverterFactory;
        this.documentService = documentService;
    }

    @Async
    @Transactional
    public void convertPdfToText(Long fileId, String conversionId) {
        Document doc = documentService.getDocumentById(fileId);
        checkRetrievedDocFilePath(conversionId, doc);

        Path inputFile = Paths.get(doc.getFilePath());

        Path outputFile = convertedDir.resolve(doc.getOriginalName().replace(".pdf", ".docx"));

        documentService.updateDocumentStatus(conversionId, DocumentStatus.IN_PROGRESS);
        documentConverterFactory.getConverter(DocumentConversionType.PDF_TO_TEXT).convert(inputFile.toFile(), outputFile.toFile());
        documentService.updateDocumentStatus(conversionId, DocumentStatus.COMPLETED, outputFile.getFileName().toString(), LocalDateTime.now(), outputFile.toString());
    }


    @Async
    @Transactional
    public void convertWordToPdf(Long fileId, String conversionId) {
        Document doc = documentService.getDocumentById(fileId);
        checkRetrievedDocFilePath(conversionId, doc);

        Path inputFile = Paths.get(doc.getFilePath());

        Path outputFile = convertedDir.resolve(doc.getOriginalName().replace(".docx", ".pdf"));
        documentService.updateDocumentStatus(conversionId, DocumentStatus.IN_PROGRESS);
        documentConverterFactory.getConverter(DocumentConversionType.WORD_TO_PDF).convert(inputFile.toFile(), outputFile.toFile());
        documentService.updateDocumentStatus(conversionId, DocumentStatus.COMPLETED, outputFile.getFileName().toString(), LocalDateTime.now(), outputFile.toString());
    }

    @Async
    @Transactional
    public void convertPdfToDocx(Long fileId, String conversionId) {
        Document doc = documentService.getDocumentById(fileId);
        checkRetrievedDocFilePath(conversionId, doc);

        Path inputFile = Paths.get(doc.getFilePath());

        Path outputFile = convertedDir.resolve(doc.getOriginalName().replace(".pdf", ".docx"));
        log.info("Converting to Doc: {}", inputFile.toFile().getAbsolutePath());
        documentService.updateDocumentStatus(conversionId, DocumentStatus.IN_PROGRESS);
        documentConverterFactory.getConverter(DocumentConversionType.PDF_TO_WORD).convert(inputFile.toFile(), outputFile.toFile());
        documentService.updateDocumentStatus(conversionId, DocumentStatus.COMPLETED, outputFile.getFileName().toString(), LocalDateTime.now(), outputFile.toString());
    }


    private void checkRetrievedDocFilePath(String conversionId, Document doc) {
        if (doc.getFilePath() == null) {
            documentService.updateDocumentStatus(conversionId, DocumentStatus.FAILED);
            log.error("Original doc file path is null");
            throw new RuntimeException("Original doc file path is null");
        }
    }


}
