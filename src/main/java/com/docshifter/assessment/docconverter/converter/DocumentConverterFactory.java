package com.docshifter.assessment.docconverter.converter;

import com.docshifter.assessment.docconverter.converter.implementation.PdfToTextConverter;
import com.docshifter.assessment.docconverter.converter.implementation.PdfToWordConverter;
import com.docshifter.assessment.docconverter.converter.implementation.WordToPdfConverter;
import org.springframework.stereotype.Component;

@Component
public class DocumentConverterFactory {
    private final PdfToTextConverter pdfToTextConverter;
    private final PdfToWordConverter pdfToWordConverter;
    private final WordToPdfConverter wordToPdfConverter;


    public DocumentConverterFactory(PdfToTextConverter pdfToTextConverter, PdfToWordConverter pdfToWordConverter, WordToPdfConverter wordToPdfConverter) {
        this.pdfToTextConverter = pdfToTextConverter;
        this.pdfToWordConverter = pdfToWordConverter;
        this.wordToPdfConverter = wordToPdfConverter;
    }

    public DocumentConverter getConverter(DocumentConversionType conversionType) {
        return switch (conversionType) {
            case PDF_TO_TEXT -> pdfToTextConverter;
            case PDF_TO_WORD -> pdfToWordConverter;
            case WORD_TO_PDF -> wordToPdfConverter;
        };
    }
}