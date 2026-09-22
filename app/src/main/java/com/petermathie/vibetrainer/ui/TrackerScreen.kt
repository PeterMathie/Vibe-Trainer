package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petermathie.vibetrainer.data.local.*
import java.time.LocalDate

@Composable
fun TrackerScreen(vm: EditorViewModel) {
    val trackers by vm.trackers.collectAsStateWithLifecycle()
    val fields by vm.fields.collectAsStateWithLifecycle()
    val values by vm.values.collectAsStateWithLifecycle()
    var day by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val epoch = runCatching { LocalDate.parse(day).toEpochDay() }.getOrNull()
    var edit by remember { mutableStateOf<TrackerEntity?>(null) }
    var field by remember { mutableStateOf<TrackerFieldEntity?>(null) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Habits", style = MaterialTheme.typography.headlineSmall)
            EditField("Date (YYYY-MM-DD)", day) { day = it }
            Button(onClick = { edit = TrackerEntity(newId(), "", false) }) { Text("New habit") }
        }
        items(trackers, key = { it.id }) { tracker ->
            val trackerFields = fields.filter { it.trackerId == tracker.id }
            val activeFields = trackerFields.filterNot { it.isArchived }.sortedBy { it.position }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(tracker.name, style = MaterialTheme.typography.titleLarge)
                    Row {
                        TextButton(onClick = { edit = tracker }) { Text("Rename") }
                        TextButton(onClick = {
                            field = TrackerFieldEntity(
                                newId(),
                                tracker.id,
                                "",
                                "NUMBER",
                                null,
                                null,
                                null,
                                activeFields.size,
                            )
                        }) { Text("Add field") }
                        TextButton(onClick = { vm.save(tracker.copy(isArchived = true)) }) { Text("Archive") }
                    }
                    activeFields.forEachIndexed { index, habitField ->
                        HabitDailyInput(vm, habitField, values.find { it.fieldId == habitField.id && it.epochDay == epoch }, epoch)
                        TargetSummary(habitField)
                        Row {
                            TextButton(onClick = { field = habitField }) { Text("Edit") }
                            TextButton(enabled = index > 0, onClick = { vm.moveTrackerField(habitField.id, -1) }) { Text("↑") }
                            TextButton(enabled = index < activeFields.lastIndex, onClick = { vm.moveTrackerField(habitField.id, 1) }) { Text("↓") }
                            TextButton(onClick = { vm.save(habitField.copy(isArchived = true)) }) { Text("Archive") }
                            TextButton(enabled = epoch != null, onClick = { epoch?.let { vm.clearValue(habitField.id, it) } }) { Text("Clear day") }
                        }
                    }
                    val archived = trackerFields.filter { it.isArchived }
                    if (archived.isNotEmpty()) {
                        Text("Archived fields", style = MaterialTheme.typography.labelLarge)
                        archived.forEach { archivedField ->
                            Row {
                                Text(archivedField.name, modifier = Modifier.weight(1f))
                                TextButton(onClick = {
                                    vm.save(archivedField.copy(isArchived = false, position = activeFields.size))
                                }) { Text("Restore") }
                            }
                        }
                    }
                }
            }
        }
    }
    edit?.let { tracker ->
        NameDialog("Habit name", tracker.name, { edit = null }) {
            if (trackers.none { existing -> existing.id == tracker.id }) vm.createTracker(tracker.copy(name = it))
            else vm.save(tracker.copy(name = it))
            edit = null
        }
    }
    field?.let { HabitFieldDialog(vm, it) { field = null } }
}
