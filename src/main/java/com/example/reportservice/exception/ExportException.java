package com.example.reportservice.exception;


import com.example.reportservice.model.ReportFormat;

/**
 * Thrown when exporting a filled report to the target format fails.
 */
public class ExportException extends ReportException {

    private final ReportFormat targetFormat;
    private final int pageCount;

    public ExportException(ReportFormat targetFormat, Throwable cause) {
        super(ErrorCode.EXPORT_ERROR,
                "Failed to export report to " + targetFormat, cause);
        this.targetFormat = targetFormat;
        this.pageCount = -1;
    }

    public ExportException(ReportFormat targetFormat, String message, int pageCount) {
        super(ErrorCode.EXPORT_ERROR,
                "Export to " + targetFormat + " failed: " + message);
        this.targetFormat = targetFormat;
        this.pageCount = pageCount;
    }

    public ReportFormat getTargetFormat() {
        return targetFormat;
    }

    public int getPageCount() {
        return pageCount;
    }
}