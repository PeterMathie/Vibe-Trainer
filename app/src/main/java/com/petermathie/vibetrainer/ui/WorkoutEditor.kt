package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petermathie.vibetrainer.data.local.*
import com.petermathie.vibetrainer.domain.workout.SetDetailsForm
import com.petermathie.vibetrainer.domain.workout.parsePerformance
import org.json.JSONArray
import java.time.Instant
import java.time.ZoneId

fun emptySet(id: String, ordinal: Int) = WorkoutSetEntity(newId(), id, ordinal, "WORKING", "COMPLETED", null, null, null, null, null, null, null, null, null, null, null, null, null, "", System.currentTimeMillis(), System.currentTimeMillis())
fun emptyEntryDraft(id: String, ordinal: Int) = WorkoutEntryDraftEntity(id, newId(), ordinal, "", "", false, false, false, "[]", null, "", "", "", "", "", "cm", System.currentTimeMillis())

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
    var editDate by remember { mutableStateOf(false) }
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
            if(workout.status=="FINISHED")TextButton(onClick={editDate=true}){Text("Change workout date")}
        }
        items(rows, key = { it.id }) { row ->
            val exercise = definitions.find { it.id == row.actualExerciseId }?.let { if(row.exerciseName.isNotBlank())it.copy(canonicalName=row.exerciseName,trackingType=row.trackingType) else it }
            val previousIds = snapshots.filter { it.actualExerciseId == row.actualExerciseId && it.workoutId != workout.id && workouts.any { w -> w.id == it.workoutId && w.status == "FINISHED" && (w.finishedAt ?: 0) < workout.startedAt } }.map { it.id }.toSet()
            val previousRow = sets.filter { it.workoutExerciseId in previousIds }.maxByOrNull { it.loggedAt }?.workoutExerciseId
            val previous = sets.filter { it.workoutExerciseId==previousRow }.sortedBy { it.ordinal }
            val restSeconds=if(row.supersetGroup==null)row.restSeconds else rows.filter { it.supersetGroup==row.supersetGroup }.maxOf { it.restSeconds }
            WorkoutExerciseCard(vm, row, exercise, sets.filter { it.workoutExerciseId == row.id }, previous,restSeconds) {
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
    if (editNotes) NameDialog("Workout notes", workout.notes, { editNotes = false }, allowEmpty = true) { vm.save(workout.copy(notes = it)); editNotes = false }
    if (bodyweight) NameDialog("Bodyweight in kg", workout.bodyweightKg?.toString().orEmpty(), { bodyweight = false }) { it.toDoubleOrNull()?.takeIf { n -> n > 0 }?.let { n -> vm.save(workout.copy(bodyweightKg = n)) }; bodyweight = false }
    if(editDate)NameDialog("Workout date (YYYY-MM-DD)",Instant.ofEpochMilli(workout.finishedAt ?: workout.startedAt).atZone(ZoneId.systemDefault()).toLocalDate().toString(),{editDate=false}) { value ->
        runCatching { java.time.LocalDate.parse(value) }.getOrNull()?.let { date ->
            val old=Instant.ofEpochMilli(workout.finishedAt ?: workout.startedAt).atZone(ZoneId.systemDefault())
            val end=date.atTime(old.toLocalTime()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            vm.changeWorkoutDate(workout,end);editDate=false
        }
    }
    if (add) ExercisePicker(vm, { add = false }) { e -> vm.save(WorkoutExerciseEntity(newId(), workout.id, e.id, e.id, rows.size, "", 120, null)); add = false }
}

@Composable
private fun WorkoutExerciseCard(vm: EditorViewModel, row: WorkoutExerciseEntity, exercise: ExerciseEntity?, sets: List<WorkoutSetEntity>, previous: List<WorkoutSetEntity>, restSeconds: Int, onSaved: () -> Unit) {
    var notes by rememberSaveable(row.id) { mutableStateOf(row.notes) }
    var substitute by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf<WorkoutSetEntity?>(null) }
    var entryDraft by remember(row.id) { mutableStateOf<WorkoutEntryDraftEntity?>(null) }
    var detailsDraft by remember(row.id) { mutableStateOf<WorkoutEntryDraftEntity?>(null) }
    var draftLoaded by remember(row.id) { mutableStateOf(false) }
    var submitting by remember(row.id) { mutableStateOf(false) }
    var expanded by rememberSaveable(row.id) { mutableStateOf(false) }
    var result by remember(row.id) { mutableStateOf("") }
    var rpe by remember(row.id) { mutableStateOf("") }
    val context = LocalContext.current
    val hold = exercise?.trackingType in listOf("HOLD", "SKILL_HOLD")
    var timerStart by rememberSaveable { mutableStateOf<Long?>(null) }
    var elapsed by remember { mutableStateOf(0L) }
    LaunchedEffect(timerStart) { while (timerStart != null) { elapsed = android.os.SystemClock.elapsedRealtime() - timerStart!!; kotlinx.coroutines.delay(100) } }
    LaunchedEffect(row.id) {
        val recovered = vm.entryDraft(row.id) ?: emptyEntryDraft(row.id, (sets.maxOfOrNull { it.ordinal } ?: 0) + 1)
        entryDraft = recovered
        result = recovered.performance
        rpe = recovered.rpe
        if (recovered.detailsOpen) detailsDraft = recovered
        draftLoaded = true
    }
    val lb = context.getSharedPreferences("settings",0).getBoolean("lb",false)
    fun updateCompact(performance: String = result, exertion: String = rpe) {
        val updated = entryDraft?.copy(performance = performance, rpe = exertion, updatedAt = System.currentTimeMillis()) ?: return
        entryDraft = updated
        vm.saveEntryDraft(updated)
    }
    fun resetDraft(ordinal: Int) {
        entryDraft = emptyEntryDraft(row.id, ordinal)
        detailsDraft = null
        result = ""
        rpe = ""
        submitting = false
    }
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(exercise?.canonicalName.orEmpty(), style = MaterialTheme.typography.titleMedium)
        if(row.targets.isNotBlank())Text(row.targets,style=MaterialTheme.typography.bodySmall)
        if(previous.isNotEmpty()) Text("Previous: ${previous.joinToString(" · ") { setDescription(it) }}", style = MaterialTheme.typography.bodySmall)
        row.supersetGroup?.let { Text("Circuit: $it · rest after round", style = MaterialTheme.typography.labelSmall) }
        sets.sortedBy { it.ordinal }.forEach { set ->
            Row(Modifier.fillMaxWidth()) {
                TextButton(onClick = { edit = set }, modifier = Modifier.weight(1f)) { Text("${set.ordinal}. ${setDescription(set)}${set.rpe?.let { " · RPE $it" }.orEmpty()}") }
                TextButton(onClick = { vm.removeSet(set.id) }) { Text("×") }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(result, { result = it; updateCompact(performance = it) }, enabled = draftLoaded && !submitting, label = { Text(if (hold) "Seconds" else if (exercise?.trackingType == "WEIGHT_REPS") "${if (lb) "lb" else "kg"} × reps" else "Reps") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(rpe, { rpe = it; updateCompact(exertion = it) }, enabled = draftLoaded && !submitting, label = { Text("RPE") }, singleLine = true, modifier = Modifier.width(70.dp))
            TextButton(enabled = draftLoaded && !submitting, onClick = {
                val pending = entryDraft ?: return@TextButton
                val parsed = parsePerformance(emptySet(row.id, pending.ordinal).copy(id = pending.setId), result, hold, exercise?.trackingType == "WEIGHT_REPS", lb)
                if (parsed != null && (rpe.isBlank() || rpe.toDoubleOrNull()?.let { it in 0.0..10.0 } == true)) {
                    submitting = true
                    vm.submitEntryDraft(parsed.copy(rpe = rpe.toDoubleOrNull()), emptyList()) {
                        resetDraft(pending.ordinal + 1)
                        onSaved()
                    }
                    if(context.getSharedPreferences("settings",0).getBoolean("haptic",true)) {
                        val vibrator=context.getSystemService(android.os.Vibrator::class.java)
                        if(android.os.Build.VERSION.SDK_INT>=26)vibrator.vibrate(android.os.VibrationEffect.createOneShot(30,android.os.VibrationEffect.DEFAULT_AMPLITUDE)) else vibrator.vibrate(30)
                    }
                }
            }) { Text("+") }
        }
        if (hold) TextButton(onClick = {
            if (timerStart == null) timerStart = android.os.SystemClock.elapsedRealtime()
            else {
                result = (elapsed / 1000.0).toString()
                updateCompact(performance = result)
                timerStart = null
            }
        }) { Text(if (timerStart == null) "Start hold timer" else "Stop · ${elapsed / 1000.0}s") }
        Row {
            TextButton(
                enabled = draftLoaded && !submitting,
                modifier = Modifier.semantics { contentDescription = "Set details for ${exercise?.canonicalName.orEmpty()}" },
                onClick = {
                entryDraft?.copy(detailsOpen = true, updatedAt = System.currentTimeMillis())?.let {
                    entryDraft = it
                    detailsDraft = it
                    vm.saveEntryDraft(it)
                }
            }) { Text("Bands / details") }
            TextButton(onClick = { RestTimer.start(context, restSeconds) }) { Text("Rest ${restSeconds}s") }
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.semantics { contentDescription = "More actions for ${exercise?.canonicalName.orEmpty()}" },
            ) { Text("More") }
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
    detailsDraft?.let { pending ->
        DraftSetDetails(vm, pending, row.actualExerciseId, hold, exercise?.trackingType == "WEIGHT_REPS", lb, {
            vm.discardEntryDraft(row.id)
            resetDraft(pending.ordinal)
        }) { s, b ->
            submitting = true
            vm.submitEntryDraft(s, b) {
                resetDraft(pending.ordinal + 1)
                onSaved()
            }
        }
    }
}

fun setDescription(s: WorkoutSetEntity): String = when {
    s.romValue != null -> "ROM ${s.romValue} ${s.romUnit.orEmpty()}"
    s.leftReps != null || s.rightReps != null -> "L ${s.leftReps ?: 0} / R ${s.rightReps ?: 0} reps"
    s.leftHoldMillis != null || s.rightHoldMillis != null -> "L ${(s.leftHoldMillis ?: 0)/1000.0} / R ${(s.rightHoldMillis ?: 0)/1000.0}s"
    s.holdMillis != null -> "${s.holdMillis/1000.0}s"
    else -> "${s.weightKg?.let { "$it kg × " }.orEmpty()}${s.reps ?: 0} reps"
} + (if (s.setType == "WARM_UP") " · warm-up" else "") + s.addedWeightKg?.let { " +${it}kg" }.orEmpty() + s.assistanceKg?.let { " −${it}kg" }.orEmpty()

private fun bandIds(value: String): List<String> {
    val values = JSONArray(value)
    return (0 until values.length()).map(values::getString)
}

private fun encodeBandIds(values: List<String>): String =
    JSONArray().apply { values.forEach { put(it) } }.toString()

@Composable
private fun DraftSetDetails(
    vm: EditorViewModel,
    original: WorkoutEntryDraftEntity,
    exerciseId: String,
    hold: Boolean,
    weighted: Boolean,
    lb: Boolean,
    dismiss: () -> Unit,
    save: (WorkoutSetEntity, List<String>) -> Unit,
) {
    val bands by vm.bands.collectAsStateWithLifecycle()
    val variations by vm.variations.collectAsStateWithLifecycle()
    var draft by remember(original.workoutExerciseId, original.setId) { mutableStateOf(original) }
    var form by remember(original.workoutExerciseId, original.setId) { mutableStateOf(SetDetailsForm.from(original)) }
    var error by remember { mutableStateOf<String?>(null) }
    fun update(transform: (SetDetailsForm) -> SetDetailsForm) {
        form = transform(form)
        draft = form.applyTo(draft).copy(updatedAt = System.currentTimeMillis())
        vm.saveEntryDraft(draft)
    }
    fun updateBands(values: List<String>) {
        draft = draft.copy(bandIds = encodeBandIds(values), updatedAt = System.currentTimeMillis())
        vm.saveEntryDraft(draft)
    }
    val selected = bandIds(draft.bandIds)
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("Set details") },
        text = {
            LazyColumn {
                item {
                    SetDetailsFields(
                        form = form,
                        hold = hold,
                        weighted = weighted,
                        bands = bands,
                        selectedBandIds = selected,
                        variations = variations,
                        exerciseId = exerciseId,
                        error = error,
                        onFormChange = { next -> update { next } },
                        onBandSelectionChange = ::updateBands,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val base = emptySet(draft.workoutExerciseId, draft.ordinal).copy(id = draft.setId)
                val result = form.buildSet(base, hold, weighted, lb)
                error = result.error
                result.set?.let { save(it, selected) }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } },
    )
}

@Composable
private fun SetDetails(vm: EditorViewModel, original: WorkoutSetEntity, exerciseId: String, hold: Boolean, weighted: Boolean, lb: Boolean, dismiss: () -> Unit, save: (WorkoutSetEntity, List<String>) -> Unit) {
    val bands by vm.bands.collectAsStateWithLifecycle()
    val assignments by vm.setBands.collectAsStateWithLifecycle()
    val variations by vm.variations.collectAsStateWithLifecycle()
    var form by remember(original.id) { mutableStateOf(SetDetailsForm.from(original, hold, weighted, lb)) }
    var selected by remember(assignments) { mutableStateOf(assignments.filter { it.setId == original.id }.map { it.bandId }) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = dismiss, title = { Text("Set details") }, text = { LazyColumn {
        item {
            SetDetailsFields(
                form = form,
                hold = hold,
                weighted = weighted,
                bands = bands,
                selectedBandIds = selected,
                variations = variations,
                exerciseId = exerciseId,
                error = error,
                onFormChange = { form = it },
                onBandSelectionChange = { selected = it },
            )
        }
    } }, confirmButton = { TextButton(onClick = {
        val result = form.buildSet(original, hold, weighted, lb)
        error = result.error
        result.set?.let { save(it, selected) }
    }) { Text("Save") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}
