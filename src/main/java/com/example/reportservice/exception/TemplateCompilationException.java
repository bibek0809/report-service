package com.example.reportservice.exception;


/**
 * Thrown when JRXML compilation fails due to syntax errors,
 * missing resources, or JasperReports internal errors.
 */
public class TemplateCompilationException extends ReportException {

    private final String templateName;
    private final int lineNumber;

    public TemplateCompilationException(String templateName, Throwable cause) {
        super(ErrorCode.TEMPLATE_COMPILE_ERROR,
                "Failed to compile template: '" + templateName +
                        "'. Check JRXML syntax.", cause);
        this.templateName = templateName;
        this.lineNumber = -1;
    }

    public TemplateCompilationException(String templateName, String message, int lineNumber) {
        super(ErrorCode.TEMPLATE_COMPILE_ERROR,
                "Compilation error in '" + templateName +
                        "' at line " + lineNumber + ": " + message);
        this.templateName = templateName;
        this.lineNumber = lineNumber;
    }

    public String getTemplateName() {
        return templateName;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
