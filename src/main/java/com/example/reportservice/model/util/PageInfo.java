package com.example.reportservice.model.util;


import lombok.Builder;
import lombok.Data;

/**
 * Pagination metadata for paginated reports.
 */
@Data
@Builder
public class PageInfo {

    private int currentPage;
    private int totalPages;
    private int pageSize;
    private long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;

    public static PageInfo of(int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return PageInfo.builder()
                .currentPage(page)
                .pageSize(size)
                .totalElements(total)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }
}
