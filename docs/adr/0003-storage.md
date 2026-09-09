# Room (SQLite) for records, app-private files for media, Storage Access Framework for export

- Status: accepted
- Date: 2026-09-04

## Context and Problem Statement

Nursery coordinates are sensitive. Data must survive app kills mid-recording, must not reach the
cloud unless the user exports it, and must be exportable as one folder or one SQLite file for Shiny
and Power BI. Where do records and media live, and how do they leave the device?

## Decision Drivers

- No storage permission prompts: app-specific external storage needs none.
- Media must not appear in the gallery or be auto-backed-up.
- Exports must be reproducible and user-controlled.
- Field Book users expect the folder names `field_import`, `field_export`, `plot_data` [web:
  https://raw.githubusercontent.com/PhenoApps/Field-Book/main/docs/storage.md].

## Considered Options

1. Room database + app-private files; export via SAF document tree.
2. Files only (CSV/NDJSON append logs) with no database.
3. MediaStore for photos/video plus a database.
4. User-chosen SAF tree as the live storage location (Field Book's model).

## Decision Outcome

Option 1. Records go in Room with WAL journaling so 1 Hz track inserts are cheap; the track is
additionally appended line-by-line to `sessions/<id>/track.ndjson` so a process kill loses at most
one fix (PLAN.md, "Field workflows"). Media is written under `getExternalFilesDir()` (falling back to
`filesDir`), which is private to the app and removed on uninstall. `android:allowBackup="false"` and
an all-exclude `dataExtractionRules` keep the database and media out of cloud backup and
device-to-device transfer. Export writes a bundle (docs/data-formats.md) into a folder the user picks
through `ACTION_OPEN_DOCUMENT_TREE`, which is the only path by which data leaves the sandbox; the
SQLite export is a copy of the database file taken after `PRAGMA wal_checkpoint(TRUNCATE)`.

Option 2 was rejected because plot assignment, review and joins need indexed queries. Option 3 was
rejected because MediaStore images are visible to every gallery app and to photo cloud sync, which
would leak geotagged nursery photos. Option 4 was rejected because writing continuously through SAF
`DocumentFile` is slow and permission-fragile for 1 Hz appends [inference from SAF's per-document URI
model; not measured]; users still get Field Book's folder names inside the export bundle.

### Consequences

- Positive: zero storage permissions; deterministic export; survivable recording.
- Negative: uninstalling the app deletes unexported data — the home screen shows an "unexported
  sessions" count and the export dialog is the first item in the overflow menu to make this visible.
- Room schema JSON is exported (`room.schemaLocation`) so migrations are testable from version 1.
