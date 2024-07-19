package com.docshifter.assessment.docconverter.converter.implementation;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class WordToPdfConverterTest {

    @InjectMocks
    private WordToPdfConverter wordToPdfConverter;


    @Test
    void testConvert() throws IOException {
        // Create a temporary DOCX file with some content
        File inputFile = createTempDocxFile();
        File outputFile = Files.createTempFile("testOutput", ".pdf").toFile();

        inputFile.deleteOnExit();
        outputFile.deleteOnExit();

        // Perform the conversion and verify no exceptions are thrown
        assertDoesNotThrow(() -> wordToPdfConverter.convert(inputFile, outputFile));

        // Clean up temporary files
        inputFile.delete();
        outputFile.delete();
    }

    private File createTempDocxFile() throws IOException {
        File tempFile = Files.createTempFile("testInput", ".docx").toFile();
        try (XWPFDocument document = new XWPFDocument()) {
            document.createParagraph().createRun().setText("This is a test DOCX content.");
            document.createStyles();
            document.createHeaderFooterPolicy();

            // Ensure the document has section properties and page size
            CTSectPr sectPr = document.getDocument().getBody().isSetSectPr() ? document.getDocument().getBody().getSectPr() : document.getDocument().getBody().addNewSectPr();
            CTPageSz pageSize = sectPr.isSetPgSz() ? sectPr.getPgSz() : sectPr.addNewPgSz();
            pageSize.setW(11906);
            pageSize.setH(16838);

            outputFile(tempFile, document);
        }
        return tempFile;
    }

    private static void outputFile(File tempFile, XWPFDocument document) throws IOException {
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            document.write(out);
        }
    }
}
