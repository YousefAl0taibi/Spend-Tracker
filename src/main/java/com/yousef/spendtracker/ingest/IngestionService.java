package com.yousef.spendtracker.ingest;

import com.yousef.spendtracker.categorise.CategoryGuess;
import com.yousef.spendtracker.categorise.MccCategoryLookup;
import com.yousef.spendtracker.categorise.RuleEngine;
import com.yousef.spendtracker.normalise.DescriptionNormaliser;
import com.yousef.spendtracker.transaction.Category;
import com.yousef.spendtracker.transaction.CategoryRepository;
import com.yousef.spendtracker.transaction.Transaction;
import com.yousef.spendtracker.transaction.TransactionInserter;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Ties parsing, normalisation, categorisation, fingerprinting and idempotent
 * persistence together. This is the only place that decides what counts as
 * "the same transaction" — for both statement formats it accepts (CSV and PDF).
 */
@Service
public class IngestionService {

    /** Single-user app: everything lands in the one account seeded by V1__init.sql. */
    private static final long DEFAULT_ACCOUNT_ID = 1L;

    private final StatementParser csvParser;
    private final PdfStatementParser pdfParser;
    private final DescriptionNormaliser normaliser;
    private final RuleEngine ruleEngine;
    private final CategoryRepository categoryRepository;
    private final TransactionInserter transactionInserter;

    public IngestionService(StatementParser csvParser, PdfStatementParser pdfParser,
                             DescriptionNormaliser normaliser, RuleEngine ruleEngine,
                             CategoryRepository categoryRepository, TransactionInserter transactionInserter) {
        this.csvParser = csvParser;
        this.pdfParser = pdfParser;
        this.normaliser = normaliser;
        this.ruleEngine = ruleEngine;
        this.categoryRepository = categoryRepository;
        this.transactionInserter = transactionInserter;
    }

    public IngestionResponse ingest(MultipartFile file, ColumnMapping mapping) throws IOException {
        byte[] content = file.getBytes();
        String fileHash = DigestUtils.sha256Hex(content);

        ParseResult parseResult;
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8)) {
            parseResult = csvParser.parse(reader, mapping);
        }

        return ingestParsedRows(parseResult, fileHash);
    }

    public IngestionResponse ingestPdf(MultipartFile file) throws IOException {
        byte[] content = file.getBytes();
        String fileHash = DigestUtils.sha256Hex(content);

        ParseResult parseResult;
        try (var pdfStream = new ByteArrayInputStream(content)) {
            parseResult = pdfParser.parse(pdfStream);
        }

        return ingestParsedRows(parseResult, fileHash);
    }

    private IngestionResponse ingestParsedRows(ParseResult parseResult, String fileHash) {
        int inserted = 0;
        for (ParsedRow row : parseResult.rows()) {
            String normalisedDescription = normaliser.normalise(row.rawDescription());
            String fingerprint = Fingerprint.of(DEFAULT_ACCOUNT_ID, row.bookedOn(), row.amount(),
                    row.direction(), row.rawDescription());
            Long categoryId = guessCategoryId(normalisedDescription, row.direction(), row.mccCode());

            Transaction transaction = new Transaction(DEFAULT_ACCOUNT_ID, row.bookedOn(), row.rawDescription(),
                    normalisedDescription, row.amount(), row.direction(), fingerprint, fileHash);
            transaction.setCategoryId(categoryId);

            try {
                transactionInserter.insertOrThrow(transaction);
                inserted++;
            } catch (DataIntegrityViolationException duplicateFingerprint) {
                // Already ingested; not an error.
            }
        }

        int duplicates = parseResult.rows().size() - inserted;
        return new IngestionResponse(parseResult.rows().size(), inserted, duplicates, fileHash, parseResult.errors());
    }

    /** MCC (present on PDF card transactions) decides first; CSV rows have none and fall back to RuleEngine. */
    private Long guessCategoryId(String normalisedDescription, Direction direction, String mccCode) {
        if (mccCode != null) {
            var mccCategory = MccCategoryLookup.categoryNameFor(mccCode);
            if (mccCategory.isPresent()) {
                return categoryRepository.findByNameEn(mccCategory.get()).map(Category::getId).orElse(null);
            }
        }

        CategoryGuess guess = ruleEngine.categorise(normalisedDescription, direction);
        return categoryRepository.findByNameEn(guess.nameEn())
                .map(Category::getId)
                .orElse(null);
    }
}
