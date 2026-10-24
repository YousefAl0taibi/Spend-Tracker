package com.yousef.spendtracker.ingest;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a bank's PDF statement export. PDF text extraction has no concept of a
 * table — a row that looked like "24/10/2026 | TAMIMI | 125.50" on the page comes
 * out as a flat stream of lines in whatever order the PDF's internal layout used,
 * mixed with repeated boilerplate (terminal numbers, reference numbers, VAT lines,
 * masked card numbers) between each real transaction. This class finds each
 * transaction's block by anchoring on the parts that are always there — the
 * amount/merchant line and, a bit further on, the date/type/time line — and skips
 * everything else in between rather than trying to match every field.
 *
 * Arabic PDFs often extract as Arabic Presentation Forms (visually identical to
 * standard Arabic letters, but different Unicode code points — a font/rendering
 * artifact, not a typo in this class). NFKC normalisation folds them back to
 * standard Arabic before any pattern is tried; without it, a correctly-written
 * Arabic regex simply never matches.
 */
@Component
public class PdfStatementParser {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * balance, amount and merchant come from the line PDF extraction produces for
     * what was visually "<balance> <amount> ,<merchant>"; everything up to the next
     * "رقم الشاشة" (terminal number) label is skipped non-greedily, then the
     * date + Arabic transaction-type text + time that follow some lines later.
     */
    private static final Pattern TRANSACTION_BLOCK = Pattern.compile(
            "(?<balance>[0-9,]+\\.\\d{2})\\s+(?<amount>[0-9,]+\\.\\d{2})\\s*,\\s*(?<merchant>.+?)\\s+"
                    + "رقم\\s*الشاشة.*?"
                    + "(?<date>\\d{1,2}/\\d{1,2}/\\d{4})\\s+(?<type>.+?)\\s+(?<time>\\d{1,2}:\\d{2})");

    /** "شراء" (purchase). Other wordings (salary, transfer, ATM) need their own entry once seen. */
    private static final Map<String, Direction> DIRECTION_BY_KEYWORD = Map.of(
            "شراء", Direction.DEBIT
    );

    public ParseResult parse(InputStream pdfStream) throws IOException {
        String text;
        try (PDDocument document = Loader.loadPDF(pdfStream.readAllBytes())) {
            text = new PDFTextStripper().getText(document);
        }
        return parse(text);
    }

    public ParseResult parse(String extractedText) {
        String normalised = Normalizer.normalize(extractedText, Normalizer.Form.NFKC);
        String flattened = normalised.replaceAll("\\s+", " ").trim();

        List<ParsedRow> rows = new ArrayList<>();
        List<ParseError> errors = new ArrayList<>();

        Matcher matcher = TRANSACTION_BLOCK.matcher(flattened);
        long blockNumber = 0;
        while (matcher.find()) {
            blockNumber++;
            try {
                rows.add(toRow(matcher));
            } catch (Exception e) {
                errors.add(new ParseError(blockNumber, matcher.group(), e.getMessage()));
            }
        }

        return new ParseResult(rows, errors);
    }

    private ParsedRow toRow(Matcher matcher) {
        BigDecimal amount = new BigDecimal(matcher.group("amount").replace(",", ""));
        String merchant = matcher.group("merchant").trim();
        LocalDate bookedOn = LocalDate.parse(matcher.group("date"), DATE_FORMAT);
        String type = matcher.group("type").trim();

        Direction direction = DIRECTION_BY_KEYWORD.entrySet().stream()
                .filter(entry -> type.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unrecognised transaction type: " + type));

        return new ParsedRow(bookedOn, merchant, amount, direction);
    }
}
