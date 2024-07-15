package com.docshifter.assessment.docconverter.converter;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class PdfToTextConverter extends Converter {

    @Override
    public void convert(File inputFile, File outputFile) {
        try (PDDocument pdfDocument = Loader.loadPDF(inputFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String pdfText = pdfStripper.getText(pdfDocument);

            try (XWPFDocument wordDocument = new XWPFDocument()) {
                XWPFParagraph paragraph = wordDocument.createParagraph();
                paragraph.createRun().setText(pdfText);

                outputConvertedDocument(outputFile, wordDocument);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void outputConvertedDocument(File outputFile, XWPFDocument wordDocument) throws IOException {
        try (OutputStream wordOutputStream = new FileOutputStream(outputFile)) {
            wordDocument.write(wordOutputStream);
        }
    }
}
