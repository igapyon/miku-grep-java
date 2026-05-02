# CLI JSON Parity Policy

This document fixes the compatibility policy for the `miku-grep-java`
straight conversion.

## Scope

The Java runtime should preserve the stdin / stdout JSON contract of the
upstream Node.js `miku-grep` CLI as the primary compatibility target.

The Java implementation should start as a single-module Maven runtime jar.

Fixed Java-side identity:

- base package: `jp.igapyon.mikugrep`
- Maven artifactId: `miku-grep`
- CLI class: `jp.igapyon.mikugrep.cli.MikuGrepCli`

## Parity Targets

The Java CLI should match the Node CLI as closely as practical for the
following items.

- stdin request JSON
- stdout result JSON
- stderr role
- exit codes
- result top-level shape
- request and result top-level `version: 1`
- field names
- default values
- limit values
- diagnostic codes
- diagnostic severities
- `ok`, `error`, `effectiveRequest`, `matches`, `summary`, and `diagnostics`
  structure
- `detail` and `file-summary` match shapes
- root-relative result paths
- `/` path separators in result JSON
- no absolute paths in result JSON
- unknown request fields as validation errors
- 2-space pretty-printed JSON on stdout
- trailing newline after stdout JSON
- `--help` and `--version` as stdin-free meta commands

Stdout must not contain progress logs, warnings, or runtime chatter when the
CLI is returning request results. Agents and scripts should be able to parse
stdout as JSON.

## Known Runtime Differences

The following runtime differences are known and acceptable.

- Regex engine differences between Node.js `RegExp` and Java `Pattern`
- Shift_JIS decoder behavior differences between upstream Node dependencies and
  Java charset handling

These differences should be documented as runtime differences when they affect
observable behavior. They should not be used as a reason to change JSON field
names, result shape, default values, diagnostic codes, or exit code policy.

## JSON Ordering

The Java implementation should preserve stable JSON object ordering where the
upstream contract or tests make ordering visible.

Use ordered data structures or explicit serialization order for result objects.
In particular, `effectiveRequest`, `summary`, diagnostics, and match objects
should not depend on reflection order or unordered map iteration.

## Boundary Rule

Java runtime convenience should not leak into the JSON contract.

Path objects, Java exception names, platform-specific separators, absolute
working paths, and Java-specific diagnostic wording should stay inside the Java
implementation unless the upstream Node contract already exposes equivalent
information.
