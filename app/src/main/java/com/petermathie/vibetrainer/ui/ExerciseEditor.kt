package com.petermathie.vibetrainer.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petermathie.vibetrainer.data.local.*
import com.petermathie.vibetrainer.domain.model.TrackingType
import com.petermathie.vibetrainer.domain.programme.ExerciseInputConfig

@Composable
fun ExerciseEditor(vm: EditorViewModel) {
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    val muscles by vm.muscles.collectAsStateWithLifecycle()
    val mappings by vm.mappings.collectAsStateWithLifecycle()
    val aliases by vm.aliases.collectAsStateWithLifecycle()
    val variations by vm.variations.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<ExerciseEntity?>(null) }
    var configuring by remember { mutableStateOf<ExerciseEntity?>(null) }
    var variation by remember { mutableStateOf<String?>(null) }
    val matchingMuscles=muscles.filter { it.displayName.contains(query,true) }.map { it.id }.toSet()
    val matchingIds=mappings.filter { it.muscleId in matchingMuscles }.map { it.exerciseId }.toSet()+aliases.filter { it.alias.contains(query,true) }.map { it.exerciseId }
    LazyColumn(Modifier.fillMaxSize(), contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        item { Text("Exercises",style=MaterialTheme.typography.headlineSmall); EditField("Name, alias or muscle",query){query=it}; Button(onClick={selected=ExerciseEntity(newId(),"","STRENGTH","WEIGHT_REPS",null,null,"custom",true)}){Text("Custom exercise")} }
        items(exercises.filter { !it.isArchived && (it.canonicalName.contains(query,true)||it.id in matchingIds) },key={it.id}) { e ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                Text(e.canonicalName,style=MaterialTheme.typography.titleMedium)
                Text(mappings.filter { it.exerciseId==e.id }.joinToString { m -> "${muscles.find { it.id==m.muscleId }?.displayName} (${m.role.lowercase()})" },style=MaterialTheme.typography.bodySmall)
                Row {
                    VibeActionButton("Settings", { configuring=e }, importance = ActionImportance.COMPACT)
                    VibeActionButton(if(e.isCustom)"Edit" else "Duplicate", { selected=if(e.isCustom)e else e.copy(id=newId(),canonicalName=e.canonicalName+" (custom)",isCustom=true,source=e.id) }, importance = ActionImportance.COMPACT)
                    VibeActionButton("Add variation", { variation=e.id }, importance = ActionImportance.COMPACT)
                }
                if(e.isCustom) VibeActionButton("Archive", { vm.saveExercise(e.copy(isArchived=true),aliases.filter { it.exerciseId==e.id }.map { it.alias },mappings.filter { it.exerciseId==e.id }.associate { it.muscleId to it.role }) }, importance = ActionImportance.COMPACT)
                val seededVariations = variations.filter { it.exerciseId==e.id && it.isSeeded }.sortedBy { it.progressionRank }
                val customVariations = variations.filter { it.exerciseId==e.id && !it.isSeeded }.sortedBy { it.progressionRank }
                val variationOrder = rememberReorderState(customVariations.map { it.id }) { key, from, to ->
                    vm.moveVariation(key as String, to - from)
                }
                (seededVariations + variationOrder.ordered(customVariations) { it.id }).forEach { v ->
                    Row(Modifier.animateContentSize()) {
                        if (!v.isSeeded && customVariations.size > 1) ReorderHandle(variationOrder, v.id, v.name)
                        Text(v.name,Modifier.weight(1f))
                    }
                }
            } }
        }
    }
    variation?.let { id -> NameDialog("Variation name","",{variation=null}) { vm.save(ExerciseVariationEntity(newId(),id,it,(variations.filter { it.exerciseId==id }.maxOfOrNull { it.progressionRank } ?: 0)+1,false));variation=null } }
    configuring?.let { exercise ->
        ExerciseSettingsDialog(
            exercise = exercise,
            onDismiss = { configuring = null },
            onSave = {
                vm.saveExerciseSettings(it)
                configuring = null
            },
        )
    }
    selected?.let { e ->
        val source=if(e.source.startsWith("core:")||e.source.startsWith("free:"))e.source else e.id
        var name by remember(e.id){mutableStateOf(e.canonicalName)}
        var alias by remember(e.id){mutableStateOf(aliases.filter { it.exerciseId==source }.joinToString { it.alias })}
        var tag by remember(e.id){mutableStateOf(e.tag)}
        var tracking by remember(e.id){mutableStateOf(e.trackingType)}
        var roles by remember(e.id){mutableStateOf(mappings.filter { it.exerciseId==source }.associate { it.muscleId to it.role })}
        AlertDialog(onDismissRequest={selected=null},title={Text("Custom exercise")},text={LazyColumn { item {
            EditField("Name",name){name=it};EditField("Aliases separated by commas",alias){alias=it}
            listOf("STRENGTH","STRETCHING","BOTH").forEach { v -> TextButton(onClick={tag=v}){Text((if(tag==v)"✓ " else "")+v)} }
            TrackingType.entries.forEach { v -> TextButton(onClick={tracking=v.name}){Text((if(tracking==v.name)"✓ " else "")+v.name)} }
            Text("Tap muscle to cycle: none → primary → secondary")
            muscles.forEach { m -> TextButton(onClick={roles=when(roles[m.id]) { null -> roles+(m.id to "PRIMARY");"PRIMARY"->roles+(m.id to "SECONDARY");else->roles-m.id }}){Text("${m.displayName}: ${roles[m.id] ?: "none"}")} }
        } }},confirmButton={TextButton(enabled=name.isNotBlank()&&roles.isNotEmpty(),onClick={vm.saveExercise(e.copy(canonicalName=name,tag=tag,trackingType=tracking),alias.split(','),roles);selected=null}){Text("Save")}},dismissButton={TextButton(onClick={selected=null}){Text("Cancel")}})
    }
}

@Composable
internal fun ExerciseSettingsDialog(
        exercise: ExerciseEntity,
        onDismiss: () -> Unit,
        onSave: (ExerciseEntity) -> Unit,
    ) {
        var config by remember(exercise.id) {
            mutableStateOf(ExerciseInputConfig.decode(exercise.inputConfig, exercise.trackingType))
        }
        var sets by remember(exercise.id) { mutableStateOf(exercise.targetSets?.toString().orEmpty()) }
        var minimum by remember(exercise.id) { mutableStateOf(exercise.targetRepsMin?.toString().orEmpty()) }
        var maximum by remember(exercise.id) { mutableStateOf(exercise.targetRepsMax?.toString().orEmpty()) }
        var rpe by remember(exercise.id) { mutableStateOf(exercise.targetRpe?.toString().orEmpty()) }
        var rest by remember(exercise.id) { mutableStateOf(exercise.restSeconds.toString()) }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Exercise settings") },
            text = {
                LazyColumn {
                    item {
                        Text(exercise.canonicalName, style = MaterialTheme.typography.titleMedium)
                        Text("Resistance", style = MaterialTheme.typography.titleMedium)
                        ExerciseSettingCheckbox("Weight (kg)", config.weightUnit == "kg") {
                            config = config.copy(weightUnit = if (it) "kg" else null)
                        }
                        ExerciseSettingCheckbox("Weight (lb)", config.weightUnit == "lb") {
                            config = config.copy(weightUnit = if (it) "lb" else null)
                        }
                        ExerciseSettingCheckbox("Band resistance", config.bandResistance) {
                            config = config.copy(bandResistance = it)
                        }
                        ExerciseSettingCheckbox("Time held (seconds)", config.timeHeld) {
                            config = config.copy(timeHeld = it)
                        }
                        ExerciseSettingCheckbox("Time under tension (seconds)", config.timeUnderTension) {
                            config = config.copy(timeUnderTension = it)
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text("Reps", style = MaterialTheme.typography.titleMedium)
                        ExerciseSettingCheckbox("Track repetitions", config.reps) {
                            config = config.copy(reps = it)
                        }
                        EditField("Sets", sets) { sets = it }
                        EditField("Minimum target", minimum) { minimum = it }
                        EditField("Upper target", maximum) { maximum = it }
                        EditField("Target RPE", rpe) { rpe = it }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text("Rest", style = MaterialTheme.typography.titleMedium)
                        EditField("Rest seconds", rest) { rest = it }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSave(
                            exercise.copy(
                                inputConfig = config.encode(),
                                targetSets = sets.toIntOrNull(),
                                targetRepsMin = minimum.toIntOrNull(),
                                targetRepsMax = maximum.toIntOrNull(),
                                targetRpe = rpe.toDoubleOrNull(),
                                restSeconds = rest.toIntOrNull() ?: 120,
                            ),
                        )
                    },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        )
    }

@Composable
private fun ExerciseSettingCheckbox(
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.semantics { contentDescription = label },
            )
            Text(label)
    }
}
