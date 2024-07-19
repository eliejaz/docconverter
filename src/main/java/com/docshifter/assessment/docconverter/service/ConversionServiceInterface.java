package com.docshifter.assessment.docconverter.service;

public interface ConversionServiceInterface {

    public void convertPdfToText(Long fileId, String conversionId);
    public void convertWordToPdf(Long fileId, String conversionId);
    public void convertPdfToDocx(Long fileId, String conversionId);
}
