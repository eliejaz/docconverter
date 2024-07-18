package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.converter.PdfToTextConverter;
import com.docshifter.assessment.docconverter.converter.PdfToWordConverter;
import com.docshifter.assessment.docconverter.converter.WordToPdfConverter;
import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.model.DocumentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ConversionService {
    private final Path convertedDir = Paths.get("converted");

    private final DocumentService documentService;
    private final PdfToTextConverter pdfToTextConverter;
    private final WordToPdfConverter wordToPdfConverter;
    private final PdfToWordConverter pdfToWordConverter;

    public ConversionService(PdfToTextConverter pdfToTextConverter, WordToPdfConverter wordToPdfConverter, PdfToWordConverter pdfToWordConverter, DocumentService documentService) {
        this.pdfToTextConverter = pdfToTextConverter;
        this.wordToPdfConverter = wordToPdfConverter;
        this.pdfToWordConverter = pdfToWordConverter;
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
        pdfToTextConverter.convert(inputFile.toFile(), outputFile.toFile());
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
        wordToPdfConverter.convert(inputFile.toFile(), outputFile.toFile());
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
        pdfToWordConverter.convert(inputFile.toFile(), outputFile.toFile());
        documentService.updateDocumentStatus(conversionId, DocumentStatus.COMPLETED, outputFile.getFileName().toString(), LocalDateTime.now(), outputFile.toString());
    }

    private static void checkInputPAth(Path inputFile) {
        if (inputFile == null) {
            log.error("Original file name was not found");
            throw new RuntimeException("Original file name was not found");
        }
    }


    private void checkRetrievedDocFilePath(String conversionId, Document doc) {
        if (doc.getFilePath() == null){
            documentService.updateDocumentStatus(conversionId, DocumentStatus.FAILED);
            log.error("Original doc file path is null");
            throw new RuntimeException("Original doc file path is null");
        }
    }


}
