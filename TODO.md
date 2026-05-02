# TODO

## Maintenance / Upstream-Following

- [x] Check upstream differences
  - Compare the vendored upstream snapshot in `vendor/miku-grep` with the
    latest upstream `devel` branch.
  - Record whether Java-side source, tests, README, or CLI contract docs need
    follow-up.
  - Result on 2026-05-02: latest upstream `devel` is
    `e3e3a998aaec6b0a8263e1b56fcdbea6ccfef2e2`, matching
    `docs/upstream-snapshot.md`; no vendored source diff was found.

- [x] Synchronize README and CLI spec
  - Keep root `README.md`, `docs/cli-json-parity.md`, CLI `--help`, and
    upstream CLI specification wording aligned where they describe the same
    public contract.
  - Keep Java-side runtime differences explicit instead of silently copying
    Node.js-only wording.
  - Result on 2026-05-02: added `docs/miku-grep-cli-spec.md`, linked it from
    README and CLI help, and documented Java runtime differences in README and
    parity docs.

- [x] Record practical runtime differences
  - Add concrete observations from maintenance checks to
    `docs/upstream-followup-log.md`.
  - Keep regex engine and Shift_JIS decoder differences documented when they
    affect observable behavior.
  - Result on 2026-05-02: recorded latest-upstream no-diff check and kept Java
    regex / Shift_JIS differences explicit in runtime-facing docs.

- [x] Strengthen regression tests
  - Add focused tests for any contract gap found during maintenance checks.
  - Prefer tests that protect stdout JSON shape, CLI help wording, packaging
    artifacts, and accepted runtime differences.
  - Result on 2026-05-02: added `DocumentationSyncTest` to guard README,
    Java CLI spec, parity doc, help text pointers, and distribution assembly
    runtime-facing contents.
