package com.yousef.spendtracker.ingest;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class StatementParserTest {

    private final StatementParser parser = new StatementParser();

    private Reader fixture(String name) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("fixtures/" + name);
        assertThat(stream).as("fixture " + name + " must exist on the test classpath").isNotNull();
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }

    @Test
    void parsesDebitCreditMappingAndSkipsMalformedRows() {
        ColumnMapping bankA = ColumnMapping.debitCredit(
                "Transaction Date", "Description", "Debit", "Credit", "dd/MM/yyyy");

        ParseResult result = parser.parse(fixture("bank-a-sample.csv"), bankA);

        assertThat(result.rows()).hasSize(2);
        assertThat(result.errors()).hasSize(1);

        ParsedRow purchase = result.rows().get(0);
        assertThat(purchase.bookedOn()).isEqualTo(LocalDate.of(2026, 10, 24));
        assertThat(purchase.amount()).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(purchase.direction()).isEqualTo(Direction.DEBIT);

        ParsedRow salary = result.rows().get(1);
        assertThat(salary.amount()).isEqualByComparingTo(new BigDecimal("9500.00"));
        assertThat(salary.direction()).isEqualTo(Direction.CREDIT);
    }

    @Test
    void parsesSignedAmountMappingAndSkipsMalformedRows() {
        ColumnMapping bankB = ColumnMapping.signedAmount("Date", "Narrative", "Amount", "yyyy-MM-dd");

        ParseResult result = parser.parse(fixture("bank-b-sample.csv"), bankB);

        assertThat(result.rows()).hasSize(2);
        assertThat(result.errors()).hasSize(1);

        ParsedRow debit = result.rows().get(0);
        assertThat(debit.bookedOn()).isEqualTo(LocalDate.of(2026, 10, 24));
        assertThat(debit.amount()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(debit.direction()).isEqualTo(Direction.DEBIT);

        ParsedRow credit = result.rows().get(1);
        assertThat(credit.amount()).isEqualByComparingTo(new BigDecimal("9500.00"));
        assertThat(credit.direction()).isEqualTo(Direction.CREDIT);
    }

    @Test
    void malformedRowIsReportedNotFatal() {
        ColumnMapping bankA = ColumnMapping.debitCredit(
                "Transaction Date", "Description", "Debit", "Credit", "dd/MM/yyyy");

        ParseResult result = parser.parse(fixture("bank-a-sample.csv"), bankA);

        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0).message()).contains("neither a debit nor a credit");
    }
}
