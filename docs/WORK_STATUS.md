# Work status and agent handoff

Updated: 22 September 2026. Owner of the current pass: ChatGPT coding agent.

The detailed, claimable checklist is [TODO.md](TODO.md). RUN-02 completed successfully on 22 September: compilation, unit tests, emulator tests, APK installation and launch all passed. Its screenshot was inspected and runtime-error log was empty. Outstanding implementation and real-device tasks remain open.

## Start here

- Repository: PeterMathie/Vibe-Trainer.
- Working branch: `codex/greenfield-foundation`.
- Draft PR: https://github.com/PeterMathie/Vibe-Trainer/pull/1 (targets `develop`). Do not merge without instruction.
- Product contract: `docs/PRODUCT_REQUIREMENTS.md`; tester walkthrough: `docs/BETA_TESTING.md`.
- Native Kotlin / Compose / Room / Hilt app. This branch has no runnable web version.
- User has authorised replacing the old implementation and completing the beta. No further approval is needed for ordinary implementation or tests.
- Latest verified branch commit: `e0a47b76aaae8d8dfaa45295c1d7a3dc710fd433` (same application code as `6e8570f`).
- Verified run: https://github.com/PeterMathie/Vibe-Trainer/actions/runs/35701312673. Unit tests, debug APK, Android API 35 emulator tests, APK installation and MainActivity launch all passed. `am start` reported `Status: ok`; the process remained alive, a home screenshot was captured, and the AndroidRuntime error log was empty.
- APK artifact: `vibe-trainer-debug`; reports and generated Room schemas: `validation-reports`.

## What is implemented

- Full bundled exercise catalogue plus curated skills/stretches, canonical muscle mappings, aliases, tags, tracking types, search, custom definitions, duplication and archival. Custom variations can be added/reordered; seeded ordering is protected.
- Programme/day/exercise creation, editing, duplication, ordering, archival/removal, targets, rest and circuit groups.
- Room-backed workout drafts and saved sets; finish-gated maps/progress; compact performance entry, RPE, exercise/workout notes, warm-ups, failed attempts, unilateral fields, bands, holds, substitutions and previous-session sets.
- Actual exercise identity owns its graph after substitution. Name, tracking type, targets and muscle mappings are snapshotted for real workouts and demos.
- Separate strength/stretch recency maps using male/female front/back vectors, exact selected-path outlines and accessibility actions; historical day navigation, back navigation, mode switch and muscle explanations.
- Exercise session scoring, baseline/rolling index, variation filters, raw performance/notes, aligned RPE graph, PR summaries and separate ROM view.
- Homepage activity heatmap, historical calendar navigation, editable/searchable finished workouts, custom multi-field habits and daily totals with numeric targets.
- Body measurements and photo import/removal/export; structured JSON merge/restore, CSV including band stacks and notes, last-backup indicator, backed-up profile preferences.
- AlarmManager rest alerts, notification/precise-alarm permission controls, hold timer, units/plate calculator, haptics and reduced-motion ripple control.
- Central palette tokens, built-in/custom colours, debug demo history and removable demo records. Production personal data starts empty.

## Fixes completed in this continuation

- Fixed CI KVM access so emulator verification actually runs.
- Added programme ordering and history date corrections.
- Serialised editor writes; retained historical exercise/muscle snapshots.
- Added transactional backup tests, invalid-import rollback test and a draft-gating/history-preservation regression test.
- Kept band-assisted and unassisted reps on the same relative scale for assisted-rep exercises; added a regression test for removing the last band.
- Corrected PR search to inspect all valid sets, not only session-score winners.
- Kept missing RPE as chart gaps at the correct session positions.
- Fixed left/right results retaining obsolete shared reps and validated numeric input.
- Added full previous-session sets, exercise-note history search and immediate map refresh after Finish.
- Added editable measurements, removable photos and photo I/O error reporting.
- Wrote the beta walkthrough and this handoff.

## Latest verification

The verified follow-up bundles the existing third-party asset notices in the APK, exposes them in Settings, aligns manual circuit rest with the group rest rule, and adds an actual MainActivity launch + screenshot capture after emulator tests. The launch screenshot was inspected: the home screen, front/back recency vectors, mode switch and calendar render. A visible polish issue remains: status-bar icons have poor contrast against the dark background; include this in UI-01.

Earlier run `35639647804` at commit `c071717` passed compilation, unit tests and instrumentation tests, then failed the added launch command because the test runner had uninstalled the target APK. The verified revision explicitly installs the built APK before launching it. That launch-harness failure is resolved.

Downloads for the verified build:
- APK ZIP: https://github.com/PeterMathie/Vibe-Trainer/actions/runs/35701312673/artifacts/10682633925
- Reports, schema and launch screenshot: https://github.com/PeterMathie/Vibe-Trainer/actions/runs/35701312673/artifacts/10682594156

The documentation-only handoff update after this verified commit does not alter application code.

## Remaining work — explicit, claimable tasks

The app is an expanded beta. A green build is not evidence that every product requirement or device interaction is finished. Another agent can take one task below, record ownership here and work on a separate branch.

| ID | Priority | Task and acceptance criteria | Main files |
|---|---|---|---|
| QA-01 | High | Install the newest APK on a real Android phone. Check background rest notification/sound/vibration while locked, denied permissions, timer replacement and auto/manual circuits. Record device/version/results. | `ui/RestTimer.kt`, `ui/WorkoutEditor.kt` |
| DATA-01 | High | Add an actual v1 → v2 upgrade test using an exported v1 schema/fixture. Verify user records survive, Room validates the migrated schema, and snapshots/ordering exist. Current tests use fresh in-memory databases. | `di/DataModule.kt`, `data/local/Entities.kt`, `androidTest` |
| DATA-02 | High | Harden structured import beyond columns/FKs: validate enum values, finite numeric ranges, set/variation ownership, bands and protected seeded definitions. Add rollback tests for invalid domain data. Publish an example import file/schema for externally formatted notes. | `data/DataTransfer.kt`, `data/BackupPreferences.kt` |
| UI-01 | High | Run the full tester walkthrough and capture phone-sized screenshots. Check large fonts, narrow layouts, keyboard, history return, all programme controls, SVG selection and TalkBack. Existing Compose coverage is limited to programme creation/navigation. | `ui/*`, `androidTest/ProgrammeUiTest.kt` |
| LOG-01 | Medium | Improve unfinished input recovery. Saved sets persist immediately; unsaved quick-entry text/details use Compose saved state rather than durable database storage. Persist unfinished input if crash recovery is intended to include text not yet submitted as a set. | `ui/WorkoutEditor.kt`, `ui/EditorViewModel.kt` |
| HABIT-01 | Medium | Complete richer field configuration. Current choice/date-time fields are text entry, not configured choice/date widgets; targets support at least/at most/exact, not ranges. Add field archival/reordering while preserving historical values. | `ui/TrackerScreen.kt`, `data/local/Entities.kt`, `Daos.kt` |
| PROGRESS-01 | Medium | Improve graph axes/date labels, hover support for desktop input, ROM unit separation and PR presentation. Rep PR currently appears as best scored performance, not a distinct badge. Explain the heuristic overall skill scale in the UI. | `ui/ProgressScreen.kt`, `domain/progress/*` |
| DATA-03 | Medium | Review history immutability for variation ordering/band definitions and habit-target edits. Exercise names/types/muscle mappings are snapshotted, but some derived history still reads live definitions. Decide and test historical policy before widening editors. | `data/local/*`, `domain/progress/SessionProgress.kt` |
| STYLE-01 | Medium | Expand custom palette controls beyond accent/background/surface if desired. Verify contrast and make restored/custom palette changes refresh immediately. Reduced motion currently disables ripples; review other Material animations. | `ui/theme/VibeDesignSystem.kt`, `ui/VibeTrainerApp.kt` |
| ARCH-01 | Medium | Refactor dense editor composables into smaller screens/state holders and move persistence/validation out of UI-facing code. Remove superseded private screens in `VibeTrainerApp.kt`. Preserve behaviour and avoid a new visual redesign. | `ui/*`, `ui/EditorViewModel.kt` |
| RELEASE-01 | Later | Release signing/versioning, complete dependency-license inventory, migration policy and store preparation. Asset notices are bundled; that is not a complete release/legal audit. | Gradle, manifest, notices |

Cloud provider selection/sync and detailed habit metric charts are deliberately deferred by the user. Do not choose a provider or build those charts as an incidental change.

## Non-negotiable product decisions

- Maps describe recency, not measured fatigue. Primary/secondary weighting describes dose and does not gate recency.
- Draft workouts do not update derived views. Zero/failed/warm-up sets do not affect strength progression.
- No per-set notes UI; notes belong to exercises/workouts.
- No programme scheduling. No missing-set warning on Finish.
- Bands are Yellow 0.6 cm, Red 1.2 cm, Black 2.2 cm, Purple 3.1 cm; stack widths. Compare only within the same exercise variation. No claims of calibrated force.
- Substitutes keep their own progress graph; never combine lat-pulldown weights with pull-up results.
- One profile/database. Fresh production install has no personal routines. Demo data is for test builds.
- Keep the existing app structure with Strength/Stretching mode selection; do not split it into unrelated dashboards.

## Working and verification

Use JDK 17, Gradle 9.6 and Android SDK 36 (see CI). Commands:

```sh
gradle :app:testDebugUnitTest :app:assembleDebug
gradle :app:connectedDebugAndroidTest
```

The current chat workspace has no local Android SDK/adb; execution has used GitHub Actions. There is no live interactive emulator preview in this workspace. CI saves a launch screenshot under the validation artifact after the follow-up above.

Update this document after each task: owner, commit, actual verification result and remaining limits. Do not mark a feature complete merely because a button exists or a unit test passes.
