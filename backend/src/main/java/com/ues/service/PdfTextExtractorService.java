package com.ues.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Extracts free-form text from an uploaded PDF so it can be indexed in Elasticsearch.
 */
@Service
public class PdfTextExtractorService {

    private static final Logger log = LoggerFactory.getLogger(PdfTextExtractorService.class);

    public String extractText(MultipartFile pdf) {
        if (pdf == null || pdf.isEmpty()) return null;
        try (PDDocument document = Loader.loadPDF(pdf.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text != null ? text.trim() : null;
        } catch (Exception e) {
            log.warn("Could not extract text from PDF '{}': {}", pdf.getOriginalFilename(), e.getMessage());
            return null;
        }
    }
}
