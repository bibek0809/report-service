package com.example.reportservice.exception;


/**
 * Thrown when a requested report template cannot be located
 * in the configured template path.
 */
public class TemplateNotFoundException extends ReportException {

    private final String templateName;
    private final String searchedPath;

    public TemplateNotFoundException(String templateName) {
        super(ErrorCode.TEMPLATE_NOT_FOUND,
                "Report template not found: '" + templateName + ".jrxml'");
        this.templateName = templateName;
        this.searchedPath = null;
    }

    public TemplateNotFoundException(String templateName, String searchedPath) {
        super(ErrorCode.TEMPLATE_NOT_FOUND,
                "Report template not found: '" + templateName +
                        "' in path: " + searchedPath);
        this.templateName = templateName;
        this.searchedPath = searchedPath;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getSearchedPath() {
        return searchedPath;
    }
}
