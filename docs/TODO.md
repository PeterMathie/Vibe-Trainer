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

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Claimed and completed: 23 September 2026.

The initial docs-only claim at `160e9cc` is superseded by the user-approved three-checkpoint scope below. Preserve data/domain behavior and the established visual language; do not redesign Strength/Stretch behavior.

Checkpoint 1 — global controls and Home:

- [x] Remove the repeated product/header copy from page content and remove the three specified Home headings while retaining the recency maps and heat map as the visual focus.
- [x] Replace naked clickable action text on the affected list/action surfaces with reusable, importance-appropriate action controls.
- [x] Replace textual reorder controls in every ordered list with a consistent six-dot drag handle, stable-key drag/drop, placement animation, persistence on completed drag and accessibility reorder actions.
- [x] Keep Earlier, date range and Later in one coherent Home row; add focused semantics/interaction evidence.

Checkpoint 1 evidence: `ActionControls.kt` provides the shared action hierarchy, stable-key preview order, completion-only persistence and equivalent Move earlier/Move later accessibility actions. Programme, day, programme-exercise, tracker-field and variation lists use the six-dot handle; no textual Up/Down/Move controls remain. `EndUserControlsTest` verifies the removed Home copy, coherent date-navigation semantics and one completed drag producing exactly one persisted move. Focused `EndUserControlsTest`, `ProgrammeUiTest` and `HabitUiTest` passed on the API 35 emulator.

Checkpoint 2 — discoverable navigation:

- [x] Remove Workout as a top-level destination while retaining contextual active-workout access.
- [x] Replace the horizontally scrolling destination row with a fixed Home/Programmes/Progress/More bar.
- [x] More visibly exposes Exercises, Habits, History, Body, Settings and Style; Compose coverage proves Habits is reachable without horizontal swiping.

Checkpoint 2 evidence: Home continuation and programme starts still route to the active workout editor, but Workout is absent from the fixed four-item `NavigationBar`. More presents six full-width outlined destination controls and secondary-screen back returns to More. `NavigationUiTest` renders at 320dp, verifies all four primary items are displayed, proves Workout is absent, and opens the real Habits screen through visible More → Habits controls on the API 35 emulator.

Checkpoint 3 — programme user flow:

- [x] Programme list cards show name, edit pencil, start/play affordance and drag handle only; secondary actions live in the editor.
- [x] A coherent programme editor exposes rename, duplicate, archive/delete, workout/day and exercise management.
- [x] Workout/day exercise summaries are visible by default, with prominent Start and a small edit pencil.
- [x] Programme/day/exercise drag interactions persist order; creation, targets/rest/groups/notes and start/finish behavior remain available.
- [x] End-user Compose tests cover edit/start/default exercise visibility/secondary-action placement plus drag persistence.
- [x] Run focused validation after every checkpoint, full unit/instrumentation/debug validation at the end, then install and inspect the exact APK on the visible emulator with Home, navigation/More, programme-list and programme-editor screenshots.

Checkpoint 3 focused evidence: `ProgrammeUiTest` now approaches the feature only through visible end-user controls. It proves secondary actions are absent from the programme list; the edit pencil opens one programme editor; workout exercise names and target/rest summaries are visible without another navigation step; Start invokes the selected day; the workout pencil reveals same-card rename/add-exercise/target controls; create/rename/duplicate/archive remain functional; and touch drags persist programme, workout and exercise order. The focused programme, navigation, global-control and workout-logging suites passed on API 35.

Final evidence: source commit `0a61560` passed `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` with 33 unit tests and 40 API 35 instrumentation tests. The exact APK (`SHA-256 2d6997316bacd4f52b0113448c842de77a8265e4c17f09a23057b2035ccf08e9`) installed and cold-launched on `vibe-log01-api35`; MainActivity remained focused with PID 9389 and no AndroidRuntime errors. Screenshots in the session artifact directory `files/ux-02-final/` cover Home, More, programme list and programme editor. Inspection confirmed the mode selector clears the status bar, Home centers the maps/heat map and one-row date controls, all More destinations are visible, list cards contain only pencil/play/drag affordances, and workout exercises/targets are visible by default. An initial screenshot exposed a wrapped Archive label; commit `9e9d570` corrected the layout and the final screenshot has no clipping or overlap.

### UX-03 — Historical recency navigation

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Claimed and completed: 23 September 2026.

- [x] Put Previous day and Next day at the far sides of the Strength/Stretch selector on one coherent historical-day row.
- [x] Remove the historical-day slider and the “Reconstructed from records up to the end of this day” copy.
- [x] Add a Home-only date slider directly beneath the muscle recency maps; dragging previews recency at an earlier date without opening the detailed historical-day screen.
- [x] Preserve heat-map day selection, historical activities, mode switching and back behavior.
- [x] Add focused Compose coverage, run relevant validation, push evidence and leave the exact app running on the visible emulator.

Evidence: commit `d9c2801` separates Home preview date from detailed history selection and adds the requested controls; `98a38ea` gives the date/back controls semantic contrast and distinct day-step chevrons after emulator inspection. `HistoryUiTest` proves the compact row, removed copy/slider, previous-day navigation, mode switching, activity rendering and back behavior; it also proves Home preview does not select/open a historical day. `EndUserControlsTest` drives the Home slider and verifies its callback. Exact-final `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 33 unit tests and 41 API 35 instrumentation tests.

The debug APK at source `98a38ea` has SHA-256 `d743948ee1d9f515709d05b60c3ea63f2c29be5bf5b38e06249dd735b631b3bd`. It installed and launched on `vibe-log01-api35`; MainActivity remained focused with PID 13082. Exact-build screenshots `files/ux-03-final/home-98a38ea.png` and `history-day-98a38ea.png` show the slider directly beneath the Home maps and the historical-day chevron/Strength–Stretch/chevron row with no old slider or reconstruction copy.

### UX-04 — Programme editor hierarchy polish

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Claimed and completed: 23 September 2026.

- [x] Put every visible six-dot handle at the far left of its item and hide it when the list has only one reorderable item; dragging the handle continues to move the whole item and persist order.
- [x] Replace the programme editor’s heading/rename action with an inline editable programme-name field and explicit save affordance.
- [x] Move Duplicate, Delete and Archive to one secondary three-button row at the bottom of the programme editor. Delete removes only the programme definition and its cascading templates; historical workouts remain.
- [x] Put a full-width plus-only Add exercise control at the bottom of each expanded workout exercise list, with an accessible label.
- [x] Update end-user Compose/persistence coverage, run focused and full validation, push evidence and inspect the exact installed UI.

Evidence: implementation commit `7d96f8b` moves programme, day, exercise, tracker-field and custom-variation handles to the leading edge and suppresses singleton handles. Programme names save inline; the programme-level Duplicate/Delete/Archive controls form a bottom row; each expanded workout ends with a full-width plus control whose accessibility label remains `Add exercise`. Strength/Stretch now appears only at the top-right of the Home recency card and beside the Programmes title, not globally, in programme editing or in historical-day details.

Focused `ProgrammeUiTest`, `HistoryUiTest`, `EndUserControlsTest` and `HabitUiTest` passed, followed by `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` with 33 unit tests and 41 API 35 instrumentation tests. Coverage includes persisted programme/day/exercise drag order, singleton suppression, leading handle placement, inline rename, bottom actions, historical-workout preservation after programme deletion, plus-only exercise addition and exact mode-selector placement.

The exact `7d96f8b` debug APK has SHA-256 `626cf6d3ce834fbeaaf823822eb0578c7f0d894a218b4ea836d83069aff0259e`. It installed and cold-launched on visible `vibe-log01-api35` (Android 15/API 35); MainActivity remained resumed with PID 15321 and the crash log contained no AndroidRuntime failure. Screenshots under `files/ux-04-final/` show Home, the programme list, the programme editor’s top and bottom, and historical-day details. Inspection found no clipping, overlap or unresolved defect in this slice. Real-phone behavior remains governed by QA-01 and was not claimed.

### UX-05 — Remove duplicated workout-card controls

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Claimed and completed: 23 September 2026.

- [x] Remove the duplicated workout title, Rename workout action and Delete workout action from the expanded single-workout programme card.
- [x] Remove the redundant Add workout action from the programme editor.
- [x] Preserve Start workout, exercise editing/reordering/removal and the full-width Add exercise control.
- [x] Update focused end-user coverage, validate, install and inspect the exact build.

Evidence: commit `a63b898` removes the repeated heading and both duplicated actions from the single-workout card. Exercise edit/remove/reorder controls are available directly in the programme edit context; multi-workout programmes retain compact titled reorder rows so their workouts remain distinguishable. Focused `ProgrammeUiTest` passed and the final `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` rerun passed with 33 unit tests and 41 API 35 instrumentation tests. The first full run encountered an unrelated `WorkoutLoggingUiTest` closed-connection teardown race; the unchanged test passed in the clean full rerun.

The exact APK has SHA-256 `55ff7035fb4bf69a3eb7c6d4dc76343c510b8a73280672d528263df746533455`. It installed and launched on visible `vibe-log01-api35`; MainActivity remained resumed with PID 17232 and no AndroidRuntime crash. `files/ux-05-final/programme-editor-a63b898.png` confirms the card starts with Start workout, retains exercise controls and the plus action, and contains no duplicated title, Rename workout or Delete workout.

Follow-up commit `372f22a` removes Add workout as requested. Focused `ProgrammeUiTest` and the full 33-unit/41-instrumentation gate passed. Exact-build APK SHA-256 is `bfef1e2dd568929a3eb7c6d4dc76343c510b8a73280672d528263df746533455`; `files/ux-05-final/programme-editor-372f22a.png` confirms the programme-name field now flows directly into Start workout, with the retained exercise and programme-management controls visible. MainActivity remained resumed on the API 35 emulator with PID 18170 and no crash.

### UX-06 — Direct handle dragging

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Claimed and completed: 23 September 2026.

- [x] Make six-dot handles begin reordering on a normal thumb drag without requiring a long press or being intercepted by a click target.
- [x] Preserve completion-only persistence and accessibility Move earlier/Move later actions.
- [x] Prove direct-drag programme exercise reordering persists, then run the full gate and inspect the installed build.

Evidence: commits `fd8f0f1` and `ca70b76` remove the empty clickable/long-press path and use Compose’s dedicated vertical `draggable` modifier, committing from its stop callback. `ProgrammeUiTest` now performs the programme/day/exercise gesture without a hold and verifies Room order. Focused coverage and full `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 33 unit tests and 41 API 35 instrumentation tests.

The exact `ca70b76` APK has SHA-256 `5c052cf9f065fad606af57fa1333c2b4a3de4e260a499a06f7b40ac47187d34f`. An actual `adb input swipe` on the visible Handstand handle immediately placed Back squat first and Handstand second; the on-device Room database contained the same persisted positions. `files/ux-06-final/persisted-order-ca70b76.png` records the result. MainActivity remains resumed with PID 20421 and no AndroidRuntime crash.

### UX-07 — Drag feedback and motion

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Claimed and completed: 23 September 2026.

- [x] Highlight and animate the active six-dot handle so touch drag state is immediately visible.
- [x] Make programme exercise rows follow the drag and animate displaced-row placement.
- [x] Preserve persisted ordering, reduced-motion behavior and accessibility actions; validate and inspect the installed interaction.

Evidence: commits `cd5065d`, `d2cbf5b`, `0d63c8f` and `02608b9` add active-handle state, finger-following row translation and eased displaced-row placement. The final active treatment is a subtle 36dp dark-grey circle inside the unchanged 48dp touch target, so it remains visible without clipping or becoming a bright primary control. The dragged row receives an opaque surface while moving. Reduced motion retains direct manipulation but removes decorative scale/colour/placement transitions; accessibility reorder actions remain.

Focused `ProgrammeUiTest` and final full `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 33 unit tests and 41 API 35 instrumentation tests. The exact `02608b9` APK has SHA-256 `eca60be25baa16211a2677d64a0003cbc6892cfb50e9d637e424ac587980413c`. `files/ux-07-final/active-drag-02608b9.png` captures the highlighted handle during motion and `settled-drag-02608b9.png` captures the result. The on-device database confirms Back squat at position 0 and Handstand at position 1; MainActivity remains resumed with PID 24088 and no crash.

### UX-08 — Stable drag and programme preview

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Claimed and completed: 23 September 2026.

- [x] Add drag-boundary hysteresis so a handle held near an item boundary does not oscillate between positions.
- [x] Make programme-list cards expand/collapse with smooth motion to show every workout exercise and target summary read-only.
- [x] Replace the programme editor’s textual Back control with a left-facing chevron.
- [x] Add end-user coverage, run validation once after the complete slice, install and inspect the exact build.

Evidence: commit `bbe3818` adds 1.5-row-direction-change hysteresis to the shared reorder state, so small reverse movement near a crossed boundary does not immediately swap back. A focused unit regression pins this behavior. Programme names now expand/collapse an animated read-only list of every workout/day exercise and target summary; reduced motion shows the same content without decorative transitions. The programme editor uses a chevron-only Back control with its accessible label retained.

Strength and Stretch use the same `ProgrammeEditor`, reorder state and persistence methods; only the mode-filtered data differs. UX-13 subsequently corrected the seeded Stretching data shape to one workout containing four reorderable exercises. `ProgrammeUiTest` now proves Stretch preview content and persisted exercise reordering explicitly.

The final `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` rerun passed with 34 unit tests and 42 API 35 instrumentation tests. The first combined run exposed and prompted correction of a mode-specific test wait, alongside one known asynchronous History assertion flake; both passed in the clean rerun. The exact `bbe3818` APK has SHA-256 `1c19e3338287da2698d4840c75426def10c889239f63b1159a2c476c002a1f12`. Screenshots under `files/ux-08-final/` show the expanded Strength and Stretch cards, chevron-only editor and Stretch reorder handles. MainActivity remains resumed with PID 25277 and no crash.

### UX-09 — Progress exercise eligibility

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Claimed and completed: 23 September 2026.

- [x] Restrict the Progress exercise picker to exercises with at least one valid performance or ROM progress point from a finished workout.
- [x] Replace the shared 100-result catalogue picker so Progress has no artificial alphabetical cutoff and the complete eligible set can scroll.
- [x] Cover eligible/ineligible selection and deep scrolling, then run one final validation gate and inspect the installed screen.

Evidence: commit `5b6c7ec` gives Progress a dedicated lazy picker. Eligibility requires a finished workout row with either a valid completed working set or a recorded ROM value, so catalogue-only and archived exercises are absent while both performance and flexibility charts remain reachable. The shared picker’s `.take(100)` cap no longer applies. Search still matches eligible canonical names, aliases and mapped muscles, and an explicit empty result is shown.

`ProgressUiTest` proves an exercise without history is excluded, an eligible exercise opens its charts, and the lazy picker can scroll to item 105 rather than stopping at the former 100-item boundary. Final `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 34 unit tests and 43 API 35 instrumentation tests. The exact APK has SHA-256 `76c8bb5c6250de2a1f69bec5925a7037079a079640ee4f48424de0840ea73782`; `files/ux-09-final/progress-picker-5b6c7ec.png` shows only the eight demo exercises with qualifying history. MainActivity remains resumed with PID 26663 and no crash.

### UX-12 — Correct programme start and structured set logging

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Claimed and completed: 23 September 2026.

- [x] Make each programme-list Play affordance start that programme's workout directly, never enter edit context or reuse another programme/day.
- [x] Remove weekday/scheduling language from maintained demo programme-day names without rewriting user-authored or historical workout names.
- [x] Present the programmed set count as editable Resistance/Reps/RPE rows, keep notes below the rows, expose rest timing from the exercise header, and use a plus-only control for extra sets.
- [x] Preserve hold/unilateral type behavior, durable unfinished-entry recovery and duplicate-submission protection.
- [x] Add end-user routing/logging regressions, run focused and full validation, install the exact APK and inspect the corrected flow.

Evidence: implementation commit `4826194` routes main-list Play through the selected programme's first ordered workout, prompts before replacing an unrelated active draft, and transactionally discards only after explicit confirmation. Maintained seeded day names are schedule-free; existing historical workout names remain unchanged. Room schema 6 gives unfinished entry drafts a `(workoutExerciseId, ordinal)` key, with a validated 5→6 migration preserving existing input and allowing independent recoverable rows. Programmed rows now expose only relevant Resistance/Seconds, Reps and RPE inputs, with header rest timing, notes below and a plus-only extra-row action. Submission consumes only the submitted ordinal and reuses saved set IDs.

Focused programme, logging, recovery, migration and workflow classes passed. The final `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` gate passed 35 unit and 47 API 35 instrumentation tests. The installed APK from `4826194f6b85271ae1d0cfda1ffb5825a7b5863a` has SHA-256 `842a97fc0a745a463929285744b3bbd1145a74aa557f7847c4d455ea546ce3ab`. Screenshots under `files/ux-12-final/` show the programme-list Play affordance, direct schedule-free `Legs + Mobility` workout, its Handstand warm-up followed by Back squat/Lunge exercises, three structured rows, header timer, notes and plus-only action. MainActivity is resumed on `vibe-log01-api35` with PID 9090 and no crash-buffer entry.

Residual limitation: programmes with multiple workouts currently launch their first ordered workout from the programme-level Play affordance; this correction intentionally did not introduce a workout chooser. Existing historical names containing weekdays are preserved by design.

### UX-13 — Canonical exercise settings and flat Stretching programme

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Make the exercise database the canonical owner of resistance inputs, repetition targets, target RPE and rest duration.
- [x] Remove conflicting logging-input and target configuration from programme exercise editing.
- [x] Snapshot canonical settings when a workout starts so later exercise edits do not rewrite historical or active workout behavior.
- [x] Migrate existing programme settings deterministically without losing historical workout snapshots.
- [x] Model Stretching as one workout containing Front split, Forward fold, Side split and Bridge, rather than four nested workout/category rows.
- [x] Preserve Handstand's independent freestanding and total-wall time inputs.

Evidence: implementation commit `d442e52` adds Room schema 8, exercise-level settings editing for both seeded and custom exercises, canonical snapshot wiring and a validated 7→8 migration. Existing programme settings are copied to their exercise definitions where present; programme rows remain for ordering and contextual notes, while historical workout snapshots remain unchanged. Fresh and migrated demo data now retain `demo-day-front-splits` as the single `Stretching` workout and move all four stretch exercises into it in a stable order.

The final gate passed 33 unit tests and focused API 35 instrumentation suites: `ExerciseEditorUiTest` (1), `ProgrammeUiTest` (4), `WorkoutWorkflowTest` (3), `VibeDatabaseMigrationTest` (6), `WorkoutLoggingUiTest` (6) and `WorkoutEntryDraftTest` (4). Debug app and test APK assembly passed. The installed APK SHA-256 is `f2b3f5cfd4230030e5227edcdf377604706838ad0f12c32911423f352b543b43`; installed UI inspection expanded one Stretching card containing Front split, Forward fold, Side split and Bridge. Evidence is `files/ux-13-final/stretching-expanded-d442e52.png`.

UX-14 subsequently reduced the active seeded catalogue to the maintained exercises referenced by programmes while preserving historical references.

### UX-14 — Compact autosaving workout rows and curated catalogue

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Remove the per-set check action and autosave valid row input without duplicate sets.
- [x] Keep Handstand freestanding seconds, total wall seconds and compact RPE on one row.
- [x] Restrict RPE to a compact two-character integer field with a maximum value of 10.
- [x] Remove the redundant Start hold timer action and generic workout-notes editor while retaining exercise notes and the header rest timer.
- [x] Remove fixed 100-item exercise-list/picker caps.
- [x] Seed only the 17 maintained exercises used by programmes and hide unused legacy catalogue entries without breaking historical references.

Evidence: implementation commit `1753c9e` replaces explicit set submission with ordered autosave through the existing durable set ID, so corrections update rather than duplicate a set. Clearing the final logged metric removes that saved set while preserving the recoverable draft. The Handstand row places Freestanding sec, Total wall sec and the 68dp RPE field on one visual line; installed semantics bounds confirm matching vertical centres. The old hold-timer component/test and workout-notes action are removed; exercise notes and header rest controls remain.

Fresh databases contain exactly the 17 exercises referenced by maintained programmes. Existing `free-exercise-db` rows and the unused Lat pulldown seed are archived only when no programme references them, preserving historical foreign-key and snapshot integrity. Both Exercises and exercise-picker lazy lists no longer truncate at 100 items.

Validation passed 33 unit tests, debug app/test assembly, `WorkoutLoggingUiTest` (6), `WorkoutEntryDraftTest` (4) and `WorkoutWorkflowTest` (4). The installed APK SHA-256 is `1e769f52c0318c710ac3a63e5ed609ca427bd6d961ef457a39356cde71a364c6`; `files/ux-14-final/compact-handstand.png` records the inspected exact build.

### UX-15 — Single-line Handstand labels and representative annual history

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Shorten Handstand labels to `Freestanding s` and `Total wall s`, keep them on one line and preserve the compact three-field row.
- [x] Seed 52 weeks of consistent sessions across Planche + Push, Legs + Mobility and Muscle-up + Pull.
- [x] Include deterministic improving, plateaued and declining exercise trajectories.
- [x] Replace the older two-exercise demo dataset cleanly on existing debug installs.

Evidence: implementation commit `f17d605` uses single-line 9sp field labels. Installed semantic bounds for Freestanding, Total wall and RPE are identical vertically (`677–845`), confirming no wrap or second line.

Progress demo version 3 creates 156 workouts (three per week), covering every exercise in the three strength programmes with three sets per exercise. Handstand, Planche, Bench press, Back squat and Pull-up improve; Overhead press, Jefferson curl and Leg raise decline; remaining exercises fluctuate within stable plateaus. Skill variation, assistance, weight, repetition and hold data follow each exercise type. Bulk Room inserts keep deterministic seeding practical, and the previous `demo-progress-*` rows are replaced before regeneration.

Validation passed 33 unit tests, debug app/test assembly, `ProgressUiTest` (2), `WorkoutLoggingUiTest` (6) and `WorkoutWorkflowTest` (4). Tests assert 156 annual sessions and representative improving/plateau/declining trajectories. The installed APK SHA-256 is `9959b4c8607a954e1e994bbe0f49a3e959adb42df902bd2d3e828feda4882105`; `files/ux-15-final/handstand-labels.png` records the inspected row.

### UX-16 — Progress overview, habit heat maps and bodyweight photos

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Show an aggregate weekly training trend from normalized exercise scores.
- [x] Show bodyweight history and open a same-day progress photo when one exists.
- [x] Show one Progress heat map per active habit with a persistent user-selected color.
- [x] Seed one year of representative bodyweight and habit history in debug builds.
- [x] Preserve tracker data through a tested Room migration and generated schema.

Evidence: implementation commit `1b4df3b` adds the aggregate trend, bodyweight chart/photo popup and colored habit heat maps. Room schema 9 adds `trackers.colourArgb`; migration 8→9 preserves existing trackers with the green default. The tracker editor offers seven accessible color swatches. Progress demo version 4 supplies 52 bodyweight records plus year-long Piano, Meditation and Protein activity.

Validation passed 33 unit tests and debug app/test assembly. Focused API 35 instrumentation passed: `ProgressUiTest` (2), `HabitUiTest` (1), `VibeDatabaseMigrationTest` (7) and `WorkoutLoggingUiTest` (6). The exact installed APK SHA-256 is `49b3154d80adc5727d4fbea3b0c051776fa82e13083978ae43036f6b277df96a`. Installed screenshots `files/ux-16-final/progress-top.png` and `progress-lower.png` confirm readable annual charts, distinct habit colors and continued scrolling into the existing Progress content.

Limitation: photo association intentionally reuses the existing timestamp-named photo files and matches by local calendar date. A photo captured on a different date from a historical bodyweight entry is not automatically linked.

### UX-17 — Shared card spacing and sharper corners

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Give every top-level Progress section the shared 16dp vertical rhythm used by the rest of the app.
- [x] Route the Progress list through the shared `ScreenList` layout.
- [x] Reduce the shared card corner radius from 22dp to 16dp across the app.

Evidence: commit `bdccd30` updates the shared `VibeShapes.card` token rather than overriding individual screens. Progress uses `ScreenList` and `VibeSpacing.medium`, so its charts and habit cards no longer touch while retaining the same spacing source as Home and Programmes. Unit tests, debug app/test assembly and `ProgressUiTest` (2) passed. Exact APK SHA-256: `d6ac4138be3a1ba84d103f8cbacdced856b7bb12f093efa006fc025d9fd153df`. Installed screenshot: `files/ux-17-final/progress-spacing-final.png`.

### UX-18 — Reorderable Progress cards and configurable habit intensity

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Reorder Overall training trend, Bodyweight, each habit heat map and Training progress through top-right drag handles; persist the custom Progress order.
- [x] Put exercise and variation selection inside the Training progress card.
- [x] Configure each habit's light/medium/dark value boundaries using plain-language labels.
- [x] Use the habit's chosen color for all three intensity shades and remove the redundant color dot.
- [x] Default Piano/Meditation to 7/15 minutes and Protein to 140/160 grams.
- [x] Add persistent habit ordering and threshold fields through Room schema 10 with migration coverage.
- [x] Replace technical habit setup labels with user-facing names and goal comparisons.
- [x] Restore historical muscle-map highlighting for seeded completed workouts.
- [x] Promote Habits and Body to a fixed, non-scrolling six-item bottom bar.

Evidence: implementation commit `9b4e07f` adds persistent Progress layout ordering, Room 9→10 habit settings, value-derived heat-map intensity, plain-language habit configuration, the historical muscle-snapshot backfill, and direct bottom navigation. The compact navigation label is `Plans`; the programme page title remains unchanged.

The exact build passed unit tests and debug app/test assembly. Focused API 35 instrumentation passed: `ProgressUiTest` (2), `HabitUiTest` (1), `VibeDatabaseMigrationTest` (8), `HistoryUiTest` (2) and `NavigationUiTest` (1). APK SHA-256: `e51a218809c807f0636cdfab20f5190116887abc2df1391c34d9c73294374814`. Installed screenshots are `files/ux-18-final/home-nav-final.png`, `habits-final.png` and `progress-final.png`.

### UX-19 — Distinct Body navigation and dedicated bodyweight entry

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Keep the dumbbell icon for Plans and give Body a distinct raised-arms torso/person icon.
- [x] Make Body a dedicated bodyweight entry page with no editable metric name.
- [x] Save new Body entries with the hard-coded `Bodyweight` metric.
- [x] Display bodyweight consistently to exactly two decimal places without changing stored precision.

Evidence: implementation commit `18e2b48` uses Material `FitnessCenter` for Plans and `AccessibilityNew` for Body. `MeasurementsUiTest` proves the Metric control is absent, a `78.126` entry is stored unchanged as Bodyweight and displayed as `78.13 kg`. Unit tests, debug app/test assembly and focused API 35 Navigation (1), Measurements (1), Progress (2) and Workout logging (6) suites passed. Exact APK SHA-256: `25ec51213f51899922bc5b0e352082b57cd40ae97b6765f4328e06a12eda610c`. Installed evidence: `files/ux-19-final/body-final.png`.

### UX-11 — Exercise-aware personal records

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Claimed and completed: 23 September 2026.

- [x] Promote Personal records above Progress charts and present records as a visually distinct app-styled summary.
- [x] Remove the repetition-only PR and replace the opaque calculated-performance row with the highest scored performance.
- [x] Filter weight, hold and estimated-one-rep-max records by the selected exercise's tracking type so irrelevant metrics are absent rather than rendered as empty values.
- [x] Add focused domain and Compose regression coverage, run the combined validation gate, install the exact APK and inspect the resulting Progress page.

Evidence: implementation `a990214` moves a bordered Personal records card directly below the exercise selectors. Best performance reports the highest existing domain score and identifies its contributing set. `WEIGHT_REPS` exercises show available Heaviest weight and Estimated 1RM records; `HOLD`/`SKILL_HOLD` exercises show Longest hold; bodyweight, assisted-repetition and repetition exercises do not receive irrelevant weight/hold rows. Null records are omitted rather than displayed as placeholders. The standalone Repetition PR and Calculated performance PR labels are removed.

Focused unit/assembly/Progress instrumentation passed. The final clean full gate passed 35 unit tests and 43 API 35 instrumentation tests; two preceding full instrumentation attempts encountered the established asynchronous in-memory Room teardown race in two unrelated tests, while all Progress tests passed in every attempt. The exact APK SHA-256 is `8972fd690469e8b335520ef89f34c0e94075be32b4e5dec037e11e823c737fbb`. `files/ux-11-final/weighted-records-a990214.png` visibly shows only the weighted records for Bench press, while `hold-records-a990214.png` shows only hold-relevant records for Handstand. MainActivity remains resumed with PID 29387 and no AndroidRuntime crash.

### UX-10 — Compact Progress controls and annual demo

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Claimed and completed: 23 September 2026.

- [x] Put the exercise selector and variation dropdown on one row; show the selected variation name in the dropdown control.
- [x] Remove the large inline methodology explanation while retaining concise on-demand explanation.
- [x] Expand debug demo progress history to approximately one year and upgrade existing debug seed data without restoring deliberately removed demos.
- [x] Update coverage, run one combined final gate, install and inspect the exact build.

Evidence: commit `945117b` places equal-width exercise and variation controls on one row. The variation dropdown contains All variations plus the selected exercise’s configured choices; its closed label changes from Variations to the chosen name. The large raw-performance/skill paragraph is removed from the main page and remains available from the compact `How progress works` info action.

Debug progress seed version 2 now creates 52 weekly Planche and Bench press sessions spanning approximately one year, with gradual performance changes and a mid-year band transition. Existing debug installs upgrade from the earlier eight-session seed by stable IDs; installs where demo records were deliberately removed remain untouched. `ProgressUiTest` verifies the Handstand → Wall handstand dropdown flow, compact on-demand explanation and 52 seeded progress workouts.

Final `gradle :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest` passed with 34 unit tests and 43 API 35 instrumentation tests. The exact APK has SHA-256 `66ce2582f2ff6858a031c4119c9ed1391250ac038fd4cd815b31fe6e313ac14d`. Screenshots `files/ux-10-final/progress-controls-945117b.png`, `variation-selected-945117b.png` and `year-progress-945117b.png` show the compact row, selected label and chart from 24 September 2025 to 22 September 2026. MainActivity remains resumed with PID 27370 and no crash.

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

### UX-20 — Compact habit cards and progressive settings

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Keep default habit cards focused on daily entry, with spacing consistent with Progress cards and one visible `Edit settings` action.
- [x] Move colour, thresholds, measurement management, rename and archive controls into settings.
- [x] Hide the horizontal colour palette until requested and explain light/medium/dark boundaries in plain language.
- [x] Seed representative annual light, medium and dark values for Piano, Meditation and Protein.

Evidence: commit `3062c46` implements the compact cards, progressive settings dialog and upgraded debug seed. Unit tests and debug app/test assembly passed; focused API 35 `HabitUiTest` (1) and `ProgressUiTest` (2) passed.

### UX-21 — Fitted Home dashboard

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Make the untrained/body-white SVG regions transparent and remove visible separator strokes while retaining selection highlighting and the body silhouette.
- [x] Render the recency slider as a neutral grey track with only the current-position thumb in the accent colour.
- [x] Fit the Home recency card and five-week heat map within the available viewport without vertical scrolling.
- [x] Preserve full-size heat maps outside Home and keep the fixed bottom navigation visible.

Evidence: commit `3d29f5a` uses a viewport-fitted Home column, flexible map region and Home-only compact heat-map cells. The transparent SVG follow-up was verified through debug assembly and installed screenshot inspection. `files/ux-22-transparent-svg.png` shows only tracked colour fills over the card background, with both cards and fixed navigation still fitting without scrolling.

### UX-22 — Tracking display refinements

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Make all white and never-trained anatomy fills transparent and enlarge both Home figures without losing the fitted layout.
- [x] Rename the Home activity card to `Work tracker`.
- [x] Render Progress RPE as bars against a fixed 0–10 scale.
- [x] Increase vertical spacing throughout the habit settings dialog.
- [x] Move the persistent reduced-motion explanation behind a compact `(i)` action.

Evidence: commit `6f3519e` implements the refinements. Unit tests and debug app/test assembly passed; focused API 35 `HabitUiTest` (1) and `ProgressUiTest` (2) passed, including a semantic assertion for the fixed 0–10 RPE range. Final debug assembly passed with APK SHA-256 `983bf1520c8199baf02ec99a05c8364b5abee8653bd769942b9cbef0cdedd6cf`. `files/ux-22-home-final.png` shows the enlarged transparent figures and renamed Work tracker while retaining the one-screen Home layout.

### UX-23 — Simplified habits and representative body photos

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Remove the Habits date override and always record against today.
- [x] Show each habit colour beside its card title.
- [x] Consolidate Count, Duration and Rating into Number with a configurable unit; remove Date and time from new field configuration.
- [x] Map ordered choice values from light to dark and define meaningful yes/no and written-entry shades.
- [x] Seed Mood, Journal and Reading examples with annual values across non-numeric input types.
- [x] Seed four local placeholder progress images tied to bodyweight dates and show them ahead of the long measurement history.
- [x] Remove the plate calculator while retaining the on-demand reduced-motion information dialog.

Evidence: commit `272f902` implements the simplified field model, non-numeric heat-map classification, extra demo habits, placeholder photo generation and settings cleanup. Unit tests and debug app/test assembly passed. Focused API 35 `HabitUiTest` (1), `ProgressUiTest` (2) and `MeasurementsUiTest` (1) passed; unit coverage pins ordered-choice, boolean and written-entry shades. The exact APK SHA-256 is `85034bb7814f57e77c8bd375f1259449f51975ee5d5a9bf281099ea7f2663358`; `files/ux-23-habits.png` and `files/ux-23-body.png` record the colour markers, alternate habit inputs and visible demo photo gallery.

### UX-24 — Linked bodyweight calendar and habit lifecycle

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Replace the long Body measurement list with a month calendar showing each recorded daily weight.
- [x] Reuse the Progress bodyweight chart on Body and synchronize chart selection, calendar selection and selected-day details.
- [x] Show and manage only the selected day’s weight and photos; attach new entries and photos to that date.
- [x] Keep Body and Progress backed by the same Room measurement stream so changes update both.
- [x] Hide archived habits from daily entry and Progress, expose an Archived habits restore section, and allow permanent deletion only when no daily values exist.

Evidence: commit `386a598` implements the shared chart/calendar/day selection and guarded habit lifecycle. Unit tests and debug app/test assembly passed; focused API 35 `HabitUiTest` (2), `MeasurementsUiTest` (1) and `ProgressUiTest` (2) passed. Coverage proves empty-habit deletion, history-preserving archive/restore, calendar weight updates and chart synchronization. Exact APK SHA-256 is `1e02d004f6686c715d7959a8d5c253027982265386299745e84df7293194ae25`; `files/ux-24-body-calendar.png` shows the linked annual chart and readable monthly calendar.

### UX-25 — Visual habit choice scales and icons

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Replace comma-separated list configuration with draggable, named choice rows ordered from lightest to darkest.
- [x] Preview every choice in the habit colour and show fixed Light/Medium and Medium/Dark divider lines.
- [x] Persist choice order and shade boundaries and use them for Progress heat-map intensity.
- [x] Add a reusable habit icon catalogue, persist each habit’s icon and render it on the compact card.
- [x] Replace the full-width card settings action with an accessible pencil action.
- [x] Edit choice lists directly in the main habit settings dialog with compact rows; adding defaults to Medium and dragging across a divider changes shade.
- [x] Save daily habit values automatically and remove the separate daily Save action.
- [x] Preserve existing databases through an explicit Room 10→11 migration and schema export.

Evidence: commit `5ce5fb2` implements the visual editor, icon catalogue and preserving schema 11 migration. Unit tests and debug app/test assembly passed. Focused API 35 `HabitUiTest` (2), `ProgressUiTest` (2) and the 10→11 migration test passed; coverage proves icon persistence, accessible choice reordering, persisted boundaries, boundary-driven Mood intensity and preservation of existing tracker/field values. Exact APK SHA-256 is `38bff4fe233e4b7cddb9933e91ceeedeb11500dd08d764f033bc7e23c90d2b10`; installed evidence is under `files/ux-25-habit-choice/`.

Follow-up commits `4439ea3` and `0e9e3ce` move the full list editor into the main habit settings dialog, replace boundary selectors with spatial divider lines, autosave daily input, move the pencil into the title row and shorten each choice box. Unit tests and API 35 `HabitUiTest` (2) passed after the compact-row change; the preceding inline-editor checkpoint also passed `ProgressUiTest` (2) and debug app/test assembly. The installed APK SHA-256 is `f52a20948af92ce1268e1472cadfa39db8c2d9c4067d96c511dc5c42fd0987d8`; inspected evidence is under `files/ux-26-inline-habits/`.

Follow-up `3f764a8` removes measurement-level archive actions and redundant non-numeric heat-map copy, keeps permanent deletion in the archived-habits area, aligns Archive/Cancel/Save in the dialog action row and matches remove icons to the grey reorder handles. Unit tests and API 35 `HabitUiTest` (2) passed. Installed APK SHA-256: `2eaedfc29338211c238440dd974880e8612c6cccf87da201a3effc3cd995191f`; inspected evidence is under `files/ux-27-habit-settings/`.

Follow-up `c96b8b0` labels measurements by type, removes explanatory choice-editor headings, keeps the compact icon catalogue visible independently of the colour palette and uses a full-width footer so Archive aligns to the content edge. Unit tests and API 35 `HabitUiTest` (2) passed. Installed APK SHA-256: `cc288f2bf8e1ce90feb77c0084646d7f49912ea98ba2dc3ecb05777c96dd1daa`; inspected evidence is under `files/ux-28-clean-habit-settings/`.

Follow-up `6b872f9` replaces the inline icon strip with a compact button and a scrollable picker of 62 curated habit-relevant icons. It also shortens `Save settings` to `Save` and replaces the textual Add choice control with the standard plus icon. Unit tests and API 35 `HabitUiTest` (2) passed; the exact installed APK was assembled successfully with SHA-256 `515da56c8446d2c89c1fdd19169bc738e3abf64f71da26ca09a3857efb8bac5b`. Inspected evidence is under `files/ux-29-icon-picker/`.

### UX-26 — Habit, anatomy and secondary-screen polish

Status: **COMPLETED**. Owner: GitHub Copilot session `33304680-6464-408c-b019-2abe27a4f884`. Branch: `pmathie-cicpilot-persist-workout-drafts`. Completed: 23 September 2026.

- [x] Use an icon-only habit picker, a full-width standard plus control, a centered Cancel action and a title-row New habit action.
- [x] Show persisted habit icons on Progress heat-map cards.
- [x] Render never-trained mapped muscles with the neutral recency colour instead of transparency.
- [x] Separate the exercise database into Strength and Stretching sections and keep all results scrollable.
- [x] Space exercise and History actions evenly and use shared outlined buttons/cards instead of the unrelated tonal fill.
- [x] Show only kilograms in exercise settings unless the existing Pounds setting is enabled, then show pounds.
- [x] Put corrected icons on the left of More actions, including exercise and cog Settings icons.
- [x] Add four coherent built-in palettes and show every semantic palette colour while excluding habit indicator colours.

Evidence: `f843253` implements the habit, Progress and anatomy refinements. `c9bf07b` modernizes Exercises, History, More and Style while reusing exercise tags, the existing `lb` preference, shared cards/actions and the central palette registry. Unit tests passed, including contrast validation for all six built-in palettes. Focused API 35 instrumentation passed for `ExerciseEditorUiTest`, `HistoryUiTest`, `NavigationUiTest`, `StyleUiTest`, `HabitUiTest`, `ProgressUiTest` and `MuscleMapUiTest`. The exact installed APK SHA-256 is `41f65144b47d7713371f9f21484d51a53d3fa7c1523583b45972f5f52b9b6f39`; inspected evidence is under `files/ux-30-ui-modernization/`.

## Intentionally deferred

- Cloud provider selection and sync implementation.
- Detailed habit metric charts.

## Completion log

- 21 September: build, unit tests, instrumentation tests, APK installation and MainActivity launch passed in run `35640509017`; screenshot inspected and runtime-error log empty. See WORK_STATUS.md for links and feature inventory.
- 22 September: RUN-02 passed all build/test/install/launch checks; new screenshot inspected. Detailed outstanding tasks remain unchecked below their respective headings. This result does not establish full feature or real-device coverage.
