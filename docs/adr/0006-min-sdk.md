# minSdk 26, targetSdk 36, compileSdk 36

- Status: accepted
- Date: 2026-09-04

## Context and Problem Statement

Which Android API levels should the app support and target? Field devices in public programs are
often older phones and rugged tablets, while Google Play imposes a target level and the platform
speech APIs are recent.

## Decision Drivers

- Device coverage: cumulative share by minimum level, April 2026 data — API 24+: 96.6 %, API 26+:
  96.1 %, API 28+: 93.5 %, API 30+: 86.9 %, API 31+: 78.8 %, API 33+: 68.9 % [web: https://apilevels.com/].
- Play requirement: from 2026-08-31 new apps and updates must target API 36 [web:
  https://support.google.com/googleplay/android-developer/answer/11926878].
- Peer choices: Field Book minSdk 24 [web: https://raw.githubusercontent.com/PhenoApps/Field-Book/main/app/build.gradle],
  Intercross 23 [web: https://raw.githubusercontent.com/PhenoApps/Intercross/master/app/build.gradle],
  ODK Collect 26 [web: https://raw.githubusercontent.com/getodk/collect/master/gradle/libs.versions.toml],
  nowinandroid 23, MapLibre library 23, Vosk demo 21.
- Platform features: `java.time` is native from API 26 [web:
  https://developer.android.com/reference/java/time/Instant]; notification channels (needed for the
  foreground-service notification) are API 26; on-device `SpeechRecognizer` is API 31 and
  `EXTRA_AUDIO_SOURCE` API 33 (ADR 0004), both handled by runtime checks; typed foreground services
  are mandatory from API 34 [web: https://developer.android.com/develop/background-work/services/fgs/service-types].

## Considered Options

- minSdk 24 (Field Book parity): +0.5 % devices, requires core-library desugaring for `java.time`.
- minSdk 26 (ODK Collect parity): native `java.time`, notification channels unconditional.
- minSdk 31: on-device speech API available unconditionally; loses about 17 % of devices.

## Decision Outcome

minSdk 26, targetSdk 36, compileSdk 36. The 0.5 % difference between 24 and 26 does not justify
desugaring and dual code paths; 31 would exclude too many of the older tablets this audience owns
[inference]. Feature availability above 26 is gated at runtime: on-device platform speech (31+),
audio-source feeding (33+), typed foreground services (declared for all; required at 34+).
targetSdk 36 satisfies the Play deadline in force at the time of writing.

### Consequences

- Positive: one code path for time, notifications and storage; Play-compliant target.
- Negative: users on Android 7 (API 24–25) cannot install; those devices are also the ones least
  likely to run Vosk models at acceptable speed [speculation].
- Revisit when the API 26–27 share falls below 1 %.
