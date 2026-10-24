package com.yousef.spendtracker.transaction;

import java.util.List;

public record MonthlySummaryResponse(int year, List<MonthSummary> months) {
}
