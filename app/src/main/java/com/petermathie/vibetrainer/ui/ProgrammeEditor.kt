package com.petermathie.vibetrainer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun ProgrammeEditor(vm: EditorViewModel, mode: TrainingMode, onStart: (String) -> Unit) {
    val programmes by vm.programmes.collectAsStateWithLifecycle()
    val days by vm.days.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var dayId by rememberSaveable { mutableStateOf<String?>(null) }
    var rename by remember { mutableStateOf<ProgrammeEntity?>(null) }
    var editDay by remember { mutableStateOf<ProgrammeDayEntity?>(null) }
    var editEntry by remember { mutableStateOf<ProgrammeExerciseEntity?>(null) }
    var addExercise by rememberSaveable { mutableStateOf(false) }
    val programmeRows = programmes.filter { it.mode == mode.name }.sortedBy { it.position }
    val dayRows = days.filter { it.programmeId == selected }.sortedBy { it.position }
    val entryRows = entries.filter { it.programmeDayId == dayId }.sortedBy { it.position }
    val programmeOrder = rememberReorderState(programmeRows.map { it.id }) { key, from, to ->
        vm.moveProgramme(key as String, to - from)
    }
    val dayOrder = rememberReorderState(dayRows.map { it.id }) { key, from, to ->
        vm.moveDay(key as String, to - from)
    }
    val entryOrder = rememberReorderState(entryRows.map { it.id }) { key, from, to ->
        vm.moveEntry(key as String, to - from)
    }
    BackHandler(selected != null) { if (dayId != null) dayId = null else selected = null }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row {
                if (selected != null) VibeActionButton("Back", { if (dayId != null) dayId = null else selected = null }, importance = ActionImportance.COMPACT)
                Text(if (dayId != null) days.find { it.id == dayId }?.name.orEmpty() else programmes.find { it.id == selected }?.name ?: "Programmes", style = MaterialTheme.typography.headlineSmall)
            }
        }
        if (selected == null) {
            item { Button(onClick = { rename = ProgrammeEntity(newId(), "", mode.name, false) }) { Text("Create programme") } }
            items(programmeOrder.ordered(programmeRows) { it.id }, key = { it.id }) { p ->
                Card(Modifier.fillMaxWidth().animateItem()) { Column(Modifier.padding(12.dp)) {
                    VibeActionButton(p.name, { selected = p.id }, modifier = Modifier.fillMaxWidth(), importance = ActionImportance.PRIMARY)
                    Row {
                        VibeActionButton("Rename", { rename = p }, importance = ActionImportance.COMPACT)
                        VibeActionButton("Duplicate", { vm.duplicate(p) }, importance = ActionImportance.COMPACT)
                        VibeActionButton("Archive", { vm.save(p.copy(isArchived = true)) }, importance = ActionImportance.COMPACT)
                        ReorderHandle(programmeOrder, p.id, p.name)
                    }
                } }
            }
        } else if (dayId == null) {
            item { Button(onClick = { editDay = ProgrammeDayEntity(newId(), selected!!, "", days.count { it.programmeId == selected }) }) { Text("Add day") } }
            items(dayOrder.ordered(dayRows) { it.id }, key = { it.id }) { d ->
                Card(Modifier.fillMaxWidth().animateItem()) { Column(Modifier.padding(12.dp)) {
                    Text(d.name, style = MaterialTheme.typography.titleLarge)
                    Row {
                        VibeActionButton("Exercises", { dayId = d.id }, importance = ActionImportance.PRIMARY)
                        VibeActionButton("Start", { onStart(d.id) }, importance = ActionImportance.SECONDARY)
                        VibeActionButton("Rename", { editDay = d }, importance = ActionImportance.COMPACT)
                        ReorderHandle(dayOrder, d.id, d.name)
                        VibeActionButton("Delete", { vm.removeDay(d.id) }, importance = ActionImportance.COMPACT)
                    }
                } }
            }
        } else {
            item { Button(onClick = { addExercise = true }) { Text("Add exercise") } }
            items(entryOrder.ordered(entryRows) { it.id }, key = { it.id }) { e ->
                Card(Modifier.fillMaxWidth().animateItem()) { Column(Modifier.padding(12.dp)) {
                    Text(exercises.find { it.id == e.exerciseId }?.canonicalName.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    Text("${e.targetSets ?: 3} sets · ${e.targetRepsMin ?: e.targetHoldSeconds ?: 0}${if (e.targetHoldSeconds != null) " sec" else " reps"} · ${e.restSeconds}s rest")
                    e.supersetGroup?.let { Text("Circuit: $it") }
                    Row {
                        VibeActionButton("Targets", { editEntry = e }, importance = ActionImportance.COMPACT)
                        ReorderHandle(entryOrder, e.id, exercises.find { it.id == e.exerciseId }?.canonicalName.orEmpty())
                        VibeActionButton("Remove", { vm.removeEntry(e.id) }, importance = ActionImportance.COMPACT)
                    }
                } }
            }
        }
    }
    rename?.let { p -> NameDialog("Programme name", p.name, { rename = null }) { vm.save(p.copy(name = it)); rename = null } }
    editDay?.let { d -> NameDialog("Day name", d.name, { editDay = null }) { vm.save(d.copy(name = it)); editDay = null } }
    editEntry?.let { e -> EntryDialog(e, { editEntry = null }) { vm.save(it); editEntry = null } }
    if (addExercise) ExercisePicker(vm, { addExercise = false }) { e ->
        vm.save(ProgrammeExerciseEntity(newId(), dayId!!, e.id, entries.count { it.programmeDayId == dayId }, 3, null, null, null, 120, null, "", null))
        addExercise = false
    }
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
