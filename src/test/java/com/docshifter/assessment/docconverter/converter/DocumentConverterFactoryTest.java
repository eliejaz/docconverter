package com.docshifter.assessment.docconverter.converter;

import com.docshifter.assessment.docconverter.converter.implementation.PdfToTextConverter;
import com.docshifter.assessment.docconverter.converter.implementation.PdfToWordConverter;
import com.docshifter.assessment.docconverter.converter.implementation.WordToPdfConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DocumentConverterFactoryTest {
    @Mock
    private PdfToTextConverter pdfToTextConverter;

    @Mock
    private PdfToWordConverter pdfToWordConverter;
    @Mock
    private WordToPdfConverter wordToPdfConverter;

    @InjectMocks
    private DocumentConverterFactory converterFactory;


    @Test
    void testGetConverterForPdfToText() {
        DocumentConverter converter = converterFactory.getConverter(DocumentConversionType.PDF_TO_TEXT);
        assertEquals(pdfToTextConverter, converter);
    }

    @Test
    void testGetConverterForPdfToWord() {
        DocumentConverter converter = converterFactory.getConverter(DocumentConversionType.PDF_TO_WORD);
        assertEquals(pdfToWordConverter, converter);
    }

    @Test
    void testGetConverterForWordToPdf() {
        DocumentConverter converter = converterFactory.getConverter(DocumentConversionType.WORD_TO_PDF);
        assertEquals(wordToPdfConverter, converter);
    }

}
