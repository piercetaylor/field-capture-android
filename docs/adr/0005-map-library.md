# MapLibre Native Android for the map view; plots-only rendering must work without any tiles

- Status: accepted
- Date: 2026-09-04

## Context and Problem Statement

The app shows plots, the walker's position, the recorded track and captured points, mostly with no
connectivity. Which map library, and how are basemaps handled offline?

## Decision Drivers

- Active maintenance and a permissive licence.
- Offline operation with an optional pre-downloaded basemap.
- Vector rendering of a few thousand plot polygons and a few thousand track points.
- No API key or token requirement.

## Considered Options

### osmdroid

Apache-2.0, Java, `org.osmdroid:osmdroid-android:6.1.20`, offline raster archives (SQLite, zip, GEMF)
and a GeoPackage module. The repository was archived on 2024-11-20 and "will no longer receive
updates or new releases" [web: https://github.com/osmdroid/osmdroid].

### MapLibre Native Android

BSD-2-Clause, actively developed with `android-ci`/`android-release` workflows [web:
https://github.com/maplibre/maplibre-native]. Artifact `org.maplibre.gl:android-sdk` (11.11.0 resolves;
package `org.maplibre.android` since v11) [web:
https://raw.githubusercontent.com/maplibre/maplibre-native/main/platform/android/docs/getting-started.md].
Library minSdk 23 [web:
https://raw.githubusercontent.com/maplibre/maplibre-native/main/platform/android/MapLibreAndroid/build.gradle.kts].
Offline: `OfflineManager` creates and lists offline regions, merges a secondary database, and enforces
a tile-count limit that defaults to 6,000 [web:
https://maplibre.org/maplibre-native/android/api/-map-libre%20-native%20-android/org.maplibre.android.offline/-offline-manager/index.html].
Vector styles and GeoJSON sources are native.

### Google Maps SDK / Mapbox

Both require keys or tokens (ODK Collect ships both as separate modules [web:
https://raw.githubusercontent.com/getodk/collect/master/settings.gradle]); rejected on the
no-key and F-Droid drivers.

## Decision Outcome

MapLibre Native Android. Plots, track and points are GeoJSON sources generated from `:core:model`'s
writers, so the map is useful with zero basemap tiles ("plots-only mode", the default in the field).
A basemap is optional: on Wi-Fi the user can download a region around a field through
`OfflineManager` from a style URL configured in settings; no default tile provider is hard-coded,
because usage terms and keys vary by provider and the app must not ship a key. If MapLibre's native
library size or minSdk ever becomes a problem, osmdroid's archived code remains usable as a fallback
for raster archives.

### Consequences

- Positive: active library, permissive licence, vector rendering, documented offline regions.
- Negative: native `.so` per ABI increases APK size; the 6,000-tile default limit constrains basemap
  regions unless raised via `setOfflineMapboxTileCountLimit`; Compose interop is through
  `AndroidView` [inference].
