# Contributing

## Ground rules

- Domain logic (plot assignment, parsing, schema mapping, export serialization) goes in
  `:core:model` as pure Kotlin with unit tests. Android APIs are not allowed there.
- Data contracts are documented in `docs/data-formats.md` before code changes them; import and
  export validate against those contracts at the boundary.
- No API keys, tokens, keystores or field data in the repository.
- Decisions with lasting effect get an ADR in `docs/adr/` (MADR format, next free number).

## Workflow

1. Branch from `main`.
2. Commit with [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/):
   `feat(planter): …`, `fix(export): …`, `docs: …`, `build: …`, `test: …`.
   Breaking changes carry `!` or a `BREAKING CHANGE:` footer.
3. Run locally before pushing:
   ```
   ./gradlew -PjvmOnly=true :core:model:test :core:model:detekt   # no Android SDK needed
   ./gradlew lint testDebugUnitTest detekt                          # with an Android SDK
   ```
4. Update `CHANGELOG.md` under `[Unreleased]`.
5. Open a pull request; CI must pass.

## Versioning

SemVer. `versionCode` increments on every release build; `versionName` follows the tag.
