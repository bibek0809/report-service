package com.example.reportservice.model.dto;

import com.example.reportservice.model.ReportRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Batch request for generating multiple reports concurrently.
 */
@Data
@Builder
public class BatchReportRequest {

    /** List of individual report requests */
    @NotEmpty(message = "At least one report request is required")
    @Size(max = 100, message = "Maximum 100 reports per batch")
    private List<@Valid ReportRequest> requests;

    /** Common parameters applied to all reports */
    private java.util.Map<String, Object> sharedParameters;

    /** Delivery mode for completed reports */
    @Builder.Default
    private DeliveryMode deliveryMode = DeliveryMode.IMMEDIATE;

    /** Callback URL for async notification */
    private String callbackUrl;

    public enum DeliveryMode {
        IMMEDIATE,      // Return in response
        CALLBACK,       // POST to callback URL
        PRESIGNED_URL   // Upload to S3, return URL
    }
}
