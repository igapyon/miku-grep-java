# Upstream Follow-up Log

This document records concrete upstream-following checks for `miku-grep-java`.

The stable upstream reference is `vendor/miku-grep`.

Use one entry per upstream file or Java-side extension.

## Entry Template

```text
upstream file:
  vendor/miku-grep/src/<target>.ts

java classes:
  jp.igapyon.mikugrep.<package>.<ClassA>

tests:
  <RelatedTest>

diff summary:
  behavior diff:
  naming diff:
  unmigrated diff:
  Java-side extension:

follow-up:
  - checks performed:
  - fixture:
  - next check viewpoint:
```

## Initial Preparation

```text
upstream file:
  vendor/miku-grep/src/main.ts
  vendor/miku-grep/docs/miku-grep-cli-spec.md

java classes:
  jp.igapyon.mikugrep.coreapi.MikuGrep
  jp.igapyon.mikugrep.cli.MikuGrepCli

tests:
  jp.igapyon.mikugrep.cli.MikuGrepCliTest

diff summary:
  behavior diff:
    - Not implemented yet.
    - Java CLI should preserve stdin request JSON, stdout result JSON, stderr role, and exit codes.
  naming diff:
    - Java CLI class is fixed as MikuGrepCli.
  unmigrated diff:
    - All Java CLI behavior is pending.
  Java-side extension:
    - Runtime jar packaging is Java-side packaging behavior.

follow-up:
  - `docs/cli-json-parity.md` records the CLI JSON parity policy.
  - `docs/upstream-class-mapping.md` records the planned Java class group.
  - Implement CLI only after model, validation, result builder, and search core exist.
```

```text
upstream file:
  vendor/miku-grep/src/public-types.ts
  vendor/miku-grep/src/types.ts
  vendor/miku-grep/src/request-contract.ts

java classes:
  jp.igapyon.mikugrep.model.*
  jp.igapyon.mikugrep.contract.RequestContract

tests:
  jp.igapyon.mikugrep.validation.ValidationTest
  jp.igapyon.mikugrep.cli.MikuGrepCliTest

diff summary:
  behavior diff:
    - Not implemented yet.
    - JSON field names, defaults, limits, and result shape should preserve upstream behavior.
  naming diff:
    - Java model names use UpperCamelCase while preserving upstream JSON field names.
  unmigrated diff:
    - All Java model and request contract behavior is pending.
  Java-side extension:
    - Jackson is selected as the Java JSON implementation detail.

follow-up:
  - `docs/cli-json-parity.md` records Jackson usage and JSON ordering policy.
  - Implement stable JSON serialization order before CLI parity tests.
```

```text
upstream file:
  vendor/miku-grep/src/regex-safety.ts
  vendor/miku-grep/src/path-security.ts

java classes:
  jp.igapyon.mikugrep.regex.RegexSafety
  jp.igapyon.mikugrep.pathsecurity.PathSecurity

tests:
  jp.igapyon.mikugrep.regex.RegexSafetyTest
  jp.igapyon.mikugrep.pathsecurity.PathSecurityTest

diff summary:
  behavior diff:
    - Not implemented yet.
    - Regex engine behavior may differ between Node.js and Java.
  naming diff:
    - Java class names use responsibility names from upstream files.
  unmigrated diff:
    - Both helper implementations are pending.
  Java-side extension:
    - None planned for these helpers.

follow-up:
  - These helpers are the recommended first implementation unit after the Maven skeleton.
```
