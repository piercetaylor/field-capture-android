# MIT license for the repository

- Status: accepted
- Date: 2026-09-04

## Context and Problem Statement

The repository borrows formats from GPL-2.0 apps (Field Book, Intercross), patterns from Apache-2.0
projects (nowinandroid, ODK Collect), and depends on BSD-2-Clause (MapLibre), Apache-2.0 (Vosk,
osmdroid if ever used, brapi-java-client) and MIT (whisper.cpp, BrAPI spec) software. Which licence
should the repository use?

## Decision Drivers

- No Field Book or Intercross code is copied, so GPL-2.0 does not govern the result (ADR 0002); file
  formats and folder names are facts, not copyrightable expression [inference].
- Any future contribution of this code to Field Book or Intercross must be possible under GPL-2.0.
- Compatibility with every runtime dependency.
- Simplicity for academic collaborators and public-sector adopters.

## Considered Options

- Apache-2.0: explicit patent grant; used by nowinandroid and ODK Collect; generally regarded as
  incompatible with GPL-2.0-only for combined works [inference; the FSF's compatibility list was not
  fetched].
- MIT: permissive, GPL-2.0-compatible, no patent clause.
- GPL-2.0: matches Field Book, restricts downstream reuse by other university projects.

## Decision Outcome

MIT. It keeps the door open to contributing code upstream to the GPL-2.0 PhenoApps projects, is
compatible with every dependency listed above, and is the default the brief allowed. The absence of a
patent grant is immaterial for this project [inference]. Third-party notices for BSD/Apache
dependencies are surfaced in the app's About screen at M4 via the dependency metadata AGP produces.

### Consequences

- Positive: maximal reuse; upstream contribution possible; no copyleft obligations for adopters.
- Negative: no reciprocity — a vendor could ship a closed fork; acceptable for a public-sector tool.
