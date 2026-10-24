package com.yousef.spendtracker.ingest;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns a bank's raw CSV export into {@link ParsedRow}s, using a {@link ColumnMapping}
 * to resolve the bank-specific column names, date format and debit/credit convention.
 *
 * A row that fails to parse is recorded as a {@link ParseError} and skipped — one bad
 * row must never fail the whole upload.
 */
@Component
public class StatementParser {

    public ParseResult parse(Reader csvReader, ColumnMapping mapping) {
        List<ParsedRow> rows = new ArrayList<>();
        List<ParseError> errors = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(mapping.getDateFormat());

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreSurroundingSpaces(true)
                .build();

        try (CSVParser parser = format.parse(csvReader)) {
            for (CSVRecord record : parser) {
                try {
                    rows.add(parseRow(record, mapping, dateFormatter));
                } catch (Exception e) {
                    errors.add(new ParseError(record.getRecordNumber(), record.toString(), e.getMessage()));
                }
            }
        } catch (IOException e) {
            errors.add(new ParseError(0, "", "Failed to read statement: " + e.getMessage()));
        }

        return new ParseResult(rows, errors);
    }

    private ParsedRow parseRow(CSVRecord record, ColumnMapping mapping, DateTimeFormatter dateFormatter) {
        String rawDate = require(record, mapping.getDate());
        LocalDate bookedOn = LocalDate.parse(rawDate, dateFormatter);
        String rawDescription = require(record, mapping.getDescription());

        BigDecimal amount;
        Direction direction;

        if (mapping.usesSignedAmount()) {
            BigDecimal signed = parseAmount(require(record, mapping.getAmount()));
            direction = signed.signum() < 0 ? Direction.DEBIT : Direction.CREDIT;
            amount = signed.abs();
        } else {
            String debitStr = optional(record, mapping.getDebit());
            String creditStr = optional(record, mapping.getCredit());

            if (isBlank(debitStr) && isBlank(creditStr)) {
                throw new IllegalArgumentException("row has neither a debit nor a credit amount");
            }
            if (!isBlank(debitStr)) {
                amount = parseAmount(debitStr);
                direction = Direction.DEBIT;
            } else {
                amount = parseAmount(creditStr);
                direction = Direction.CREDIT;
            }
        }

        return new ParsedRow(bookedOn, rawDescription, amount.setScale(2, RoundingMode.HALF_UP), direction);
    }

    private static BigDecimal parseAmount(String raw) {
        return new BigDecimal(raw.replace(",", "").trim());
    }

    private static String require(CSVRecord record, String column) {
        String value = optional(record, column);
        if (isBlank(value)) {
            throw new IllegalArgumentException("missing required column: " + column);
        }
        return value;
    }

    private static String optional(CSVRecord record, String column) {
        if (column == null || !record.isMapped(column)) {
            return null;
        }
        return record.get(column);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
