# Build a new Field Book–compatible app rather than fork Field Book or contribute upstream

- Status: accepted
- Date: 2026-09-04

## Context and Problem Statement

PhenoApps Field Book already collects per-entry phenotypes on Android, has Location and GNSS traits, a
GeoNav feature, photo capture, and BrAPI sync [web: https://github.com/PhenoApps/Field-Book]. Planter
mode, geotagged walk-mode video, and the analytics export are new. Should the work be a fork of Field
Book, a new app that speaks Field Book's file formats, or a set of upstream contributions?

## Decision Drivers

- License: Field Book is GPL-2.0 [web: https://github.com/PhenoApps/Field-Book]; a fork or any copied
  code is GPL-2.0.
- Fit: Field Book's core loop is entry → trait → value. Planter mode is a stream of triggers advancing a
  planting order with audio and fixes; walk mode is a continuous recording with post-hoc assignment.
  Neither is an entry/trait interaction.
- Maintainability by one developer, and the ability to publish on F-Droid.
- Time to a working M1 for the next planting season.

## Considered Options

### (a) Fork Field Book

Inherit a mature app (import, traits, BrAPI, Play presence). Costs: GPL-2.0 for everything; a large
Java/Kotlin codebase with legacy view dependencies (`androidx.legacy`, `easypermissions`, `jxl`,
Volley), Firebase Analytics and Crashlytics in the build [web:
https://raw.githubusercontent.com/PhenoApps/Field-Book/main/app/build.gradle], which conflicts with
F-Droid's ban on proprietary analytics [web: https://f-droid.org/docs/Inclusion_Policy/]; a tracking
burden against an active upstream; and no natural home for continuous video capture in its Collect
screen.

### (b) New app that reads and writes Field Book–compatible files and, optionally, BrAPI

Own architecture and license; interoperate through the field import format [web:
https://docs.fieldbook.phenoapps.org/en/latest/fields.html], Field Book–style exports, and the same
`plot_id` vocabulary. Costs: re-implementing field import and a plot list; two apps on the tablet.

### (c) Contribute upstream

A minimal planter mode already exists in Field Book as a composition of features: a Location trait with
"Automatically Switch to Next Plot" records the device coordinates and advances [web:
https://raw.githubusercontent.com/PhenoApps/Field-Book/main/docs/traits/trait-location.md], and the GNSS
trait adds external receivers [web: https://raw.githubusercontent.com/PhenoApps/Field-Book/main/docs/traits/trait-gnss.md].
Voice-first triggering, audio-with-transcript, and Bluetooth buttons could be proposed as enhancements.
Continuous video with a track sidecar, plot assignment from a track, the star-schema export, weather/soil
attachment, and sample manifests are outside Field Book's stated scope, and acceptance timing is not
under the project's control.

## Decision Outcome

Option (b), a new app under the MIT license (ADR 0007), with Field Book as the interoperability
target: import its field files, write files it can import, name folders the way it does, and leave
BrAPI phenotype upload to Field Book where a program already uses it (ADR 0009). Field Book's
Location-trait-plus-auto-advance is acknowledged as the baseline a user can use today; this app's
planter mode must beat it on hands-free operation (voice and hardware triggers), audio evidence, and
correction tooling, or it has no reason to exist. Small, self-contained improvements discovered along
the way (for example the `geo_coordinates` ordering ambiguity noted in docs/data-formats.md) will be
offered upstream as issues or GPL-2.0 patches; MIT code can be relicensed by its author for that
purpose.

### Consequences

- Positive: license freedom, F-Droid eligibility, clean modular architecture, no upstream tracking.
- Negative: two apps for a user who also scores traits in Field Book; duplicated import logic;
  responsibility for Play Store presence and signing.
- The ADR is revisited if Field Book adds continuous-capture features or if maintaining a separate
  app proves heavier than expected after M2.
