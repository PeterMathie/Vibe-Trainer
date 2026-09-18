# Vibe Trainer product requirements

## Product direction
Vibe Trainer preserves the information architecture and interaction style of the Training Log beta.

Top-level sections remain:
- Home
- Programmes
- Workout
- Progress
- Habits
- Exercises
- Export data

A segmented control at the top switches the content context between **Strength** and **Stretching**. Shared data and utilities remain shared.

## First-install content
First install must not be empty.

Seed:
- Strength programmes: Planche + push, Legs + dips, Muscle-up + pull.
- Stretching programmes: Front Splits, Forward Fold, Side Splits, Bridge.
- Habits: Piano, Meditation, Stretching, Workouts.
- Representative demo workout history from the beta.
- Demo history is tagged so it can be removed without removing programme templates.

## Anatomy diagram
The body map supports:
- Male front/back.
- Female front/back.
- Strength mode: training recency / recovery display.
- Stretching mode: stretch-recency display.
- Tapping a region explains the contributing exercises and timestamps.
- The chosen anatomy sex changes only presentation; muscle IDs/history remain shared.

## Approved quality-of-life features
- Previous performance during workouts.
- Exercise notes.
- Exercise substitutions.
- Supersets and circuits.
- Full programme editing.
- Personal records.
- Better bodyweight and assistance handling.
- Bodyweight history.
- Body measurements.
- Progress photos.
- Explainable muscle recovery/fatigue.
- Optional recovery tuning.
- Workout autosave.
- Crash recovery / resume unfinished workout.
- Undo and safe destructive actions.
- Background/screen-off rest timer.
- Timer vibration and notifications.
- Backup/restore with last-backup indicator.
- JSON backup/restore and CSV export.
- Workout-history search/filtering.
- Exercise aliases and variants.
- Plate calculator.
- kg/lb support, kg default.
- Accessibility options.
- Haptics controls.
- Reduced-motion support.
- Database migrations/versioning.
- Historical workout editing.
- Custom/reusable programmes.
- Duplicate programme/day.
- Reorder/add/remove programme exercises.
- Target sets/reps/rest/RPE.
- PRs for weight, reps, estimated 1RM and hold duration.
- Stretch logging by reps, duration, load and side where relevant.
- Flexibility / ROM progress tracking.

## Explicitly deferred
- Session notes.
- Planned-vs-actual performance comparison.
- Automatic progression suggestions.
- Extra warm-up-set automation.

## Technical constraints
- Native Android.
- Kotlin, Jetpack Compose, Material 3.
- Room/SQLite for training data.
- DataStore for lightweight preferences.
- Hilt.
- Coroutines/Flow.
- Local-first/offline-first.
- Central design tokens; no arbitrary styling scattered through screens.
- Modular feature packages and SOLID design where it improves maintainability.
