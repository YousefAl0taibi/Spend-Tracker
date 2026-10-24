package com.yousef.spendtracker.sms;

import com.yousef.spendtracker.ingest.Direction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParsedSmsTransaction(LocalDate bookedOn, String rawDescription, BigDecimal amount, Direction direction) {
}
