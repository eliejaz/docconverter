# convert_pdf_to_word.py
from pdf2docx import Converter
import sys

def convert(pdf_file, docx_file):
    cv = Converter(pdf_file)
    cv.convert(docx_file)
    cv.close()

if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("Usage: python convert_pdf_to_word.py <input_pdf> <output_docx>")
        sys.exit(1)

    pdf_file = sys.argv[1]
    docx_file = sys.argv[2]
    convert(pdf_file, docx_file)
