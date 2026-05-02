# Upstream Snapshot

This repository uses a vendored upstream snapshot for straight conversion.

- upstream repository: `https://github.com/igapyon/miku-grep`
- upstream default branch: `devel`
- upstream commit: `f1104a0e4e650b23a0b09ea22e15731a4d67c589`
- vendored path: `vendor/miku-grep`

The vendored upstream source is the stable reference for mapping documents such as
`docs/upstream-class-mapping.md` and `docs/upstream-test-mapping.md`.

Temporary latest-upstream checks may use `workplace/`, but normal mapping paths
should refer to `vendor/miku-grep`.
