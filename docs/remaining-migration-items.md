# Remaining Migration Items

## Current State

The repository is in the pre-implementation stage of straight conversion.

Completed preparation:

- `workplace/` local work area
- `.mvn/jvm.config` IPv4-preferred Maven setting
- vendored upstream snapshot under `vendor/miku-grep`
- upstream snapshot record in `docs/upstream-snapshot.md`
- CLI JSON parity policy in `docs/cli-json-parity.md`
- upstream class mapping in `docs/upstream-class-mapping.md`
- upstream test mapping in `docs/upstream-test-mapping.md`
- development workflow note in `docs/development.md`

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

Initial implementation should proceed in this order.

1. Maven skeleton
   - `pom.xml`
   - Java 1.8 compiler configuration
   - JUnit Jupiter
   - Jackson dependency
   - runtime jar main class configuration
2. model and JSON support
   - request / effective request / result / diagnostic / match / summary models
   - stable JSON field names and ordering
   - JSON parse / stringify helper around Jackson
3. request contract
   - version
   - defaults
   - limits
   - default excludes
4. small pure helpers
   - regex safety
   - path security
   - glob matching
5. validation
   - unknown-field handling
   - default expansion
   - limit checks
   - diagnostic codes
6. result builder
   - top-level result shape
   - diagnostic sorting
   - summary diagnostic count
7. search
   - directory traversal
   - filename search
   - content search
   - snippet trimming
   - summary counts
   - diagnostics
   - encoding selection and decode behavior
8. CLI
   - stdin request JSON
   - stdout result JSON
   - stderr usage / malformed stdin / unexpected runtime messages
   - `--help`
   - `--version`
   - exit codes
9. packaging
   - executable runtime jar
   - sources jar
   - optional distribution zip when useful

## Known Runtime Difference Items

Document and test these as runtime differences when they affect observable
behavior.

- regex engine differences between Node.js `RegExp` and Java `Pattern`
- Shift_JIS decoder behavior differences between upstream Node dependencies and
  Java charset handling

## Focused Regression

Planned focused regression commands:

- `mvn test -Dtest=RegexSafetyTest,PathSecurityTest`
- `mvn test -Dtest=ValidationTest`
- `mvn test -Dtest=SearchContentTest,SearchFilenameTest`
- `mvn test -Dtest=EncodingDiagnosticsTest,LimitsTest`
- `mvn test -Dtest=MikuGrepCliTest`
- `mvn test`

## Latest Verification

- Not yet run.

The Maven skeleton and Java tests do not exist yet. Docs-only preparation has
been performed before Java implementation starts.

## Next Step

Create the Maven skeleton and the first Java implementation unit:

- `pom.xml`
- `src/main/java/jp/igapyon/mikugrep/...`
- `src/test/java/jp/igapyon/mikugrep/...`
- initial tests for `RegexSafety` and `PathSecurity`
