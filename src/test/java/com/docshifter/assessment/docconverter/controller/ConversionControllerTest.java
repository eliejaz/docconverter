package com.docshifter.assessment.docconverter.controller;

import com.docshifter.assessment.docconverter.dto.ConversionResponse;
import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.model.DocumentStatus;
import com.docshifter.assessment.docconverter.service.implementation.ConversionService;
import com.docshifter.assessment.docconverter.service.implementation.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversionControllerTest {

    @Mock
    private ConversionService conversionService;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private ConversionController conversionController;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Captor
    private ArgumentCaptor<Long> longCaptor;

    private final Path convertedDir = Paths.get("converted");

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(convertedDir);
    }

    @Test
    void testConvertPdfToWord() {
        Long fileId = 1L;
        String conversionId = "conversion-id-123";
        when(documentService.createRequestedDocument(fileId)).thenReturn(conversionId);

        ResponseEntity<ConversionResponse> response = conversionController.convertPdfToWord(fileId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(conversionId, response.getBody().getConversionId());
        verify(documentService, times(1)).createRequestedDocument(fileId);
        verify(conversionService, times(1)).convertPdfToDocx(fileId, conversionId);
    }

    @Test
    void testConvertPdfToWordTextOnly() {
        Long fileId = 1L;
        String conversionId = "conversion-id-123";
        when(documentService.createRequestedDocument(fileId)).thenReturn(conversionId);

        ResponseEntity<ConversionResponse> response = conversionController.convertPdfToWordTextOnly(fileId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(conversionId, response.getBody().getConversionId());
        verify(documentService, times(1)).createRequestedDocument(fileId);
        verify(conversionService, times(1)).convertPdfToText(fileId, conversionId);
    }

    @Test
    void testConvertWordToPdf() {
        Long fileId = 1L;
        String conversionId = "conversion-id-123";
        when(documentService.createRequestedDocument(fileId)).thenReturn(conversionId);

        ResponseEntity<ConversionResponse> response = conversionController.convertWordToPdf(fileId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(conversionId, response.getBody().getConversionId());
        verify(documentService, times(1)).createRequestedDocument(fileId);
        verify(conversionService, times(1)).convertWordToPdf(fileId, conversionId);
    }

    @Test
    void testGetConversionStatus() {
        String conversionId = "conversion-id-123";
        DocumentStatus status = DocumentStatus.COMPLETED;
        when(documentService.getConversionStatus(conversionId)).thenReturn(status);

        ResponseEntity<DocumentStatus> response = conversionController.getConversionStatus(conversionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(status, response.getBody());
        verify(documentService, times(1)).getConversionStatus(conversionId);
    }

    @Test
    void testDownloadConvertedFileNotFound() {
        String conversionId = "conversion-id-123";
        when(documentService.getCompletedDocumentByConversionID(conversionId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = conversionController.downloadConvertedFile(conversionId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Document not found or conversion not completed.", response.getBody());
        verify(documentService, times(1)).getCompletedDocumentByConversionID(conversionId);
    }


    @Test
    void testDownloadConvertedFileIOException() throws Exception {
        String conversionId = "conversion-id-123";
        Document document = new Document();
        document.setConversionId(conversionId);
        document.setConvertedFilePath("converted/testFile2.docx");
        when(documentService.getCompletedDocumentByConversionID(conversionId)).thenReturn(Optional.of(document));

        ResponseEntity<?> response = conversionController.downloadConvertedFile(conversionId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("File not found.", response.getBody());
        verify(documentService, times(1)).getCompletedDocumentByConversionID(conversionId);
    }
}
