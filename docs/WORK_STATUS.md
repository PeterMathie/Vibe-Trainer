# Work status and agent handoff

Updated: 23 September 2026. Owner of the current pass: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`.

Current implementation task: **UX-04 programme editor hierarchy polish is claimed and in progress** on `pmathie-cicpilot-persist-workout-drafts`. It moves all visible drag handles to the far left and hides them for singleton lists, makes the programme name directly editable, moves Duplicate/Delete/Archive to a bottom management row, and replaces the workout’s Add exercise label with a full-width accessible plus control. Programme deletion is scoped to the definition and cascading templates; historical workouts remain.

UX-03 is complete at source commit `98a38ea`. The date slider now sits directly beneath the Home muscle-recency maps, displays its effective date and previews earlier recency without opening the detailed historical screen. Heat-map selection still opens that screen. Its reconstruction copy and slider are removed; Previous day and Next day are distinct far-left/far-right chevrons around the unchanged Strength/Stretch selector in one semantic row. The date, activity cards and back behavior remain intact.

Focused `HistoryUiTest` and `EndUserControlsTest` passed, followed by exact-final `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` with 33 unit tests and 41 API 35 instrumentation tests. The exact APK SHA-256 is `d743948ee1d9f515709d05b60c3ea63f2c29be5bf5b38e06249dd735b631b3bd`; it is installed on visible `vibe-log01-api35`, where MainActivity is focused with PID 13082. Screenshots `files/ux-03-final/home-98a38ea.png` and `history-day-98a38ea.png` were inspected and match the requested placement with no observed clipping or overlap.

Checkpoint 1 removes the repeated header and specified Home copy, establishes reusable visible action controls, and replaces every textual ordered-list control with a stable-key six-dot drag interaction plus accessibility reorder actions and completion-only persistence. Checkpoint 2 removes Workout from top-level navigation and introduces a fixed Home/Programmes/Progress/More bar whose More screen visibly exposes Exercises, Habits, History, Body, Settings and Style. Checkpoint 3 replaces the fragmented programme/day flow with compact list affordances and a coherent editor where workout exercise summaries are visible by default. Each checkpoint requires focused end-user evidence and a pushed status boundary; final acceptance additionally requires the full test/build suite and installed-emulator screenshot inspection.

UX-02 checkpoint 1 is complete locally. The repeated app header and specified Home headings are removed; the recency maps and activity heat map remain central, with Earlier/range/Later in one semantic row. `ActionControls.kt` now supplies the shared action hierarchy and six-dot reorder handle. Programme, programme-day, programme-exercise, tracker-field and variation ordering previews stable-key movement and invokes persistence once at gesture completion; Move earlier/Move later accessibility actions provide the non-touch equivalent. Focused `EndUserControlsTest`, `ProgrammeUiTest` and `HabitUiTest` passed on the API 35 emulator. Checkpoint 2 navigation is the next executable step.

UX-02 checkpoint 2 is complete locally. The fixed bottom `NavigationBar` shows Home, Programmes, Progress and More at 320dp with no horizontal scrolling or Workout destination. Home continuation and programme starts retain contextual access to the active workout editor. More visibly lists Exercises, Habits, History, Body, Settings and Style; back from those secondary screens returns to More. `NavigationUiTest` passed on API 35 and opens the actual Habits screen through More. Checkpoint 3 programme flow is the next executable step.

UX-02 checkpoint 3 is complete. Programme list cards now contain only the name, edit pencil, play/choose affordance and six-dot handle. The edit pencil opens one programme surface with rename/duplicate/archive, Add workout, every workout and its exercise/target summary visible by default, prominent Start workout, and an inline workout-edit mode for rename/delete/add exercise/edit targets/remove/reorder. The focused `ProgrammeUiTest`, `NavigationUiTest`, `EndUserControlsTest` and `WorkoutLoggingUiTest` suites passed on API 35; tests exercise the visible edit/start flow and physical touch-drag persistence for programmes, workouts and exercises.

UX-02 final verification at source commit `0a61560`: `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 33 unit tests and 40 API 35 instrumentation tests. The exact debug APK SHA-256 is `2d6997316bacd4f52b0113448c842de77a8265e4c17f09a23057b2035ccf08e9`; it installed and cold-launched on the visible `vibe-log01-api35` Android 15/API 35 emulator. MainActivity remained focused with PID 9389 and the post-launch AndroidRuntime log was empty. Exact-build screenshots are under the session artifact directory `files/ux-02-final/`: `home-0a61560.png`, `more-0a61560.png`, `programmes-0a61560.png` and `programme-editor-0a61560.png`. Inspection confirmed:

- Home has no repeated product header or removed explanatory headings; the mode selector clears the status bar, maps and heat map remain central, and Earlier/range/Later share one row.
- The fixed four-item navigation fits without scrolling; More visibly exposes Exercises, Habits, History, Body, Settings and Style.
- Programme cards show only name, pencil, play and six-dot drag handle.
- The programme editor shows readable secondary management controls, a prominent Start workout action and every workout exercise/target summary by default.
- An initial device screenshot exposed an Archive label wrapping mid-word; commit `9e9d570` corrected the action layout, and the final screenshot has no observed clipping or overlap.

UX-02 is complete. The emulator remains visible with the final app running. No merge, PR, auto-merge, signing or publication occurred.

ARCH-01 remains complete at its safe behavior-preserving boundary, and repository-controlled RELEASE-01 readiness remains complete. UX-02 does not reopen either effort. Generated `.gradle/` and `app/build/` content must remain ignored and untracked; QA-01 still requires a real Android phone.

The detailed, claimable checklist is [TODO.md](TODO.md). RUN-02 completed successfully on 22 September: compilation, unit tests, emulator tests, APK installation and launch all passed. Its screenshot was inspected and runtime-error log was empty. Outstanding implementation and real-device tasks remain open.

## Start here

- Repository: PeterMathie/Vibe-Trainer.
- Long-lived working branch: `pmathie-cicpilot-persist-workout-drafts`. Keep all continuation work on this branch.
- Historical draft PR: https://github.com/PeterMathie/Vibe-Trainer/pull/1 (targets `develop`). Do not merge it or enable auto-merge. A future PR may be draft/status-only unless the user explicitly authorises merging.
- Product contract: `docs/PRODUCT_REQUIREMENTS.md`; tester walkthrough: `docs/BETA_TESTING.md`.
- Native Kotlin / Compose / Room / Hilt app. This branch has no runnable web version.
- User has authorised replacing the old implementation and completing the beta. No further approval is needed for ordinary implementation or tests.
- Latest locally verified implementation commit: `562bb0b`.
- Latest successful branch run: https://github.com/PeterMathie/Vibe-Trainer/actions/runs/35749861428 at final architecture/repository-readiness checkpoint `cf2c8ba`.
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

Local ARCH-01l verification at commit `562bb0b`: `HoldTimerButtonTest` and focused `WorkoutLoggingUiTest`/`WorkoutEntryDraftTest` passed, followed by full `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest :app:assembleRelease` validation with 33 unit tests and 36 API 35 instrumentation tests. GitHub Actions run `35749861428` also passed at `cf2c8ba`.

Local ARCH-01k verification at commit `d5e0fcd`: three `CompactEntryFormTest` cases plus `SetDetailsFormTest`, `WorkoutLoggingUiTest` and `WorkoutEntryDraftTest` passed, followed by full `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` validation with 33 unit tests and 35 API 35 instrumentation tests.

Local ARCH-01j verification at commit `3ad34a8`: three `TrackerEditorStoreTest` cases and focused `HabitFieldFormTest`/`HabitUiTest`/`HabitFieldTest` passed, followed by full `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` validation with 30 unit tests and 35 API 35 instrumentation tests.

Local ARCH-01i verification at commit `01fe0ce`: four `HabitFieldFormTest` cases and focused `HabitUiTest`/`HabitFieldTest` passed, followed by full `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` validation with 30 unit tests and 32 API 35 instrumentation tests.

Local ARCH-01h verification at commit `6d5923e`: focused `HabitUiTest` and `HabitFieldTest` plus debug assembly passed, followed by full `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` validation with 26 unit tests and 32 API 35 instrumentation tests.

Local ARCH-01g verification at commit `89bb498`: focused `SetDetailsFormTest`, `WorkoutLoggingUiTest` and `WorkoutEntryDraftTest` passed, then `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest :app:assembleRelease` passed with 26 unit tests and 32 API 35 instrumentation tests. History date navigation subsequently passed three consecutive focused emulator runs at `3ab8eee`; GitHub Actions run `35744137335` passed for that commit.

Local ARCH-01f verification at commit `2ef436f`: five `SetDetailsFormTest` cases plus `SessionProgressTest` passed; focused `WorkoutLoggingUiTest` and `WorkoutEntryDraftTest` passed; then `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest :app:assembleRelease` passed with 26 unit tests and 32 API 35 instrumentation tests.

Local RELEASE-01b verification at commit `f6174cd`: `releaseRuntimeClasspath` and `coreLibraryDesugaring` resolved successfully, the repository and asset notices matched byte-for-byte, `gradle :app:assembleRelease` passed, and APK inspection found `assets/THIRD_PARTY_NOTICES.md` plus `assets/FREE_EXERCISE_DB_LICENSE.md`.

GitHub Actions runs `35740721923` and `35742800627` exposed two stages of the same `HistoryUiTest` race: the navigated date required the unmerged semantics tree and an explicit wait for asynchronous `StateFlow` recomposition. Commit `3ab8eee` applies both corrections; three consecutive focused local runs and replacement GitHub Actions run `35744137335` passed.

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

## Remaining external or evidence-blocked work

The repository-controlled beta implementation and readiness work is complete. These gates cannot be truthfully closed by emulator or repository changes alone:

| ID | Priority | Task and acceptance criteria | Main files |
|---|---|---|---|
| QA-01 | High | Install the newest APK on a real Android phone. Check background rest notification/sound/vibration while locked, denied permissions, timer replacement and auto/manual circuits. Record device/version/results. | `ui/RestTimer.kt`, `ui/WorkoutEditor.kt` |
| DATA-01 | High | Add an actual v1 → v2 upgrade test using an exported v1 schema/fixture. Verify user records survive, Room validates the migrated schema, and snapshots/ordering exist. Current tests use fresh in-memory databases. | `di/DataModule.kt`, `data/local/Entities.kt`, `androidTest` |
| Release external gates | High | Review exact dependency licence/NOTICE distribution requirements, obtain legal approval, provide protected signing credentials, configure the store listing/data-safety declaration, verify a signed artifact and publish only with explicit authorization. | `docs/RELEASE.md`, `THIRD_PARTY_NOTICES.md`, external CI/store configuration |

Future architecture work is not an active task. Splitting `WorkoutEditor` exercise-card orchestration or the remaining cross-aggregate `EditorViewModel` operations would alter lifecycle/transaction boundaries and should begin only with a new bounded claim and purpose-built regression coverage.
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
