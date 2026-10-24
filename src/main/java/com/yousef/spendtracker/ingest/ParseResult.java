package com.yousef.spendtracker.ingest;

import java.util.List;

public record ParseResult(List<ParsedRow> rows, List<ParseError> errors) {
}
