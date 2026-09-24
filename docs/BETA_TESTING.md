# Beta testing

Use the `vibe-check-debug` artifact from the latest successful Android Actions run on PR #1. Unzip it and install `app-debug.apk` on Android. This is a debug beta, not a signed store release.

Release versioning, migration gates and unsigned build checks are documented in
[RELEASE.md](RELEASE.md).

Debug installs include sample programmes, stretches, habits and history. Production installs contain only the exercise catalogue and supporting definitions. Settings → Remove demo data removes sample personal records. JSON backup should be used before replacing existing records during an import.

## Main workflows to try

1. Programmes: create a programme and a day, add exercises, change targets/rest, reorder, duplicate and archive. Start a day whenever you choose; there is no schedule.
2. Workout: enter `70 x 5`, reps, or hold seconds in the compact row; add optional RPE. Use Bands / details for stacked coloured bands, variations, sides, warm-ups, added weight, assistance or ROM. Zero and failed attempts do not affect scores. Exercise notes are under More.
3. Recovery: close and reopen the app after saving a set. The active workout should remain. Only Finish workout adds it to the maps, progress and activity calendar.
4. Substitution: replace pull-ups with lat pulldowns. The new results must appear only on the lat-pulldown graph; earlier pull-up results stay intact.
5. Maps: tap a region, inspect its last session and set-equivalents, switch anatomy in Settings, and open an old day from the heatmap. Back returns to the app. Strength and stretching have separate histories.
6. Progress: choose an exercise and variation, tap a graph point for its performance/RPE/notes, and compare the RPE graph below. Missing RPE leaves a gap rather than shifting sessions. Three valid sessions establish the index baseline.
7. Habits: create a tracker, add fields and units, optionally set targets, and edit one daily total. The homepage counts each completed habit once and each finished training session separately.
8. History: search names or notes, correct sets and the workout date. Changes recompute the graphs and historical maps.
9. Body: add/edit measurements, import/remove photos and export photos separately.
10. Settings: try units, plate calculations, colours, haptics and reduced motion. Export JSON, change a record, and restore the backup. Export CSV to inspect raw sets, variations and bands.

## Timers and platform checks

Hold timers fill an editable result. Rest timers can start manually or after a saved set; circuits wait for the completed round in automatic mode. Android notification and precise-alarm permissions control background alerts. Test an alert with the screen locked on your phone: emulator coverage does not establish behaviour under every manufacturer's battery restrictions.

## Interpretation

The body map shows training **recency**, not measured fatigue or recovery. Band widths are a relative assistance proxy within one exercise variation, not calibrated force. Overall skill indices include ordered variation levels and should be read alongside the actual performance.

Cloud sync and detailed habit metric charts remain intentionally deferred. Backups contain structured records and profile preferences; photographs export as a separate ZIP.

When reporting a bug, include the screen, steps, expected result, actual result, device/Android version and whether the records were demo or personal. Avoid posting personal backups or photos in public issues.
