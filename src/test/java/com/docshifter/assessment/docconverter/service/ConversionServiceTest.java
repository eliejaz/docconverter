package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.converter.PdfToTextConverter;
import com.docshifter.assessment.docconverter.converter.WordToPdfConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(conversionService, "uploadDir", Paths.get("uploads"));
    }

    @Test
    void testConvertPdfToText() throws IOException {
        Path inputFile = Paths.get("uploads/test.pdf");
        Path outputFile = Paths.get("uploads/test.docx");

        // Ensure the directories exist
        Files.createDirectories(inputFile.getParent());

        // Create an empty input file
        Files.deleteIfExists(inputFile);
        Files.createFile(inputFile);

        // Mock the convert method
        doNothing().when(pdfToTextConverter).convert(any(File.class), any(File.class));

        String result = conversionService.convertPdfToText("test.pdf");

        assertEquals(outputFile.toString(), result);
        verify(pdfToTextConverter, times(1)).convert(inputFile.toFile(), outputFile.toFile());

        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputFile);
    }


    @Test
    void testConvertWordToPdf() throws IOException {
        Path inputFile = Paths.get("uploads/test.docx");
        Path outputFile = Paths.get("uploads/test.pdf");

        Files.createDirectories(inputFile.getParent());
        Files.createFile(inputFile);

        doNothing().when(wordToPdfConverter).convert(any(File.class), any(File.class));

        String result = conversionService.convertWordToPdf("test.docx");

        assertEquals(outputFile.toString(), result);
        verify(wordToPdfConverter, times(1)).convert(inputFile.toFile(), outputFile.toFile());

        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputFile);
    }

    @Test
    void testConvertPdfToDocx() throws IOException, InterruptedException {
        Path inputFile = Paths.get("uploads/test.pdf");
        Path outputFile = Paths.get("uploads/test.docx");
        Path scriptPath = Paths.get("temp/convert_pdf_to_word.py");

        // Ensure the directories exist
        Files.createDirectories(inputFile.getParent());

        // Delete the file if it already exists
        Files.deleteIfExists(inputFile);
        Files.createFile(inputFile);

        // Use spy to mock the actual service
        ConversionService spyService = spy(conversionService);

        // Mock the loadAndExtractPdf2docxScript method to return the desired script path
        doReturn(scriptPath).when(spyService).loadAndExtracPdf2docxtScript();

        // Mock the runPdf2docxScript method to return a mocked process
        Process mockProcess = mock(Process.class);
        doReturn(mockProcess).when(spyService).runPdf2docxtScript(any(Path.class), any(Path.class), any(Path.class));

        // Mock the methods that handle the script output and errors
        doNothing().when(spyService).debugScriptOutput(any(Process.class));
        doNothing().when(spyService).handleScriptPotentialError(any(Process.class), eq(scriptPath));

        String result = spyService.convertPdfToDocx("test.pdf");

        assertEquals(outputFile.toString(), result);
        verify(spyService, times(1)).loadAndExtracPdf2docxtScript();
        verify(spyService, times(1)).runPdf2docxtScript(scriptPath, inputFile, outputFile);
        verify(spyService, times(1)).debugScriptOutput(mockProcess);
        verify(spyService, times(1)).handleScriptPotentialError(mockProcess, scriptPath);

        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputFile);
    }




    @Test
    void testHandleScriptPotentialError() throws IOException, InterruptedException {
        Process mockProcess = mock(Process.class);
        Path scriptPath = Paths.get("temp/convert_pdf_to_word.py");

        when(mockProcess.waitFor()).thenReturn(0);

        assertDoesNotThrow(() -> conversionService.handleScriptPotentialError(mockProcess, scriptPath));

        when(mockProcess.waitFor()).thenReturn(1);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> conversionService.handleScriptPotentialError(mockProcess, scriptPath));
        assertEquals("Conversion failed with exit code: 1", exception.getMessage());
    }

    @Test
    void testDebugScriptOutput() throws IOException {
        Process mockProcess = mock(Process.class);
        InputStream inputStream = new ByteArrayInputStream("Script output".getBytes());
        when(mockProcess.getInputStream()).thenReturn(inputStream);

        assertDoesNotThrow(() -> conversionService.debugScriptOutput(mockProcess));
    }
}
