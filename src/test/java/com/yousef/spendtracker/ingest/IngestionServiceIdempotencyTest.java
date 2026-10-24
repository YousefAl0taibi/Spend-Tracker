package com.yousef.spendtracker.ingest;

import com.yousef.spendtracker.transaction.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The idempotency test required by the spec: upload the same file three times,
 * assert the transaction count is identical after each upload.
 */
@SpringBootTest
class IngestionServiceIdempotencyTest {

    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private StatementProperties statementProperties;

    @Test
    void reuploadingTheSameFileThreeTimesInsertsOnlyOnce() throws IOException {
        ColumnMapping mapping = statementProperties.require("bank-a");

        ingestionService.ingest(loadFixture("idempotency-sample.csv"), mapping);
        long afterFirst = transactionRepository.count();

        ingestionService.ingest(loadFixture("idempotency-sample.csv"), mapping);
        long afterSecond = transactionRepository.count();

        IngestionResponse third = ingestionService.ingest(loadFixture("idempotency-sample.csv"), mapping);
        long afterThird = transactionRepository.count();

        assertThat(afterFirst).isEqualTo(3);
        assertThat(afterSecond).isEqualTo(afterFirst);
        assertThat(afterThird).isEqualTo(afterFirst);

        assertThat(third.parsed()).isEqualTo(3);
        assertThat(third.inserted()).isEqualTo(0);
        assertThat(third.duplicates()).isEqualTo(3);
    }

    private MockMultipartFile loadFixture(String name) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("fixtures/" + name)) {
            return new MockMultipartFile("file", name, "text/csv", stream.readAllBytes());
        }
    }
}
