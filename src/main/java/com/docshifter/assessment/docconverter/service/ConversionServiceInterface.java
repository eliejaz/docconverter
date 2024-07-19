package com.docshifter.assessment.docconverter.service;

public interface ConversionServiceInterface {

    void convertPdfToText(Long fileId, String conversionId);

    void convertWordToPdf(Long fileId, String conversionId);

    void convertPdfToDocx(Long fileId, String conversionId);
}
