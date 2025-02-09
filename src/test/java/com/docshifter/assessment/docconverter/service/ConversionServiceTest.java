package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.converter.DocumentConversionType;
import com.docshifter.assessment.docconverter.converter.DocumentConverterFactory;
import com.docshifter.assessment.docconverter.converter.implementation.PdfToTextConverter;
import com.docshifter.assessment.docconverter.converter.implementation.PdfToWordConverter;
import com.docshifter.assessment.docconverter.converter.implementation.WordToPdfConverter;
import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.model.DocumentStatus;
import com.docshifter.assessment.docconverter.service.implementation.ConversionService;
import com.docshifter.assessment.docconverter.service.implementation.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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
    private DocumentConverterFactory documentConverterFactory;
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

    }

    @Test
    void testConvertPdfToText() throws IOException {
        Path outputFile = Paths.get("converted/test.docx");

        when(documentConverterFactory.getConverter(DocumentConversionType.PDF_TO_TEXT)).thenReturn(pdfToTextConverter);
        doNothing().when(pdfToTextConverter).convert(any(File.class), any(File.class));

        when(documentService.getDocumentById(2L)).thenReturn(originalPDfDocument);

        conversionService.convertPdfToText(2L, conversionId);

        verify(pdfToTextConverter, times(1)).convert(Paths.get(originalPDfDocument.getFilePath()).toFile(), outputFile.toFile());
        verify(documentService, times(1)).updateDocumentStatus(eq(conversionId),  eq(DocumentStatus.COMPLETED), eq(outputFile.getFileName().toString()), any(LocalDateTime.class), eq(outputFile.toString()));

    }

    @Test
    void testConvertWordToPdf() throws IOException {
        Path outputFile = Paths.get("converted/test.pdf");

        when(documentConverterFactory.getConverter(DocumentConversionType.WORD_TO_PDF)).thenReturn(wordToPdfConverter);
        doNothing().when(wordToPdfConverter).convert(any(File.class), any(File.class));

        when(documentService.getDocumentById(2L)).thenReturn(orginalDocxDocument);

        conversionService.convertWordToPdf(2L, conversionId);

        verify(wordToPdfConverter, times(1)).convert(Paths.get(orginalDocxDocument.getFilePath()).toFile(), outputFile.toFile());
        verify(documentService, times(1)).updateDocumentStatus(eq(conversionId),  eq(DocumentStatus.COMPLETED), eq(outputFile.getFileName().toString()), any(LocalDateTime.class), eq(outputFile.toString()));
    }

    @Test
    void testConvertPdfToDocx() throws IOException {
        Path outputFile = Paths.get("converted/test.docx");

        when(documentConverterFactory.getConverter(DocumentConversionType.PDF_TO_WORD)).thenReturn(pdfToWordConverter);
        doNothing().when(pdfToWordConverter).convert(any(File.class), any(File.class));

        when(documentService.getDocumentById(2L)).thenReturn(originalPDfDocument);

        conversionService.convertPdfToDocx(2L, conversionId);

        verify(pdfToWordConverter, times(1)).convert(Paths.get(originalPDfDocument.getFilePath()).toFile(), outputFile.toFile());
        verify(documentService, times(1)).updateDocumentStatus(eq(conversionId),  eq(DocumentStatus.COMPLETED), eq(outputFile.getFileName().toString()), any(LocalDateTime.class), eq(outputFile.toString()));
    }
}
