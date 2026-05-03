# miku-grep-java

Java straight-conversion runtime for `miku-grep`.

This repository ports the upstream Node.js / TypeScript `miku-grep` CLI into a
Java 1.8-compatible Maven runtime while preserving the stdin / stdout JSON
contract as the primary compatibility target.

The Java CLI contract is documented in `docs/miku-grep-cli-spec.md`.

## Usage

Build the executable runtime jar:

```bash
mvn package
```

Run the CLI:

```bash
java -jar target/miku-grep.jar < request.json > result.json
java -jar target/miku-grep.jar --version
java -jar target/miku-grep.jar --help
```

The primary input is stdin JSON. The primary output is stdout JSON.
stdout is reserved for result JSON except for `--version` and `--help`.

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

## Build Outputs

`mvn package` creates:

- `target/miku-grep.jar`
  - executable shaded runtime jar
- `target/miku-grep-sources.jar`
  - source jar
- `target/miku-grep-0.8.4-dist.zip`
  - distribution zip containing the versioned runtime jar, README, LICENSE,
    and runtime-oriented docs

## Development

Primary verification:

```bash
mvn test
mvn package
```

Repository-specific migration state is tracked in
`docs/remaining-migration-items.md`.

CLI JSON compatibility policy is tracked in `docs/cli-json-parity.md`.

## Runtime Differences

The Java runtime intentionally documents these accepted differences:

- regex search uses Java `Pattern` instead of Node.js `RegExp`
- Shift_JIS decoding uses Java charset handling instead of upstream Node
  dependencies

Other observable JSON contract differences should be treated as regressions
unless explicitly recorded in the follow-up log.

## v0.8.4 Contract Notes

- Search targets are specified as `search.targets`, a non-empty array of
  `content`, `filepath`, and/or `directory`.
- Summary output mode is `output.mode: "summary"`.
- Detail content matches can include `contextBefore` and `contextAfter` when
  context line options are requested.
- Detail filepath / directory matches return at most one representative hit per
  matched path.
- Ignore file handling is enabled by default for `.gitignore`, `.ignore`, and
  `.git/info/exclude`; use `ignore.mode: "none"` to disable it.
