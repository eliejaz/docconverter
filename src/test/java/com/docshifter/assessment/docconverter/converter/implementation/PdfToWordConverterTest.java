package com.docshifter.assessment.docconverter.converter.implementation;

import com.docshifter.assessment.docconverter.converter.implementation.PdfToWordConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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

    @Test
    void testConvert() throws IOException {
        File inputFile = createTempPdfFile();
        File outputFile = Files.createTempFile("testOutput", ".docx").toFile();

        inputFile.deleteOnExit();
        outputFile.deleteOnExit();

        // Test the convert method
        assertDoesNotThrow(() -> pdfToWordConverter.convert(inputFile, outputFile));

        inputFile.delete();
        outputFile.delete();
    }

    private File createTempPdfFile() throws IOException {
        File tempFile = Files.createTempFile("testInput", ".pdf").toFile();
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(0));
            contentStream.beginText();
            contentStream.newLineAtOffset(25, 500);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);

            contentStream.showText("This is a test PDF content.");
            contentStream.endText();
            contentStream.close();
            document.save(tempFile);
        }
        return tempFile;
    }
}
