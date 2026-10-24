package com.yousef.spendtracker.ingest;

/** A row that could not be parsed. Malformed input is reported, never fatal. */
public record ParseError(long lineNumber, String rawRecord, String message) {
}
