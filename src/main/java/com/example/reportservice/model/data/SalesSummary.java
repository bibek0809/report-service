package com.example.reportservice.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Monthly sales summary for analytics reports.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummary {

    private YearMonth period;
    private String region;
    private String productCategory;

    private Integer unitsSold;
    private BigDecimal revenue;
    private BigDecimal cost;

    public BigDecimal getProfit() {
        return revenue.subtract(cost);
    }

    public BigDecimal getProfitMargin() {
        if (revenue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getProfit()
                .multiply(BigDecimal.valueOf(100))
                .divide(revenue, 2, java.math.RoundingMode.HALF_UP);
    }
}
