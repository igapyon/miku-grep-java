# Upstream Snapshot

This repository uses a vendored upstream snapshot for straight conversion.

- upstream repository: `https://github.com/igapyon/miku-grep`
- upstream default branch: `devel`
- upstream commit: `0dde0221b16118848b7c859aeb74c32595ff40ed`
- upstream tag: `v0.8.4`
- vendored path: `vendor/miku-grep`

The vendored upstream source is the stable reference for mapping documents such as
`docs/upstream-class-mapping.md` and `docs/upstream-test-mapping.md`.

Temporary latest-upstream checks may use `workplace/`, but normal mapping paths
should refer to `vendor/miku-grep`.
