package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petermathie.vibetrainer.domain.progress.SessionProgress
import com.petermathie.vibetrainer.domain.model.ActivityDay
import java.time.Instant
import java.time.ZoneId

@Composable
fun ProgressScreen(vm:EditorViewModel) {
    val exercises by vm.exercises.collectAsStateWithLifecycle();val workouts by vm.workouts.collectAsStateWithLifecycle();val rows by vm.workoutExercises.collectAsStateWithLifecycle()
    val sets by vm.sets.collectAsStateWithLifecycle();val links by vm.setBands.collectAsStateWithLifecycle();val bands by vm.bands.collectAsStateWithLifecycle();val variations by vm.variations.collectAsStateWithLifecycle()
    var exerciseId by remember { mutableStateOf<String?>(null) };var picker by remember { mutableStateOf(false) };var filter by remember { mutableStateOf<String?>(null) };var selected by remember { mutableStateOf<Int?>(null) }
    var variationMenu by remember { mutableStateOf(false) };var methodology by remember { mutableStateOf(false) }
    val points=exerciseId?.let { SessionProgress.points(it,workouts,rows,sets,links,bands,filter,variations) }.orEmpty()
    val finishedIds=workouts.filter { it.status=="FINISHED" }.map { it.id }.toSet()
    val trackableSetRowIds = sets.filter { SessionProgress.valid(it) || it.romValue != null }.map { it.workoutExerciseId }.toSet()
    val eligibleExerciseIds = rows.filter { it.workoutId in finishedIds && it.id in trackableSetRowIds }.map { it.actualExerciseId }.toSet()
    val exerciseRows=rows.filter { it.actualExerciseId==exerciseId && it.workoutId in finishedIds }.map { it.id }.toSet()
    val valid=sets.filter { it.workoutExerciseId in exerciseRows && SessionProgress.valid(it) && (filter==null || it.variationId==filter) }
    val records=SessionProgress.records(valid,points)
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                Text("Progress",style=MaterialTheme.typography.headlineSmall,modifier=Modifier.weight(1f))
                IconButton(onClick={methodology=true}) { Icon(Icons.Outlined.Info,contentDescription="How progress works") }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                Button(onClick={picker=true},enabled=eligibleExerciseIds.isNotEmpty(),modifier=Modifier.weight(1f)) {
                    Text(exercises.find { it.id==exerciseId }?.canonicalName ?: "Choose exercise",maxLines=1,overflow=TextOverflow.Ellipsis)
                }
                Box(Modifier.weight(1f)) {
                    val exerciseVariations=variations.filter { it.exerciseId==exerciseId }
                    OutlinedButton(
                        onClick={variationMenu=true},
                        enabled=exerciseVariations.isNotEmpty(),
                        modifier=Modifier.fillMaxWidth(),
                    ) {
                        Text(exerciseVariations.find { it.id==filter }?.name ?: "Variations",maxLines=1,overflow=TextOverflow.Ellipsis)
                    }
                    DropdownMenu(expanded=variationMenu,onDismissRequest={variationMenu=false}) {
                        DropdownMenuItem(
                            text={Text("All variations")},
                            onClick={filter=null;selected=null;variationMenu=false},
                        )
                        exerciseVariations.forEach { variation ->
                            DropdownMenuItem(
                                text={Text(variation.name)},
                                onClick={filter=variation.id;selected=null;variationMenu=false},
                            )
                        }
                    }
                }
            }
            if(eligibleExerciseIds.isEmpty())Text("Complete a working set before an exercise appears here.")
            if(points.isNotEmpty()) {
                MiniChart(points.map { it.index ?: it.score },points.map { it.trend },points.map { it.date },if(points.any { it.index!=null }) "index" else "score"){selected=it}
                val last=points.mapNotNull { it.trend }.takeLast(2)
                if(last.size==2) Text(if(last[1]>last[0]*1.01)"Rising" else if(last[1]<last[0]*0.99)"Falling" else "Flat")
                Text("RPE")
                MiniChart(points.map { it.performance.rpe ?: Double.NaN },dates=points.map { it.date },unit="RPE"){selected=it}
                selected?.let { points.getOrNull(it) }?.let { p ->
                    Text("${Instant.ofEpochMilli(p.date).atZone(ZoneId.systemDefault()).toLocalDate()} · ${setDescription(p.performance)}")
                    Text("Bands: ${p.bands.joinToString().ifBlank { "None" }} · RPE ${p.performance.rpe ?: "not recorded"}")
                    Text(p.notes.ifBlank { "No exercise notes" })
                }
                Text("Personal records",style=MaterialTheme.typography.titleMedium)
                Text("Weight PR · ${records.weightKg ?: "—"} kg")
                Text("Repetition PR · ${records.reps ?: "—"} reps")
                Text("Hold PR · ${records.holdMillis?.div(1000.0) ?: "—"} sec")
                Text("Estimated 1RM PR · ${records.estimatedOneRepMaxKg ?: "—"} kg")
                Text("Calculated performance PR · ${records.scoredPerformance?.performance?.let(::setDescription) ?: "—"}")
                ActivityHeatmap(points.groupBy { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay() }.map { ActivityDay(it.key,it.value.size) }) { day -> selected=points.indexOfFirst { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()==day }.takeIf { it>=0 } }
            }
        }
        val ids=rows.filter { it.actualExerciseId==exerciseId && workouts.any { w -> w.id==it.workoutId && w.status=="FINISHED" } }.map { it.id }.toSet()
        val romSeries=SessionProgress.romSeries(sets.filter { it.workoutExerciseId in ids })
        if(romSeries.isNotEmpty()) item {
            Text("Flexibility / ROM (separate from frequency)")
            romSeries.forEach { (unit,series) ->
                Text("ROM · $unit",style=MaterialTheme.typography.titleSmall)
                MiniChart(series.map { it.romValue!! },dates=series.map { it.loggedAt },unit=unit) { }
                series.forEach { Text("${it.romValue} $unit") }
            }
        }
        items(points.reversed()) { p -> TextButton(onClick={selected=points.indexOf(p)}){Text("${Instant.ofEpochMilli(p.date).atZone(ZoneId.systemDefault()).toLocalDate()} · ${setDescription(p.performance)}")} }
    }
    if(picker)ProgressExercisePicker(vm,eligibleExerciseIds,{picker=false}){exerciseId=it.id;filter=null;selected=null;picker=false}
    if(methodology)AlertDialog(
        onDismissRequest={methodology=false},
        title={Text("How progress works")},
        text={Text("The first three valid sessions establish a personal baseline of 100. Before that, charts show raw performance. Skill index is a heuristic: variation order sets the main difficulty; holds, reps and assistance adjust progress only within the same variation.")},
        confirmButton={TextButton(onClick={methodology=false}){Text("Close")}},
    )
}

@Composable
private fun ProgressExercisePicker(
    vm: EditorViewModel,
    eligibleExerciseIds: Set<String>,
    onDismiss: () -> Unit,
    onChoose: (com.petermathie.vibetrainer.data.local.ExerciseEntity) -> Unit,
) {
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    val aliases by vm.aliases.collectAsStateWithLifecycle()
    val mappings by vm.mappings.collectAsStateWithLifecycle()
    val muscles by vm.muscles.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val aliasIds = aliases.filter { it.alias.contains(query, true) }.map { it.exerciseId }.toSet()
    val muscleIds = muscles.filter { it.displayName.contains(query, true) }.map { it.id }.toSet()
    val mappedIds = mappings.filter { it.muscleId in muscleIds }.map { it.exerciseId }.toSet()
    val available = exercises.filter {
        it.id in eligibleExerciseIds &&
            !it.isArchived &&
            (it.canonicalName.contains(query, true) || it.id in aliasIds || it.id in mappedIds)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose exercise") },
        text = {
            Column {
                OutlinedTextField(query, { query = it }, label = { Text("Name, alias or muscle") })
                LazyColumn(
                    Modifier
                        .heightIn(max = 420.dp)
                        .semantics { contentDescription = "Eligible progress exercises" },
                ) {
                    if (available.isEmpty()) {
                        item { Text("No matching exercises with progress data") }
                    }
                    items(available, key = { it.id }) { exercise ->
                        Text(
                            exercise.canonicalName,
                            Modifier
                                .fillMaxWidth()
                                .clickable { onChoose(exercise) }
                                .padding(vertical = 14.dp),
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
fun MiniChart(values:List<Double>,smooth:List<Double?> = emptyList(),dates:List<Long> = emptyList(),unit:String = "",onSelect:(Int)->Unit) {
    val color=MaterialTheme.colorScheme.primary; val secondary=MaterialTheme.colorScheme.secondary;val axis=MaterialTheme.colorScheme.outline
    val finite=values.filter { it.isFinite() }
    if(finite.isEmpty())return
    val min=finite.minOrNull() ?: 0.0;val max=finite.maxOrNull() ?: 1.0
    fun select(x:Float,width:Float) {
        if(values.isNotEmpty())onSelect(((x/width)*(values.size-1)).toInt().coerceIn(values.indices))
    }
    Column(Modifier.semantics { contentDescription="Progress chart from ${formatChartDate(dates.firstOrNull())} to ${formatChartDate(dates.lastOrNull())}, $min to $max $unit" }) {
        Text("${formatAxis(max)} $unit",style=MaterialTheme.typography.labelSmall)
        Canvas(Modifier.fillMaxWidth().height(130.dp)
            .pointerInput(values){detectTapGestures { select(it.x,size.width.toFloat()) }}
            .pointerInput(values){awaitPointerEventScope { while(true) { val event=awaitPointerEvent();if(event.type==PointerEventType.Move || event.type==PointerEventType.Enter)event.changes.firstOrNull()?.position?.let { select(it.x,size.width.toFloat()) } } }}) {
        val finite=values.filter { it.isFinite() }
        if(finite.isEmpty())return@Canvas
        val min=finite.minOrNull() ?: 0.0;val max=finite.maxOrNull() ?: 1.0;val span=(max-min).coerceAtLeast(1.0)
        fun point(i:Int,v:Double)=Offset(if(values.size==1)size.width/2 else 16+(size.width-24)*i/(values.size-1),size.height-12-((v-min)/span*(size.height-20)).toFloat())
        drawLine(axis,Offset(12f,4f),Offset(12f,size.height-10f),2f)
        drawLine(axis,Offset(12f,size.height-10f),Offset(size.width,size.height-10f),2f)
        values.forEachIndexed { i,v -> if(v.isFinite()) { if(i>0 && values[i-1].isFinite())drawLine(color,point(i-1,values[i-1]),point(i,v),3f);drawCircle(color,5f,point(i,v)) } }
        smooth.forEachIndexed { i,v -> if(i>0 && v!=null && smooth[i-1]!=null)drawLine(secondary,point(i-1,smooth[i-1]!!),point(i,v),5f) }
        }
        Row(Modifier.fillMaxWidth()) {
            Text(formatChartDate(dates.firstOrNull()),style=MaterialTheme.typography.labelSmall,modifier=Modifier.weight(1f))
            Text(formatChartDate(dates.lastOrNull()),style=MaterialTheme.typography.labelSmall,textAlign=TextAlign.End,modifier=Modifier.weight(1f))
        }
        Text("${formatAxis(min)} $unit",style=MaterialTheme.typography.labelSmall)
    }
}

private fun formatChartDate(value:Long?):String=value?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toString() }.orEmpty()
private fun formatAxis(value:Double):String=if(value%1.0==0.0)value.toLong().toString() else "%.1f".format(value)
