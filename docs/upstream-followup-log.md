# Upstream Follow-up Log

This document records concrete upstream-following checks for `miku-grep-java`.

The stable upstream reference is `vendor/miku-grep`.

Use one entry per upstream file or Java-side extension.

## 2026-05-07 Vendor Snapshot Refresh

```text
upstream file:
  vendor/miku-grep

java classes:
  jp.igapyon.mikugrep.validation.Validation
  jp.igapyon.mikugrep.search.Search
  jp.igapyon.mikugrep.search.ListFiles
  jp.igapyon.mikugrep.coreapi.MikuGrep
  jp.igapyon.mikugrep.model.*

tests:
  jp.igapyon.mikugrep.search.SearchTest
  jp.igapyon.mikugrep.coreapi.MikuGrepTest
  jp.igapyon.mikugrep.validation.ValidationTest
  jp.igapyon.mikugrep.cli.MikuGrepCliTest

diff summary:
  behavior diff:
    - Upstream `devel` advanced from
      `6919c0f65d8347c0b92338806e859d4bf59f2fdb` to
      `c123b943549bb0e85bdc3e95feb4be7b7ddc003d`.
    - Upstream tag `v0.9.0.1` points at the new snapshot commit.
    - Upstream package version is 0.9.0.
    - New request fields include detectGitRoot, mode, query.case,
      output.sort, output.includeReadfileRequestHints, and encoding.preset.
    - New behavior includes listFiles mode, agent output mode, relevance
      sorting, readfile hints, glob query support, case-insensitive matching,
      and ignore negation.
  naming diff:
    - listFiles mode returns top-level files and fileSummary.
    - agent mode emits agentFile / agentDirectory match types.
  unmigrated diff:
    - None recorded for the Java v0.9.0 request / result contract in this
      follow-up.
  Java-side extension:
    - Java packaging remains a Java-side runtime artifact.

follow-up:
  - Fetched `https://github.com/igapyon/miku-grep.git` into
    `workplace/upstream-miku-grep`.
  - Synchronized the vendored snapshot under `vendor/miku-grep`.
  - Updated `docs/upstream-snapshot.md` to `c123b943549bb0e85bdc3e95feb4be7b7ddc003d`.
  - Updated Java request validation, model shape, search, listFiles, core API,
    version, README, and mapping documents.
  - Added focused Java tests for listFiles, agent relevance/readfile hints,
    and ignore negation.
  - `mvn test` passed with 64 tests.
```

## 2026-05-03 Vendor Snapshot Refresh

```text
upstream file:
  vendor/miku-grep

java classes:
  jp.igapyon.mikugrep.search.Search
  jp.igapyon.mikugrep.cli.HelpText

tests:
  jp.igapyon.mikugrep.search.SearchTest
  jp.igapyon.mikugrep.docs.DocumentationSyncTest

diff summary:
  behavior diff:
    - Upstream `devel` advanced from
      `0dde0221b16118848b7c859aeb74c32595ff40ed` to
      `6919c0f65d8347c0b92338806e859d4bf59f2fdb`.
    - Upstream package version remains 0.8.4.
    - The new upstream commit is not tagged.
  naming diff:
    - Detail mode filepath / directory hits are documented and tested as
      one representative hit per matched path.
  unmigrated diff:
    - None for the path detail representative-hit contract in this follow-up.
  Java-side extension:
    - Java packaging remains a Java-side runtime artifact.

follow-up:
  - Fetched `https://github.com/igapyon/miku-grep.git` into
    `workplace/upstream-miku-grep`.
  - Fast-forwarded local upstream checkout to
    `6919c0f65d8347c0b92338806e859d4bf59f2fdb`.
  - Synchronized the vendored snapshot under `vendor/miku-grep`.
  - Updated `docs/upstream-snapshot.md` to the new commit.
  - Updated Java filepath / directory detail search to choose one
    representative path match.
  - Updated Java CLI help, Java CLI spec, README, and focused tests.
  - `mvn test -Dtest=SearchTest` passed.
  - `mvn test` passed with 61 tests.
  - `mvn package` passed with 61 tests and created
    `target/miku-grep-0.8.4-dist.zip`.
```

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
  vendor/miku-grep

java classes:
  mapped Java class groups

tests:
  mapping inventory only

diff summary:
  behavior diff:
    - Upstream `devel` advanced from
      `f1104a0e4e650b23a0b09ea22e15731a4d67c589` to
      `0dde0221b16118848b7c859aeb74c32595ff40ed`.
    - Upstream package version at the new snapshot is 0.8.4.
    - Upstream tag `v0.8.4` points at the new snapshot commit.
    - Request search target changed from singular `target` values
      `content` / `filename` / `both` to `targets` arrays containing
      `content` / `filepath` / `directory`.
    - Output summary mode changed from `file-summary` to `summary`.
    - Detail output gained `contextLines`, `contextLinesBefore`, and
      `contextLinesAfter`.
    - Ignore file handling was added for `.gitignore`, `.ignore`, and
      `.git/info/exclude`.
    - Directory matches, directory summary matches, ignored counters, and
      directory scan counters were added to the result contract.
  naming diff:
    - Upstream renamed filename match vocabulary to filepath match vocabulary.
  unmigrated diff:
    - None for the Java v0.8.4 request / result contract accepted in this
      follow-up.
  Java-side extension:
    - Java packaging remains a Java-side runtime artifact.

follow-up:
  - Fetched `https://github.com/igapyon/miku-grep.git` into
    `workplace/upstream-miku-grep`.
  - Fast-forwarded local upstream checkout to
    `0dde0221b16118848b7c859aeb74c32595ff40ed`.
  - Synchronized the vendored snapshot under `vendor/miku-grep`.
  - Updated `docs/upstream-snapshot.md` to the new commit.
  - Fetched and confirmed upstream tag `v0.8.4`.
  - Updated upstream class and test mappings for new upstream files and tests.
  - Updated Java request validation, result models, search traversal, help
    text, Java CLI spec, README, and focused tests for upstream v0.8.4
    behavior.
  - `mvn test -Dtest=SearchTest,ValidationTest,MikuGrepJsonTest,MikuGrepCliTest`
    passed.
  - `mvn test` passed.
  - `mvn package` passed and created `target/miku-grep-0.8.4-dist.zip`.
  - `java -jar target/miku-grep.jar --version` printed `miku-grep 0.8.4`.
  - `java -jar target/miku-grep.jar --help` succeeded.
```

```text
upstream file:
  vendor/miku-grep/src/validation.ts
  vendor/miku-grep/src/help.ts
  vendor/miku-grep/docs/miku-grep-cli-spec.md
  vendor/miku-grep/test/validation.test.ts
  vendor/miku-grep/test/search-content.test.ts
  vendor/miku-grep/test/search-filename.test.ts

java classes:
  jp.igapyon.mikugrep.validation.Validation
  jp.igapyon.mikugrep.cli.HelpText

tests:
  jp.igapyon.mikugrep.validation.ValidationTest
  jp.igapyon.mikugrep.search.SearchTest
  jp.igapyon.mikugrep.cli.MikuGrepCliTest

diff summary:
  behavior diff:
    - Upstream now treats request excludeFileNamePatterns and
      excludeDirNamePatterns as replacements for default excludes when those
      fields are specified.
    - Omitted exclude pattern fields keep the default exclude preset.
    - Empty exclude pattern arrays now allow default-excluded files or
      directories to be included by request.
  naming diff:
    - Java field names remain aligned with upstream JSON field names.
  unmigrated diff:
    - Node release bundle workflow is vendored under vendor/miku-grep but is
      not a Java runtime workflow.
  Java-side extension:
    - Java package version and CLI --version output are aligned to 0.8.1.

follow-up:
  - Updated Validation to preserve the upstream replacement semantics.
  - Updated HelpText and Java CLI specification wording for exclude defaults.
  - Added focused Java tests for empty exclude arrays and omitted default
    excludes.
  - `mvn test -Dtest=ValidationTest,SearchTest,MikuGrepCliTest,DocumentationSyncTest`
    passed.
  - `mvn test` passed.
  - `mvn package` passed and created `target/miku-grep-0.8.1-dist.zip`.
  - `java -jar target/miku-grep.jar --version` printed `miku-grep 0.8.1`.
```

```text
upstream file:
  vendor/miku-grep

java classes:
  all mapped Java class groups

tests:
  not applicable for diff inventory

diff summary:
  behavior diff:
    - No latest-upstream source diff was found.
  naming diff:
    - No latest-upstream naming diff was found.
  unmigrated diff:
    - None from the latest-upstream diff check.
  Java-side extension:
    - Java packaging and root README remain Java-side runtime extensions.

follow-up:
  - Checked latest upstream `devel` by cloning
    `https://github.com/igapyon/miku-grep.git` under `workplace/`.
  - Latest upstream HEAD was `e3e3a998aaec6b0a8263e1b56fcdbea6ccfef2e2`,
    matching `docs/upstream-snapshot.md`.
  - `diff -qr vendor/miku-grep workplace/upstream-miku-grep` found no source
    differences except the temporary clone's `.git` directory.
  - No Java-side source or test follow-up was required from upstream changes.
```

```text
upstream file:
  vendor/miku-grep/src/help.ts
  vendor/miku-grep/docs/miku-grep-cli-spec.md

java classes:
  jp.igapyon.mikugrep.cli.HelpText

tests:
  jp.igapyon.mikugrep.docs.DocumentationSyncTest
  jp.igapyon.mikugrep.cli.MikuGrepCliTest

diff summary:
  behavior diff:
    - Java help preserves the upstream CLI contract sections and examples while
      stating Java `Pattern` for regex runtime behavior.
    - Java README and `docs/miku-grep-cli-spec.md` document the same stdio
      contract and accepted runtime differences.
  naming diff:
    - Java-side CLI specification lives at `docs/miku-grep-cli-spec.md`.
  unmigrated diff:
    - None for the current runtime-facing documentation contract.
  Java-side extension:
    - Distribution assembly includes runtime-facing docs in addition to the
      executable jar.

follow-up:
  - Added `DocumentationSyncTest` to guard README, Java CLI spec,
    `docs/cli-json-parity.md`, CLI help pointers, and distribution assembly
    contents.
  - Java regex engine and Shift_JIS decoder differences remain documented as
    accepted runtime differences.
```

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
    - Java CLI stdin JSON execution has been implemented.
    - CLI writes result JSON to stdout, usage / malformed stdin / unexpected
      runtime messages to stderr, and returns upstream-aligned exit codes.
    - Core API runRequest performs validation, root checks, search execution,
      and result assembly.
  naming diff:
    - Java CLI class is fixed as MikuGrepCli.
  unmigrated diff:
    - None for the current CLI meta contract.
  Java-side extension:
    - Runtime jar and distribution zip packaging are Java-side packaging
      behavior.

follow-up:
  - `docs/cli-json-parity.md` records the CLI JSON parity policy.
  - `docs/upstream-class-mapping.md` records the planned Java class group.
  - `MikuGrepTest` verifies runRequest success, validation failure, and root
    failure paths.
  - `MikuGrepCliTest` verifies version, usage error, malformed stdin, success
    result JSON, request failure exit code, and detailed help contract text.
  - Packaging now creates a single fat jar and distribution zip.
  - `mvn test -Dtest=MikuGrepCliTest`, `mvn test`, and `mvn package` passed.
  - `java -jar target/miku-grep.jar --version` and `--help` passed.
```

```text
upstream file:
  vendor/miku-grep/src/search.ts
  vendor/miku-grep/src/internal-types.ts

java classes:
  jp.igapyon.mikugrep.search.Search
  jp.igapyon.mikugrep.search.SearchResult
  jp.igapyon.mikugrep.search.SearchState

tests:
  jp.igapyon.mikugrep.search.SearchTest

diff summary:
  behavior diff:
    - Search traversal, filename/content matching, snippet trimming, summary
      counts, diagnostics, and encoding selection have been implemented.
    - Shift_JIS decoder behavior may differ from upstream Node dependencies as
      previously accepted.
  naming diff:
    - Upstream runSearch function name is preserved.
  unmigrated diff:
    - Dedicated split tests for encoding diagnostics and limits can be expanded
      later; representative coverage currently lives in SearchTest.
  Java-side extension:
    - Java NIO is used for filesystem traversal and root resolution.

follow-up:
  - `SearchTest` verifies file-summary, detail regex, include/exclude,
    traversal depth, filename/both search, snippets, line ending normalization,
    binary skip, file size, and decode diagnostics.
  - `mvn test` passed.
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
    - JSON model classes and Jackson helper have been implemented.
    - Stable field names and visible field order are covered by focused JSON tests.
    - Request contract version, defaults, limits, default excludes, and
      request shape metadata have been implemented.
  naming diff:
    - Java model names use UpperCamelCase while preserving upstream JSON field names.
  unmigrated diff:
    - Unknown-field validation and effective request expansion are pending in
      the validation layer.
  Java-side extension:
    - Jackson is selected as the Java JSON implementation detail.

follow-up:
  - `docs/cli-json-parity.md` records Jackson usage and JSON ordering policy.
  - `MikuGrepJsonTest` verifies request field-name decoding, result field
    order, and tree parsing for later unknown-field validation.
  - `RequestContractTest` verifies upstream contract values and request shape.
  - `mvn test` passed.
```

```text
upstream file:
  vendor/miku-grep/src/result.ts

java classes:
  jp.igapyon.mikugrep.result.ResultBuilder

tests:
  jp.igapyon.mikugrep.result.ResultBuilderTest

diff summary:
  behavior diff:
    - Result builder has been implemented for base summary creation, top-level
      result assembly, summary diagnostic count, and diagnostics sorting.
  naming diff:
    - Upstream function names are mapped to createSummary and finish.
  unmigrated diff:
    - Search-produced summaries and diagnostics are pending in the search layer.
    - CLI/main use of the result builder is pending.
  Java-side extension:
    - None planned for this builder.

follow-up:
  - `ResultBuilderTest` verifies summary defaults, error fallback values,
    diagnostic count propagation, and sort order.
  - `mvn test` passed.
```

```text
upstream file:
  vendor/miku-grep/src/validation.ts
  vendor/miku-grep/src/internal-types.ts

java classes:
  jp.igapyon.mikugrep.validation.Validation
  jp.igapyon.mikugrep.validation.ValidationResult

tests:
  jp.igapyon.mikugrep.validation.ValidationTest

diff summary:
  behavior diff:
    - Validation and effective request normalization have been implemented.
    - Regex syntax validation uses Java Pattern, so regex engine differences
      remain an accepted runtime difference.
  naming diff:
    - Upstream validateAndNormalize function name is preserved.
    - ValidationResult is a Java object rather than a TypeScript union.
  unmigrated diff:
    - Root filesystem checks are still pending in the main/core layer.
    - Top-level error result construction is still pending in the result
      builder and main/core layer.
  Java-side extension:
    - Validation accepts Jackson JsonNode directly to preserve upstream-like
      type checks and error codes before POJO binding.

follow-up:
  - `ValidationTest` verifies defaults, excludes, unknown fields, request
    shape errors, regex checks, limits, and encoding rules.
  - `mvn test` passed.
```

```text
upstream file:
  vendor/miku-grep/src/glob.ts

java classes:
  jp.igapyon.mikugrep.glob.Glob

tests:
  jp.igapyon.mikugrep.glob.GlobTest

diff summary:
  behavior diff:
    - MVP glob helper has been implemented for filename and path pattern use.
  naming diff:
    - Upstream function names are preserved as Java static methods:
      matchesAny, globMatch, and pathGlobMatch.
  unmigrated diff:
    - Search and encoding rule usage of this helper is pending.
  Java-side extension:
    - None planned for this helper.

follow-up:
  - `GlobTest` verifies `*`, `?`, regex escaping, and `**` path-part behavior.
  - `mvn test` passed.
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
    - Regex safety and path security helpers have been implemented.
    - Regex engine behavior may differ between Node.js and Java when patterns
      are compiled or executed later.
  naming diff:
    - Java class names use responsibility names from upstream files.
  unmigrated diff:
    - Glob matching and search behavior that use these helpers are still
      pending.
  Java-side extension:
    - None planned for these helpers.

follow-up:
  - Implemented as the first Java implementation unit after the Maven skeleton.
  - `mvn test` passed.
  - `mvn package` passed.
```
