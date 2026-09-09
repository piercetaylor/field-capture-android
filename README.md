# field-capture-android

Status: scaffold / pre-alpha. Nothing here runs on a device yet; the domain module and its tests do.

Android app (Kotlin, Jetpack Compose) for public-sector plant breeders to capture field data
hands-free and geotagged, structured so it drops straight into Excel/CSV, Shiny and Power BI.

- Planter mode: each tap, hardware button or voice trigger records the GPS fix and a spoken note and
  advances through a planting order loaded from a Field Book–compatible field file, so every seed
  envelope gets a coordinate. Pause, resume, skip, redo and manual correction are first-class events.
- Walk mode: continuous video with a synchronized ≥ 1 Hz GPS track in a sidecar; spoken notes become
  time-indexed annotations mapped to plots by polygon or nearest centroid, with a review screen.
- Photos land in `trial/field/plot/note_type/` with EXIF GPS and a JSON sidecar.
- Export: flat CSVs, GeoJSON, GPX, a SQLite copy, Field Book–style CSVs, a genotyping plate manifest.
- Integrations (planned): Open-Meteo/Daymet weather, USDA SSURGO soil, harvest CSV import, GIS layers.
- Offline-first; data leaves the device only through an explicit export.

See `PLAN.md` for the full plan, `docs/adr/` for decisions, `docs/data-formats.md` for file contracts.

## Build

Requirements: JDK 17 or newer; Android Studio (or the command-line SDK) with API 36 platform for the
full build. The domain module needs no Android SDK.

```
# Domain tests and static analysis (no Android SDK required)
./gradlew -PjvmOnly=true :core:model:test :core:model:detekt

# Full build (Android SDK required)
./gradlew lint testDebugUnitTest detekt :app:assembleDebug
```

The wrapper pins Gradle 8.14.3. `-PjvmOnly=true` configures only the pure-Kotlin modules.

## Layout

```
app/                    application shell, navigation, permissions
core/model/             pure Kotlin: domain types, geodesy, plot assignment, CSV/GeoJSON/GPX, manifests (tested)
core/data/              Room database, repositories, storage layout, export
core/location/          Fused Location wrapper, recording foreground service
core/media/             CameraX video/photo, EXIF, audio tee, speech engines
core/ui/                high-contrast theme, large-target controls
feature/planter/        planter mode
feature/walk/           walk mode and review
feature/fieldbook/      fields, import, map, export
feature/integrations/   weather, soil, harvest, GIS layers
feature/crossing/       pollination log
build-logic/            Gradle convention plugins
docs/                   ADRs, data formats, reference repositories
```

## License

MIT (see `LICENSE`). No code from GPL-licensed PhenoApps applications is included; only their file
formats are implemented for interoperability.
