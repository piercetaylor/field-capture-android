# Reference repositories

Every repository below was fetched while writing this plan; facts marked [web] come from the URL
given. Repositories in the brief that did not resolve are listed at the end. Nothing was copied
from any GPL repository; "borrowed" means a file format, folder vocabulary, or structural idea.

## PhenoApps Field Book

URL: https://github.com/PhenoApps/Field-Book. License: GPL-2.0 [web]. Language: Kotlin with Java
(`kotlin-android`, Compose BOM 2025.11.00, Hilt 2.56.2, CameraX 1.4.2, `org.brapi:brapi-java-client:2.1.0`,
minSdk 24, targetSdk 36) [web: https://raw.githubusercontent.com/PhenoApps/Field-Book/main/app/build.gradle].
Layout: `app/`, `brapi-provider/`, `docs/`, GitHub Actions release workflow [web]. Distribution: Google
Play (`com.fieldbook.tracker`) and GitHub releases, with a stated no-Play-updates window from April 15 to
September 15 [web]. Docs: https://docs.fieldbook.phenoapps.org (docsify Markdown under `docs/`).

What it does: trait-by-trait phenotype collection per entry, with photo, audio, GNSS and location
traits, GeoNav auto-navigation from an external RTK receiver, and BrAPI import/export [web:
https://raw.githubusercontent.com/PhenoApps/Field-Book/main/docs/geonav.md,
https://raw.githubusercontent.com/PhenoApps/Field-Book/main/docs/brapi.md].

Borrowed: the field import contract (CSV/XLS/XLSX; a unique identifier column plus two ordering
columns; extra columns optional; header characters `/ ? * | "` rejected) [web:
https://docs.fieldbook.phenoapps.org/en/latest/fields.html]; the `geo_coordinates` column for entry
coordinates [web: https://raw.githubusercontent.com/PhenoApps/Field-Book/main/docs/geonav.md]; the
storage folder vocabulary `field_import`, `field_export`, `plot_data` [web:
https://raw.githubusercontent.com/PhenoApps/Field-Book/main/docs/storage.md]; the Table vs Database export
distinction [web: https://docs.fieldbook.phenoapps.org/en/latest/export.html]; the photo naming idea
(unique id, trait, number, timestamp under `plot_data/FIELD/picture/`) [web:
https://raw.githubusercontent.com/PhenoApps/Field-Book/main/docs/traits/trait-photo.md]; the observation of
minSdk 24 and targetSdk 36 as a sector baseline.

Deliberately not borrowed: any source code (GPL-2.0 would govern the result; see ADR 0002 and 0007);
Firebase Analytics/Crashlytics dependencies, which are present in Field Book's build [web: app/build.gradle]
and would disqualify F-Droid inclusion [web: https://f-droid.org/docs/Inclusion_Policy/]; the
`plot_data/FIELD/picture/` flat photo folder, replaced by `trial/field/plot/note_type/` so the tree
mirrors the CSV columns.

## PhenoApps Intercross

URL: https://github.com/PhenoApps/Intercross. License: GPL-2.0 [web]. Language: Kotlin (Room 2.8.4,
Hilt 2.56.2, `brapi-java-client:2.1.0`, minSdk 23, targetSdk 36) [web:
https://raw.githubusercontent.com/PhenoApps/Intercross/master/app/build.gradle]. Layout: `app/`, `docs/`,
Crowdin translations [web]. Distribution: Google Play and GitHub releases [web]. CI/tests: not visible
from the fetched pages.

Borrowed: the parents import CSV (`id,name,sex` with `0 = female`, `1 = male`) [web:
https://raw.githubusercontent.com/PhenoApps/Intercross/master/docs/parents.md]; the wishlist columns
`femaleDbId, maleDbId, femaleName, maleName, wishType, wishMin, wishMax` [web:
https://raw.githubusercontent.com/PhenoApps/Intercross/master/docs/crosses.md]; the export columns
`crossID, femaleObsUnitID, maleObsUnitID, timestamp, person, experiment, type` plus optional
`fruits, flowers, seeds` [web: https://raw.githubusercontent.com/PhenoApps/Intercross/master/docs/export.md];
the folder names `parents_import`, `crosses_export`, `wishlist_import` [web:
https://raw.githubusercontent.com/PhenoApps/Intercross/master/docs/storage.md].

Deliberately not borrowed: Zebra label printing and barcode-first entry; this app's crossing module is
voice/GPS-first and treats Intercross as the label-printing companion.

## PhenoApps Coordinate

URL: https://github.com/PhenoApps/Coordinate. License: GPL-2.0 [web]. Language: Java/Android [web].
Layout: `app/`, `docs/`, `paper/`, `.github/` workflows [web]. Distribution: Google Play and GitHub
releases [web]. What it does: grid-based sample collection with Seed Tray and DNA Plate templates [web].

Borrowed: the idea that a plate layout is a first-class export. Not borrowed: its export column
names, which were not fetched; the sample manifest here is defined independently (docs/data-formats.md).

## PhenoApps Verify

URL: https://github.com/PhenoApps/Verify. License: GPL-2.0 [web]. Language: Java/Android with
`app/`, `javalib/`, `androidlibrary-release/` [web]. Distribution: APK download [web]. CI/tests/docs:
none visible [web]. What it does: imports a list, scans barcodes, reports membership with audio/visual
cues [web]. Borrowed: nothing beyond noting that a pure-Java library module (`javalib`) is the same
separation this repo makes with `:core:model`.

## android/nowinandroid

URL: https://github.com/android/nowinandroid. License: Apache-2.0 [web]. Language: Kotlin, Jetpack
Compose [web]. Layout: `app`, `core/*`, `feature/*`, `build-logic`, `benchmarks`, `lint`, `sync` [web:
https://raw.githubusercontent.com/android/nowinandroid/main/docs/ModularizationLearningJourney.md].
Build: version catalog (Kotlin 2.3.0, AGP 9.3.2, Hilt 2.59, Room 2.8.3, Compose BOM 2025.09.01) [web:
https://raw.githubusercontent.com/android/nowinandroid/main/gradle/libs.versions.toml]; compileSdk 36,
minSdk 23 set in a convention plugin [web:
https://raw.githubusercontent.com/android/nowinandroid/main/build-logic/convention/src/main/kotlin/com/google/samples/apps/nowinandroid/KotlinAndroid.kt].
Tests: unit, Roborazzi screenshot, instrumented, macrobenchmark [web].

Borrowed: the `:app` / `:core:*` / `:feature:*` split, the rule that features depend on core and never
on each other, the `build-logic` included build with convention plugins, and the version catalog.
Not borrowed: the `feature:x:api` / `feature:x:impl` sub-split (over-structured for five features and
one app), Roborazzi and benchmarks (deferred to M4), and any code — the two convention plugins here
are written from scratch against the AGP DSL.

## getodk/collect (ODK Collect)

URL: https://github.com/getodk/collect. License: Apache-2.0 [web]. Language: Kotlin 57 %, Java 43 % [web].
Layout: 40+ Gradle modules including `geo`, `maps`, `google-maps`, `mapbox`, `audio-recorder`, `location`,
`permissions`, `db` [web: https://raw.githubusercontent.com/getodk/collect/master/settings.gradle].
Build: minSdk 26, targetSdk 36, compileSdk 37, WorkManager 2.11.2, CameraX 1.6.1, `play-services-location`
21.4.0, Robolectric 4.16.1 [web: https://raw.githubusercontent.com/getodk/collect/master/gradle/libs.versions.toml].
CI: CircleCI [web]. Distribution: Google Play, GitHub releases [web].

Borrowed: minSdk 26 as a defensible floor for an offline field-data app; keeping location, audio
recording and permissions in their own modules; the assumption that maps are pluggable. Not borrowed:
Google Maps/Mapbox (API keys and tokens) — the map module list confirms Collect currently ships
`google-maps` and `mapbox` modules and no osmdroid module [web: settings.gradle].

## opengisch/QField

URL: https://github.com/opengisch/QField. License: GPL-2.0-or-later [web]. Language: C++ 58 %, QML 30 %
[web]. Layout: `src/`, `test/`, `docs/`, `platform/`, CMake, pre-commit [web]. Distribution: Google Play,
App Store, Microsoft Store, direct downloads, master-branch beta channel [web]. Features: offline-first
with optional QFieldCloud sync, GPS tracking, geotagged photos, form-based capture [web].

Borrowed: the product stance that offline is the default and sync is optional; the "track while
recording" UX. Not borrowed: anything technical (Qt/C++ stack).

## maplibre/maplibre-native

URL: https://github.com/maplibre/maplibre-native. License: BSD-2-Clause [web]. Language: C++ core with
Kotlin/Java Android bindings [web]. Android artifact: `org.maplibre.gl:android-sdk` (11.11.0 resolves on
Maven Central; the getting-started guide shows 11.8.0 and the `org.maplibre.android` package) [web:
https://raw.githubusercontent.com/maplibre/maplibre-native/main/platform/android/docs/getting-started.md].
Android module config: minSdk 23, compileSdk 34 in the library build [web:
https://raw.githubusercontent.com/maplibre/maplibre-native/main/platform/android/MapLibreAndroid/build.gradle.kts].
Offline: `OfflineManager` creates regions, lists them, merges a secondary database, and applies a
tile-count limit defaulting to 6,000 [web:
https://maplibre.org/maplibre-native/android/api/-map-libre%20-native%20-android/org.maplibre.android.offline/-offline-manager/index.html].
CI: GitHub Actions `android-ci`, `android-release` [web]. Chosen map library (ADR 0005).

## osmdroid/osmdroid

URL: https://github.com/osmdroid/osmdroid. License: Apache-2.0 [web]. Language: Java [web]. Artifact:
`org.osmdroid:osmdroid-android:6.1.20` (last release 2024-08-18) [web]. Status: archived 2024-11-20,
"will no longer receive updates or new releases" [web]. Offline: SQLite/zip/GEMF archives, GeoPackage
module [web]. Not chosen because it is archived (ADR 0005); its raster-archive approach remains a
fallback if MapLibre proves too heavy.

## alphacep/vosk-android-demo and vosk-api

URL: https://github.com/alphacep/vosk-android-demo (the brief's `alphacephei/vosk-android-demo`
returned 404; the organisation is `alphacep`). License: Apache-2.0 [web]. Demo build: minSdk 21,
`net.java.dev.jna:jna:5.18.1@aar`, `com.alphacephei:vosk-android:0.3.75@aar`, model unpacked into
assets with a generated UUID file [web:
https://raw.githubusercontent.com/alphacep/vosk-android-demo/master/app/build.gradle,
https://raw.githubusercontent.com/alphacep/vosk-android-demo/master/models/build.gradle]. Library on
Maven Central: `com.alphacephei:vosk-android:0.3.75`, Apache-2.0, depends on JNA 5.18.1 [web:
https://central.sonatype.com/artifact/com.alphacephei/vosk-android]. API: `Recognizer(model, sampleRate,
grammar)` takes a JSON array of phrases; `setGrammar()` reconfigures at runtime; `acceptWaveForm()`
streams PCM; `getPartialResult()`/`getResult()` return JSON [web:
https://raw.githubusercontent.com/alphacep/vosk-api/master/android/lib/src/main/java/org/vosk/Recognizer.java].
Models "are small (50 Mb)" with "zero-latency response with streaming API, reconfigurable vocabulary"
[web: https://raw.githubusercontent.com/alphacep/vosk-api/master/README.md]. Chosen default STT engine (ADR 0004).

## ggml-org/whisper.cpp

URL: https://github.com/ggml-org/whisper.cpp. License: MIT [web]. Language: C/C++ [web]. Android:
`examples/whisper.android` copies a model into assets and recommends tiny or base models on a device;
it transcribes sample files rather than a live stream [web:
https://raw.githubusercontent.com/ggml-org/whisper.cpp/master/examples/whisper.android/README.md].
CI: GitHub Actions [web]. Reserved for batch re-transcription (ADR 0004); requires an NDK build.

## plantbreeding/API (BrAPI)

URL: https://github.com/plantbreeding/API. License: MIT [web]. Current version V2.1 (2022-07-01), four
modules: Core, Phenotyping, Genotyping, Germplasm [web:
https://raw.githubusercontent.com/plantbreeding/API/master/README.md]. Relevant endpoints:
`POST /brapi/v2/observations` with `observationUnitDbId`, `observationVariableDbId`,
`observationTimeStamp`, `geoCoordinates` (a GeoJSON Feature; Point or Polygon) and a
`Authorization: Bearer {token}` header [web:
https://raw.githubusercontent.com/plantbreeding/API/master/Specification/BrAPI-Phenotyping/Observations/README.md];
`POST /brapi/v2/observationunits` with `observationUnitPosition.geoCoordinates` and
`observationLevel` [web: .../ObservationUnits/README.md]; `POST /brapi/v2/crosses` with `parent1`,
`parent2` (`parentType` MALE/FEMALE), `pollinationEvents[]` [web: .../BrAPI-Germplasm/Crosses/README.md].
Java client: `org.brapi:brapi-java-client:2.2.0`, Apache-2.0 [web:
https://central.sonatype.com/artifact/org.brapi/brapi-java-client]. Decision in ADR 0009.

## Existing planter-logging and geotagged-video apps

Searches for open-source planter/seed-envelope logging apps found nothing purpose-built; results were
garden planners and tree-planting trackers. For geotagged video with a track, one relevant project was
found: abinpaul1/GPS-Video-Logger (MIT, Java) records MP4 plus a GPX with the same journey name, and is
on F-Droid [web: https://github.com/abinpaul1/GPS-Video-Logger]. For geotagged voice memos,
geocam/geocamMemoAndroid records text/audio notes with geotags but targets Android 2.2-era tooling and
appears unmaintained [web: https://github.com/geocam/geocamMemoAndroid]. The PhenoApps catalogue lists
Field Book, Coordinate, Intercross and Prospector and no planting or video app [web:
https://phenoapps.org/apps/]. Borrowed from GPS-Video-Logger: the "same stem, two files" pairing of
video and track. Nothing else.

## URLs from the brief that did not resolve

- https://github.com/alphacephei/vosk-android-demo — 404; replaced by https://github.com/alphacep/vosk-android-demo.
- https://docs.intercross.phenoapps.org/en/latest/ — DNS failure; Intercross docs were read from the
  repository's `docs/` Markdown instead.
- https://docs.fieldbook.phenoapps.org/en/latest/storage.html and `.../settings.html` — blocked by
  robots rules for the fetcher used here; the same pages were read from the repository's `docs/`
  Markdown (`storage.md`).
