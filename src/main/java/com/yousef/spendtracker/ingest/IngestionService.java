package com.yousef.spendtracker.ingest;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Parses an uploaded statement file and hands each row to {@link TransactionIngestor}.
 */
@Service
public class IngestionService {

    /** Single-user app: everything lands in the one account seeded by V1__init.sql. */
    private static final long DEFAULT_ACCOUNT_ID = 1L;

    private final StatementParser parser;
    private final TransactionIngestor transactionIngestor;

    public IngestionService(StatementParser parser, TransactionIngestor transactionIngestor) {
        this.parser = parser;
        this.transactionIngestor = transactionIngestor;
    }

    public IngestionResponse ingest(MultipartFile file, ColumnMapping mapping) throws IOException {
        byte[] content = file.getBytes();
        String fileHash = DigestUtils.sha256Hex(content);

        ParseResult parseResult;
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8)) {
            parseResult = parser.parse(reader, mapping);
        }

        int inserted = 0;
        for (ParsedRow row : parseResult.rows()) {
            boolean wasInserted = transactionIngestor.ingestOne(DEFAULT_ACCOUNT_ID, row.bookedOn(),
                    row.rawDescription(), row.amount(), row.direction(), fileHash);
            if (wasInserted) {
                inserted++;
            }
        }

        int duplicates = parseResult.rows().size() - inserted;
        return new IngestionResponse(parseResult.rows().size(), inserted, duplicates, fileHash, parseResult.errors());
    }
}
