package com.example.reportservice.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Complete invoice data model.
 * Used as the main data source for invoice report templates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceData {

    /** Unique invoice identifier */
    private String invoiceNumber;

    /** Issue date */
    private LocalDate invoiceDate;

    /** Payment due date */
    private LocalDate dueDate;

    /** Customer information */
    private String customerName;
    private String customerAddress;
    private String customerEmail;
    private String customerTaxId;

    /** Seller information */
    private String sellerName;
    private String sellerAddress;
    private String sellerTaxId;

    /** Line items */
    private List<InvoiceItem> items;

    /** Tax rate percentage */
    @Builder.Default
    private BigDecimal taxRate = new BigDecimal("10.00");

    /** Currency code (ISO 4217) */
    @Builder.Default
    private String currency = "USD";

    /** Payment terms */
    private String paymentTerms;

    /** Notes or terms and conditions */
    private String notes;

    // Calculated fields (accessed by JasperReports via getters)

    public BigDecimal getSubtotal() {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTaxAmount() {
        return getSubtotal()
                .multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getTaxAmount());
    }

    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public String getFormattedTotal() {
        return String.format("%s %,.2f", currency, getTotal());
    }
}