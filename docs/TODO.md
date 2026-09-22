# Detailed todo list

Updated: 22 September 2026. See [WORK_STATUS.md](WORK_STATUS.md) for implemented features, code locations and verified builds. This file is the task checklist; keep both documents consistent.

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

Status: **IN PROGRESS**. Owner: ChatGPT coding agent. Branch: `codex/greenfield-foundation`. Started: 22 September 2026. Implementation and rollback tests are the current work; completion requires a passing Android test run.

- [ ] Validate enums and reject non-finite or invalid numerical values.
- [ ] Validate exercise/variation ownership and band references.
- [ ] Protect seeded definitions from unintended import modification.
- [ ] Test transaction rollback for each invalid-domain case, alongside the existing foreign-key test.
- [ ] Publish a well-formed import example and versioned format documentation.

### UI-01 — Full interaction and accessibility checks

- [ ] Fix the known low-contrast status-bar icons on the dark home screen.
- [ ] Exercise every programme create/edit/duplicate/reorder/archive control.
- [ ] Check compact logging with keyboard open, large fonts and narrow screens.
- [ ] Check substitutions, stacked bands, unilateral values, hold corrections and notes.
- [ ] Check history navigation and both historical map modes.
- [ ] Check every relevant SVG region, selected outlines and TalkBack actions.
- [ ] Capture screenshots and report concrete defects; add focused regression coverage.

### QA-01 — Real-phone timers

- [ ] Record phone model, Android version and notification/alarm permissions.
- [ ] Verify sound/vibration with the screen locked and app backgrounded.
- [ ] Verify permission-denied behaviour, timer cancellation and replacement.
- [ ] Verify automatic/manual rest and complete superset/circuit rounds.

## Remaining implementation and polish

- [ ] **LOG-01:** Persist unfinished entry drafts beyond Compose saved state if unsubmitted text must survive crashes. Test interruption and recovery without duplicate sets.
- [ ] **HABIT-01:** Add configured choice/date-time input, range targets and field archival/reordering. Preserve historical daily values.
- [ ] **PROGRESS-01:** Add chart axes/date labels, desktop hover, ROM separation by unit, explicit PR presentation and clearer skill-index explanation.
- [ ] **DATA-03:** Define and test historical behaviour when variation order, band definitions or habit targets change; some calculations still read live definitions.
- [ ] **STYLE-01:** Fix immediate palette refresh after editing/restoring; check contrast and other Material motion. Extend custom palette controls if useful.
- [ ] **ARCH-01:** Split dense editors into maintainable components/state holders, centralise validation and remove superseded private screens without changing the agreed navigation.
- [ ] **RELEASE-01:** Release signing/versioning, full dependency notices, upgrade policy and store preparation.

## Intentionally deferred

- Cloud provider selection and sync implementation.
- Detailed habit metric charts.

## Completion log

- 21 September: build, unit tests, instrumentation tests, APK installation and MainActivity launch passed in run `35640509017`; screenshot inspected and runtime-error log empty. See WORK_STATUS.md for links and feature inventory.
- 22 September: RUN-02 passed all build/test/install/launch checks; new screenshot inspected. Detailed outstanding tasks remain unchecked below their respective headings. This result does not establish full feature or real-device coverage.
