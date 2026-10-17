package com.example.expensetracker.dto;

import com.example.expensetracker.model.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotBlank(message = "title is required")
        @Size(max = 255, message = "title must be at most 255 characters")
        String title,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be at least 0.01")
        BigDecimal amount,

        @NotNull(message = "category is required")
        Category category,

        @NotNull(message = "date is required")
        LocalDate date,

        @Size(max = 500, message = "notes must be at most 500 characters")
        String notes
) {
}
