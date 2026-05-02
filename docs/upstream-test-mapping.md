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

java tests:
  jp.igapyon.mikugrep.search.SearchContentTest

fixtures:
  temporary files from JUnit TempDir

focused regression:
  mvn test -Dtest=SearchContentTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/search-filename.test.ts

java tests:
  jp.igapyon.mikugrep.search.SearchFilenameTest

fixtures:
  temporary files from JUnit TempDir

focused regression:
  mvn test -Dtest=SearchFilenameTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/encoding-diagnostics.test.ts

java tests:
  jp.igapyon.mikugrep.encoding.EncodingDiagnosticsTest

fixtures:
  temporary files from JUnit TempDir

focused regression:
  mvn test -Dtest=EncodingDiagnosticsTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/limits.test.ts

java tests:
  jp.igapyon.mikugrep.search.LimitsTest

fixtures:
  temporary files from JUnit TempDir

focused regression:
  mvn test -Dtest=LimitsTest
```

```text
upstream test / intent:
  vendor/miku-grep/test/cli-meta.test.ts

java tests:
  jp.igapyon.mikugrep.cli.MikuGrepCliTest

fixtures:
  inline stdin JSON
  temporary files from JUnit TempDir where subprocess-style behavior is needed

focused regression:
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

## Suggested Initial Regression Order

1. `mvn test -Dtest=RegexSafetyTest,PathSecurityTest`
2. `mvn test -Dtest=ValidationTest`
3. `mvn test -Dtest=SearchContentTest,SearchFilenameTest`
4. `mvn test -Dtest=EncodingDiagnosticsTest,LimitsTest`
5. `mvn test -Dtest=MikuGrepCliTest`
6. `mvn test`
