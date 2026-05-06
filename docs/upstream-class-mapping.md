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
  jp.igapyon.mikugrep.model.RequestMode
  jp.igapyon.mikugrep.model.Diagnostic
  jp.igapyon.mikugrep.model.Summary
  jp.igapyon.mikugrep.model.MikuGrepMatch
  jp.igapyon.mikugrep.model.ContentMatch
  jp.igapyon.mikugrep.model.FileSummaryMatch
  jp.igapyon.mikugrep.model.FileSummarySnippet
  jp.igapyon.mikugrep.model.DirectorySummaryMatch
  jp.igapyon.mikugrep.model.AgentFileMatch
  jp.igapyon.mikugrep.model.AgentDirectoryMatch
  jp.igapyon.mikugrep.model.FileListEntry
  jp.igapyon.mikugrep.model.FileListSummary
  jp.igapyon.mikugrep.model.FileListSummaryCount
  jp.igapyon.mikugrep.model.ReadfileRequestHint
  jp.igapyon.mikugrep.model.ReadfileRequest
  jp.igapyon.mikugrep.model.ReadfileRequestFile
  jp.igapyon.mikugrep.model.RelevanceInfo
  jp.igapyon.mikugrep.model.ReadRangeCandidate
  jp.igapyon.mikugrep.model.ContextLine
  jp.igapyon.mikugrep.model.EncodingRuleInput
  jp.igapyon.mikugrep.model.EncodingRuleResult
  jp.igapyon.mikugrep.json.MikuGrepJson

notes:
  - Model field names should preserve upstream JSON names.
  - JSON output ordering should be stable and should follow the upstream
    contract where visible.
  - `MikuGrepJson` is the Jackson helper for request / result JSON handling.
  - v0.9.0 adds listFiles, agent matches, relevance, readfile hints,
    query case, and glob query model fields.
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
  vendor/miku-grep/src/search-results.ts
  vendor/miku-grep/src/match-text.ts
  vendor/miku-grep/src/encoding.ts

java classes:
  jp.igapyon.mikugrep.search.Search
  jp.igapyon.mikugrep.search.SearchResult
  jp.igapyon.mikugrep.search.SearchState

notes:
  - Covers traversal, filepath search, directory search, content search,
    diagnostics, summary counts, match ordering, snippet trimming, context
    lines, ignore files, and encoding selection.
  - Runtime differences in Shift_JIS decoding are acceptable when documented.
```

```text
upstream file:
  vendor/miku-grep/src/list-files.ts

java classes:
  jp.igapyon.mikugrep.search.ListFiles
  jp.igapyon.mikugrep.search.ListFilesResult
  jp.igapyon.mikugrep.model.FileListEntry
  jp.igapyon.mikugrep.model.FileListSummary
  jp.igapyon.mikugrep.model.FileListSummaryCount

notes:
  - Covers `mode: "listFiles"` traversal, candidate filtering, optional glob
    query path filtering, file list ordering, and file summary aggregation.
```

```text
upstream file:
  vendor/miku-grep/src/context-lines.ts

java classes:
  jp.igapyon.mikugrep.search.Search
  jp.igapyon.mikugrep.model.ContextLine

notes:
  - Covers detail-mode context lines around content hits.
  - Java implementation is covered by `Search` and `ContextLine`.
```

```text
upstream file:
  vendor/miku-grep/src/ignore-files.ts

java classes:
  jp.igapyon.mikugrep.search.Search
  jp.igapyon.mikugrep.model.IgnoreMode
  jp.igapyon.mikugrep.model.IgnoreOptions
  jp.igapyon.mikugrep.model.IgnoreLoadedSource

notes:
  - Covers `.gitignore`, `.ignore`, and `.git/info/exclude` loading.
  - Java implementation covers the upstream MVP ignore pattern subset,
    ignored counters, loaded source reporting, and ignore diagnostics.
```

```text
upstream file:
  vendor/miku-grep/src/string-order.ts

java classes:
  jp.igapyon.mikugrep.search.Search
  jp.igapyon.mikugrep.result.ResultBuilder

notes:
  - Covers deterministic Node-side ordering aligned with Java string ordering.
  - Java should continue to use deterministic lexicographic ordering for
    matches and diagnostics.
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
