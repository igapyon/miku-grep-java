# miku-grep-java

Java straight-conversion runtime for `miku-grep`.

This repository ports the upstream Node.js / TypeScript `miku-grep` CLI into a
Java 1.8-compatible Maven runtime while preserving the upstream search contract
and stdin / stdout JSON interface for machine-readable automation.

The Java CLI contract is documented in `docs/miku-grep-cli-spec.md`.

## Usage

Build the executable runtime jar:

```bash
mvn package
```

Run the CLI:

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

With query arguments, the default output is human-readable text. Use
`--format json` for structured result JSON. With no arguments, stdin JSON is
still accepted and stdout is result JSON. `--version` and `--help` are
stdin-free metadata commands.

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
- `target/miku-grep-0.10.0-dist.zip`
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

## v0.10.0 Contract Notes

- Search targets are specified as `search.targets`, a non-empty array of
  `content`, `filepath`, and/or `directory`.
- Summary output mode is `output.mode: "summary"`.
- Detail content matches can include `contextBefore` and `contextAfter` when
  context line options are requested.
- Detail filepath / directory matches return at most one representative hit per
  matched path.
- Ignore file handling is enabled by default for `.gitignore`, `.ignore`, and
  `.git/info/exclude`; use `ignore.mode: "none"` to disable it.
- `mode: "listFiles"` lists candidate files and returns `files` plus
  `fileSummary`; an optional glob query can filter paths.
- `output.mode: "agent"` returns agent-oriented file and directory matches.
- `output.sort: "relevance"` annotates summary / agent matches with relevance
  score information and sorts by score.
- `query.type: "glob"` is supported for filepath / directory-oriented matching,
  including `listFiles` path filtering.
- `query.case: "insensitive"` enables case-insensitive literal and regex
  matching.
