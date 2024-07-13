package com.docshifter.assessment.docconverter.converter;

import java.io.File;

public abstract class Converter {

    public abstract void convert(File inputFile, File outputFile);
}
