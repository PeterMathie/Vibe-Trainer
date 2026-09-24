# Vibe Check

**Vibe Check 0.4.0 Beta 1**

Native, local-first Android strength, flexibility and open-ended activity tracking.

## Current direction

The greenfield architecture uses Kotlin, Jetpack Compose, Room, Hilt and an explicit domain layer. Objective workout history drives recency maps, activity heatmaps and progress calculations.

Visual styling is isolated behind semantic tokens and interchangeable palettes. Production builds seed application knowledge but no personal programmes or history; debug builds add removable representative data.

See [`docs/PRODUCT_REQUIREMENTS.md`](docs/PRODUCT_REQUIREMENTS.md) for the product contract.
The current Android identity is `com.petermathie.vibecheck`, version `0.4.0-beta.1`
(`versionCode 3`). This is a distinct install from the former Vibe Trainer
package; use its JSON export/import flow to move structured records.

For the current implementation, verified builds and agent-ready tasks, start with
[`docs/WORK_STATUS.md`](docs/WORK_STATUS.md). See
[`docs/BETA_TESTING.md`](docs/BETA_TESTING.md) for the tester walkthrough.

### Anatomy

The muscle diagram uses real male/female front/back SVG muscle path data adapted from the MIT-licensed [Jsplice/MuscleMap](https://github.com/Jsplice/MuscleMap) project. The paths are parsed and drawn natively in Compose; they are not hand-sketched placeholders.

## Development

- Stable baseline: `main`
- Active development: `develop`
- Native stack: Kotlin, Jetpack Compose, Material 3, Room, DataStore, Hilt, Coroutines/Flow
- Default units: kg
- Storage: local-first on-device SQLite

Pushing to `develop` triggers a GitHub Actions debug APK build.
