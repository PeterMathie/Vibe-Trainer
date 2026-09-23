package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petermathie.vibetrainer.data.local.*
import com.petermathie.vibetrainer.domain.programme.ExerciseInputConfig
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
            VibeActionButton(
                "Bodyweight: ${workout.bodyweightKg?.let(::formatBodyweight) ?: "—"} kg",
                { bodyweight = true },
                importance = ActionImportance.COMPACT,
            )
            if(workout.status=="FINISHED") VibeActionButton("Change workout date", { editDate=true }, importance = ActionImportance.COMPACT)
        }
        items(rows, key = { it.id }) { row ->
            val exercise = definitions.find { it.id == row.actualExerciseId }?.let { if(row.exerciseName.isNotBlank())it.copy(canonicalName=row.exerciseName,trackingType=row.trackingType) else it }
            val previousIds = snapshots.filter { it.actualExerciseId == row.actualExerciseId && it.workoutId != workout.id && workouts.any { w -> w.id == it.workoutId && w.status == "FINISHED" && (w.finishedAt ?: 0) < workout.startedAt } }.map { it.id }.toSet()
            val previousRow = sets.filter { it.workoutExerciseId in previousIds }.maxByOrNull { it.loggedAt }?.workoutExerciseId
            val previous = sets.filter { it.workoutExerciseId==previousRow }.sortedBy { it.ordinal }
            val restSeconds=if(row.supersetGroup==null)row.restSeconds else rows.filter { it.supersetGroup==row.supersetGroup }.maxOf { it.restSeconds }
            WorkoutExerciseCard(vm, row, exercise, sets.filter { it.workoutExerciseId == row.id }, previous, restSeconds)
        }
        item {
            VibeActionButton("Add exercise to workout", { add = true }, importance = ActionImportance.SECONDARY)
            Button(onClick = { onFinish(workout.id) }, modifier = Modifier.fillMaxWidth()) { Text(if (workout.status == "FINISHED") "Done editing" else "Finish workout") }
        }
    }
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
private fun WorkoutExerciseCard(vm: EditorViewModel, row: WorkoutExerciseEntity, exercise: ExerciseEntity?, sets: List<WorkoutSetEntity>, previous: List<WorkoutSetEntity>, restSeconds: Int) {
    val assignments by vm.setBands.collectAsStateWithLifecycle()
    var notes by rememberSaveable(row.id) { mutableStateOf(row.notes) }
    var substitute by remember { mutableStateOf(false) }
    var draftLoaded by remember(row.id) { mutableStateOf(false) }
    var expanded by rememberSaveable(row.id) { mutableStateOf(false) }
    var visibleRows by rememberSaveable(row.id) { mutableIntStateOf(0) }
    val drafts = remember(row.id) { mutableStateMapOf<Int, WorkoutEntryDraftEntity>() }
    val context = LocalContext.current
    val inputConfig = ExerciseInputConfig.decode(row.inputConfig, exercise?.trackingType.orEmpty())
    val hold = inputConfig.timeHeld
    val weighted = inputConfig.weightUnit != null
    val targetSets = row.targets.substringBefore(" sets").toIntOrNull()?.coerceAtLeast(1) ?: 3
    LaunchedEffect(row.id) {
        val recovered = vm.entryDrafts(row.id)
        sets.forEach { set ->
            drafts[set.ordinal] = draftFromSet(
                set,
                hold,
                inputConfig.weightUnit,
                assignments.filter { it.setId == set.id }.sortedBy { it.ordinal }.map { it.bandId },
            )
        }
        recovered.forEach { draft ->
            drafts[draft.ordinal] = draft.copy(detailsOpen = false)
        }
        visibleRows = maxOf(targetSets, sets.maxOfOrNull { it.ordinal } ?: 0, recovered.maxOfOrNull { it.ordinal } ?: 0)
        draftLoaded = true
    }
    fun draftAt(ordinal: Int) = drafts[ordinal] ?: emptyEntryDraft(row.id, ordinal).also { drafts[ordinal] = it }
    fun performanceParts(draft: WorkoutEntryDraftEntity): Pair<String,String> {
        if (!weighted) return "" to if (inputConfig.reps) draft.performance else ""
        val parts = draft.performance.split(Regex("\\s*[x×]\\s*"), limit = 2)
        return parts.getOrElse(0) { "" } to parts.getOrElse(1) { "" }
    }
    fun parsedSet(pending: WorkoutEntryDraftEntity): WorkoutSetEntity? {
        val (resistance, reps) = performanceParts(pending)
        val base = sets.find { it.ordinal == pending.ordinal }
            ?: emptySet(row.id, pending.ordinal).copy(id = pending.setId)
        val parsed = base.copy(
            weightKg = resistance.toDoubleOrNull()?.let {
                if (inputConfig.weightUnit == "lb") it / 2.2046226218 else it
            },
            reps = reps.toIntOrNull(),
            holdMillis = pending.timeHeld.toDoubleOrNull()?.times(1000)?.toLong(),
            timeUnderTensionMillis = pending.timeUnderTension.toDoubleOrNull()?.times(1000)?.toLong(),
            bandResistance = inputConfig.bandResistance,
            rpe = pending.rpe.toDoubleOrNull()?.takeIf { it in 0.0..10.0 },
            updatedAt = System.currentTimeMillis(),
        )
        if (
            parsed.weightKg == null &&
            parsed.reps == null &&
            parsed.holdMillis == null &&
            parsed.timeUnderTensionMillis == null &&
            !inputConfig.bandResistance
        ) return null
        return parsed
    }
    fun updateDraft(updated: WorkoutEntryDraftEntity) {
        drafts[updated.ordinal] = updated
        val parsed = parsedSet(updated)
        if (parsed == null) {
            vm.saveEntryDraft(updated)
            sets.find { it.ordinal == updated.ordinal }?.let { vm.removeSet(it.id) }
        } else {
            vm.submitEntryDraft(parsed, bandIds(updated.bandIds)) {}
        }
    }
    fun updatePerformance(ordinal: Int, resistance: String, result: String) {
        val performance = if (weighted) {
            if (resistance.isBlank() && result.isBlank()) "" else "$resistance x $result"
        } else result
        updateDraft(draftAt(ordinal).copy(performance = performance, updatedAt = System.currentTimeMillis()))
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
                        enabled=draftLoaded,
                        label={Text(inputConfig.weightUnit.orEmpty())},
                        singleLine=true,
                        modifier=Modifier.weight(1f).semantics { contentDescription="Resistance for ${exercise?.canonicalName.orEmpty()} set $ordinal" },
                    )
                    if (inputConfig.reps) OutlinedTextField(
                            result,
                            { updatePerformance(ordinal,resistance,it) },
                            enabled=draftLoaded,
                            label={Text("Reps")},
                            singleLine=true,
                            modifier=Modifier.weight(1f).semantics { contentDescription="Reps for ${exercise?.canonicalName.orEmpty()} set $ordinal" },
                        )
                    if (inputConfig.timeHeld) OutlinedTextField(
                        value = draft.timeHeld.ifBlank {
                            if (!inputConfig.reps && !weighted) draft.performance else ""
                        },
                        onValueChange = {
                            updateDraft(draft.copy(timeHeld = it, performance = "", updatedAt = System.currentTimeMillis()))
                        },
                        label = {
                            Text(
                                if (exercise?.canonicalName?.contains("Handstand", true) == true) "Freestanding s" else "Held s",
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f).semantics {
                            contentDescription = "Time held for ${exercise?.canonicalName.orEmpty()} set $ordinal"
                        },
                    )
                    if (inputConfig.timeUnderTension) OutlinedTextField(
                        value = draft.timeUnderTension,
                        onValueChange = {
                            updateDraft(draft.copy(timeUnderTension = it, updatedAt = System.currentTimeMillis()))
                        },
                        label = {
                            Text(
                                if (exercise?.canonicalName?.contains("Handstand", true) == true) "Total wall s" else "Tension s",
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f).semantics {
                            contentDescription = "Time under tension for ${exercise?.canonicalName.orEmpty()} set $ordinal"
                        },
                    )
                    OutlinedTextField(
                        draft.rpe,
                        {
                            val value = it.filter(Char::isDigit).take(2)
                            if (value.toIntOrNull()?.let { number -> number <= 10 } != false) {
                                updateDraft(draft.copy(rpe=value,updatedAt=System.currentTimeMillis()))
                            }
                        },
                        enabled=draftLoaded,
                        label={Text("RPE")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine=true,
                        modifier=Modifier.width(68.dp).semantics { contentDescription="RPE for ${exercise?.canonicalName.orEmpty()} set $ordinal" },
                    )
                }
                if (inputConfig.bandResistance) Text("Band resistance", style = MaterialTheme.typography.labelMedium)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
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
        FilledTonalButton(
            onClick={
                val ordinal = visibleRows + 1
                visibleRows = ordinal
                updateDraft(emptyEntryDraft(row.id, ordinal))
            },
            modifier=Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Add set for ${exercise?.canonicalName.orEmpty()}" },
        ) { Icon(Icons.Outlined.Add,contentDescription=null) }
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

private fun draftFromSet(set:WorkoutSetEntity,hold:Boolean,weightUnit:String?,bands:List<String> = emptyList()):WorkoutEntryDraftEntity {
    val reps=set.reps ?: listOfNotNull(set.leftReps,set.rightReps).minOrNull()
    val holdMillis=set.holdMillis ?: listOfNotNull(set.leftHoldMillis,set.rightHoldMillis).minOrNull()
    val performance=when {
        hold -> holdMillis?.div(1000.0)?.toString().orEmpty()
        weightUnit != null -> "${set.weightKg?.let { if (weightUnit == "lb") it * 2.2046226218 else it } ?: ""} x ${reps ?: ""}"
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
        timeHeld=set.holdMillis?.div(1000.0)?.toString().orEmpty(),
        timeUnderTension=set.timeUnderTensionMillis?.div(1000.0)?.toString().orEmpty(),
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
