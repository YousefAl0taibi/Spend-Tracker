# Spend Tracker

A personal spending tracker that ingests bank statement CSV exports, cleans and
categorises the transactions, stores them, and serves them over a REST API.

## Quickstart

**Option A — with Docker (matches production: PostgreSQL + Flyway):**

```bash
docker compose up
curl -F "file=@samples/sample-statement.csv" \
     "http://localhost:8080/statements?mapping=bank-a"
```

**Option B — no Docker, for trying it on your own machine right now:**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
curl -F "file=@samples/sample-statement.csv" \
     "http://localhost:8080/statements?mapping=bank-a"
```

This uses a local H2 database file (`./data/spendtracker-local.mv.db`, already
`.gitignore`d) instead of PostgreSQL — see `application-local.yml` and
`CategorySeeder`. Data persists across restarts. This path **has** been run
end-to-end on this machine: upload, summary, pagination and the duplicate-
upload check all verified live, not just in tests.

> **Note on this build:** every piece below (parsing, normalisation, idempotent
> ingestion, rule-based categorisation, transfer matching, pagination, summaries)
> is implemented and covered by its own unit/integration tests — 34 tests, all
> green. Option B above has been exercised live. **Option A (`docker compose
> up` against real PostgreSQL) has not** — Docker Desktop's backend was
> unresponsive on this machine the whole time this was built. Run it once on a
> machine with a working Docker install if you want the real Postgres path
> (and the CV lines below) verified too.

```bash
curl "http://localhost:8080/summary/monthly?year=2026"
```

```json
{
  "year": 2026,
  "months": [
    { "month": "2026-09", "total": 1273.64, "byCategory": [
        { "category": "Groceries", "total": 733.15, "count": 2 },
        { "category": "Fuel",      "total": 330.00, "count": 2 },
        { "category": "Dining",    "total": 117.50, "count": 2 },
        { "category": "Subscriptions", "total": 64.99, "count": 2 }
    ]}
  ]
}
```

## How ingestion stays idempotent

Every row gets a SHA-256 **fingerprint** of `(account, date, amount, direction,
raw description)`. The `transaction` table has a unique constraint on
`fingerprint`, and the insert path (`TransactionInserter`) relies on that
constraint — not a read-then-check in application code, which races with
itself under concurrent uploads. Re-uploading an overlapping statement
inserts zero new rows for the rows already seen; see
`IngestionServiceIdempotencyTest` for the triple-upload proof.

(The exact SQL differs slightly from the spec's `ON CONFLICT DO NOTHING`
sketch — see **Implementation note** below for why.)

## PDF statements (not just CSV)

Real bank statement exports are usually PDF, not CSV. `POST /statements/pdf`
accepts the raw PDF and runs it through the same pipeline.

PDF text extraction has no concept of a table — a visual row becomes a flat
stream of lines in whatever order the PDF's internal layout used, interleaved
with boilerplate (terminal numbers, reference numbers, masked card numbers,
VAT lines) between each real transaction. `PdfStatementParser` finds each
transaction by anchoring on the two parts that are always present — the
amount/merchant line, and a bit further on, the date + Arabic transaction-type
+ time line — and skips everything in between rather than matching every
field.

Arabic PDFs frequently extract as **Arabic Presentation Forms**: visually
identical to standard Arabic letters but different Unicode code points (a
font-rendering artifact, not a bug in the regex). Without folding them back
first, a correctly-written Arabic pattern simply never matches anything.
`PdfStatementParser` runs Unicode NFKC normalisation on the extracted text
before matching, which fixes this independent of which PDF producer the bank
used.

Only one transaction wording is recognised today — "شراء" (purchase, via
mada/Apple Pay POS) — because that is the only real example seen so far (see
`PdfStatementParserTest`, built from an actual anonymised statement excerpt).
Salary deposits, transfers and ATM withdrawals will use different Arabic
wording and are reported as unrecognised (not mis-parsed as the wrong
direction) until a real example of each is added to
`PdfStatementParser.DIRECTION_BY_KEYWORD`.

## Categorisation by Merchant Category Code (MCC) — no merchant-name list needed

Every mada/Visa/Mastercard POS transaction already carries a 4-digit Merchant
Category Code (MCC) assigned by the card network itself — e.g. `5411` for a
grocery store, `5814` for fast food. `PdfStatementParser` extracts it from the
statement, and `MccCategoryLookup` maps it straight to a category: no merchant
name list to maintain, no external lookup, no API, nothing sent anywhere.

This was a direct answer to "shouldn't this generalise instead of listing
merchant names" — MCC generalises for free because it's already in the data.
Verified against a real statement: before MCC-based categorisation, **100%**
of 72 real transactions came back `Uncategorised` (the `rules.yml`
merchant-name patterns were all placeholder examples — `TAMIMI`, `ALDREES` —
that never matched this user's real merchants). After: only **~12%** stayed
`Uncategorised`, the rest correctly split across Dining, Groceries, Shopping,
Fuel, Pets, Health, Automotive and Transport, entirely from MCC codes already
present in the real file.

`RuleEngine`'s merchant-name matching still runs as the fallback for sources
with no MCC (CSV exports) or an MCC this table doesn't cover yet. The mapping
table's descriptions were verified against
[Checkout.com's MCC reference](https://www.checkout.com/docs/resources/codes/merchant-category-codes)
before being hardcoded, not taken from memory.

## How categorisation works

`rules.yml` lists categories as data, each with a priority and a list of
merchant-string patterns (optionally restricted to `CREDIT` or `DEBIT`).
`RuleEngine` tries rules lowest-priority-number first and returns the first
pattern match; the catch-all `Uncategorised` rule (priority 9999, no
patterns) guarantees every transaction gets a category.

`PATCH /transactions/{id}/category` records a human correction
(`categorised_by = MANUAL`). Those corrections are the evaluation set:
`GET /eval/accuracy` re-runs the rule engine against every `MANUAL` row and
reports how often it would have guessed right. Accuracy is **not yet
measured** — that needs real statements ingested and ~150-200 corrections
made by hand over real usage, which hasn't happened yet on this build.

## Privacy

Real statements live in `data/`, which is `.gitignore`d from the first
commit. `samples/sample-statement.csv` is synthetic — it was generated for
this README, not anonymised from a real export. No account numbers appear in
logs; only the fingerprint (a hash) and file hash do.

## Known limitations

- **Same-day, same-amount collisions collapse.** Two genuinely identical
  purchases — same merchant, same amount, same day — produce the same
  fingerprint and the second is silently treated as a duplicate. Accepted
  trade-off for idempotent re-uploads; see `Fingerprint`'s Javadoc.
- **Single account by default.** `POST /statements` always ingests into the
  one account seeded by `V1__init.sql`. `TransferMatcher` (which needs two
  different accounts to find a transfer pair) is implemented and unit-tested
  against synthetic candidates, but has no live data to match yet — the API
  doesn't currently expose a way to pick an account per upload.
- **Rule accuracy is unmeasured.** See above — no labelled corrections exist
  yet on this build.
- **No authentication on any endpoint.** Fine for a single-user app reachable
  only on your own local network; not fine if you expose this beyond that
  without adding one.
- **PDF parsing only knows one transaction wording** (mada/Apple Pay POS
  purchases) and **has not been run against a real multi-page PDF file** —
  only against a real extracted-text excerpt (`PdfStatementParserTest`'s
  fixture). The full PDF is loaded and its text extracted in one pass rather
  than streamed page-by-page; fine at the ~41-page scale mentioned when this
  was built, since splitting by page risked cutting a transaction's block in
  half across a page boundary. Revisit if a statement turns out to be
  hundreds of pages.

## Implementation note: why not `ON CONFLICT DO NOTHING` verbatim

The original design (see the spec this was built from) calls for a native
`INSERT ... ON CONFLICT (fingerprint) DO NOTHING` query. That is exactly what
ships here conceptually, but it is expressed differently: `TransactionInserter`
does a plain JPA `save()` + `flush()` inside its own `REQUIRES_NEW`
transaction, and the caller catches the resulting
`DataIntegrityViolationException` as "duplicate". Three reasons:

1. H2 (used for the fast tests) has no `ON CONFLICT` support at all, so a
   literal `ON CONFLICT DO NOTHING` native query cannot run against it —
   Testcontainers (a real throwaway Postgres per test run) was the spec's own
   alternative, but Docker was not available on this machine while building
   this.
2. The uniqueness guarantee still comes entirely from the database's unique
   constraint on `fingerprint` (`Transaction.fingerprint` is `@Column(unique =
   true)`, created for real by `V1__init.sql` in production) — the same
   correctness property the spec's section 18 talking point describes, just
   surfaced as a caught exception instead of a silent zero-row-affected insert.
3. Each insert gets its own transaction (`REQUIRES_NEW`) specifically because
   PostgreSQL aborts an entire transaction on any statement error — without
   that isolation, one duplicate row partway through a 500-row upload would
   silently poison every row after it in the same batch.

If a real Postgres/Testcontainers environment is available, reverting to a
native `ON CONFLICT DO NOTHING` query is a reasonable follow-up — it saves
one transaction-per-row and is one line of SQL closer to the original design.

## Stack

Java 21, Spring Boot 3.3, PostgreSQL 16, Flyway, Maven, JUnit 5 + AssertJ, H2
(fast tests), Docker Compose.

## Running the tests

```bash
./mvnw test
```

34 tests: `StatementParserTest`, `PdfStatementParserTest` (built from a real
anonymised statement excerpt), `DescriptionNormaliserTest` (13 cases covering
every normalisation step plus the bilingual example), `FingerprintTest`,
`IngestionServiceIdempotencyTest` (the triple-upload proof), `RuleEngineTest`,
`TransferMatcherTest` (including the tricky same-amount-same-direction
negative case).
