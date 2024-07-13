package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.converter.Converter;
import com.docshifter.assessment.docconverter.converter.PdfToTextConverter;
import com.docshifter.assessment.docconverter.converter.PdfToWordConverter;
import com.docshifter.assessment.docconverter.converter.WordToPdfConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class ConversionService {
    private final Path uploadDir = Paths.get("uploads");

    PdfToTextConverter pdfToTextConverter;
    WordToPdfConverter wordToPdfConverter;
    PdfToWordConverter pdfToWordConverter;

    public ConversionService(PdfToTextConverter pdfToTextConverter, WordToPdfConverter wordToPdfConverter, PdfToWordConverter pdfToWordConverter) {
        this.pdfToTextConverter = pdfToTextConverter;
        this.wordToPdfConverter = wordToPdfConverter;
        this.pdfToWordConverter = pdfToWordConverter;
    }

    public String convertPdfToText(String fileName) throws IOException {

        Path inputFile = uploadDir.resolve(fileName);
        Path outputFile = uploadDir.resolve(fileName.replace(".pdf", ".docx"));
        pdfToTextConverter.convert(inputFile.toFile(), outputFile.toFile());
        return outputFile.toString();
    }

    public String convertWordToPdf(String fileName) throws IOException {
        Path inputFile = uploadDir.resolve(fileName);
        Path outputFile = uploadDir.resolve(fileName.replace(".docx", ".pdf"));
        wordToPdfConverter.convert(inputFile.toFile(), outputFile.toFile());
        return outputFile.toString();
    }

    public String convertPdfToDocx(String fileName) throws IOException {
        Path inputFile = uploadDir.resolve(fileName);
        Path outputFile = uploadDir.resolve(fileName.replace(".pdf", ".docx"));
        log.info("Converting to Doc: {}", inputFile.toFile().getAbsolutePath());
        pdfToWordConverter.convert(inputFile.toFile(), outputFile.toFile());

        return outputFile.toString();
    }


}
