# Export as flat CSVs + GeoJSON + GPX + SQLite copy in one bundle; Field Book–style CSVs optional

- Status: accepted
- Date: 2026-09-04

## Context and Problem Statement

Downstream consumers are Excel, R/Shiny, Power BI, GIS tools, and Field Book. Which formats and
which structure should the export produce so one export serves all of them without transformation?

## Decision Drivers

- Star schema readable by Power BI's folder connector and by R with no renaming.
- Spatial data in formats every GIS reads: GeoJSON (RFC 7946, lon/lat, WGS 84) [web:
  https://www.rfc-editor.org/rfc/rfc7946] and GPX 1.1 for tracks [web: https://www.topografix.com/GPX/1/1/].
- A single-file option for people who want "the database": a SQLite copy.
- Interoperability with Field Book's Table/Database export vocabulary [web:
  https://docs.fieldbook.phenoapps.org/en/latest/export.html] and its import format for `plots.csv`.
- Reproducibility: the bundle carries a manifest with hashes and the app/schema version.

## Considered Options

1. One bundle with flat CSVs per table, GeoJSON for plots and points, GPX for tracks, optional
   SQLite copy, optional media tree with sidecars.
2. GeoPackage as the single spatial+tabular container.
3. Parquet files.
4. XLSX workbook with one sheet per table.

## Decision Outcome

Option 1 (docs/data-formats.md). CSV is the lowest common denominator for Excel, R and Power BI.
GeoJSON and GPX are text, diff-friendly and universally read. The SQLite copy costs nothing because
Room already stores SQLite, and R (`RSQLite`) and Power BI (ODBC) read it directly. Media files keep
their folder tree `trial/field/plot/note_type/` so the file system itself is a join key, and each file
has a JSON sidecar because EXIF is JPEG-only and is frequently stripped.

Option 2 (GeoPackage) was rejected as the primary format because Excel and Power BI do not read it
natively and it would add a spatial library dependency; it can be produced later from the SQLite copy
by desktop tools. Option 3 was rejected because there is no light Android Parquet writer and Excel
does not read it. Option 4 is offered only as a convenience in M4 if requested, because XLSX loses
precision and type control.

### Consequences

- Positive: no transformation step in Shiny/Power BI; spatial files open in QGIS; a manifest makes
  exports auditable.
- Negative: many files per export; CSV has no types (mitigated by the manifest's column-type list and
  by the SQLite copy).
