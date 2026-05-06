# Upstream Snapshot

This repository uses a vendored upstream snapshot for straight conversion.

- upstream repository: `https://github.com/igapyon/miku-grep`
- upstream default branch: `devel`
- upstream commit: `c123b943549bb0e85bdc3e95feb4be7b7ddc003d`
- upstream tag: `v0.9.0.1`; upstream package version is `0.9.0`
- vendored path: `vendor/miku-grep`

The vendored upstream source is the stable reference for mapping documents such as
`docs/upstream-class-mapping.md` and `docs/upstream-test-mapping.md`.

Temporary latest-upstream checks may use `workplace/`, but normal mapping paths
should refer to `vendor/miku-grep`.
