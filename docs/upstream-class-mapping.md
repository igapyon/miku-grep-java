# Upstream Class Mapping

This document tracks `upstream file -> Java class` mapping for the
`miku-grep-java` straight conversion.

The stable upstream reference is `vendor/miku-grep`.

The Java implementation should preserve the CLI JSON contract described in
`docs/cli-json-parity.md`.

## Source Mapping

```text
upstream file:
  vendor/miku-grep/src/request-contract.ts

java classes:
  jp.igapyon.mikugrep.contract.RequestContract
  jp.igapyon.mikugrep.contract.RequestFieldShape

notes:
  - Holds schema version, default values, limit values, default excludes, and
    request-shape metadata.
  - Defaults and limits should match upstream unless a runtime difference is
    explicitly documented.
```

```text
upstream file:
  vendor/miku-grep/src/public-types.ts
  vendor/miku-grep/src/types.ts

java classes:
  jp.igapyon.mikugrep.model.MikuGrepRequest
  jp.igapyon.mikugrep.model.EffectiveRequest
  jp.igapyon.mikugrep.model.MikuGrepResult
  jp.igapyon.mikugrep.model.Diagnostic
  jp.igapyon.mikugrep.model.Summary
  jp.igapyon.mikugrep.model.MikuGrepMatch
  jp.igapyon.mikugrep.model.FilenameMatch
  jp.igapyon.mikugrep.model.ContentMatch
  jp.igapyon.mikugrep.model.FileSummaryMatch
  jp.igapyon.mikugrep.model.FileSummarySnippet
  jp.igapyon.mikugrep.model.EncodingRuleInput
  jp.igapyon.mikugrep.model.EncodingRuleResult
  jp.igapyon.mikugrep.json.MikuGrepJson

notes:
  - Model field names should preserve upstream JSON names.
  - JSON output ordering should be stable and should follow the upstream
    contract where visible.
  - `MikuGrepJson` is the Jackson helper for request / result JSON handling.
```

```text
upstream file:
  vendor/miku-grep/src/validation.ts

java classes:
  jp.igapyon.mikugrep.validation.Validation
  jp.igapyon.mikugrep.validation.ValidationResult

notes:
  - Converts request JSON into EffectiveRequest.
  - Unknown fields, defaults, limits, diagnostic codes, and validation error
    result behavior should match upstream.
```

```text
upstream file:
  vendor/miku-grep/src/result.ts

java classes:
  jp.igapyon.mikugrep.result.ResultBuilder

notes:
  - Builds the top-level MikuGrepResult shape.
  - Diagnostics sorting and summary diagnostic counts should match upstream.
```

```text
upstream file:
  vendor/miku-grep/src/main.ts

java classes:
  jp.igapyon.mikugrep.coreapi.MikuGrep
  jp.igapyon.mikugrep.cli.MikuGrepCli

notes:
  - CLI reads stdin request JSON and writes stdout result JSON.
  - `--help` and `--version` are stdin-free meta commands.
  - Exit codes should match upstream.
  - Core API should remain callable without binding product logic to
    `System.exit`.
```

```text
upstream file:
  vendor/miku-grep/src/search.ts

java classes:
  jp.igapyon.mikugrep.search.Search
  jp.igapyon.mikugrep.search.SearchResult
  jp.igapyon.mikugrep.search.SearchState

notes:
  - Covers traversal, filename search, content search, diagnostics, summary
    counts, match ordering, snippet trimming, and encoding selection.
  - Runtime differences in Shift_JIS decoding are acceptable when documented.
```

```text
upstream file:
  vendor/miku-grep/src/glob.ts

java classes:
  jp.igapyon.mikugrep.glob.Glob

notes:
  - Covers the upstream MVP glob behavior used for include / exclude and
    encoding rules.
```

```text
upstream file:
  vendor/miku-grep/src/regex-safety.ts

java classes:
  jp.igapyon.mikugrep.regex.RegexSafety

notes:
  - Covers nested quantified group detection.
  - Regex engine behavior may differ between Node.js and Java, but validation
    limits and `unsafe_regex` policy should stay aligned where practical.
```

```text
upstream file:
  vendor/miku-grep/src/path-security.ts

java classes:
  jp.igapyon.mikugrep.pathsecurity.PathSecurity

notes:
  - Covers root-boundary checks.
  - Result JSON must use root-relative `/` paths and must not expose absolute
    paths.
```

```text
upstream file:
  vendor/miku-grep/src/help.ts

java classes:
  jp.igapyon.mikugrep.cli.HelpText

notes:
  - Java help should preserve the upstream CLI contract, request fields,
    defaults, limits, result shape, diagnostic code list, and examples.
```

```text
upstream file:
  vendor/miku-grep/src/bundle-entry.ts

java classes:
  none

notes:
  - Node single-file bundle entry.
  - Not directly ported to Java; Java packaging is handled by the runtime jar.
```

```text
upstream file:
  vendor/miku-grep/src/internal-types.ts

java classes:
  jp.igapyon.mikugrep.search.SearchState
  jp.igapyon.mikugrep.search.SearchResult
  jp.igapyon.mikugrep.validation.ValidationResult

notes:
  - Internal helper types are mapped to package-local Java classes where useful.
```
