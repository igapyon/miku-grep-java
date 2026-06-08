# miku-grep Java CLI Specification

This document describes the Java runtime CLI contract for `miku-grep-java`.

The Java CLI preserves the upstream Node.js `miku-grep` stdin / stdout JSON
contract and args-first search entry point. The upstream snapshot used for
straight conversion is recorded in `docs/upstream-snapshot.md`.

## Usage

```bash
java -jar target/miku-grep.jar TODO .
java -jar target/miku-grep.jar TODO . --context 2
java -jar target/miku-grep.jar TODO . --files
java -jar target/miku-grep.jar TODO . --agent
java -jar target/miku-grep.jar TODO . --format json
java -jar target/miku-grep.jar --files .
java -jar target/miku-grep.jar < request.json > result.json
java -jar target/miku-grep.jar --version
java -jar target/miku-grep.jar --help
```

Distribution zip users can run the versioned jar inside the extracted archive:

```bash
java -jar miku-grep-0.10.0.jar < request.json > result.json
```

## Args-First Contract

When query arguments are provided, stdout contains text unless `--format json`
or `--json` is specified. The text format is intended for humans and AI agents
in an interactive exploration loop. JSON remains the machine contract.

```bash
java -jar target/miku-grep.jar QUERY [ROOT]
java -jar target/miku-grep.jar QUERY [ROOT] --agent
java -jar target/miku-grep.jar QUERY [ROOT] --files
java -jar target/miku-grep.jar QUERY [ROOT] --context N
java -jar target/miku-grep.jar QUERY [ROOT] --format json
java -jar target/miku-grep.jar --files [ROOT]
```

Supported argument options:

- `--agent`
  - text mode returns a compact next-read summary
  - JSON mode maps to `output.mode: "agent"`, relevance sort, and readfile hints
- `--files`
  - with `QUERY`, prints matching file paths
  - without `QUERY`, lists files under `ROOT`
- `--context N`
  - maps to `output.mode: "detail"` and `output.contextLines`
- `--limit N`
  - maps to `output.maxMatches`
- `--top-files N`
  - maps to agent output with relevance sort and max matches
- `--format text|json` and `--json`
  - select text or JSON output for args-first calls
- `--encoding utf-8|shift_jis`
  - maps to `encoding.default`
- `--encoding-preset japanese-legacy`
  - maps to `encoding.preset`
- `--ignore-case` / `-i`
  - maps to `query.case: "insensitive"`
- `--regex`
  - maps to `query.type: "regex"`
- `--glob`
  - maps to `query.type: "glob"` and filepath search
- `--path`
  - maps to filepath search
- `--all-targets`
  - maps to filepath, directory, and content search
- `--detect-git-root`
  - maps to `detectGitRoot: true`
- `--no-ignore`
  - maps to `ignore.mode: "none"`

The following combinations are rejected as usage errors:

- `--files`, `--agent` / `--top-files`, and `--context`
- `--regex` and `--glob`
- `--path` and `--all-targets`
- `--glob` and `--all-targets`

## Stdio JSON Contract

```text
stdin
  request JSON

stdout
  result JSON
  success and expected failures should return JSON whenever possible

stderr
  usage errors, malformed stdin, progress, verbose logs, and unexpected
  runtime-level messages
```

stdout is reserved for result JSON when `--format json` or stdin request JSON is
used, except for explicit meta commands such as `--version` and `--help`.

Result JSON is pretty-printed with Node `JSON.stringify(result, null, 2)`-style
2-space indentation and a trailing newline.

Request JSON and result JSON both use top-level `version: 1`.

Unknown request fields are validation errors.

## Exit Codes

```text
0
  ok: true, or explicit meta command such as --version / --help

1
  ok: false expected failure with stdout result JSON when possible

2
  malformed stdin or CLI usage error

3
  unexpected runtime error
```

## Minimal Request

```json
{
  "version": 1,
  "root": ".",
  "query": {
    "type": "literal",
    "text": "RepositoryMap"
  },
  "search": {
    "targets": ["content"],
    "recursive": true,
    "maxDepth": 8
  }
}
```

## Request Mode and Query

`mode` is `search` or `listFiles`. The default is `search`.

`query.type` is `literal`, `regex`, or `glob`.

- `literal` performs substring matching.
- `regex` uses Java `Pattern`.
- `glob` performs path-style matching and is valid for filepath / directory
  matching. In `listFiles` mode, an optional query must use `glob`.

`query.case` is `sensitive` or `insensitive`. The default is `sensitive`.

`detectGitRoot` is a boolean. When true, the runtime walks upward from `root`
and uses the nearest `.git` directory or file as the effective root.

## Search Targets

`search.targets` is a non-empty array of `content`, `filepath`, and/or
`directory`.

- `content` searches file contents.
- `filepath` searches root-relative file paths.
- `directory` searches root-relative directory paths.

The default is `["content"]`.

## Output Modes and Context Lines

`output.mode` is `summary`, `detail`, or `agent`. The default is `summary`.

`output.sort` is `path` or `relevance`. The default is `path`.

`output.includeReadfileRequestHints` is a boolean. When true, search mode adds
`readfileHints` for matched files.

Detail content matches can include surrounding lines with:

- `output.contextLines`
- `output.contextLinesBefore`
- `output.contextLinesAfter`

Context lines are valid only in detail mode. The maximum context line count is
20.

For `filepath` and `directory` targets, detail mode returns at most one item per
matched path. If the query matches the same path string multiple times, the
runtime returns a representative `matchedText`: the first non-empty match, or
the first zero-length match when there is no non-empty match.

Agent mode returns `agentFile` and `agentDirectory` matches. File matches
include representative snippets and read ranges for follow-up file reads.

## List Files Mode

`mode: "listFiles"` traverses the same candidate file set and returns top-level
`files` plus `fileSummary`. `matches` is empty in this mode.

When a query is supplied in listFiles mode, it must use `query.type: "glob"` and
filters root-relative file paths.

## Ignore Files

By default, `ignore.mode` is `auto`, and the runtime reads `.gitignore`,
`.ignore`, and `.git/info/exclude` under the root. `ignore.mode: "none"`
disables ignore file handling.

The Java runtime supports the upstream MVP ignore pattern subset. Unsupported
ignore patterns are skipped and reported with `unsupported_ignore_pattern`.

## Include / Exclude Defaults

`includeFileNamePatterns` and `excludeFileNamePatterns` match file basenames.
`excludeDirNamePatterns` matches directory basenames.

When `excludeFileNamePatterns` is omitted, the default file exclude preset is
used. When specified, the request value replaces the default file exclude
preset. An empty array means no file name excludes.

When `excludeDirNamePatterns` is omitted, the default directory exclude preset
is used. When specified, the request value replaces the default directory
exclude preset. An empty array means no directory name excludes.

## Runtime Differences

The following Java runtime differences are accepted and should not change the
public JSON contract:

- regex search uses Java `Pattern` instead of Node.js `RegExp`
- Shift_JIS decoding uses Java charset handling instead of upstream Node
  dependencies

Other observable differences in JSON field names, result shape, default values,
diagnostic codes, or exit code policy should be treated as regressions unless
they are explicitly documented.

## See Also

- `docs/cli-json-parity.md`
- `docs/upstream-class-mapping.md`
- `docs/upstream-test-mapping.md`
- `docs/upstream-followup-log.md`
