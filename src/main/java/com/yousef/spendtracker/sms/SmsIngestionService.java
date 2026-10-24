package com.yousef.spendtracker.sms;

import com.yousef.spendtracker.ingest.TransactionIngestor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SmsIngestionService {

    /** Single-user app: everything lands in the one account seeded by V1__init.sql. */
    private static final long DEFAULT_ACCOUNT_ID = 1L;

    private final SmsParser smsParser;
    private final TransactionIngestor transactionIngestor;

    public SmsIngestionService(SmsParser smsParser, TransactionIngestor transactionIngestor) {
        this.smsParser = smsParser;
        this.transactionIngestor = transactionIngestor;
    }

    public SmsIngestionResponse ingest(String rawText) {
        Optional<ParsedSmsTransaction> parsed = smsParser.parse(rawText);
        if (parsed.isEmpty()) {
            return SmsIngestionResponse.unrecognised();
        }

        ParsedSmsTransaction transaction = parsed.get();
        String sourceHash = DigestUtils.sha256Hex(rawText);

        boolean inserted = transactionIngestor.ingestOne(DEFAULT_ACCOUNT_ID, transaction.bookedOn(),
                transaction.rawDescription(), transaction.amount(), transaction.direction(), sourceHash);

        return new SmsIngestionResponse(true, inserted, transaction.rawDescription(), transaction.amount(),
                transaction.direction());
    }
}
