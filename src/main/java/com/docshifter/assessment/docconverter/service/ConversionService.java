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

    PdfToTextConverter pdfToTextConverter;
    WordToPdfConverter wordToPdfConverter;

    public ConversionService(PdfToTextConverter pdfToTextConverter, WordToPdfConverter wordToPdfConverter) {
        this.pdfToTextConverter = pdfToTextConverter;
        this.wordToPdfConverter = wordToPdfConverter;
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

        // Get the path to the Python script
        Path scriptPath = loadAndExtracPdf2docxtScript();

        // Call the Python script
        log.info("Running Script...");
        Process process = runPdf2docxtScript(scriptPath, inputFile, outputFile);

        // Capture the output and errors
        debugScriptOutput(process);

        handleScriptPotentialError(process, scriptPath);

        log.info("Conversion successful, output file: {}", outputFile);
        return outputFile.toString();
    }

     Process runPdf2docxtScript(Path scriptPath, Path inputFile, Path outputFile) throws IOException {
        String[] command = new String[]{
                "python",
                scriptPath.toString(),
                inputFile.toFile().getAbsolutePath(),
                outputFile.toFile().getAbsolutePath()
        };
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // Merge stdout and stderr
        return processBuilder.start();
    }

    Path loadAndExtracPdf2docxtScript() throws IOException {
        InputStream scriptStream = getClass().getResourceAsStream("/scripts/convert_pdf_to_word.py");
        if (scriptStream == null) {
            throw new FileNotFoundException("Python script not found in resources.");
        }

        Path scriptPath = Files.createTempFile("convert_pdf_to_word", ".py");
        log.info("Invoking Script: {}", scriptPath);

        Files.copy(scriptStream, scriptPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        scriptStream.close();
        return scriptPath;
    }

    void handleScriptPotentialError(Process process, Path scriptPath) throws IOException {
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
    }

    void debugScriptOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
        }
    }
}
