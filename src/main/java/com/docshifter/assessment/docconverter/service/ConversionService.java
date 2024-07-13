package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.converter.Converter;
import com.docshifter.assessment.docconverter.converter.PdfToWordConverter;
import com.docshifter.assessment.docconverter.converter.WordToPdfConverter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ConversionService {
    private final Path uploadDir = Paths.get("uploads");

    public String convertPdfToWord(String fileName) throws IOException {

        Path inputFile = uploadDir.resolve(fileName);
        Path outputFile = uploadDir.resolve(fileName.replace(".pdf", ".docx"));
        Converter converter = new PdfToWordConverter();
        converter.convert(inputFile.toFile(), outputFile.toFile());
        return outputFile.toString();
    }

    public String convertWordToPdf(String fileName) throws IOException {
        Path inputFile = uploadDir.resolve(fileName);
        Path outputFile = uploadDir.resolve(fileName.replace(".docx", ".pdf"));
        Converter converter = new WordToPdfConverter();
        converter.convert(inputFile.toFile(), outputFile.toFile());
        return outputFile.toString();
    }
}
