package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.converter.PdfToTextConverter;
import com.docshifter.assessment.docconverter.converter.PdfToWordConverter;
import com.docshifter.assessment.docconverter.converter.WordToPdfConverter;
import com.docshifter.assessment.docconverter.model.Document;
import com.docshifter.assessment.docconverter.model.DocumentStatus;
import com.docshifter.assessment.docconverter.repository.DocumentRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConversionServiceTest {

    @InjectMocks
    private ConversionService conversionService;

    @Mock
    private PdfToTextConverter pdfToTextConverter;

    @Mock
    private WordToPdfConverter wordToPdfConverter;

    @Mock
    private PdfToWordConverter pdfToWordConverter;

    @Mock
    private DocumentRepository documentRepository;

    private String conversionId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(conversionService, "uploadDir", Paths.get("uploads"));
        conversionId = UUID.randomUUID().toString();
    }

    @Test
    void testConvertPdfToText() throws IOException {
        Path inputFile = Paths.get("uploads/test.pdf");
        Path outputFile = Paths.get("converted/test.docx");

        // Ensure the directories exist
        Files.createDirectories(inputFile.getParent());

        // Create an empty input file
        Files.deleteIfExists(inputFile);
        Files.createFile(inputFile);

        // Mock the convert method
        doNothing().when(pdfToTextConverter).convert(any(File.class), any(File.class));

        // Mock DocumentRepository behavior
        Document document = new Document();
        document.setConversionId(conversionId);
        document.setStatus(DocumentStatus.REQUESTED);
        when(documentRepository.findByConversionId(conversionId)).thenReturn(Optional.of(document));

        conversionService.convertPdfToText("test.pdf", conversionId);

        verify(pdfToTextConverter, times(1)).convert(inputFile.toFile(), outputFile.toFile());
        verify(documentRepository, times(2)).save(any(Document.class));

        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputFile);
    }

    @Test
    void testConvertWordToPdf() throws IOException {
        Path inputFile = Paths.get("uploads/test.docx");
        Path outputFile = Paths.get("converted/test.pdf");

        Files.createDirectories(inputFile.getParent());
        Files.createFile(inputFile);

        doNothing().when(wordToPdfConverter).convert(any(File.class), any(File.class));

        // Mock DocumentRepository behavior
        Document document = new Document();
        document.setConversionId(conversionId);
        document.setStatus(DocumentStatus.REQUESTED);
        when(documentRepository.findByConversionId(conversionId)).thenReturn(Optional.of(document));

        conversionService.convertWordToPdf("test.docx", conversionId);

        verify(wordToPdfConverter, times(1)).convert(inputFile.toFile(), outputFile.toFile());
        verify(documentRepository, times(2)).save(any(Document.class));

        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputFile);
    }

    @Test
    void testConvertPdfToDocx() throws IOException {
        Path inputFile = Paths.get("uploads/test.pdf");
        Path outputFile = Paths.get("converted/test.docx");

        // Ensure the directories exist
        Files.createDirectories(inputFile.getParent());

        // Delete the file if it already exists
        Files.deleteIfExists(inputFile);
        Files.createFile(inputFile);

        doNothing().when(pdfToWordConverter).convert(any(File.class), any(File.class));

        // Mock DocumentRepository behavior
        Document document = new Document();
        document.setConversionId(conversionId);
        document.setStatus(DocumentStatus.REQUESTED);
        when(documentRepository.findByConversionId(conversionId)).thenReturn(Optional.of(document));

        conversionService.convertPdfToDocx("test.pdf", conversionId);

        verify(pdfToWordConverter, times(1)).convert(inputFile.toFile(), outputFile.toFile());
        verify(documentRepository, times(2)).save(any(Document.class));

        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputFile);
    }
}
