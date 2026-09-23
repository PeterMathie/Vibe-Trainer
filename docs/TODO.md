# Detailed todo list

Updated: 23 September 2026. See [WORK_STATUS.md](WORK_STATUS.md) for implemented features, code locations and verified builds. This file is the task checklist; keep both documents consistent.

## Active run — RUN-02

Owner: ChatGPT coding agent. Status: completed successfully.

- [x] Check remote branch and local changes before starting; no intervening source changes found.
- [x] Create an explicit checklist for the remaining beta work.
- [x] Rerun compilation and unit tests on the current branch.
- [x] Run Android emulator instrumentation tests.
- [x] Install the APK, launch MainActivity and check that the process remains alive.
- [x] Inspect launch screenshot and runtime-error log.
- [x] Record the run, commit, artifact links and any failures here and in WORK_STATUS.md.

Evidence: commit `e0a47b76aaae8d8dfaa45295c1d7a3dc710fd433`, [run 35701312673](https://github.com/PeterMathie/Vibe-Trainer/actions/runs/35701312673). MainActivity reported `Status: ok`; the runtime-error log was empty. Screenshot shows the seeded home screen and confirms the existing status-bar contrast issue remains. No application source changes were made in this rerun.

[APK ZIP](https://github.com/PeterMathie/Vibe-Trainer/actions/runs/35701312673/artifacts/10682633925) · [Reports and screenshot](https://github.com/PeterMathie/Vibe-Trainer/actions/runs/35701312673/artifacts/10682594156).

## High-priority work

Tasks are unclaimed unless an owner is recorded below. Before starting, add your agent name, branch and date to the relevant task. Mark completion only with verification evidence.

### DATA-01 — Database upgrades

- [ ] Obtain an authentic v1 schema/fixture containing personal workouts, programmes, trackers and measurements.
- [ ] Add a v1 → v2 migration test and validate the final Room schema.
- [ ] Assert that records, foreign keys, programme ordering and historical snapshots survive.
- [ ] Include the regression in emulator CI; record the passing run.

### DATA-02 — Import validation

Status: **COMPLETED**. Owner: ChatGPT coding agent. Branch: `codex/greenfield-foundation`. Completed: 22 September 2026.

Evidence: validation is implemented in `ImportValidation.kt` and integrated transactionally in `DataTransfer.kt`. `ImportValidationTest.kt` covers invalid numeric/enumerated values, seeded records, relationships, habit fields, duplicate keys, multiple drafts, rollback and valid imports. `IMPORT_FORMAT.md` and `examples/historical-workout.json` document and exercise the versioned format.

Verification: commit `39739ff69ae4c081771f9eb5b31ab9f1e473a812`, [run 35705645513](https://github.com/PeterMathie/Vibe-Trainer/actions/runs/35705645513). Unit tests, debug assembly, API 35 instrumentation tests, APK install and MainActivity launch passed. The import instrumentation report includes all seven `ImportValidationTest` cases. This establishes the scoped DATA-02 validation; it does not supply the authentic v1 fixture required by DATA-01.

- [x] Validate enums and reject non-finite or invalid numerical values.
- [x] Validate exercise/variation ownership and band references.
- [x] Protect seeded definitions from unintended import modification.
- [x] Test transaction rollback for each invalid-domain case, alongside the existing foreign-key test.
- [x] Publish a well-formed import example and versioned format documentation.

### UI-01 — Full interaction and accessibility checks

UI-01a (status-bar contrast only): **COMPLETED**, ChatGPT coding agent, `codex/greenfield-foundation`, 22 September 2026. Commit `8ddccb5` selects light system icons; the launch screenshot from successful [run 35705645513](https://github.com/PeterMathie/Vibe-Trainer/actions/runs/35705645513) shows them readable on the dark home screen.

UI-01b (programme controls): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `708129e` drives programme create, rename, duplicate, reorder and archive through API 35 Compose UI and asserts Room state after each operation. Local `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 17 unit tests and 26 instrumentation tests; Gradle XML/HTML interaction reports are retained as generated build artifacts and CI will publish its standard test artifacts. This bounded slice does not claim compact logging, history/SVG accessibility or real-phone QA.

UI-01c (compact/detailed logging): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `6ad1a68` extends API 35 Compose coverage to correct a hold from 8 to 10 seconds through the detailed editor, substitute after a recorded bench set without rewriting that history, and keep the logger and Finish action reachable at 320dp width with 2.0 font scale. Together with checkpoint `e20322e`, `WorkoutLoggingUiTest` covers keyboard-open notes, unilateral values and two stacked bands. Exercise-specific details/more actions and shared entry fields expose unambiguous accessibility descriptions. Local `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 17 unit tests and 30 instrumentation tests; generated reports remain ignored build artifacts.

UI-01d (history navigation and map modes): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `dd3bf1a` adds an API 35 Compose test that opens seeded strength history, moves to the preceding day, switches to the seeded stretching map with selected-state semantics, and returns from the historical view. History mode controls now use the same accessible segmented pattern as the app header. Local `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 17 unit tests and 31 instrumentation tests. SVG region and selected-outline coverage remains a separate slice.

UI-01e (body-map regions and TalkBack): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `8af0d1a` enumerates all 13 front and 14 back muscle groups for both male and female diagrams through API 35 accessibility actions, invokes representative chest and lat actions, and verifies selection moves between views. The selected muscle already drives the accent outline; the map now also exposes that selection as a state description for TalkBack. Local `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 17 unit tests and 32 instrumentation tests. This slice does not substitute for QA-01 real-phone testing.

UI-01f (emulator screenshot review): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. The current debug APK installed and launched successfully on the API 35 phone-sized emulator (`Status: ok`). Current home and history screenshots were captured under the session artifact directory `files/ui-01f/` and inspected: status/navigation bars, segmented mode control, recency maps, heatmap, history inputs/cards/actions and compact bottom navigation were readable without clipping or overlap. No concrete visual defect was observed, and `runtime-errors.txt` contains no AndroidRuntime/FATAL entries. Artifacts remain outside Git. This does not satisfy QA-01.

- [x] Fix the known low-contrast status-bar icons on the dark home screen.
- [x] Exercise every programme create/edit/duplicate/reorder/archive control.
- [x] Check compact logging with keyboard open, large fonts and narrow screens.
- [x] Check substitutions, stacked bands, unilateral values, hold corrections and notes.
- [x] Check history navigation and both historical map modes.
- [x] Check every relevant SVG region, selected outlines and TalkBack actions.
- [x] Capture screenshots and report concrete defects; add focused regression coverage.

### UX-02 — End-user controls, navigation and programme flow

Status: **IN PROGRESS**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Claimed: 23 September 2026.

The initial docs-only claim at `160e9cc` is superseded by the user-approved three-checkpoint scope below. Preserve data/domain behavior and the established visual language; do not redesign Strength/Stretch behavior.

Checkpoint 1 — global controls and Home:

- [x] Remove the repeated product/header copy from page content and remove the three specified Home headings while retaining the recency maps and heat map as the visual focus.
- [x] Replace naked clickable action text on the affected list/action surfaces with reusable, importance-appropriate action controls.
- [x] Replace textual reorder controls in every ordered list with a consistent six-dot drag handle, stable-key drag/drop, placement animation, persistence on completed drag and accessibility reorder actions.
- [x] Keep Earlier, date range and Later in one coherent Home row; add focused semantics/interaction evidence.

Checkpoint 1 evidence: `ActionControls.kt` provides the shared action hierarchy, stable-key preview order, completion-only persistence and equivalent Move earlier/Move later accessibility actions. Programme, day, programme-exercise, tracker-field and variation lists use the six-dot handle; no textual Up/Down/Move controls remain. `EndUserControlsTest` verifies the removed Home copy, coherent date-navigation semantics and one completed drag producing exactly one persisted move. Focused `EndUserControlsTest`, `ProgrammeUiTest` and `HabitUiTest` passed on the API 35 emulator.

Checkpoint 2 — discoverable navigation:

- [ ] Remove Workout as a top-level destination while retaining contextual active-workout access.
- [ ] Replace the horizontally scrolling destination row with a fixed Home/Programmes/Progress/More bar.
- [ ] More visibly exposes Exercises, Habits, History, Body, Settings and Style; Compose coverage proves Habits is reachable without horizontal swiping.

Checkpoint 3 — programme user flow:

- [ ] Programme list cards show name, edit pencil, start/play affordance and drag handle only; secondary actions live in the editor.
- [ ] A coherent programme editor exposes rename, duplicate, archive/delete, workout/day and exercise management.
- [ ] Workout/day exercise summaries are visible by default, with prominent Start and a small edit pencil.
- [ ] Programme/day/exercise drag interactions persist order; creation, targets/rest/groups/notes and start/finish behavior remain available.
- [ ] End-user Compose tests cover edit/start/default exercise visibility/secondary-action placement plus drag persistence.
- [ ] Run focused validation after every checkpoint, full unit/instrumentation/debug validation at the end, then install and inspect the exact APK on the visible emulator with Home, navigation/More, programme-list and programme-editor screenshots.

### QA-01 — Real-phone timers

- [ ] Record phone model, Android version and notification/alarm permissions.
- [ ] Verify sound/vibration with the screen locked and app backgrounded.
- [ ] Verify permission-denied behaviour, timer cancellation and replacement.
- [ ] Verify automatic/manual rest and complete superset/circuit rounds.

## Remaining implementation and polish

- [x] **LOG-01:** Persist unfinished entry drafts beyond Compose saved state if unsubmitted text must survive crashes. Test interruption and recovery without duplicate sets.

  Status: **COMPLETED**. Owner: GitHub Copilot session `8e2b38d0-a00e-43a7-aef7-09872df1f6b3`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026.

  Evidence: commit `f868c50` stores one durable pending entry per workout exercise, including compact performance/RPE and Bands/details fields. Submission consumes the stable pending-set ID transactionally and is idempotent; cancellation, finish and parent deletion clear drafts, and late writes cannot recreate drafts for finished workouts. Room v2 → v3 migration schemas and migration coverage preserve existing workout data. Local `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 12 unit tests and 17 API 35 instrumentation tests, including three recovery/clearing/no-duplicate tests and one migration test. Draft-derived-view gating remains covered and no per-set notes UI was added.

  Remote status: commits `6cfe3f0` through `e00cdee` were pushed after writable authentication was restored. The branch is intentionally long-lived and must not be merged without a new explicit user instruction.
- [x] **HABIT-01:** Add configured choice/date-time input, range targets and field archival/reordering. Preserve historical daily values.

  Status: **COMPLETED**. Owner: GitHub Copilot session `8e2b38d0-a00e-43a7-aef7-09872df1f6b3`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026.

  Evidence: commits `3ae1167`, `77b3c62` and `eb4d9d8` add configured CHOICE selectors, Android date/time picker input, inclusive RANGE targets, field archival/restoration and ordering, import validation, and Room v3 → v4 migration. Archived field values remain available in history. Local `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 12 unit tests and 21 API 35 instrumentation tests. Coverage includes Compose choice configuration/entry, both inclusive range bounds, archival/history preservation, invalid import rollback and migration schema/data preservation.
- [x] **PROGRESS-01:** Add chart axes/date labels, desktop hover, ROM separation by unit, explicit PR presentation and clearer skill-index explanation.

  Status: **COMPLETED**. Owner: GitHub Copilot session `8e2b38d0-a00e-43a7-aef7-09872df1f6b3`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026.

  Evidence: commit `9dbb4c7` adds value/date axes and semantic chart ranges, pointer move/hover selection parity with touch, unit-separated ROM series, distinct weight/repetition/hold/e1RM/calculated-performance PR labels, and an explicit explanation of skill variation/hold/reps/assistance indexing. Local `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 14 unit tests and 22 API 35 instrumentation tests. New unit tests prove unlike ROM units remain separate and repetition PR is independent of scored performance; `ProgressUiTest` selects a seeded skill and verifies chart semantics, explanation and explicit PR presentation.
- [x] **DATA-03:** Define and test historical behaviour when variation order, band definitions or habit targets change; some calculations still read live definitions.

  Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026.

  Evidence: commits `e6f969f` and `885a6f6` snapshot variation rank and selected band name/width on set submission and persist each tracker's daily target outcome when a value is saved. Progress, activity heatmaps, CSV export and structured backup consume/preserve snapshots instead of recalculating from later definition edits. Room v4 → v5 migration backfills existing history and is schema/data tested; legacy version-1 JSON imports derive missing snapshots from definitions available during import. Regression tests prove later variation/band/target edits do not rewrite history. Local `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 15 unit tests and 24 API 35 instrumentation tests. Limitation: if a definition was edited before v5 migration/import, its earlier value is unavailable and the migration can only snapshot the definition present at upgrade.
- [x] **STYLE-01:** Fix immediate palette refresh after editing/restoring; check contrast and other Material motion. Extend custom palette controls if useful.

  Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026.

  Evidence: commit `94cb902` observes palette preferences as Compose state, so custom edits and structured preference restores immediately replace semantic colours without reopening. Editable accent/background/surface combinations are checked at WCAG AA 4.5:1 against their fixed text tokens; invalid edits and restored custom palettes are rejected. Reduced motion is exposed as a semantic composition-local and disables Material ripple; no app-owned animated transition APIs are currently present. Existing three controls remain sufficient because all other semantic tokens intentionally inherit the established base palette. Local `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 17 unit tests and 25 API 35 instrumentation tests. `StyleUiTest` proves immediate edit/restore refresh, contrast feedback and live reduced-motion propagation.
- [x] **ARCH-01:** Split dense editors into maintainable components/state holders, centralise validation and remove superseded private screens without changing the agreed navigation.

  ARCH-01a (superseded private screens): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `dd1beb9` removes 226 lines comprising the unreachable private `ProgrammeScreen`, `WorkoutScreen`, `ExerciseLogger` and their exclusive compact-entry helpers from `VibeTrainerApp.kt`; current navigation continues to use `ProgrammeEditor` and `WorkoutEditor`. Focused programme/logging/history Compose suites passed, followed by full `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` validation with 17 unit tests and 32 instrumentation tests.

  ARCH-01b (programme-target state/validation): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `41fef6c` extracts the programme exercise target form’s string state and entity conversion from `EntryDialog` into pure `ProgrammeEntryForm`, preserving the existing parsing, 120-second malformed-rest default, non-negative rest coercion, 0–10 RPE clamp, optional group and notes behavior. Two unit tests pin round-trip and malformed/boundary conversion, focused `ProgrammeUiTest` passed, and full `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` validation passed with 19 unit tests and 32 instrumentation tests.

  ARCH-01c (historical-day screen extraction): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `8fe0eb6` moves the tested `HistoryDayScreen` from the app/navigation monolith into `HistoryDayScreen.kt`, retaining its parameters, state ownership, shared design components and rendering. Focused `HistoryUiTest` and `MuscleMapUiTest` passed, followed by full validation with 19 unit tests and 32 instrumentation tests.

  ARCH-01d (style screen extraction): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `adec0d0` moves the tested `StyleScreen` and private palette card from `VibeTrainerApp.kt` into `StyleScreen.kt`, retaining preference ownership, semantic tokens, contrast validation and rendering. Focused `PaletteContrastTest` and `StyleUiTest` passed, followed by full validation with 19 unit tests and 32 instrumentation tests.

  ARCH-01e (editor ordering): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `a92bb2c` extracts the repeated item lookup, boundary check and swap algorithm used by programme, day, programme-exercise, tracker-field and variation moves into pure `moveItem`, while retaining each operation’s filtering, rank offset and Room transaction. Two unit tests pin up/down/input-immutability/boundary/missing-ID behavior; focused `ProgrammeUiTest` and `HabitFieldTest` passed, followed by full validation with 21 unit tests and 32 instrumentation tests.

  ARCH-01f (detailed-set validation): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `2ef436f` extracts `SetDetailsForm`, one pure validation/entity-conversion path used by both recovered-entry and saved-set dialogs. Five focused unit tests preserve weighted pound conversion, hold/unilateral/ROM/failure behavior, persistence fields and all existing invalid-input messages. Focused workout logging/draft tests passed, followed by full validation with 26 unit tests, 32 API 35 instrumentation tests and debug/release assembly.

  ARCH-01g (detailed-set field component): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `89bb498` extracts the duplicated controls into stateless `SetDetailsFields`, driven by `SetDetailsForm`, while each dialog retains its prior draft-persistence or saved-set state ownership. Labels, order, band/variation selection and error rendering remain unchanged. Focused `SetDetailsFormTest`, `WorkoutLoggingUiTest` and `WorkoutEntryDraftTest` passed, followed by full validation with 26 unit tests, 32 API 35 instrumentation tests and debug/release assembly. GitHub Actions run `35744137335` passed at `3ab8eee` after the history date-navigation assertion was made asynchronous and verified in three consecutive focused emulator runs.

  ARCH-01h (tracker daily-entry component): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `6d5923e` extracts tracker daily input, choice/date-time controls and target summary into `TrackerDailyInput.kt` without moving persistence or changing layout/navigation. Focused `HabitUiTest`/`HabitFieldTest` and debug assembly passed, followed by full validation with 26 unit and 32 API 35 instrumentation tests.

  ARCH-01i (tracker field configuration): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `01fe0ce` extracts pure `HabitFieldForm` validation/entity conversion and `HabitFieldDialog` from `TrackerScreen.kt`, preserving list state, persistence calls, labels and control order. Four unit tests pin choice uniqueness/normalisation, numeric/range targets and type-dependent clearing; focused habit tests passed, followed by full validation with 30 unit and 32 API 35 instrumentation tests.

  ARCH-01j (tracker persistence boundary): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `3ad34a8` moves tracker creation, tracker/field/value saves, active-field reordering and daily clearing into `TrackerEditorStore`; the ViewModel API, serialized error handling and Room transaction behavior are unchanged. Three focused database tests cover default-field creation, archive-aware reordering and value clearing; focused habit tests passed, followed by full validation with 30 unit and 35 API 35 instrumentation tests.

  ARCH-01k (compact set-entry state): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `d5e0fcd` replaces parallel compact performance/RPE variables and inline validation with pure `CompactEntryForm`, retaining durable draft writes, idempotent submission, labels and haptics. Three unit tests pin weighted pound conversion, hold parsing, RPE rejection and draft-field preservation; focused workout logging/draft tests passed, followed by full validation with 33 unit and 35 API 35 instrumentation tests.

  ARCH-01l (hold timer component): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `562bb0b` extracts the per-exercise hold timer into a focused stateful component with an injectable monotonic clock, retaining start/elapsed/stop labels and the compact-draft update callback. `HoldTimerButtonTest` proves start, elapsed display and stopped value; focused workout logging/draft tests passed, followed by full validation with 33 unit tests, 36 API 35 instrumentation tests and debug/release assembly. GitHub Actions run `35749861428` passed at the final documentation checkpoint `cf2c8ba`.

  ARCH-01 is complete at the intended behavior-preserving boundary. `WorkoutEditor` is now 311 lines, `TrackerScreen` is 87 lines, shared tracker/set controls and form rules are separated, and tracker persistence no longer lives directly in `EditorViewModel`. Further splitting would move tightly coupled exercise-card lifecycle state or cross-aggregate Room transactions without an independent product benefit; treat that as future architecture work with dedicated acceptance coverage, not continuation of this task.
- [x] **RELEASE-01:** Repository-controlled release readiness: versioning, dependency inventory, upgrade policy and unsigned build preparation. External legal approval, signing credentials, store configuration and publication remain release gates outside this repository task.

  RELEASE-01a (versioning, migration and unsigned build readiness): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. `docs/RELEASE.md` records the current `0.3.0`/code `2`, Room v5/version-upgrade policy, backup/restore and real-device gates, unsigned build procedure, DATA-01 limitation, and external signing/store blockers. Local `gradle :app:assembleRelease` passed and produced `app-release-unsigned.apk`; no signing configuration, credentials or publishing action was added.

  RELEASE-01b (dependency notices): **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 22 September 2026. Commit `f6174cd` inventories the Gradle-resolved release runtime families and core-library desugaring dependency, declared licence families and authoritative sources in the repository and bundled notice. Existing MuscleMap and Free Exercise DB notices remain intact. Both notice copies are byte-identical; `assembleRelease` passed and APK inspection found both notice assets. Exact upstream NOTICE/licence-text packaging and legal/store approval remain external release gates and are not represented as repository-complete.

## Intentionally deferred

- Cloud provider selection and sync implementation.
- Detailed habit metric charts.

## Completion log

- 21 September: build, unit tests, instrumentation tests, APK installation and MainActivity launch passed in run `35640509017`; screenshot inspected and runtime-error log empty. See WORK_STATUS.md for links and feature inventory.
- 22 September: RUN-02 passed all build/test/install/launch checks; new screenshot inspected. Detailed outstanding tasks remain unchecked below their respective headings. This result does not establish full feature or real-device coverage.
