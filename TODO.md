# TODO

## Maintenance / Upstream-Following

- [x] Accept upstream `miku-grep` v0.8.4 changes
  - [x] Fetch and synchronize vendored upstream snapshot at `v0.8.4`.
  - [x] Record snapshot commit / tag and upstream diff summary in docs.
  - [x] Update Java request / result contract models:
    `search.targets`, `output.mode: summary`, context line fields, ignore
    fields, directory matches, ignored counters, and filepath vocabulary.
  - [x] Update validation defaults, shape checks, limits, and diagnostics for
    upstream v0.8.4.
  - [x] Update search runtime for filepath / directory / content target arrays,
    detail context lines, ignore-file loading, directory summary matches, and
    deterministic ordering.
  - [x] Update CLI help, Java CLI spec, README, follow-up docs, and focused
    regression tests.
  - [x] Verify with focused tests, `mvn test`, and `mvn package`.

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

## Security Hardening

- [ ] Reduce regex ReDoS residual risk
  - Current validation rejects nested quantified groups, but Java `Pattern`
    can still backtrack heavily on other ambiguous patterns such as
    `(a|aa)+$`.
  - Consider an automation-safe mode that disables regex, a stricter
    safe-regex checker, or a bounded execution strategy with timeout.

- [ ] Add CLI stdin request-size limit
  - `MikuGrepCli.readAll` currently reads stdin into memory without an explicit
    maximum size.
  - Add a clear request JSON byte limit, for example 1 MiB to 10 MiB, and return
    a controlled CLI error when the limit is exceeded.

- [ ] Document or strengthen TOCTOU assumptions
  - Search performs realpath boundary checks before reading files, leaving a
    theoretical race if another process swaps entries between check and read.
  - For local-first CLI use this may be acceptable; if hostile concurrent file
    changes are in scope, investigate descriptor-based open/fstat/read and
    no-follow handling.

- [ ] Harden release GitHub Actions supply chain
  - Release workflow actions are tag-pinned, e.g. `actions/checkout@v4`,
    `actions/setup-java@v4`, and `softprops/action-gh-release@v2`.
  - Consider SHA pinning for release-capable actions and narrowing
    `permissions` at the job level.
