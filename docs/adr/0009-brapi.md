# BrAPI: keep the data model BrAPI-shaped; defer a sync client; let Field Book do phenotype upload

- Status: accepted
- Date: 2026-09-04

## Context and Problem Statement

BrAPI (plantbreeding/API, MIT) is the standard REST interface for breeding databases; V2.1 has Core,
Phenotyping, Genotyping and Germplasm modules [web:
https://raw.githubusercontent.com/plantbreeding/API/master/README.md]. Field Book imports fields and
uploads observations over BrAPI [web: https://raw.githubusercontent.com/PhenoApps/Field-Book/main/docs/brapi.md].
Should this app implement BrAPI sync, and if so what?

## Decision Drivers

- Offline-first and sensitive locations: every network path is opt-in.
- The target users (small public programs) may or may not run a BrAPI server; when they do, Field Book
  already handles observation upload.
- The entities this app produces map to BrAPI: plots → ObservationUnits with
  `observationUnitPosition.geoCoordinates` (GeoJSON Point/Polygon) [web:
  https://raw.githubusercontent.com/plantbreeding/API/master/Specification/BrAPI-Phenotyping/ObservationUnits/README.md];
  observations → `POST /brapi/v2/observations` with `observationTimeStamp` and `geoCoordinates` [web:
  https://raw.githubusercontent.com/plantbreeding/API/master/Specification/BrAPI-Phenotyping/Observations/README.md];
  crosses → `POST /brapi/v2/crosses` with `parent1/parent2.parentType` and `pollinationEvents[]` [web:
  https://raw.githubusercontent.com/plantbreeding/API/master/Specification/BrAPI-Germplasm/Crosses/README.md].
- A maintained Java client exists: `org.brapi:brapi-java-client:2.2.0`, Apache-2.0 [web:
  https://central.sonatype.com/artifact/org.brapi/brapi-java-client]; Field Book and Intercross use 2.1.0.
- Authentication is a bearer token in the `Authorization` header [web: Observations README].

## Considered Options

1. No BrAPI at all.
2. BrAPI-shaped schema now; import of studies/observation units and push of observation units,
   observations and crosses as an optional M4+ feature behind a settings toggle.
3. Full sync client in M3 with conflict handling like Field Book's Server/Local/Recent/Manual merge.

## Decision Outcome

Option 2. Entity and column names follow BrAPI vocabulary where it exists (`observationUnitDbId` is
`plot_id`'s external reference, `observationTimeStamp` is `timestamp`, `geoCoordinates` is emitted as
GeoJSON Feature in `points.geojson`), so a later push is a mapping layer, not a schema change. The
first BrAPI feature, if any, is read-only: import a study's observation units to create a field, which
removes the CSV hand-off for programs on a BrAPI server. Observation upload stays with Field Book;
users who need it export `plots.csv`, import it into Field Book, and sync from there. Option 3 is
rejected for M0–M4 because it would consume the effort of an entire milestone for a feature most target
users cannot exercise [inference].

### Consequences

- Positive: no server dependency, no token storage in M0–M3, schema future-proofed.
- Negative: programs with a BrAPI server still make one manual hop through Field Book for phenotypes.
- Tokens, when introduced, are stored with `EncryptedSharedPreferences` equivalents and never in the
  repository (twelve-factor configuration).
