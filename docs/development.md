# Development

This document is for repository maintainers and contributors.
For normal tool usage, see `README.md`.

## Scope

This document covers:

- repository structure
- local development commands
- focused regression commands
- local temporary workspace rules
- upstream-following documents
- JSON implementation policy

This document does not try to be the user-facing usage guide.

## Repository Structure

This repository is intended to start as a single-module Maven runtime jar.

- repository root
  - Maven project `miku-grep`
- `src/main/java/`
  - Java runtime and CLI implementation
- `src/test/java/`
  - Java tests
- `docs/`
  - migration, mapping, parity, and development documents
- `vendor/miku-grep/`
  - vendored upstream `miku-grep` snapshot
- `workplace/`
  - local upstream checks, temporary smoke inputs, and generated outputs

The Java runtime should preserve the upstream Node.js CLI JSON contract as
described in `docs/cli-json-parity.md`.

## Fixed Java Premises

- Java source / target compatibility: `1.8`
- build tool: Maven
- test framework: JUnit Jupiter
- primary verification command: `mvn test`
- packaging direction: single executable runtime jar
- base package: `jp.igapyon.mikugrep`
- Maven groupId: `jp.igapyon`
- Maven artifactId: `miku-grep`
- CLI class: `jp.igapyon.mikugrep.cli.MikuGrepCli`

## JSON Implementation

`miku-grep-java` uses Jackson for request JSON parsing and result JSON
serialization.

Expected direct dependency:

- `com.fasterxml.jackson.core:jackson-databind`

Expected transitive dependencies:

- `com.fasterxml.jackson.core:jackson-core`
- `com.fasterxml.jackson.core:jackson-annotations`

Jackson is an implementation detail. Observable behavior should follow the
upstream Node.js CLI JSON contract.

Do not rely on Jackson defaults when they would change:

- field names
- field ordering
- null handling
- unknown-field behavior
- pretty printing
- diagnostic codes

## Known Runtime Differences

The following runtime differences are accepted when documented:

- regex engine differences between Node.js `RegExp` and Java `Pattern`
- Shift_JIS decoder behavior differences between upstream Node dependencies and
  Java charset handling

Other JSON contract differences should be treated as regressions unless they are
explicitly recorded.

## Maven IPv4 Setting

This repository uses `.mvn/jvm.config` to prefer IPv4 during Maven execution.

This follows the local miku Java repository practice for environments where
Maven dependency resolution can be affected by IPv6 network behavior.

## Primary Commands

Use these commands for routine local verification after the Maven skeleton
exists:

```bash
mvn test
mvn package
```

Until the Maven skeleton exists, docs-only changes do not require additional
test execution.

## Focused Regression Commands

Planned focused regression commands:

```bash
mvn test -Dtest=RegexSafetyTest,PathSecurityTest
mvn test -Dtest=ValidationTest
mvn test -Dtest=SearchContentTest,SearchFilenameTest
mvn test -Dtest=EncodingDiagnosticsTest,LimitsTest
mvn test -Dtest=MikuGrepCliTest
```

The canonical test mapping is `docs/upstream-test-mapping.md`.

## Docs-only Operation

For updates that only change Markdown documents under `docs/`, additional tests
are not required in principle.

Run focused tests when:

- Java code changes
- Maven build configuration changes
- focused regression commands are added or changed
- CLI JSON contract examples are changed in a way that affects implementation

## Upstream-following Flow

When checking or applying upstream changes:

1. Check `docs/remaining-migration-items.md` for the current position.
2. Check `docs/upstream-snapshot.md` for the vendored upstream snapshot.
3. Use `docs/upstream-class-mapping.md` to find the Java class group for the
   changed upstream file.
4. Use `docs/upstream-test-mapping.md` to find the focused regression unit.
5. Record concrete diff checks in `docs/upstream-followup-log.md`.

## Local Temporary Work

Use `workplace/tmp` for manual smoke inputs and generated outputs.

Rules:

- keep `workplace/.gitkeep` only as tracked content
- do not commit local smoke inputs or generated outputs under `workplace/`
- do not use `workplace/` as a source directory
- do not hide required test fixtures under `workplace/`

`workplace/` is for local reference and temporary verification, not for primary
implementation files.

## Related Documents

- `docs/cli-json-parity.md`
  - CLI JSON compatibility policy
- `docs/upstream-snapshot.md`
  - vendored upstream snapshot information
- `docs/upstream-class-mapping.md`
  - `upstream file -> Java class` mapping
- `docs/upstream-test-mapping.md`
  - `upstream test intent -> Java test` mapping
- `docs/upstream-followup-log.md`
  - concrete upstream diff check records
- `docs/remaining-migration-items.md`
  - current migration state and next implementation units
- `docs/miku-soft-20-javaapp-design-v20260501.md`
  - shared miku Java application design
- `docs/miku-soft-30-straight-conversion-v20260425.md`
  - shared miku straight conversion guide
