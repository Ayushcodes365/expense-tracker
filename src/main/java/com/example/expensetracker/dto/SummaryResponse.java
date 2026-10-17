package com.example.expensetracker.dto;

import com.example.expensetracker.model.Category;

import java.math.BigDecimal;
import java.util.Map;

public record SummaryResponse(
        BigDecimal total,
        long count,
        Map<Category, BigDecimal> byCategory,
        Map<String, BigDecimal> byMonth
) {
}
