# Working notes

Read this, then `PLAN.md`. Everything below is the working agreement; `PLAN.md` is the design.

## What this is

An Android app (Kotlin, Jetpack Compose) for public-sector plant breeders to capture field data hands-free and geotagged, structured to drop straight into CSV, Shiny and Power BI. Planter mode records a GPS fix and a spoken note per tap, hardware button or voice trigger while advancing through a Field Book-compatible planting order, with pause, resume, skip, redo and manual correction as first-class events. Walk mode records continuous video with a synchronised GPS track in a sidecar and maps time-indexed spoken notes to plots by polygon or nearest centroid.

Open a session **inside this repo**, never in the parent directory. The siblings `isoline-browser` (TypeScript) and `progeny-selector` (Python) have their own toolchains and their own `PLAN.md`.

## State

M0, the scaffold, is the current deliverable: the domain module and its tests exist and nothing runs on a device yet. **M1 is next**: the planter-mode vertical slice, with ID-pronunciation mapping tests and planter event state-machine tests in pure Kotlin.

## Gates

The JVM-only path is what runs without an Android SDK or emulator, and is the fast loop:

```
./gradlew -PjvmOnly=true :core:model:test
./gradlew detekt
```

Anything touching `:app` or a `feature:` module needs the Android SDK. Say plainly in your report which gates you actually ran and which you could not, rather than implying a device build passed.

## Rules that are not negotiable

- **Never commit real field data, real coordinates, or a keystore.** Fixtures are synthetic and generated.
- **No AI attribution in commits or pull requests.** No `Co-Authored-By`, no generated-with footer.
- **Decisions go in `docs/adr/`** as MADR records. If you find a recorded decision is wrong, say so and stop rather than quietly working around it.
- Conventional Commits; user-visible changes get a `CHANGELOG.md` entry under `[Unreleased]`.
- `docs/data-formats.md` describes files that move between this app and the analysis projects. Ask the user before changing a documented column or format.

## How the work is done

Milestone by milestone from `PLAN.md`. For each milestone: state the acceptance criteria you are targeting, implement, make the tests pass, commit.

Self-contained modules are delegated to subagents with a written spec naming inputs, outputs, edge cases and the test that must pass. The main session holds the architecture, the data contracts and the review. Before every commit an adversarial reviewer attacks correctness, the data contracts and the test coverage; it does not write code. Report what it found and what changed.

For this project the reviewer should push hardest on the things a field device gets wrong and a desktop never does: a GPS fix that arrives late or with poor accuracy, a process killed mid-capture, a voice trigger firing twice, an event log replayed out of order, a clock that jumps, and storage filling during a video walk. Data loss in a field session is unrecoverable, so durability of the event log outranks UI polish.

## Environment gotchas

- `core.autocrlf` is `true` on the maintainer's machine. `.gitattributes` pins the repo to LF but keeps `gradlew.bat` as CRLF, because `cmd.exe` mis-parses an LF batch file, and marks the wrapper jar and image assets binary.
- Use `./gradlew`, never a system Gradle, so the wrapper's pinned version is what runs.
- `gradlew` must stay mode 100755 in git. A Windows checkout does not set the executable bit, so a file added from Windows lands as 100644 and every Linux CI job dies with exit code 126 before Gradle starts. Record it with `git update-index --chmod=+x gradlew`.
- Hilt's Gradle plugin transforms bytecode through AGP's `ScopedArtifact` API, so the two versions move together. Hilt 2.59 references `ScopedArtifact.POST_COMPILATION_CLASSES`, which AGP 8.13 does not define; since 8.13.2 is the newest stable AGP (9.x is alpha), Hilt is held at 2.58. Revisit both together, not separately.
- No JDK or Android SDK is installed on the maintainer's machine, so every Gradle gate is verified only by CI. Say so plainly rather than implying a local build passed.
