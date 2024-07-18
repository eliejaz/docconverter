package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.converter.PdfToTextConverter;
import com.docshifter.assessment.docconverter.converter.PdfToWordConverter;
import com.docshifter.assessment.docconverter.converter.WordToPdfConverter;
import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.model.DocumentStatus;
import com.docshifter.assessment.docconverter.repository.DocumentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConversionServiceTest {
    private final Path pdfPath = Paths.get("/uploads/test.pdf");
    private final Path docxPath = Paths.get("/uploads/test.docx");

    @InjectMocks
    private ConversionService conversionService;

    @Mock
    private PdfToTextConverter pdfToTextConverter;
    @Mock
    private WordToPdfConverter wordToPdfConverter;
    @Mock
    private PdfToWordConverter pdfToWordConverter;
    @Mock
    private DocumentService documentService;

    private String conversionId;
    private Document orginalDocxDocument;
    private Document originalPDfDocument;

    @BeforeEach
    void setUp() throws IOException {
        conversionId = UUID.randomUUID().toString();

        orginalDocxDocument = new Document();
        orginalDocxDocument.setId(2L);
        orginalDocxDocument.setOriginalName("test.docx");
        orginalDocxDocument.setStatus(DocumentStatus.UPLOADED);
        orginalDocxDocument.setUploadedAt(LocalDateTime.now());
        orginalDocxDocument.setFilePath(docxPath.toString());

        originalPDfDocument = new Document();
        originalPDfDocument.setId(2L);
        originalPDfDocument.setOriginalName("test.pdf");
        originalPDfDocument.setStatus(DocumentStatus.UPLOADED);
        originalPDfDocument.setUploadedAt(LocalDateTime.now());
        originalPDfDocument.setFilePath(pdfPath.toString());


        Files.createDirectories(pdfPath.getParent());
        Files.deleteIfExists(pdfPath);
        Files.deleteIfExists(docxPath);
        Files.createFile(pdfPath);
        Files.createFile(docxPath);
    }
    @AfterEach
    void afterEach() throws IOException {
        Files.deleteIfExists(pdfPath);
        Files.deleteIfExists(docxPath);
    }

    @Test
    void testConvertPdfToText() throws IOException {
        Path outputFile = Paths.get("converted/test.docx");

        doNothing().when(pdfToTextConverter).convert(any(File.class), any(File.class));

        when(documentService.getDocumentById(2L)).thenReturn(originalPDfDocument);

        conversionService.convertPdfToText(2L, conversionId);

        verify(pdfToTextConverter, times(1)).convert(Paths.get(originalPDfDocument.getFilePath()).toFile(), outputFile.toFile());
        verify(documentService, times(1)).updateDocumentStatus(eq(conversionId),  eq(DocumentStatus.COMPLETED), eq(outputFile.getFileName().toString()), any(LocalDateTime.class), eq(outputFile.toString()));

    }

    @Test
    void testConvertWordToPdf() throws IOException {
        Path outputFile = Paths.get("converted/test.pdf");

        doNothing().when(wordToPdfConverter).convert(any(File.class), any(File.class));

        when(documentService.getDocumentById(2L)).thenReturn(orginalDocxDocument);

        conversionService.convertWordToPdf(2L, conversionId);

        verify(wordToPdfConverter, times(1)).convert(Paths.get(orginalDocxDocument.getFilePath()).toFile(), outputFile.toFile());
        verify(documentService, times(1)).updateDocumentStatus(eq(conversionId),  eq(DocumentStatus.COMPLETED), eq(outputFile.getFileName().toString()), any(LocalDateTime.class), eq(outputFile.toString()));
    }

    @Test
    void testConvertPdfToDocx() throws IOException {
        Path outputFile = Paths.get("converted/test.docx");

        doNothing().when(pdfToWordConverter).convert(any(File.class), any(File.class));

        when(documentService.getDocumentById(2L)).thenReturn(originalPDfDocument);

        conversionService.convertPdfToDocx(2L, conversionId);

        verify(pdfToWordConverter, times(1)).convert(Paths.get(originalPDfDocument.getFilePath()).toFile(), outputFile.toFile());
        verify(documentService, times(1)).updateDocumentStatus(eq(conversionId),  eq(DocumentStatus.COMPLETED), eq(outputFile.getFileName().toString()), any(LocalDateTime.class), eq(outputFile.toString()));
    }
}
