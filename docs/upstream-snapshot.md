# Upstream Snapshot

This repository uses a vendored upstream snapshot for straight conversion.

- upstream repository: `https://github.com/igapyon/miku-grep`
- upstream default branch: `devel`
- upstream commit: `6919c0f65d8347c0b92338806e859d4bf59f2fdb`
- upstream tag: none at this commit; upstream package version remains `0.8.4`
- vendored path: `vendor/miku-grep`

The vendored upstream source is the stable reference for mapping documents such as
`docs/upstream-class-mapping.md` and `docs/upstream-test-mapping.md`.

Temporary latest-upstream checks may use `workplace/`, but normal mapping paths
should refer to `vendor/miku-grep`.
