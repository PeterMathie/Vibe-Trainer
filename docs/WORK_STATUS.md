# Work status and agent handoff

Updated: 22 September 2026. Owner of the current pass: ChatGPT coding agent.

Current implementation task: **paused at a pushed ARCH-01 boundary** on `pmathie-cicpilot-persist-workout-drafts`. ARCH-01g is complete; no later slice is claimed. The next safe candidate is a separately claimed extraction of tracker configuration from `TrackerScreen.kt`, with existing habit tests expanded before changing state ownership.

RELEASE-01b is complete at `f6174cd`: repository and bundled notices inventory the resolved release runtime families and explicit transitive helpers, both notice copies are byte-identical, and the unsigned APK contains the notices. Exact upstream NOTICE/licence-text packaging plus legal/store approval remain external release gates. Generated `.gradle/` and `app/build/` content remains ignored and untracked; QA-01 remains open for a real Android phone.

The detailed, claimable checklist is [TODO.md](TODO.md). RUN-02 completed successfully on 22 September: compilation, unit tests, emulator tests, APK installation and launch all passed. Its screenshot was inspected and runtime-error log was empty. Outstanding implementation and real-device tasks remain open.

## Start here

- Repository: PeterMathie/Vibe-Trainer.
- Long-lived working branch: `pmathie-cicpilot-persist-workout-drafts`. Keep all continuation work on this branch.
- Historical draft PR: https://github.com/PeterMathie/Vibe-Trainer/pull/1 (targets `develop`). Do not merge it or enable auto-merge. A future PR may be draft/status-only unless the user explicitly authorises merging.
- Product contract: `docs/PRODUCT_REQUIREMENTS.md`; tester walkthrough: `docs/BETA_TESTING.md`.
- Native Kotlin / Compose / Room / Hilt app. This branch has no runnable web version.
- User has authorised replacing the old implementation and completing the beta. No further approval is needed for ordinary implementation or tests.
- Latest locally verified implementation commit: `8af0d1a`.
- Verified run: https://github.com/PeterMathie/Vibe-Trainer/actions/runs/35705645513. Unit tests, debug APK, Android API 35 emulator tests, APK installation and MainActivity launch all passed. `am start` reported `Status: ok`; the process remained alive, a home screenshot was captured, and the AndroidRuntime error log was empty.
- APK artifact: `vibe-trainer-debug`; reports and generated Room schemas: `validation-reports`.

## What is implemented

- Full bundled exercise catalogue plus curated skills/stretches, canonical muscle mappings, aliases, tags, tracking types, search, custom definitions, duplication and archival. Custom variations can be added/reordered; seeded ordering is protected.
- Programme/day/exercise creation, editing, duplication, ordering, archival/removal, targets, rest and circuit groups.
- Room-backed workout drafts and saved sets; finish-gated maps/progress; compact performance entry, RPE, exercise/workout notes, warm-ups, failed attempts, unilateral fields, bands, holds, substitutions and previous-session sets.
- Actual exercise identity owns its graph after substitution. Name, tracking type, targets and muscle mappings are snapshotted for real workouts and demos.
- Separate strength/stretch recency maps using male/female front/back vectors, exact selected-path outlines and accessibility actions; historical day navigation, back navigation, mode switch and muscle explanations.
- Exercise session scoring, baseline/rolling index, variation filters, raw performance/notes, aligned RPE graph, PR summaries and separate ROM view.
- Progress charts expose value/date axes and pointer detail, ROM is charted per unit, PR types are explicit, and skill-index copy explains its heuristic and personal baseline.
- Historical progress and tracker activity are deterministic after Room v5: submitted sets retain variation rank and band definitions, while tracker days retain their target outcome.
- Custom palette edits/restores refresh immediately, unreadable custom colour combinations are blocked, and reduced motion is available throughout the semantic theme.
- Homepage activity heatmap, historical calendar navigation, editable/searchable finished workouts, custom multi-field habits and daily totals with numeric targets.
- Habit fields support configured choices, date/time pickers, inclusive range targets, archival/restoration and reordering while retaining historical values.
- Body measurements and photo import/removal/export; structured JSON merge/restore, CSV including band stacks and notes, last-backup indicator, backed-up profile preferences.
- AlarmManager rest alerts, notification/precise-alarm permission controls, hold timer, units/plate calculator, haptics and reduced-motion ripple control.
- Central palette tokens, built-in/custom colours, debug demo history and removable demo records. Production personal data starts empty.

## Fixes completed in this continuation

- Added Room-backed recovery for unfinished compact and Bands/details workout input. Added a v2 → v3 migration, stable idempotent set submission and cleanup on cancellation/finish/deletion.
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

Local ARCH-01g verification at commit `89bb498`: focused `SetDetailsFormTest`, `WorkoutLoggingUiTest` and `WorkoutEntryDraftTest` passed, then `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest :app:assembleRelease` passed with 26 unit tests and 32 API 35 instrumentation tests.

Local ARCH-01f verification at commit `2ef436f`: five `SetDetailsFormTest` cases plus `SessionProgressTest` passed; focused `WorkoutLoggingUiTest` and `WorkoutEntryDraftTest` passed; then `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest :app:assembleRelease` passed with 26 unit tests and 32 API 35 instrumentation tests.

Local RELEASE-01b verification at commit `f6174cd`: `releaseRuntimeClasspath` and `coreLibraryDesugaring` resolved successfully, the repository and asset notices matched byte-for-byte, `gradle :app:assembleRelease` passed, and APK inspection found `assets/THIRD_PARTY_NOTICES.md` plus `assets/FREE_EXERCISE_DB_LICENSE.md`.

GitHub Actions run `35740721923` at `ba3274f` failed one of 32 instrumentation tests because `HistoryUiTest` queried a date in the merged semantics tree while the runner reported the node only in the unmerged tree; the other 31 tests passed. Both date assertions now explicitly use the unmerged tree, and the focused test passes locally on the API 35 emulator.

Local RELEASE-01a verification: `gradle :app:assembleRelease` passed for version `0.3.0`/code `2`, including release lint, and produced `app/build/outputs/apk/release/app-release-unsigned.apk`. The artifact is intentionally unsigned and untracked.

Local ARCH-01e verification at commit `a92bb2c`: focused `OrderedItemsTest`, `ProgrammeUiTest` and `HabitFieldTest` passed, then the full unit/instrumentation/build command passed with 21 unit tests and 32 API 35 instrumentation tests.

Local ARCH-01d verification at commit `adec0d0`: focused `PaletteContrastTest` and `StyleUiTest` passed, then the full unit/instrumentation/build command passed with 19 unit tests and 32 API 35 instrumentation tests.

Local ARCH-01c verification at commit `8fe0eb6`: focused `HistoryUiTest` and `MuscleMapUiTest` passed, then the full unit/instrumentation/build command passed with 19 unit tests and 32 API 35 instrumentation tests.

Local ARCH-01b verification at commit `41fef6c`: focused `ProgrammeEntryFormTest` and `ProgrammeUiTest` passed, then the full unit/instrumentation/build command passed with 19 unit tests and 32 API 35 instrumentation tests. Generated XML reports contain zero failures or errors.

Local ARCH-01a verification at commit `dd1beb9`: focused `ProgrammeUiTest`, `WorkoutLoggingUiTest` and `HistoryUiTest` passed, then the full unit/instrumentation/build command passed with 17 unit tests and 32 API 35 instrumentation tests. The removed private screens and helpers had no callers, and the reachable editors retained their existing UI coverage.

Local UI-01e verification at commit `8af0d1a`: `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 17 unit tests and 32 API 35 instrumentation tests. `MuscleMapUiTest` inventories all 13 front and 14 back region actions on male/female maps, invokes representative chest/lat actions, and verifies TalkBack selection state follows the selected outline.

Local UI-01d verification at commit `dd3bf1a`: `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 17 unit tests and 31 API 35 instrumentation tests. `HistoryUiTest` navigates seeded strength and stretching history, verifies selected mode semantics, changes historical day and returns from the historical view.

Local UI-01c verification at commit `6ad1a68`: `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 17 unit tests and 30 API 35 instrumentation tests. `WorkoutLoggingUiTest` covers keyboard-open notes, unilateral values, stacked bands, hold correction, substitution after saved history, and a 320dp-wide/2.0-font-scale layout. Persisted Room assertions prove corrected/substituted entries do not rewrite or duplicate the previously saved set.

Local UI-01b verification at commit `708129e`: `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 17 unit tests and 26 API 35 instrumentation tests. `ProgrammeUiTest` now covers the complete top-level programme lifecycle, including order and archive persistence; generated Gradle XML/HTML reports remain ignored build artifacts.

Local STYLE-01 verification at commit `94cb902`: `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 17 unit tests and 25 API 35 instrumentation tests. `PaletteContrastTest` checks WCAG ratio calculation and accepted/rejected custom combinations; `StyleUiTest` provides API 35 interaction evidence for immediate edit and restored-preference refresh, invalid contrast feedback, restored-palette rejection, and live reduced-motion state.

Local DATA-03 verification at commits `e6f969f` and `885a6f6`: `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 15 unit tests and 24 API 35 instrumentation tests. Coverage proves later variation/band/target edits cannot rewrite saved history, validates Room v4 → v5 backfill/schema, and keeps the published legacy import example idempotent. Pre-v5 definition edits cannot be reconstructed; migration/import snapshots the definitions available at upgrade/import.

Local PROGRESS-01 verification at commit `9dbb4c7`: `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 14 unit tests and 22 API 35 instrumentation tests. Two new session-progress tests cover ROM unit separation and explicit repetition versus calculated-performance records; `ProgressUiTest` selects seeded Planche history and verifies chart semantics, heuristic explanation and explicit PR labels.

Local HABIT-01 verification at commit `eb4d9d8`: `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 12 unit tests and 21 API 35 instrumentation tests. New evidence includes `HabitUiTest` exercising configured choice creation and daily selection, two `HabitFieldTest` cases for inclusive ranges and archived history, strengthened invalid-import rollback, and `VibeDatabaseMigrationTest` coverage for v3 → v4 data/schema preservation.

Local LOG-01 verification at commit `f868c50`: `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 12 unit tests and 17 API 35 emulator tests. The instrumentation total includes three `WorkoutEntryDraftTest` cases for process/database recreation, transactional no-duplicate submission, cancellation/finish/deletion cleanup and finished-workout stale-write rejection, plus `VibeDatabaseMigrationTest` for v2 → v3 data preservation and schema validation. Commits through `e00cdee` are pushed to the long-lived remote branch; a branch CI run is the next remote verification step.

Run `35705645513` at commit `39739ff` passed unit tests, debug assembly, API 35 instrumentation tests, APK installation and MainActivity launch. Its instrumentation reports include all seven `ImportValidationTest` cases, establishing DATA-02's scoped domain validation, transactional rollback, seeded-definition protection and published-example import. The captured home screenshot was inspected and shows readable light status-bar icons on the dark palette, establishing UI-01a only. The remaining programme, compact logging, history, SVG and TalkBack checks under UI-01 are still unverified.

Earlier run `35639647804` at commit `c071717` passed compilation, unit tests and instrumentation tests, then failed the added launch command because the test runner had uninstalled the target APK. The verified revision explicitly installs the built APK before launching it. That launch-harness failure is resolved.

Downloads for the verified build:
- APK ZIP: https://github.com/PeterMathie/Vibe-Trainer/actions/runs/35705645513/artifacts/10684382510
- Reports, schema and launch screenshot: https://github.com/PeterMathie/Vibe-Trainer/actions/runs/35705645513/artifacts/10684731891

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
