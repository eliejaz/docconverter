package com.docshifter.assessment.docconverter.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PdfToWordConverterTest {
    @InjectMocks
    PdfToWordConverter pdfToWordConverter;


    @Test
    void testHandleScriptPotentialError() throws InterruptedException {
        Process mockProcess = mock(Process.class);
        Path scriptPath = Paths.get("temp/convert_pdf_to_word.py");

        when(mockProcess.waitFor()).thenReturn(0);

        assertDoesNotThrow(() -> pdfToWordConverter.handleScriptPotentialError(mockProcess, scriptPath));

        when(mockProcess.waitFor()).thenReturn(1);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> pdfToWordConverter.handleScriptPotentialError(mockProcess, scriptPath));
        assertEquals("Conversion failed with exit code: 1", exception.getMessage());
    }

    @Test
    void testDebugScriptOutput() {
        Process mockProcess = mock(Process.class);
        InputStream inputStream = new ByteArrayInputStream("Script output".getBytes());
        when(mockProcess.getInputStream()).thenReturn(inputStream);

        assertDoesNotThrow(() -> pdfToWordConverter.debugScriptOutput(mockProcess));
    }
}
