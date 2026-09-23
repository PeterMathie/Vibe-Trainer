package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Timer
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
import com.petermathie.vibetrainer.domain.workout.CompactEntryForm
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
            if(restRemaining>0) VibeActionButton("Rest: ${restRemaining/60}:${(restRemaining%60).toString().padStart(2,'0')} · cancel", { RestTimer.cancel(context) }, importance = ActionImportance.COMPACT)
            if (logged.isNotEmpty()) Text("Logged duration: ${((logged.maxOf { it.loggedAt } - logged.minOf { it.loggedAt }) / 60000)} min")
            Row {
                VibeActionButton("Workout notes", { editNotes = true }, importance = ActionImportance.COMPACT)
                VibeActionButton("Bodyweight: ${workout.bodyweightKg ?: "—"} kg", { bodyweight = true }, importance = ActionImportance.COMPACT)
            }
            if(workout.status=="FINISHED") VibeActionButton("Change workout date", { editDate=true }, importance = ActionImportance.COMPACT)
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
            VibeActionButton("Add exercise to workout", { add = true }, importance = ActionImportance.SECONDARY)
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
    val assignments by vm.setBands.collectAsStateWithLifecycle()
    var notes by rememberSaveable(row.id) { mutableStateOf(row.notes) }
    var substitute by remember { mutableStateOf(false) }
    var draftLoaded by remember(row.id) { mutableStateOf(false) }
    var expanded by rememberSaveable(row.id) { mutableStateOf(false) }
    var visibleRows by rememberSaveable(row.id) { mutableIntStateOf(0) }
    val drafts = remember(row.id) { mutableStateMapOf<Int, WorkoutEntryDraftEntity>() }
    val submitting = remember(row.id) { mutableStateMapOf<Int, Boolean>() }
    val context = LocalContext.current
    val hold = exercise?.trackingType in listOf("HOLD", "SKILL_HOLD")
    val weighted = exercise?.trackingType == "WEIGHT_REPS"
    val targetSets = row.targets.substringBefore(" sets").toIntOrNull()?.coerceAtLeast(1) ?: 3
    LaunchedEffect(row.id) {
        val recovered = vm.entryDrafts(row.id)
        sets.forEach { set ->
            drafts[set.ordinal] = draftFromSet(
                set,
                hold,
                weighted,
                assignments.filter { it.setId == set.id }.sortedBy { it.ordinal }.map { it.bandId },
            )
        }
        recovered.forEach { draft ->
            drafts[draft.ordinal] = draft.copy(detailsOpen = false)
        }
        visibleRows = maxOf(targetSets, sets.maxOfOrNull { it.ordinal } ?: 0, recovered.maxOfOrNull { it.ordinal } ?: 0)
        draftLoaded = true
    }
    val lb = context.getSharedPreferences("settings",0).getBoolean("lb",false)
    fun draftAt(ordinal: Int) = drafts[ordinal] ?: emptyEntryDraft(row.id, ordinal).also { drafts[ordinal] = it }
    fun updateDraft(updated: WorkoutEntryDraftEntity) {
        drafts[updated.ordinal] = updated
        vm.saveEntryDraft(updated)
    }
    fun performanceParts(draft: WorkoutEntryDraftEntity): Pair<String,String> {
        if (!weighted) return "" to draft.performance
        val parts = draft.performance.split(Regex("\\s*[x×]\\s*"), limit = 2)
        return parts.getOrElse(0) { "" } to parts.getOrElse(1) { "" }
    }
    fun updatePerformance(ordinal: Int, resistance: String, result: String) {
        val performance = if (weighted) {
            if (resistance.isBlank() && result.isBlank()) "" else "$resistance x $result"
        } else result
        updateDraft(draftAt(ordinal).copy(performance = performance, updatedAt = System.currentTimeMillis()))
    }
    fun submit(ordinal: Int) {
        val pending = draftAt(ordinal)
        val parsed = CompactEntryForm(pending.performance, pending.rpe).buildSet(
            sets.find { it.ordinal == ordinal } ?: emptySet(row.id, ordinal).copy(id = pending.setId),
            hold,
            weighted,
            lb,
        ) ?: return
        submitting[ordinal] = true
        vm.submitEntryDraft(parsed, bandIds(pending.bandIds)) {
            drafts[ordinal] = draftFromSet(parsed, hold, weighted, bandIds(pending.bandIds))
            submitting[ordinal] = false
            onSaved()
        }
    }
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(exercise?.canonicalName.orEmpty(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { RestTimer.start(context, restSeconds) }) {
                Icon(Icons.Outlined.Timer, contentDescription = "Start ${restSeconds} second rest for ${exercise?.canonicalName.orEmpty()}")
            }
        }
        if(row.targets.isNotBlank())Text(row.targets,style=MaterialTheme.typography.bodySmall)
        if(previous.isNotEmpty()) Text("Previous: ${previous.joinToString(" · ") { setDescription(it) }}", style = MaterialTheme.typography.bodySmall)
        row.supersetGroup?.let { Text("Circuit: $it · rest after round", style = MaterialTheme.typography.labelSmall) }
        repeat(visibleRows) { index ->
            val ordinal = index + 1
            val draft = draftAt(ordinal)
            val (resistance,result) = performanceParts(draft)
            val busy = submitting[ordinal] == true
            Column(
                Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Set $ordinal for ${exercise?.canonicalName.orEmpty()}" },
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text("$ordinal", style = MaterialTheme.typography.labelLarge)
                    if (weighted) OutlinedTextField(
                        resistance,
                        { updatePerformance(ordinal,it,result) },
                        enabled=draftLoaded&&!busy,
                        label={Text(if(lb)"lb" else "kg")},
                        singleLine=true,
                        modifier=Modifier.weight(1f).semantics { contentDescription="Resistance for ${exercise?.canonicalName.orEmpty()} set $ordinal" },
                    )
                    OutlinedTextField(
                        result,
                        { updatePerformance(ordinal,resistance,it) },
                        enabled=draftLoaded&&!busy,
                        label={Text(if(hold)"Seconds" else "Reps")},
                        singleLine=true,
                        modifier=Modifier.weight(1f).semantics { contentDescription="${if(hold)"Seconds" else "Reps"} for ${exercise?.canonicalName.orEmpty()} set $ordinal" },
                    )
                    OutlinedTextField(
                        draft.rpe,
                        { updateDraft(draft.copy(rpe=it,updatedAt=System.currentTimeMillis())) },
                        enabled=draftLoaded&&!busy,
                        label={Text("RPE")},
                        singleLine=true,
                        modifier=Modifier.weight(1f).semantics { contentDescription="RPE for ${exercise?.canonicalName.orEmpty()} set $ordinal" },
                    )
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
                    IconButton(onClick={submit(ordinal)},enabled=draftLoaded&&!busy) {
                        Icon(Icons.Outlined.Check,contentDescription="Save set $ordinal for ${exercise?.canonicalName.orEmpty()}")
                    }
                    if(sets.any { it.ordinal==ordinal }) IconButton(onClick={
                        sets.find { it.ordinal==ordinal }?.let { vm.removeSet(it.id) }
                        vm.discardEntryDraft(row.id,ordinal)
                        drafts[ordinal]=emptyEntryDraft(row.id,ordinal)
                    }) {
                        Icon(Icons.Outlined.Delete,contentDescription="Remove set $ordinal for ${exercise?.canonicalName.orEmpty()}")
                    }
                }
            }
        }
        if (hold) {
            HoldTimerButton { seconds ->
                val ordinal=(1..visibleRows).firstOrNull { drafts[it]?.performance.isNullOrBlank() } ?: visibleRows
                updatePerformance(ordinal,"",seconds)
            }
        }
        FilledTonalButton(
            onClick={visibleRows++},
            modifier=Modifier.fillMaxWidth(),
        ) { Icon(Icons.Outlined.Add,contentDescription="Add set for ${exercise?.canonicalName.orEmpty()}") }
        OutlinedTextField(notes, { notes = it; vm.save(row.copy(notes = it)) }, label = { Text("Exercise notes") }, modifier = Modifier.fillMaxWidth())
        Row {
            VibeActionButton(
                label = "More",
                onClick = { expanded = !expanded },
                modifier = Modifier.semantics { contentDescription = "More actions for ${exercise?.canonicalName.orEmpty()}" },
                importance = ActionImportance.COMPACT,
            )
        }
        if (expanded) {
            VibeActionButton("Substitute exercise", { substitute = true }, importance = ActionImportance.SECONDARY)
        }
    } }
    if (substitute) ExercisePicker(vm, { substitute = false }) { e ->
        // Keep previously recorded results attached to their actual exercise.
        if (sets.isEmpty()) vm.save(row.copy(actualExerciseId = e.id,exerciseName="",trackingType=""))
        else vm.save(row.copy(id = newId(), actualExerciseId = e.id, position = row.position + 1, notes = "",exerciseName="",trackingType=""))
        substitute = false
    }
}

private fun draftFromSet(set:WorkoutSetEntity,hold:Boolean,weighted:Boolean,bands:List<String> = emptyList()):WorkoutEntryDraftEntity {
    val reps=set.reps ?: listOfNotNull(set.leftReps,set.rightReps).minOrNull()
    val holdMillis=set.holdMillis ?: listOfNotNull(set.leftHoldMillis,set.rightHoldMillis).minOrNull()
    val performance=when {
        hold -> holdMillis?.div(1000.0)?.toString().orEmpty()
        weighted -> "${set.weightKg ?: ""} x ${reps ?: ""}"
        else -> reps?.toString().orEmpty()
    }
    return emptyEntryDraft(set.workoutExerciseId,set.ordinal).copy(
        setId=set.id,
        performance=performance,
        rpe=set.rpe?.toString().orEmpty(),
        warmUp=set.setType=="WARM_UP",
        failed=set.result=="FAILED",
        bandIds=encodeBandIds(bands),
        variationId=set.variationId,
        leftValue=(if(hold)set.leftHoldMillis?.div(1000.0) else set.leftReps)?.toString().orEmpty(),
        rightValue=(if(hold)set.rightHoldMillis?.div(1000.0) else set.rightReps)?.toString().orEmpty(),
        addedWeight=set.addedWeightKg?.toString().orEmpty(),
        assistance=set.assistanceKg?.toString().orEmpty(),
        romValue=set.romValue?.toString().orEmpty(),
        romUnit=set.romUnit ?: "cm",
    )
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
