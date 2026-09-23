package com.petermathie.vibetrainer.ui

import androidx.compose.animation.animateContentSize
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
            val fieldOrder = rememberReorderState(activeFields.map { it.id }) { key, from, to ->
                vm.moveTrackerField(key as String, to - from)
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(tracker.name, style = MaterialTheme.typography.titleLarge)
                    Row {
                        VibeActionButton("Rename", { edit = tracker }, importance = ActionImportance.COMPACT)
                        VibeActionButton("Add field", {
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
                        }, importance = ActionImportance.COMPACT)
                        VibeActionButton("Archive", { vm.save(tracker.copy(isArchived = true)) }, importance = ActionImportance.COMPACT)
                    }
                    fieldOrder.ordered(activeFields) { it.id }.forEach { habitField ->
                        Column(Modifier.animateContentSize()) {
                            HabitDailyInput(vm, habitField, values.find { it.fieldId == habitField.id && it.epochDay == epoch }, epoch)
                            TargetSummary(habitField)
                            Row {
                                if (activeFields.size > 1) ReorderHandle(fieldOrder, habitField.id, habitField.name)
                                VibeActionButton("Edit", { field = habitField }, importance = ActionImportance.COMPACT)
                                VibeActionButton("Archive", { vm.save(habitField.copy(isArchived = true)) }, importance = ActionImportance.COMPACT)
                                VibeActionButton("Clear", { epoch?.let { vm.clearValue(habitField.id, it) } }, importance = ActionImportance.COMPACT, enabled = epoch != null)
                            }
                        }
                    }
                    val archived = trackerFields.filter { it.isArchived }
                    if (archived.isNotEmpty()) {
                        Text("Archived fields", style = MaterialTheme.typography.labelLarge)
                        archived.forEach { archivedField ->
                            Row {
                                Text(archivedField.name, modifier = Modifier.weight(1f))
                                VibeActionButton("Restore", {
                                    vm.save(archivedField.copy(isArchived = false, position = activeFields.size))
                                }, importance = ActionImportance.COMPACT)
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
