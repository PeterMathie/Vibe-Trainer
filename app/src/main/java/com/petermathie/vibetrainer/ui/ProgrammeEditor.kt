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
    BackHandler(selected != null) { if (dayId != null) dayId = null else selected = null }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row {
                if (selected != null) TextButton(onClick = { if (dayId != null) dayId = null else selected = null }) { Text("Back") }
                Text(if (dayId != null) days.find { it.id == dayId }?.name.orEmpty() else programmes.find { it.id == selected }?.name ?: "Programmes", style = MaterialTheme.typography.headlineSmall)
            }
        }
        if (selected == null) {
            item { Button(onClick = { rename = ProgrammeEntity(newId(), "", mode.name, false) }) { Text("Create programme") } }
            items(programmes.filter { it.mode == mode.name }, key = { it.id }) { p ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                    TextButton(onClick = { selected = p.id }) { Text(p.name) }
                    Row {
                        TextButton(onClick = { rename = p }) { Text("Rename") }
                        TextButton(onClick = { vm.duplicate(p) }) { Text("Duplicate") }
                        TextButton(onClick = { vm.save(p.copy(isArchived = true)) }) { Text("Archive") }
                    }
                    Row { TextButton(onClick={vm.moveProgramme(p.id,-1)}){Text("Move up")};TextButton(onClick={vm.moveProgramme(p.id,1)}){Text("Move down")} }
                } }
            }
        } else if (dayId == null) {
            item { Button(onClick = { editDay = ProgrammeDayEntity(newId(), selected!!, "", days.count { it.programmeId == selected }) }) { Text("Add day") } }
            val siblings = days.filter { it.programmeId == selected }.sortedBy { it.position }
            items(siblings, key = { it.id }) { d ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                    Text(d.name, style = MaterialTheme.typography.titleLarge)
                    Row {
                        TextButton(onClick = { dayId = d.id }) { Text("Exercises") }
                        TextButton(onClick = { onStart(d.id) }) { Text("Start") }
                        TextButton(onClick = { editDay = d }) { Text("Rename") }
                    }
                    Row {
                        TextButton(onClick = { vm.moveDay(d.id,-1) }, enabled = siblings.firstOrNull()?.id != d.id) { Text("Move up") }
                        TextButton(onClick = { vm.moveDay(d.id,1) }, enabled = siblings.lastOrNull()?.id != d.id) { Text("Down") }
                        TextButton(onClick = { vm.removeDay(d.id) }) { Text("Delete day") }
                    }
                } }
            }
        } else {
            item { Button(onClick = { addExercise = true }) { Text("Add exercise") } }
            val siblings = entries.filter { it.programmeDayId == dayId }.sortedBy { it.position }
            items(siblings, key = { it.id }) { e ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                    Text(exercises.find { it.id == e.exerciseId }?.canonicalName.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    Text("${e.targetSets ?: 3} sets · ${e.targetRepsMin ?: e.targetHoldSeconds ?: 0}${if (e.targetHoldSeconds != null) " sec" else " reps"} · ${e.restSeconds}s rest")
                    e.supersetGroup?.let { Text("Circuit: $it") }
                    Row {
                        TextButton(onClick = { editEntry = e }) { Text("Targets") }
                        TextButton(onClick = { vm.moveEntry(e.id,-1) }, enabled = siblings.firstOrNull()?.id != e.id) { Text("Up") }
                        TextButton(onClick = { vm.moveEntry(e.id,1) }, enabled = siblings.lastOrNull()?.id != e.id) { Text("Down") }
                        TextButton(onClick = { vm.removeEntry(e.id) }) { Text("Remove") }
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
    var sets by remember { mutableStateOf(e.targetSets?.toString().orEmpty()) }
    var reps by remember { mutableStateOf(e.targetRepsMin?.toString().orEmpty()) }
    var maxReps by remember { mutableStateOf(e.targetRepsMax?.toString().orEmpty()) }
    var hold by remember { mutableStateOf(e.targetHoldSeconds?.toString().orEmpty()) }
    var rest by remember { mutableStateOf(e.restSeconds.toString()) }
    var rpe by remember { mutableStateOf(e.targetRpe?.toString().orEmpty()) }
    var group by remember { mutableStateOf(e.supersetGroup.orEmpty()) }
    var notes by remember { mutableStateOf(e.notes) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Targets and rest") }, text = {
        LazyColumn { item {
            EditField("Sets", sets) { sets = it }; EditField("Reps minimum", reps) { reps = it }; EditField("Reps maximum", maxReps) { maxReps = it }
            EditField("Hold seconds", hold) { hold = it }; EditField("Rest seconds", rest) { rest = it }; EditField("Target RPE", rpe) { rpe = it }
            EditField("Circuit/group name (optional)", group) { group = it }; EditField("Exercise notes", notes) { notes = it }
        } }
    }, confirmButton = { TextButton(onClick = { onSave(e.copy(targetSets = sets.toIntOrNull(), targetRepsMin = reps.toIntOrNull(), targetRepsMax = maxReps.toIntOrNull(), targetHoldSeconds = hold.toIntOrNull(), restSeconds = rest.toIntOrNull()?.coerceAtLeast(0) ?: 120, targetRpe = rpe.toDoubleOrNull()?.coerceIn(0.0,10.0), supersetGroup = group.takeIf { it.isNotBlank() }, notes = notes)) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
fun EditField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = label }, singleLine = true)
}
