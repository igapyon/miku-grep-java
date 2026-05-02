# miku-grep Java CLI Specification

This document describes the Java runtime CLI contract for `miku-grep-java`.

The Java CLI preserves the upstream Node.js `miku-grep` stdin / stdout JSON
contract as the primary compatibility target. The upstream snapshot used for
straight conversion is recorded in `docs/upstream-snapshot.md`.

## Usage

```bash
java -jar target/miku-grep.jar < request.json > result.json
java -jar target/miku-grep.jar --version
java -jar target/miku-grep.jar --help
```

Distribution zip users can run the versioned jar inside the extracted archive:

```bash
java -jar miku-grep-0.8.1.jar < request.json > result.json
```

## Stdio Contract

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

stdout is reserved for result JSON except for explicit meta commands such as
`--version` and `--help`.

Result JSON is pretty-printed with 2-space indentation and a trailing newline.

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
    "target": "content",
    "recursive": true,
    "maxDepth": 8
  }
}
```

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
