package com.yousef.spendtracker.transaction;

import java.math.BigDecimal;
import java.util.List;

public record MonthSummary(String month, BigDecimal total, List<CategorySummary> byCategory) {
}
