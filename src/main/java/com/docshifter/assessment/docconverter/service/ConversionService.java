package com.docshifter.assessment.docconverter.service;

import com.docshifter.assessment.docconverter.converter.Converter;
import com.docshifter.assessment.docconverter.converter.PdfToTextConverter;
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

    public String convertPdfToText(String fileName) throws IOException {

        Path inputFile = uploadDir.resolve(fileName);
        Path outputFile = uploadDir.resolve(fileName.replace(".pdf", ".docx"));
        Converter converter = new PdfToTextConverter();
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

    public String convertPdfToDocx(String fileName) throws IOException {
        Path inputFile = uploadDir.resolve(fileName);
        Path outputFile = uploadDir.resolve(fileName.replace(".pdf", ".docx"));
        log.info("Converting to Doc: {}", inputFile.toFile().getAbsolutePath());


        // Get the path to the Python script
        InputStream scriptStream = getClass().getResourceAsStream("/scripts/convert_pdf_to_word.py");
        if (scriptStream == null) {
            throw new FileNotFoundException("Python script not found in resources.");
        }

        Path scriptPath = Files.createTempFile("convert_pdf_to_word", ".py");
        log.info("Invoking Script: {}", scriptPath);

        Files.copy(scriptStream, scriptPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        scriptStream.close();

        // Call the Python script
        String[] command = new String[]{
                "python",
                scriptPath.toString(),
                inputFile.toFile().getAbsolutePath(),
                outputFile.toFile().getAbsolutePath()
        };
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // Merge stdout and stderr
        Process process = processBuilder.start();

        log.info("Running Script...");

        // Capture the output and errors
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug(line);
            }
        }

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Conversion failed with exit code: {}", exitCode);
                throw new RuntimeException("Conversion failed with exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            log.error("Conversion interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt(); // Reset interrupt flag
            throw new RuntimeException("Conversion interrupted", e);
        } finally {
            Files.deleteIfExists(scriptPath);
        }

        log.info("Conversion successful, output file: {}", outputFile);
        return outputFile.toString();
    }
}
