package com.example.reportservice.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Line item for invoice reports.
 * Field names match JRXML field declarations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {

    /** Sequential line item number */
    private Integer itemNumber;

    /** Product or service description */
    private String description;

    /** Quantity ordered */
    private Integer quantity;

    /** Unit price (per item) */
    private BigDecimal unitPrice;

    /** Discount percentage (0-100) */
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    /**
     * Calculated line total after discount.
     * JasperReports can call getter methods as fields.
     */
    public BigDecimal getLineTotal() {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discount = gross.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        return gross.subtract(discount);
    }

    /**
     * Formatted unit price for display.
     */
    public String getFormattedUnitPrice() {
        return String.format("$%,.2f", unitPrice);
    }
}
