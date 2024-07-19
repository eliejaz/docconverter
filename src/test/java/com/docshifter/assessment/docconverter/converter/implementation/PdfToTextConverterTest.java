package com.docshifter.assessment.docconverter.converter.implementation;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class PdfToTextConverterTest {
    @InjectMocks
    private PdfToTextConverter pdfToTextConverter;

    @Test
    void testConvert() throws IOException {
        File inputFile = createTempPdfFile();
        File outputFile = Files.createTempFile("testOutput", ".docx").toFile();

        inputFile.deleteOnExit();
        outputFile.deleteOnExit();

        // Test the convert method
        assertDoesNotThrow(() -> pdfToTextConverter.convert(inputFile, outputFile));

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
