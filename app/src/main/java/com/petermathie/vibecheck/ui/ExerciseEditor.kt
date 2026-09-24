package com.petermathie.vibecheck.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import java.io.File
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petermathie.vibecheck.data.local.*
import com.petermathie.vibecheck.domain.model.TrackingType
import com.petermathie.vibecheck.domain.programme.ExerciseInputConfig

@Composable
fun ExerciseEditor(vm: EditorViewModel) {
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    val muscles by vm.muscles.collectAsStateWithLifecycle()
    val mappings by vm.mappings.collectAsStateWithLifecycle()
    val aliases by vm.aliases.collectAsStateWithLifecycle()
    val variations by vm.variations.collectAsStateWithLifecycle()
    val videos by vm.referenceVideos.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<ExerciseEntity?>(null) }
    var configuring by remember { mutableStateOf<ExerciseEntity?>(null) }
    var configuringVariation by remember { mutableStateOf<ExerciseVariationEntity?>(null) }
    var videoExerciseId by remember { mutableStateOf<String?>(null) }
    var playingVideo by remember { mutableStateOf<ExerciseReferenceVideoEntity?>(null) }
    var deletingVideo by remember { mutableStateOf<ExerciseReferenceVideoEntity?>(null) }
    var variation by remember { mutableStateOf<String?>(null) }
    var exerciseType by remember { mutableStateOf("STRENGTH") }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val exerciseId = videoExerciseId
        if (uri != null && exerciseId != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/').orEmpty()
            vm.attachReferenceVideo(context, exerciseId, uri, name)
        }
        videoExerciseId = null
    }
    val matchingMuscles=muscles.filter { it.displayName.contains(query,true) }.map { it.id }.toSet()
    val matchingIds=mappings.filter { it.muscleId in matchingMuscles }.map { it.exerciseId }.toSet()+aliases.filter { it.alias.contains(query,true) }.map { it.exerciseId }
    val filteredExercises = exercises.filter {
        !it.isArchived && (it.canonicalName.contains(query, true) || it.id in matchingIds)
    }
    val visibleExercises = filteredExercises.filter {
        it.tag == exerciseType || it.tag == "BOTH"
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Exercises",style=MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                SingleChoiceSegmentedButtonRow {
                    listOf("STRENGTH" to "Strength", "STRETCHING" to "Stretch").forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = exerciseType == option.first,
                            onClick = { exerciseType = option.first },
                            shape = SegmentedButtonDefaults.itemShape(index, 2),
                        ) { Text(option.second) }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EditField("Name, alias or muscle", query, Modifier.weight(1f)) { query = it }
                IconButton(
                    onClick = {
                        selected = ExerciseEntity(newId(), "", exerciseType, "WEIGHT_REPS", null, null, "custom", true)
                    },
                    modifier = Modifier.semantics { contentDescription = "Add custom exercise" },
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                }
            }
        }
        items(visibleExercises,key={it.id}) { e ->
            VibeCard {
                Text(e.canonicalName,style=MaterialTheme.typography.titleMedium)
                listOf("PRIMARY" to "Primary", "SECONDARY" to "Secondary").forEach { (role, label) ->
                    val names = mappings.filter { it.exerciseId == e.id && it.role == role }
                        .mapNotNull { mapping -> muscles.find { it.id == mapping.muscleId }?.displayName }
                    if (names.isNotEmpty()) Text(
                        buildAnnotatedString {
                            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            append("$label  ")
                            pop()
                            append(names.joinToString())
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VibeActionButton("Settings", { configuring=e }, modifier = Modifier.weight(1f), importance = ActionImportance.SECONDARY)
                    VibeActionButton(if(e.isCustom)"Edit" else "Duplicate", { selected=if(e.isCustom)e else e.copy(id=newId(),canonicalName=e.canonicalName+" (custom)",isCustom=true,source=e.id) }, modifier = Modifier.weight(1f), importance = ActionImportance.SECONDARY)
                    VibeActionButton("Variation", { variation=e.id }, modifier = Modifier.weight(1f), importance = ActionImportance.SECONDARY)
                }
                if(e.isCustom) VibeActionButton("Archive", { vm.saveExercise(e.copy(isArchived=true),aliases.filter { it.exerciseId==e.id }.map { it.alias },mappings.filter { it.exerciseId==e.id }.associate { it.muscleId to it.role }) }, importance = ActionImportance.SECONDARY)
                val seededVariations = variations.filter { it.exerciseId==e.id && it.isSeeded }.sortedBy { it.progressionRank }
                val customVariations = variations.filter { it.exerciseId==e.id && !it.isSeeded }.sortedBy { it.progressionRank }
                val variationOrder = rememberReorderState(customVariations.map { it.id }) { key, from, to ->
                    vm.moveVariation(key as String, to - from)
                }
                (seededVariations + variationOrder.ordered(customVariations) { it.id }).forEach { v ->
                    Row(Modifier.animateContentSize()) {
                        if (!v.isSeeded && customVariations.size > 1) ReorderHandle(variationOrder, v.id, v.name)
                        Text(v.name,Modifier.weight(1f))
                        IconButton(onClick = { configuringVariation = v }) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Settings for ${v.name}")
                        }
                    }
                }
                videos.filter { it.exerciseId == e.id }.forEach { video ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(video.displayName, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        IconButton(onClick = { playingVideo = video }) {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = "Play ${video.displayName}")
                        }
                        IconButton(onClick = { deletingVideo = video }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete ${video.displayName}")
                        }
                    }
                }
                VibeActionButton(
                    "Attach reference video",
                    { videoExerciseId = e.id; videoPicker.launch(arrayOf("video/*")) },
                    Modifier.fillMaxWidth(),
                    ActionImportance.SECONDARY,
                )
            }
        }
    }
    variation?.let { id ->
        val parent = exercises.find { it.id == id }
        NameDialog("Variation name","",{variation=null}) {
            vm.save(
                ExerciseVariationEntity(
                    id = newId(),
                    exerciseId = id,
                    name = it,
                    progressionRank = (variations.filter { row -> row.exerciseId == id }.maxOfOrNull { row -> row.progressionRank } ?: 0) + 1,
                    isSeeded = false,
                    trackingType = parent?.trackingType.orEmpty(),
                    inputConfig = parent?.inputConfig.orEmpty(),
                    targetSets = parent?.targetSets,
                    targetRepsMin = parent?.targetRepsMin,
                    targetRepsMax = parent?.targetRepsMax,
                    targetRpe = parent?.targetRpe,
                    restSeconds = parent?.restSeconds ?: 120,
                ),
            )
            variation=null
        }
    }
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
    configuringVariation?.let { row ->
        val parent = exercises.find { it.id == row.exerciseId }
        VariationSettingsDialog(row, parent, { configuringVariation = null }) {
            vm.save(it)
            configuringVariation = null
        }
    }
    playingVideo?.let { row ->
        AlertDialog(
            onDismissRequest = { playingVideo = null },
            title = { Text(row.displayName) },
            text = {
                AndroidView(
                    factory = { viewContext ->
                        VideoView(viewContext).apply {
                            setVideoPath(File(File(context.filesDir, "exercise-reference-videos"), row.fileName).path)
                            setOnPreparedListener { it.isLooping = true; start() }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(320.dp),
                )
            },
            confirmButton = { TextButton(onClick = { playingVideo = null }) { Text("Close") } },
        )
    }
    deletingVideo?.let { row ->
        AlertDialog(
            onDismissRequest = { deletingVideo = null },
            title = { Text("Delete reference video?") },
            text = { Text(row.displayName) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteReferenceVideo(context, row)
                    deletingVideo = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deletingVideo = null }) { Text("Cancel") } },
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
private fun VariationSettingsDialog(
        variation: ExerciseVariationEntity,
        parent: ExerciseEntity?,
        onDismiss: () -> Unit,
        onSave: (ExerciseVariationEntity) -> Unit,
    ) {
        val effective = ExerciseEntity(
            id = variation.id,
            canonicalName = variation.name,
            tag = parent?.tag ?: "STRENGTH",
            trackingType = variation.trackingType.ifBlank { parent?.trackingType.orEmpty() },
            equipment = null,
            instructions = null,
            source = "variation",
            isCustom = !variation.isSeeded,
            inputConfig = variation.inputConfig.ifBlank { parent?.inputConfig.orEmpty() },
            targetSets = variation.targetSets,
            targetRepsMin = variation.targetRepsMin,
            targetRepsMax = variation.targetRepsMax,
            targetRpe = variation.targetRpe,
            restSeconds = variation.restSeconds,
        )
        ExerciseSettingsDialog(effective, onDismiss) { settings ->
            onSave(
                variation.copy(
                    trackingType = settings.trackingType,
                    inputConfig = settings.inputConfig,
                    targetSets = settings.targetSets,
                    targetRepsMin = settings.targetRepsMin,
                    targetRepsMax = settings.targetRepsMax,
                    targetRpe = settings.targetRpe,
                    restSeconds = settings.restSeconds,
                ),
            )
    }
}

@Composable
internal fun ExerciseSettingsDialog(
        exercise: ExerciseEntity,
        onDismiss: () -> Unit,
        onSave: (ExerciseEntity) -> Unit,
    ) {
        val pounds = LocalContext.current.getSharedPreferences("settings", 0).getBoolean("lb", false)
        val weightUnit = if (pounds) "lb" else "kg"
        var config by remember(exercise.id) {
            val decoded = ExerciseInputConfig.decode(exercise.inputConfig, exercise.trackingType)
            mutableStateOf(if (decoded.weightUnit == null) decoded else decoded.copy(weightUnit = weightUnit))
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
                        ExerciseSettingCheckbox("Weight ($weightUnit)", config.weightUnit == weightUnit) {
                            config = config.copy(weightUnit = if (it) weightUnit else null)
                        }
                        ExerciseSettingCheckbox("Bodyweight", config.bodyweight) {
                            config = config.copy(bodyweight = it)
                        }
                        ExerciseSettingCheckbox("Added weight", config.addedWeight) {
                            config = config.copy(addedWeight = it)
                        }
                        ExerciseSettingCheckbox("Band resistance", config.bandResistance) {
                            config = config.copy(bandResistance = it)
                        }
                        ExerciseSettingCheckbox("Time Under Tension (seconds)", config.timeHeld) {
                            config = config.copy(timeHeld = it)
                        }
                        ExerciseSettingCheckbox("Total Time (seconds)", config.timeUnderTension) {
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
