# Kotlin, Jetpack Compose, MVVM with unidirectional data flow, Hilt, Room, WorkManager, CameraX, Fused Location

- Status: accepted
- Date: 2026-09-04

## Context and Problem Statement

The app must run offline in a field for a full day, record location, audio and video reliably, and
be maintainable by one developer who is fluent in Kotlin and Python but new to Android. Which
application stack and architecture should the repository commit to?

## Decision Drivers

- Offline-first with long-running capture (foreground services, local persistence).
- Testable domain logic outside the Android runtime.
- Alignment with what the two closest open-source references use, so that patterns and help are
  transferable: Field Book (Kotlin, Compose, Hilt, CameraX) [web:
  https://raw.githubusercontent.com/PhenoApps/Field-Book/main/app/build.gradle] and nowinandroid (Kotlin,
  Compose, Hilt, Room, WorkManager, convention plugins) [web:
  https://raw.githubusercontent.com/android/nowinandroid/main/gradle/libs.versions.toml].
- Everything in the catalog must resolve today; no pre-release dependencies.

## Considered Options

1. Kotlin + Jetpack Compose + Hilt + Room + WorkManager + CameraX + Fused Location Provider.
2. Kotlin + XML Views (Fragments) with the same data layer.
3. Cross-platform (Flutter or React Native) with platform channels for GPS/audio/video.

## Decision Outcome

Option 1. Compose is the current UI toolkit used by both reference apps; large-target, high-contrast
layouts are simpler to express as composables than as view hierarchies. Hilt gives constructor
injection for ViewModels and workers. Room provides the SQLite file that doubles as an export. WorkManager
runs export and fetch jobs that must survive process death. CameraX `Recorder` handles video with
quality fallback and audio [web: https://developer.android.com/media/camera/camerax/video-capture]. The
Fused Location Provider supplies priority/interval control and the batching guidance used for the
battery budget [web: https://developer.android.com/develop/sensors-and-location/location/battery].

Architecture: each feature has one ViewModel exposing a `StateFlow<UiState>` and one `onAction(Action)`
entry point; state flows down, actions flow up; repositories in `:core:data` are the only writers to
Room; `:core:model` is pure Kotlin and holds all rules that can be unit-tested without a device. The
module split and the `build-logic` convention plugins follow nowinandroid's Modularization guide [web:
https://raw.githubusercontent.com/android/nowinandroid/main/docs/ModularizationLearningJourney.md],
minus the `api`/`impl` sub-modules.

Build: Gradle Kotlin DSL, version catalog `gradle/libs.versions.toml`, AGP 8.13.0, Kotlin 2.2.21, KSP
2.2.21-2.0.4, Hilt 2.59, Room 2.8.3, Compose BOM 2025.09.01, CameraX 1.6.1, WorkManager 2.11.2,
play-services-location 21.4.0. Each coordinate was resolved against Maven Central / Google Maven when
chosen (PLAN.md, "Verification status"). AGP 8.x rather than 9.x because the Gradle available for
verification is 8.14.3 and AGP 9 requires Gradle 9 [inference from AGP's Gradle-version pairing;
not fetched].

### Consequences

- Positive: the same architecture as the reference apps; domain tests run in CI without an SDK.
- Negative: Compose and Hilt raise the learning curve for a first Android project; KSP annotation
  processing lengthens builds.
- Rejected option 2 would ease glove-friendly custom views but doubles the UI code paths. Option 3 was
  rejected because continuous CameraX + foreground-service + AudioRecord work would sit entirely in
  platform channels, negating the cross-platform benefit while iOS is out of scope.
