# Vibe Trainer

Native Android training log for strength and flexibility.

## Current direction

Vibe Trainer keeps the successful structure of the original Training Log beta while adding a top-level **Strength / Stretching** mode switch and the approved quality-of-life features.

The UI uses a dark, high-contrast training-focused design with a centralised theme, floating bottom navigation, active-session card, and an interactive anatomy panel.

### Anatomy

The muscle diagram uses real male/female front/back SVG muscle path data adapted from the MIT-licensed [Jsplice/MuscleMap](https://github.com/Jsplice/MuscleMap) project. The paths are parsed and drawn natively in Compose; they are not hand-sketched placeholders.

## Development

- Stable baseline: `main`
- Active development: `develop`
- Native stack: Kotlin, Jetpack Compose, Material 3, Room, DataStore, Hilt, Coroutines/Flow
- Default units: kg
- Storage: local-first on-device SQLite

Pushing to `develop` triggers a GitHub Actions debug APK build.
