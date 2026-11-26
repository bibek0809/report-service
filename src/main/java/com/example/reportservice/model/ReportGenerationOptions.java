package com.example.reportservice.model;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.Map;

/**
 * Advanced options for fine-tuning report generation behavior.
 */
@Data
@Builder
public class ReportGenerationOptions {

    /** Override default generation timeout */
    private Duration timeout;

    /** Force recompilation even if cached version exists */
    @Builder.Default
    private boolean forceRecompile = false;

    /** Skip cache and compile from source */
    @Builder.Default
    private boolean bypassCache = false;

    /** Custom JR properties */
    private Map<String, String> jasperProperties;

    /** PDF-specific options */
    private PdfOptions pdfOptions;

    /** Excel-specific options */
    private ExcelOptions excelOptions;

    @Data
    @Builder
    public static class PdfOptions {
        private boolean compressed;
        private boolean pdfACompliance;
        private String author;
        private String title;
        private String subject;
        private boolean tagged;
    }

    @Data
    @Builder
    public static class ExcelOptions {
        private boolean onePagePerSheet;
        private boolean removeEmptySpace;
        private boolean detectCellType;
        private boolean whitePageBackground;
    }
}
