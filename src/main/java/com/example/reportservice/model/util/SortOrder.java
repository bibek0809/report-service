package com.example.reportservice.model.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Sort specification for report data ordering.
 */
@Getter
@AllArgsConstructor
public class SortOrder {

    private final String field;
    private final Direction direction;

    public enum Direction {
        ASC, DESC
    }

    public static SortOrder asc(String field) {
        return new SortOrder(field, Direction.ASC);
    }

    public static SortOrder desc(String field) {
        return new SortOrder(field, Direction.DESC);
    }
}
