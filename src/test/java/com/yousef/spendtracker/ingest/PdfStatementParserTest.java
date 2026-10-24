package com.yousef.spendtracker.ingest;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

class PdfStatementParserTest {

    private final PdfStatementParser parser = new PdfStatementParser();

    private String fixtureText(String name) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("fixtures/" + name)) {
            return new Scanner(stream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
        }
    }

    @Test
    void parsesFourMadaApplePayPurchasesFromRealExtractedText() throws IOException {
        ParseResult result = parser.parse(fixtureText("pdf-statement-extract.txt"));

        assertThat(result.errors()).isEmpty();
        assertThat(result.rows()).hasSize(4);

        ParsedRow first = result.rows().get(0);
        assertThat(first.bookedOn()).isEqualTo(LocalDate.of(2026, 9, 9));
        assertThat(first.amount()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(first.rawDescription()).isEqualTo("AlFaiha");
        assertThat(first.direction()).isEqualTo(Direction.DEBIT);

        ParsedRow second = result.rows().get(1);
        assertThat(second.rawDescription()).isEqualTo("Al Khaleej Dis");
        assertThat(second.amount()).isEqualByComparingTo(new BigDecimal("29.97"));

        ParsedRow third = result.rows().get(2);
        assertThat(third.rawDescription()).isEqualTo("eshbelya alhassan bin");
        assertThat(third.amount()).isEqualByComparingTo(new BigDecimal("21.45"));

        ParsedRow fourth = result.rows().get(3);
        assertThat(fourth.rawDescription()).isEqualTo("Boulevard World");
        assertThat(fourth.amount()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(fourth.bookedOn()).isEqualTo(LocalDate.of(2026, 9, 9));
    }

    @Test
    void headerAndAccountNumberDoNotProduceSpuriousRows() throws IOException {
        // The fixture's preamble ("Account Number 53253749200", the column-header
        // row) must not be mistaken for a transaction — exactly 4 rows, not 5+.
        ParseResult result = parser.parse(fixtureText("pdf-statement-extract.txt"));
        assertThat(result.rows()).hasSize(4);
    }

    @Test
    void unrecognisedTransactionTypeIsReportedNotGuessed() {
        String block = "100.00 10.00 ,SOME MERCHANT رقم الشاشة 123 "
                + "01/01/2026 تحويل مستلم 09:00"; // "تحويل مستلم" (transfer received) — no rule for it yet

        ParseResult result = parser.parse(block);

        assertThat(result.rows()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0).message()).contains("Unrecognised transaction type");
    }
}
