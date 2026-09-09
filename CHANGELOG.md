# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/), and this
project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Repository scaffold: multi-module Gradle build (Kotlin DSL, version catalog, convention plugins).
- `:core:model` pure-Kotlin domain: geodesy, plot assignment, row/range grid generation,
  Field Book–compatible CSV import with validation, walk-mode annotation mapping, GeoJSON/GPX/CSV
  writers, media path contract, genotyping sample-manifest layout; 25 unit tests.
- Module skeletons for `:app`, `:core:data`, `:core:location`, `:core:media`, `:core:ui`,
  `:feature:planter`, `:feature:walk`, `:feature:fieldbook`, `:feature:integrations`,
  `:feature:crossing` with documented responsibilities and interfaces.
- Room entity schema for the analytics star schema.
- Android manifest with the permission set and runtime-permission flow notes.
- CI workflow (domain tests + detekt; Android lint + unit tests), detekt configuration.
- PLAN.md, ADRs 0001–0009, data-format contracts, reference-repo notes.

## [0.1.0] - Unreleased

Reserved for the M1 planter-mode vertical slice.
