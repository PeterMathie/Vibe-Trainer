package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petermathie.vibetrainer.data.local.*
import java.time.Instant
import java.time.ZoneId

fun emptySet(id: String, ordinal: Int) = WorkoutSetEntity(newId(), id, ordinal, "WORKING", "COMPLETED", null, null, null, null, null, null, null, null, null, null, null, null, null, "", System.currentTimeMillis(), System.currentTimeMillis())

@Composable
fun WorkoutEditor(vm: EditorViewModel, workoutId: String?, onChoose: () -> Unit, onFinish: (String) -> Unit) {
    val workouts by vm.workouts.collectAsStateWithLifecycle()
    val snapshots by vm.workoutExercises.collectAsStateWithLifecycle()
    val sets by vm.sets.collectAsStateWithLifecycle()
    val definitions by vm.exercises.collectAsStateWithLifecycle()
    val measurements by vm.measurements.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var restRemaining by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) { while(true) { restRemaining=(context.getSharedPreferences("settings",0).getLong("restEnd",0)-System.currentTimeMillis()).coerceAtLeast(0)/1000;kotlinx.coroutines.delay(500) } }
    val workout = workouts.find { it.id == workoutId }
    var add by remember { mutableStateOf(false) }
    var editNotes by remember { mutableStateOf(false) }
    var bodyweight by remember { mutableStateOf(false) }
    if (workout == null) { Button(onClick = onChoose, modifier = Modifier.padding(16.dp)) { Text("Choose a programme") }; return }
    LaunchedEffect(workout.id, measurements) {
        if (workout.bodyweightKg == null) measurements.filter { it.metric == "Bodyweight" }.minByOrNull { kotlin.math.abs(it.recordedAt - workout.startedAt) }?.let {
            vm.save(workout.copy(bodyweightKg = if (it.unit == "lb") it.value / 2.2046226218 else it.value))
        }
    }
    val rows = snapshots.filter { it.workoutId == workout.id }.sortedBy { it.position }
    val logged = sets.filter { s -> rows.any { it.id == s.workoutExerciseId } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(workout.name, style = MaterialTheme.typography.headlineSmall)
            if(restRemaining>0) TextButton(onClick={RestTimer.cancel(context)}) { Text("Rest: ${restRemaining/60}:${(restRemaining%60).toString().padStart(2,'0')} · cancel") }
            if (logged.isNotEmpty()) Text("Logged duration: ${((logged.maxOf { it.loggedAt } - logged.minOf { it.loggedAt }) / 60000)} min")
            Row {
                TextButton(onClick = { editNotes = true }) { Text("Workout notes") }
                TextButton(onClick = { bodyweight = true }) { Text("Bodyweight: ${workout.bodyweightKg ?: "—"} kg") }
            }
        }
        items(rows, key = { it.id }) { row ->
            val exercise = definitions.find { it.id == row.actualExerciseId }?.let { if(row.exerciseName.isNotBlank())it.copy(canonicalName=row.exerciseName,trackingType=row.trackingType) else it }
            val previousIds = snapshots.filter { it.actualExerciseId == row.actualExerciseId && it.workoutId != workout.id && workouts.any { w -> w.id == it.workoutId && w.status == "FINISHED" && (w.finishedAt ?: 0) < workout.startedAt } }.map { it.id }.toSet()
            val previous = sets.filter { it.workoutExerciseId in previousIds }.maxByOrNull { it.loggedAt }
            WorkoutExerciseCard(vm, row, exercise, sets.filter { it.workoutExerciseId == row.id }, previous) {
                val group = row.supersetGroup
                val groupRows = rows.filter { it.supersetGroup == group }
                val roundComplete = group == null || groupRows.all { member ->
                    member.id == row.id || sets.count { it.workoutExerciseId == member.id } > sets.count { it.workoutExerciseId == row.id }
                }
                if (roundComplete && context.getSharedPreferences("settings", 0).getBoolean("autoRest", false))
                    RestTimer.start(context, if (group == null) row.restSeconds else groupRows.maxOf { it.restSeconds })
            }
        }
        item {
            TextButton(onClick = { add = true }) { Text("Add exercise to workout") }
            Button(onClick = { onFinish(workout.id) }, modifier = Modifier.fillMaxWidth()) { Text(if (workout.status == "FINISHED") "Done editing" else "Finish workout") }
        }
    }
    if (editNotes) NameDialog("Workout notes", workout.notes, { editNotes = false }) { vm.save(workout.copy(notes = it)); editNotes = false }
    if (bodyweight) NameDialog("Bodyweight in kg", workout.bodyweightKg?.toString().orEmpty(), { bodyweight = false }) { it.toDoubleOrNull()?.takeIf { n -> n > 0 }?.let { n -> vm.save(workout.copy(bodyweightKg = n)) }; bodyweight = false }
    if (add) ExercisePicker(vm, { add = false }) { e -> vm.save(WorkoutExerciseEntity(newId(), workout.id, e.id, e.id, rows.size, "", 120, null)); add = false }
}

@Composable
private fun WorkoutExerciseCard(vm: EditorViewModel, row: WorkoutExerciseEntity, exercise: ExerciseEntity?, sets: List<WorkoutSetEntity>, previous: WorkoutSetEntity?, onSaved: () -> Unit) {
    var notes by rememberSaveable(row.id) { mutableStateOf(row.notes) }
    var substitute by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf<WorkoutSetEntity?>(null) }
    var expanded by rememberSaveable(row.id) { mutableStateOf(false) }
    var result by rememberSaveable(row.id) { mutableStateOf("") }
    var rpe by rememberSaveable(row.id) { mutableStateOf("") }
    val context = LocalContext.current
    val hold = exercise?.trackingType in listOf("HOLD", "SKILL_HOLD")
    var timerStart by rememberSaveable { mutableStateOf<Long?>(null) }
    var elapsed by remember { mutableStateOf(0L) }
    LaunchedEffect(timerStart) { while (timerStart != null) { elapsed = android.os.SystemClock.elapsedRealtime() - timerStart!!; kotlinx.coroutines.delay(100) } }
    val lb = context.getSharedPreferences("settings",0).getBoolean("lb",false)
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(exercise?.canonicalName.orEmpty(), style = MaterialTheme.typography.titleMedium)
        if(row.targets.isNotBlank())Text(row.targets,style=MaterialTheme.typography.bodySmall)
        previous?.let { Text("Previous: ${setDescription(it)}", style = MaterialTheme.typography.bodySmall) }
        row.supersetGroup?.let { Text("Circuit: $it · rest after round", style = MaterialTheme.typography.labelSmall) }
        sets.sortedBy { it.ordinal }.forEach { set ->
            Row(Modifier.fillMaxWidth()) {
                TextButton(onClick = { edit = set }, modifier = Modifier.weight(1f)) { Text("${set.ordinal}. ${setDescription(set)}${set.rpe?.let { " · RPE $it" }.orEmpty()}") }
                TextButton(onClick = { vm.removeSet(set.id) }) { Text("×") }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(result, { result = it }, label = { Text(if (hold) "Seconds" else if (exercise?.trackingType == "WEIGHT_REPS") "${if (lb) "lb" else "kg"} × reps" else "Reps") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(rpe, { rpe = it }, label = { Text("RPE") }, singleLine = true, modifier = Modifier.width(70.dp))
            TextButton(onClick = {
                val parsed = parsePerformance(emptySet(row.id, (sets.maxOfOrNull { it.ordinal } ?: 0) + 1), result, hold, exercise?.trackingType == "WEIGHT_REPS", lb)
                if (parsed != null && (rpe.isBlank() || rpe.toDoubleOrNull()?.let { it in 0.0..10.0 } == true)) {
                    vm.saveSet(parsed.copy(rpe = rpe.toDoubleOrNull()), emptyList()); result = ""; rpe = ""; onSaved()
                    if(context.getSharedPreferences("settings",0).getBoolean("haptic",true)) {
                        val vibrator=context.getSystemService(android.os.Vibrator::class.java)
                        if(android.os.Build.VERSION.SDK_INT>=26)vibrator.vibrate(android.os.VibrationEffect.createOneShot(30,android.os.VibrationEffect.DEFAULT_AMPLITUDE)) else vibrator.vibrate(30)
                    }
                }
            }) { Text("+") }
        }
        if (hold) TextButton(onClick = { if (timerStart == null) timerStart = android.os.SystemClock.elapsedRealtime() else { result = (elapsed / 1000.0).toString(); timerStart = null } }) { Text(if (timerStart == null) "Start hold timer" else "Stop · ${elapsed / 1000.0}s") }
        Row {
            TextButton(onClick = { edit = emptySet(row.id, (sets.maxOfOrNull { it.ordinal } ?: 0) + 1) }) { Text("Bands / details") }
            TextButton(onClick = { RestTimer.start(context, row.restSeconds) }) { Text("Rest ${row.restSeconds}s") }
            TextButton(onClick = { expanded = !expanded }) { Text("More") }
        }
        if (expanded) {
            TextButton(onClick = { substitute = true }) { Text("Substitute exercise") }
            OutlinedTextField(notes, { notes = it; vm.save(row.copy(notes = it)) }, label = { Text("Exercise notes") }, modifier = Modifier.fillMaxWidth())
        }
    } }
    if (substitute) ExercisePicker(vm, { substitute = false }) { e ->
        // Keep previously recorded results attached to their actual exercise.
        if (sets.isEmpty()) vm.save(row.copy(actualExerciseId = e.id,exerciseName="",trackingType=""))
        else vm.save(row.copy(id = newId(), actualExerciseId = e.id, position = row.position + 1, notes = "",exerciseName="",trackingType=""))
        substitute = false
    }
    edit?.let { SetDetails(vm, it, row.actualExerciseId, hold, exercise?.trackingType == "WEIGHT_REPS", lb, { edit = null }) { s, b -> vm.saveSet(s,b); edit = null; onSaved() } }
}

fun parsePerformance(base: WorkoutSetEntity, text: String, hold: Boolean, weighted: Boolean, lb: Boolean): WorkoutSetEntity? {
    val parts = text.lowercase().replace("kg", "").replace("lb", "").replace('×','x').split('x').map { it.trim() }
    if (hold) { val seconds = parts.firstOrNull()?.removeSuffix("s")?.toDoubleOrNull() ?: return null; if (!seconds.isFinite() || seconds < 0) return null; return base.copy(holdMillis = (seconds * 1000).toLong(), result = if (seconds == 0.0) "FAILED" else "COMPLETED") }
    val reps = parts.lastOrNull()?.toIntOrNull() ?: return null
    val weight = if (weighted) parts.takeIf { it.size == 2 }?.first()?.toDoubleOrNull() ?: return null else null
    if (reps < 0 || weight?.let { !it.isFinite() || it < 0 } == true) return null
    return base.copy(reps = reps, weightKg = weight?.let { if (lb) it / 2.2046226218 else it }, result = if (reps == 0) "FAILED" else "COMPLETED")
}

fun setDescription(s: WorkoutSetEntity): String = when {
    s.romValue != null -> "ROM ${s.romValue} ${s.romUnit.orEmpty()}"
    s.leftReps != null || s.rightReps != null -> "L ${s.leftReps ?: 0} / R ${s.rightReps ?: 0} reps"
    s.leftHoldMillis != null || s.rightHoldMillis != null -> "L ${(s.leftHoldMillis ?: 0)/1000.0} / R ${(s.rightHoldMillis ?: 0)/1000.0}s"
    s.holdMillis != null -> "${s.holdMillis/1000.0}s"
    else -> "${s.weightKg?.let { "$it kg × " }.orEmpty()}${s.reps ?: 0} reps"
} + (if (s.setType == "WARM_UP") " · warm-up" else "") + s.addedWeightKg?.let { " +${it}kg" }.orEmpty() + s.assistanceKg?.let { " −${it}kg" }.orEmpty()

@Composable
private fun SetDetails(vm: EditorViewModel, original: WorkoutSetEntity, exerciseId: String, hold: Boolean, weighted: Boolean, lb: Boolean, dismiss: () -> Unit, save: (WorkoutSetEntity, List<String>) -> Unit) {
    val bands by vm.bands.collectAsStateWithLifecycle()
    val assignments by vm.setBands.collectAsStateWithLifecycle()
    val variations by vm.variations.collectAsStateWithLifecycle()
    var performance by remember { mutableStateOf(if (hold) original.holdMillis?.let { (it/1000.0).toString() }.orEmpty() else if (weighted) "${original.weightKg?.let { if(lb) it*2.2046226218 else it } ?: 0} x ${original.reps ?: 0}" else original.reps?.toString().orEmpty()) }
    var rpe by remember { mutableStateOf(original.rpe?.toString().orEmpty()) }
    var warmup by remember { mutableStateOf(original.setType == "WARM_UP") }
    var failed by remember { mutableStateOf(original.result == "FAILED") }
    var selected by remember(assignments) { mutableStateOf(assignments.filter { it.setId == original.id }.map { it.bandId }) }
    var variant by remember { mutableStateOf(original.variationId) }
    var left by remember { mutableStateOf((if (hold) original.leftHoldMillis?.div(1000.0) else original.leftReps)?.toString().orEmpty()) }
    var right by remember { mutableStateOf((if (hold) original.rightHoldMillis?.div(1000.0) else original.rightReps)?.toString().orEmpty()) }
    var added by remember { mutableStateOf(original.addedWeightKg?.toString().orEmpty()) }
    var assistance by remember { mutableStateOf(original.assistanceKg?.toString().orEmpty()) }
    var rom by remember { mutableStateOf(original.romValue?.toString().orEmpty()) }
    var unit by remember { mutableStateOf(original.romUnit ?: "cm") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = dismiss, title = { Text("Set details") }, text = { LazyColumn {
        item {
            EditField(if (hold) "Seconds" else if (weighted) "Weight × reps" else "Reps", performance) { performance = it }
            EditField("RPE (optional)", rpe) { rpe = it }
            Row { Checkbox(warmup, { warmup = it }); Text("Warm-up") }; Row { Checkbox(failed, { failed = it }); Text("Failed/partial — store zero") }
            Text("Bands: ${selected.sumOf { id -> bands.find { it.id == id }?.widthCentimetres ?: 0.0 }} cm total")
            bands.forEach { band -> Row { Checkbox(band.id in selected, { checked -> selected = if(checked) selected + band.id else selected - band.id }); Text("${band.name} (${band.widthCentimetres}cm)") } }
            Text("Variation")
            TextButton(onClick = { variant = null }) { Text(if (variant == null) "✓ Default" else "Default") }
            variations.filter { it.exerciseId == exerciseId }.forEach { v -> TextButton(onClick = { variant = v.id }) { Text((if(variant == v.id) "✓ " else "") + v.name) } }
            EditField("Left ${if(hold) "seconds" else "reps"}",left) { left=it }; EditField("Right ${if(hold) "seconds" else "reps"}",right) { right=it }
            EditField("Added weight (kg)",added) { added=it }; EditField("Assistance (kg)",assistance) { assistance=it }
            EditField("ROM measurement (optional)",rom) { rom=it }; EditField("ROM unit",unit) { unit=it }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    } }, confirmButton = { TextButton(onClick = {
        val base = parsePerformance(original, if(failed) { if(weighted) "0 x 0" else "0" } else performance, hold, weighted, lb)
        if (rpe.isNotBlank() && (rpe.toDoubleOrNull() == null || rpe.toDouble() !in 0.0..10.0)) { error = "RPE must be 0–10" }
        else if (base == null && rom.toDoubleOrNull() == null && left.toDoubleOrNull() == null && right.toDoubleOrNull() == null) { error = "Enter a valid result" }
        else {
            var saved=(base ?: original).copy(setType = if(warmup) "WARM_UP" else "WORKING", result = if(failed) "FAILED" else "COMPLETED", variationId = variant, rpe = rpe.toDoubleOrNull(), leftReps = if(!hold) left.toIntOrNull() else null, rightReps = if(!hold) right.toIntOrNull() else null, leftHoldMillis = if(hold) left.toDoubleOrNull()?.times(1000)?.toLong() else null, rightHoldMillis = if(hold) right.toDoubleOrNull()?.times(1000)?.toLong() else null, addedWeightKg = added.toDoubleOrNull(), assistanceKg = assistance.toDoubleOrNull(), romValue = rom.toDoubleOrNull(), romUnit = unit, updatedAt = System.currentTimeMillis())
            if(failed) saved=saved.copy(reps=0,holdMillis=0,leftReps=0,rightReps=0,leftHoldMillis=0,rightHoldMillis=0,romValue=null)
            save(saved, selected)
        }
    }) { Text("Save") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}
