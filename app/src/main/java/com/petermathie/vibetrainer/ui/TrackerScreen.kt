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
            Text("Habits",style = MaterialTheme.typography.headlineSmall)
            EditField("Date (YYYY-MM-DD)",day) { day=it }
            Button(onClick = { edit=TrackerEntity(newId(),"",false) }) { Text("New habit") }
        }
        items(trackers, key = { it.id }) { tracker -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
            Text(tracker.name,style = MaterialTheme.typography.titleLarge)
            Row {
                TextButton(onClick = { edit=tracker }) { Text("Rename") }
                TextButton(onClick = { field=TrackerFieldEntity(newId(),tracker.id,"","NUMBER",null,null,null,fields.count { it.trackerId == tracker.id }) }) { Text("Add field") }
                TextButton(onClick = { vm.save(tracker.copy(isArchived=true)) }) { Text("Archive") }
            }
            fields.filter { it.trackerId==tracker.id }.forEach { f ->
                val value=values.find { it.fieldId==f.id && it.epochDay==epoch }
                key(f.id,epoch,value?.updatedAt) {
                    var text by remember { mutableStateOf(value?.numericValue?.toString() ?: value?.textValue.orEmpty()) }
                    if(f.valueType=="BOOLEAN") Row {
                        Checkbox(value?.booleanValue==true,{ checked -> epoch?.let { vm.save(TrackerDailyValueEntity(f.id,it,null,checked,null,"",System.currentTimeMillis())) } })
                        Text(f.name)
                    } else {
                        EditField("${f.name}${f.unit?.let { " ($it)" }.orEmpty()}",text) { text=it }
                        TextButton(enabled=epoch!=null && text.isNotBlank(), onClick = { epoch?.let { d -> vm.save(TrackerDailyValueEntity(f.id,d,if(f.valueType in listOf("NUMBER","COUNT","DURATION","RATING")) text.toDoubleOrNull() else null,null,if(f.valueType in listOf("TEXT","CHOICE","DATETIME")) text else null,"",System.currentTimeMillis())) } }) { Text("Save daily total") }
                    }
                    if(f.targetValue!=null) Text("Target: ${f.targetComparison} ${f.targetValue} ${f.unit.orEmpty()}")
                    Row { TextButton(onClick={field=f}) { Text("Edit field") }; TextButton(enabled=epoch!=null,onClick={epoch?.let { vm.clearValue(f.id,it) }}) { Text("Clear day") } }
                }
            }
        } } }
    }
    edit?.let { t -> NameDialog("Habit name",t.name,{edit=null}) { if(trackers.none { existing -> existing.id==t.id }) vm.createTracker(t.copy(name=it)) else vm.save(t.copy(name=it)); edit=null } }
    field?.let { f ->
        var name by remember(f.id) { mutableStateOf(f.name) }
        var type by remember(f.id) { mutableStateOf(f.valueType) }
        var unit by remember(f.id) { mutableStateOf(f.unit.orEmpty()) }
        var target by remember(f.id) { mutableStateOf(f.targetValue?.toString().orEmpty()) }
        var comparison by remember(f.id) { mutableStateOf(f.targetComparison ?: "AT_LEAST") }
        AlertDialog(onDismissRequest={field=null},title={Text("Habit field")},text={LazyColumn { item {
            EditField("Name",name){name=it}; EditField("Unit (minutes, grams, pages…)",unit){unit=it}
            listOf("BOOLEAN","NUMBER","COUNT","DURATION","RATING","TEXT","CHOICE","DATETIME").forEach { v -> TextButton(onClick={type=v}) { Text((if(type==v) "✓ " else "")+v.lowercase()) } }
            EditField("Optional target",target){target=it}
            listOf("AT_LEAST","AT_MOST","EXACTLY").forEach { v -> TextButton(onClick={comparison=v}) { Text((if(comparison==v) "✓ " else "")+v) } }
        } }},confirmButton={TextButton(enabled=name.isNotBlank(),onClick={vm.save(f.copy(name=name,valueType=type,unit=unit.takeIf { it.isNotBlank() },targetValue=target.toDoubleOrNull(),targetComparison=if(target.toDoubleOrNull()==null) null else comparison));field=null}){Text("Save")}},dismissButton={TextButton(onClick={field=null}){Text("Cancel")}})
    }
}
