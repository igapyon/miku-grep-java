# Upstream Test Mapping

This document tracks `upstream test intent -> Java test` mapping for the
`miku-grep-java` straight conversion.

The stable upstream reference is `vendor/miku-grep`.

## Policy

Java tests should preserve the upstream Node.js test intent and the CLI JSON
contract described in `docs/cli-json-parity.md`.

Regex engine differences and Shift_JIS decoder differences are acceptable
runtime differences. Other observable JSON contract differences should be
treated as regressions unless explicitly documented.

## Test Mapping

```text
upstream test / intent:
  vendor/miku-grep/src/request-contract.ts
  upstream request version, defaults, limits, default excludes, and request shape

java tests:
  jp.igapyon.mikugrep.contract.RequestContractTest

fixtures:
  inline request JSON

focused regression:
  mvn test -Dtest=RequestContractTest
```

```text
upstream test / intent:
  vendor/miku-grep/src/public-types.ts
  stable CLI JSON request / result field names and field order

java tests:
  jp.igapyon.mikugrep.json.MikuGrepJsonTest

fixtures:
  inline request and result objects

focused regression:
  mvn test -Dtest=MikuGrepJsonTest
```

```text
upstream test / intent:
  vendor/miku-grep/src/result.ts
  top-level result shape, summary diagnostics count, and diagnostics sorting

java tests:
  jp.igapyon.mikugrep.result.ResultBuilderTest

fixtures:
  inline summary and diagnostic objects

focused regression:
  mvn test -Dtest=ResultBuilderTest
```

```text
upstream test / intent:
  vendor/miku-grep/src/glob.ts
  MVP glob matching used by include / exclude patterns and encoding rules

java tests:
  jp.igapyon.mikugrep.glob.GlobTest

fixtures:
  inline glob patterns and paths

focused regression:
  mvn test -Dtest=GlobTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/validation.test.ts

java tests:
  jp.igapyon.mikugrep.validation.ValidationTest

fixtures:
  inline request objects
  temporary directories where root validation requires filesystem state

focused regression:
  mvn test -Dtest=ValidationTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/regex-safety.test.ts

java tests:
  jp.igapyon.mikugrep.regex.RegexSafetyTest

fixtures:
  inline regex patterns

focused regression:
  mvn test -Dtest=RegexSafetyTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/path-security.test.ts

java tests:
  jp.igapyon.mikugrep.pathsecurity.PathSecurityTest

fixtures:
  inline paths

focused regression:
  mvn test -Dtest=PathSecurityTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/search-content.test.ts
  detail content matches, snippets, context lines, and content target behavior

java tests:
  jp.igapyon.mikugrep.search.SearchTest

fixtures:
  temporary files from JUnit TempDir

focused regression:
  mvn test -Dtest=SearchTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/search-filename.test.ts
  filepath target, directory target, combined targets, and summary aggregation

java tests:
  jp.igapyon.mikugrep.search.SearchTest

fixtures:
  temporary files from JUnit TempDir

focused regression:
  mvn test -Dtest=SearchTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/ignore-files.test.ts
  ignore file discovery, ignore source selection, ignored counters, and
  unsupported ignore-pattern diagnostics

java tests:
  jp.igapyon.mikugrep.search.SearchTest
  jp.igapyon.mikugrep.validation.ValidationTest

fixtures:
  temporary files from JUnit TempDir

focused regression:
  mvn test -Dtest=SearchTest,ValidationTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/encoding-diagnostics.test.ts

java tests:
  jp.igapyon.mikugrep.search.SearchTest

fixtures:
  temporary files from JUnit TempDir

focused regression:
  mvn test -Dtest=SearchTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/limits.test.ts

java tests:
  jp.igapyon.mikugrep.search.SearchTest

fixtures:
  temporary files from JUnit TempDir

focused regression:
  mvn test -Dtest=SearchTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/cli-meta.test.ts
  vendor/miku-grep/src/main.ts

java tests:
  jp.igapyon.mikugrep.coreapi.MikuGrepTest
  jp.igapyon.mikugrep.cli.MikuGrepCliTest

fixtures:
  inline stdin JSON
  temporary files from JUnit TempDir where subprocess-style behavior is needed

focused regression:
  mvn test -Dtest=MikuGrepTest
  mvn test -Dtest=MikuGrepCliTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/helpers.ts

java tests:
  jp.igapyon.mikugrep.testutil.MikuGrepTestFixtures

fixtures:
  Java-side test helper only

focused regression:
  used through the focused tests above
```

```text
upstream test / intent:
  vendor/miku-grep/src/help.ts
  vendor/miku-grep/docs/miku-grep-cli-spec.md
  Java-side runtime packaging documentation

java tests:
  jp.igapyon.mikugrep.docs.DocumentationSyncTest

fixtures:
  README.md
  docs/miku-grep-cli-spec.md
  docs/cli-json-parity.md
  src/assembly/dist.xml
  jp.igapyon.mikugrep.cli.HelpText

focused regression:
  mvn test -Dtest=DocumentationSyncTest
```

## Suggested Initial Regression Order

1. `mvn test -Dtest=RegexSafetyTest,PathSecurityTest`
2. `mvn test -Dtest=MikuGrepJsonTest`
3. `mvn test -Dtest=RequestContractTest`
4. `mvn test -Dtest=GlobTest`
5. `mvn test -Dtest=ValidationTest`
6. `mvn test -Dtest=ResultBuilderTest`
7. `mvn test -Dtest=SearchTest`
8. `mvn test -Dtest=MikuGrepTest`
9. `mvn test -Dtest=MikuGrepCliTest`
10. `mvn test -Dtest=DocumentationSyncTest`
11. `mvn test`
