package com.example.reportservice.model.dto;

import com.example.reportservice.model.ReportResponse;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Response for batch report generation.
 */
@Data
@Builder
public class BatchReportResponse {

    private String batchId;
    private Instant submittedAt;
    private int totalReports;
    private int completedReports;
    private int failedReports;
    private List<ReportResponse> results;
    private String status;  // PROCESSING, COMPLETED, PARTIAL_FAILURE
}
