# Remaining Migration Items

## Current State

The repository has started the first Java straight-conversion implementation
unit.

Completed preparation:

- `workplace/` local work area
- `.mvn/jvm.config` IPv4-preferred Maven setting
- vendored upstream snapshot under `vendor/miku-grep`
- upstream snapshot record in `docs/upstream-snapshot.md`
- CLI JSON parity policy in `docs/cli-json-parity.md`
- upstream class mapping in `docs/upstream-class-mapping.md`
- upstream test mapping in `docs/upstream-test-mapping.md`
- development workflow note in `docs/development.md`

Completed implementation:

- Maven skeleton
- Java 1.8 compiler configuration
- JUnit Jupiter test setup
- Jackson dependency
- executable runtime jar main class configuration
- minimal CLI entry point
- request / effective request / result / diagnostic / match / summary models
- stable JSON field-name and field-order tests
- Jackson request / result JSON helper
- request contract version, defaults, limits, default excludes, and request
  shape metadata
- glob matching helper
- regex safety helper
- path security helper
- focused tests for JSON support, request contract, glob matching, regex
  safety, and path security
- validation layer for unknown fields, default expansion, limits, regex checks,
  encoding rules, and diagnostic codes
- focused tests for validation
- result builder for top-level result shape, summary diagnostic count, and
  diagnostic sorting
- focused tests for result builder
- search layer for traversal, filepath/directory/content search, snippet
  trimming, context lines, ignore files, summary counts, diagnostics, and
  encoding selection
- core API facade for request validation, root checks, search execution, and
  result assembly
- CLI stdin JSON execution, stdout result JSON, stderr error messages, and
  exit codes
- focused tests for search, core API, and CLI
- upstream-aligned detailed `--help` text
- root README for Java runtime usage and build outputs
- distribution zip packaging with runtime jar, README, LICENSE, and docs
- Java runtime CLI spec and documentation synchronization test

Fixed implementation direction:

- single-module Maven runtime jar
- Java source / target compatibility `1.8`
- JUnit Jupiter
- Jackson for request JSON parsing and result JSON serialization
- base package `jp.igapyon.mikugrep`
- Maven artifactId `miku-grep`
- CLI class `jp.igapyon.mikugrep.cli.MikuGrepCli`
- Node CLI stdin / stdout JSON contract parity as the primary compatibility
  target

## Pending Implementation Units

Initial straight-conversion implementation units are complete.

Next work should move to maintenance checks and upstream-following operation.

## Known Runtime Difference Items

Document and test these as runtime differences when they affect observable
behavior.

- regex engine differences between Node.js `RegExp` and Java `Pattern`
- Shift_JIS decoder behavior differences between upstream Node dependencies and
  Java charset handling
  - Java uses standard charset handling; no additional decoder dependency is
    needed for the straight-conversion runtime.

## Focused Regression

Planned focused regression commands:

- `mvn test -Dtest=RegexSafetyTest,PathSecurityTest`
- `mvn test -Dtest=MikuGrepJsonTest`
- `mvn test -Dtest=RequestContractTest`
- `mvn test -Dtest=GlobTest`
- `mvn test -Dtest=ValidationTest`
- `mvn test -Dtest=ResultBuilderTest`
- `mvn test -Dtest=SearchTest`
- `mvn test -Dtest=EncodingDiagnosticsTest,LimitsTest`
- `mvn test -Dtest=MikuGrepTest`
- `mvn test -Dtest=MikuGrepCliTest`
- `mvn test -Dtest=DocumentationSyncTest`
- `mvn package`
- `mvn test`

## Latest Verification

- `mvn test -Dtest=SearchTest` succeeded with 17 tests.
- `mvn test` succeeded with 61 tests.
- `mvn package` succeeded with 61 tests and created `target/miku-grep-0.8.4-dist.zip`.
- `mvn test -Dtest=DocumentationSyncTest,MikuGrepCliTest` succeeded.
- `mvn test -Dtest=MikuGrepCliTest` succeeded.
- `mvn package` succeeded with 55 tests.
- `mvn package` succeeded and created `target/miku-grep-0.8.4-dist.zip`.
- `target/miku-grep-0.8.4-dist.zip` includes `docs/miku-grep-cli-spec.md`.
- `java -jar target/miku-grep.jar --version` succeeded.
- `java -jar target/miku-grep.jar --help` succeeded.

## Next Step

Move to maintenance checks and upstream-following operation.
