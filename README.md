# Spend Tracker

A personal spending tracker that ingests bank statement CSV exports, cleans and
categorises the transactions, stores them, and serves them over a REST API.

## Quickstart

```bash
docker compose up
curl -F "file=@samples/sample-statement.csv" \
     "http://localhost:8080/statements?mapping=bank-a"
```

> **Note on this build:** every piece below (parsing, normalisation, idempotent
> ingestion, rule-based categorisation, transfer matching, pagination, summaries)
> is implemented and covered by its own unit/integration tests — 31 tests,
> all green, run against an H2 in-memory database. `docker compose up` itself
> has **not** been exercised end-to-end on this machine because Docker Desktop's
> backend was unresponsive at the time this was built. Run it once on a machine
> with a working Docker install to get the real numbers for the CV lines below.

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

31 tests: `StatementParserTest`, `DescriptionNormaliserTest` (13 cases
covering every normalisation step plus the bilingual example),
`FingerprintTest`, `IngestionServiceIdempotencyTest` (the triple-upload
proof), `RuleEngineTest`, `TransferMatcherTest` (including the tricky
same-amount-same-direction negative case).
