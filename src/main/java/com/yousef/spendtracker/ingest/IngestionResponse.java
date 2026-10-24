package com.yousef.spendtracker.ingest;

import java.util.List;

public record IngestionResponse(int parsed, int inserted, int duplicates, String fileHash,
                                 List<ParseError> errors) {
}
