package com.petermathie.vibetrainer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petermathie.vibetrainer.data.local.*
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.domain.programme.ProgrammeEntryForm

@Composable
fun ProgrammeEditor(
    vm: EditorViewModel,
    mode: TrainingMode,
    onModeChange: (TrainingMode) -> Unit,
    onStart: (String) -> Unit,
) {
    val programmes by vm.programmes.collectAsStateWithLifecycle()
    val days by vm.days.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var rename by remember { mutableStateOf<ProgrammeEntity?>(null) }
    var editDay by remember { mutableStateOf<ProgrammeDayEntity?>(null) }
    var editEntry by remember { mutableStateOf<ProgrammeExerciseEntity?>(null) }
    var addExerciseDayId by rememberSaveable { mutableStateOf<String?>(null) }
    val programmeRows = programmes.filter { it.mode == mode.name }.sortedBy { it.position }
    val selectedProgramme = programmes.find { it.id == selected }
    var programmeName by rememberSaveable(selectedProgramme?.id, selectedProgramme?.name) {
        mutableStateOf(selectedProgramme?.name.orEmpty())
    }
    val dayRows = days.filter { it.programmeId == selected }.sortedBy { it.position }
    val programmeOrder = rememberReorderState(programmeRows.map { it.id }) { key, from, to ->
        vm.moveProgramme(key as String, to - from)
    }
    val dayOrder = rememberReorderState(dayRows.map { it.id }) { key, from, to ->
        vm.moveDay(key as String, to - from)
    }
    BackHandler(selected != null) { selected = null }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                if (selected != null) VibeActionButton("Back", { selected = null }, importance = ActionImportance.COMPACT)
                Text(
                    if (selected == null) "Programmes" else "Edit programme",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (selected == null) ModeSelector(mode, onModeChange, Modifier.weight(1.45f))
            }
        }
        if (selected == null) {
            item { Button(onClick = { rename = ProgrammeEntity(newId(), "", mode.name, false) }) { Text("Create programme") } }
            items(programmeOrder.ordered(programmeRows) { it.id }, key = { it.id }) { p ->
                Card(Modifier.fillMaxWidth().animateItem()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        if (programmeRows.size > 1) ReorderHandle(programmeOrder, p.id, p.name)
                        Text(p.name, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { selected = p.id }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit programme ${p.name}")
                        }
                        IconButton(onClick = { selected = p.id }) {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = "Choose workout in ${p.name}")
                        }
                    }
                }
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = programmeName,
                        onValueChange = { programmeName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Programme name") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { selectedProgramme?.let { vm.save(it.copy(name = programmeName.trim())) } },
                                enabled = programmeName.isNotBlank() && programmeName.trim() != selectedProgramme?.name,
                            ) { Icon(Icons.Outlined.Check, "Save programme name") }
                        },
                    )
                    VibeActionButton(
                        "Add workout",
                        { editDay = ProgrammeDayEntity(newId(), selected!!, "", dayRows.size) },
                        importance = ActionImportance.PRIMARY,
                    )
                }
            }
            items(dayOrder.ordered(dayRows) { it.id }, key = { it.id }) { d ->
                val dayEntries = entries.filter { it.programmeDayId == d.id }.sortedBy { it.position }
                val entryOrder = rememberReorderState(dayEntries.map { it.id }) { key, from, to ->
                    vm.moveEntry(key as String, to - from)
                }
                Card(Modifier.fillMaxWidth().animateItem()) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (dayRows.size > 1) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                ReorderHandle(dayOrder, d.id, d.name)
                                Text(d.name, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                            }
                        }
                        VibeActionButton(
                            "Start workout",
                            { onStart(d.id) },
                            modifier = Modifier.fillMaxWidth(),
                            importance = ActionImportance.PRIMARY,
                            icon = Icons.Outlined.PlayArrow,
                        )
                        if (dayEntries.isEmpty()) {
                            Text("No exercises yet", style = MaterialTheme.typography.bodyMedium)
                        }
                        entryOrder.ordered(dayEntries) { it.id }.forEach { entry ->
                            val exerciseName = exercises.find { it.id == entry.exerciseId }?.canonicalName.orEmpty()
                            Column(Modifier.fillMaxWidth().animateContentSize()) {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    if (dayEntries.size > 1) {
                                        ReorderHandle(entryOrder, entry.id, exerciseName)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(exerciseName, style = MaterialTheme.typography.titleMedium)
                                        Text(targetSummary(entry), style = MaterialTheme.typography.bodySmall)
                                        entry.supersetGroup?.let { Text("Circuit: $it", style = MaterialTheme.typography.bodySmall) }
                                    }
                                    IconButton(onClick = { editEntry = entry }) {
                                        Icon(Icons.Outlined.Edit, contentDescription = "Edit targets for $exerciseName")
                                    }
                                    IconButton(onClick = { vm.removeEntry(entry.id) }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Remove $exerciseName")
                                    }
                                }
                            }
                        }
                        Button(
                            onClick = { addExerciseDayId = d.id },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Icon(Icons.Outlined.Add, contentDescription = "Add exercise") }
                    }
                }
            }
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        VibeActionButton("Duplicate", { selectedProgramme?.let(vm::duplicate) }, modifier = Modifier.weight(1f), importance = ActionImportance.COMPACT)
                        VibeActionButton(
                            "Delete",
                            { selectedProgramme?.let { vm.removeProgramme(it.id); selected = null } },
                            modifier = Modifier.weight(1f),
                            importance = ActionImportance.COMPACT,
                        )
                        VibeActionButton(
                            "Archive",
                            { selectedProgramme?.let { vm.save(it.copy(isArchived = true)); selected = null } },
                            modifier = Modifier.weight(1f),
                            importance = ActionImportance.COMPACT,
                        )
                    }
                }
            }
        }
    rename?.let { p -> NameDialog("Programme name", p.name, { rename = null }) { vm.save(p.copy(name = it)); rename = null } }
    editDay?.let { d -> NameDialog("Workout name", d.name, { editDay = null }) { vm.save(d.copy(name = it)); editDay = null } }
    editEntry?.let { e -> EntryDialog(e, { editEntry = null }) { vm.save(it); editEntry = null } }
    addExerciseDayId?.let { targetDayId ->
        ExercisePicker(vm, { addExerciseDayId = null }) { exercise ->
            vm.save(
                ProgrammeExerciseEntity(
                    newId(),
                    targetDayId,
                    exercise.id,
                    entries.count { it.programmeDayId == targetDayId },
                    3,
                    null,
                    null,
                    null,
                    120,
                    null,
                    "",
                    null,
                ),
            )
            addExerciseDayId = null
        }
    }
}

private fun targetSummary(entry: ProgrammeExerciseEntity): String {
    val target = entry.targetHoldSeconds?.let { "$it sec" }
        ?: when {
            entry.targetRepsMin != null && entry.targetRepsMax != null -> "${entry.targetRepsMin}–${entry.targetRepsMax} reps"
            entry.targetRepsMin != null -> "${entry.targetRepsMin} reps"
            else -> "reps not set"
        }
    return "${entry.targetSets ?: 3} sets · $target · ${entry.restSeconds}s rest"
}

@Composable
fun NameDialog(title: String, initial: String, onDismiss: () -> Unit, allowEmpty: Boolean = false, onSave: (String) -> Unit) {
    var name by remember(initial) { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { OutlinedTextField(name, { name = it }, singleLine = !allowEmpty) }, confirmButton = { TextButton(onClick = { onSave(name.trim()) }, enabled = allowEmpty || name.isNotBlank()) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
fun ExercisePicker(vm: EditorViewModel, onDismiss: () -> Unit, onChoose: (ExerciseEntity) -> Unit) {
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    val aliases by vm.aliases.collectAsStateWithLifecycle()
    val mappings by vm.mappings.collectAsStateWithLifecycle()
    val muscles by vm.muscles.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val aliasIds = aliases.filter { it.alias.contains(query, true) }.map { it.exerciseId }.toSet()
    val muscleIds = muscles.filter { it.displayName.contains(query, true) }.map { it.id }.toSet()
    val mappedIds = mappings.filter { it.muscleId in muscleIds }.map { it.exerciseId }.toSet()
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Choose exercise") }, text = {
        Column { OutlinedTextField(query, { query = it }, label = { Text("Name, alias or muscle") })
            LazyColumn(Modifier.heightIn(max = 420.dp)) { items(exercises.filter { !it.isArchived && (it.canonicalName.contains(query, true) || it.id in aliasIds || it.id in mappedIds) }.take(100)) { e ->
                Text(e.canonicalName, Modifier.fillMaxWidth().clickable { onChoose(e) }.padding(vertical = 14.dp))
            } }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}

@Composable
private fun EntryDialog(e: ProgrammeExerciseEntity, onDismiss: () -> Unit, onSave: (ProgrammeExerciseEntity) -> Unit) {
    var form by remember(e) { mutableStateOf(ProgrammeEntryForm.from(e)) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Targets and rest") }, text = {
        LazyColumn { item {
            EditField("Sets", form.sets) { form = form.copy(sets = it) }
            EditField("Reps minimum", form.minimumReps) { form = form.copy(minimumReps = it) }
            EditField("Reps maximum", form.maximumReps) { form = form.copy(maximumReps = it) }
            EditField("Hold seconds", form.holdSeconds) { form = form.copy(holdSeconds = it) }
            EditField("Rest seconds", form.restSeconds) { form = form.copy(restSeconds = it) }
            EditField("Target RPE", form.targetRpe) { form = form.copy(targetRpe = it) }
            EditField("Circuit/group name (optional)", form.group) { form = form.copy(group = it) }
            EditField("Exercise notes", form.notes) { form = form.copy(notes = it) }
        } }
    }, confirmButton = { TextButton(onClick = { onSave(form.applyTo(e)) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
fun EditField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = label }, singleLine = true)
}
