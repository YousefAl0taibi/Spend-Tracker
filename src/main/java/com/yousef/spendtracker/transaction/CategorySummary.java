package com.yousef.spendtracker.transaction;

import java.math.BigDecimal;

public record CategorySummary(String category, BigDecimal total, int count) {
}
