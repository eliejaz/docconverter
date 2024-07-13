package com.docshifter.assessment.docconverter.converter;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class WordToPdfConverter extends Converter {

    @Override
    public void convert(File inputFile, File outputFile) {
        try (InputStream in = new FileInputStream(inputFile);
             OutputStream out = new FileOutputStream(outputFile)) {

            // Load the DOCX file using XDocReportRegistry
            XWPFDocument document = new XWPFDocument(in);

            // Prepare PDF options
            PdfOptions options = PdfOptions.create();

            // Convert DOCX to PDF
            PdfConverter.getInstance().convert(document, out, options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
