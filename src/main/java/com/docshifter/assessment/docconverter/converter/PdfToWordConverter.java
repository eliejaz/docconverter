package com.docshifter.assessment.docconverter.converter;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PdfToWordConverter extends Converter {

    @Override
    public void convert(File inputFile, File outputFile) {
        try (PDDocument pdfDocument = Loader.loadPDF(inputFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String pdfText = pdfStripper.getText(pdfDocument);

            try (XWPFDocument wordDocument = new XWPFDocument()) {
                XWPFParagraph paragraph = wordDocument.createParagraph();
                paragraph.createRun().setText(pdfText);

                try (OutputStream wordOutputStream = new FileOutputStream(outputFile)) {
                    wordDocument.write(wordOutputStream);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
