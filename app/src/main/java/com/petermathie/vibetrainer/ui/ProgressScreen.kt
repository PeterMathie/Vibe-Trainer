package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
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
    val points=exerciseId?.let { SessionProgress.points(it,workouts,rows,sets,links,bands,filter,variations) }.orEmpty()
    val finishedIds=workouts.filter { it.status=="FINISHED" }.map { it.id }.toSet()
    val exerciseRows=rows.filter { it.actualExerciseId==exerciseId && it.workoutId in finishedIds }.map { it.id }.toSet()
    val valid=sets.filter { it.workoutExerciseId in exerciseRows && SessionProgress.valid(it) && (filter==null || it.variationId==filter) }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {
            Text("Progress",style=MaterialTheme.typography.headlineSmall)
            Button(onClick={picker=true}) { Text(exercises.find { it.id==exerciseId }?.canonicalName ?: "Choose exercise") }
            TextButton(onClick={filter=null;selected=null}){Text(if(filter==null)"✓ All variations" else "All variations")}
            variations.filter { it.exerciseId==exerciseId }.forEach { v -> TextButton(onClick={filter=v.id;selected=null}){Text((if(filter==v.id)"✓ " else "")+v.name)} }
            if(points.size<3)Text("Raw performance shown until three valid sessions establish a baseline of 100.")
            if(variations.any { it.exerciseId==exerciseId })Text("Overall skill index follows variation difficulty, with holds and assistance compared within each variation. It is a progress indicator, not a force measurement.")
            if(points.isNotEmpty()) {
                MiniChart(points.map { it.index ?: it.score },points.map { it.trend }){selected=it}
                val last=points.mapNotNull { it.trend }.takeLast(2)
                if(last.size==2) Text(if(last[1]>last[0]*1.01)"Rising" else if(last[1]<last[0]*0.99)"Falling" else "Flat")
                Text("RPE")
                val rpePoints=points.withIndex().filter { it.value.performance.rpe!=null }
                MiniChart(rpePoints.map { it.value.performance.rpe!! }){selected=rpePoints[it].index}
                selected?.let { points.getOrNull(it) }?.let { p ->
                    Text("${Instant.ofEpochMilli(p.date).atZone(ZoneId.systemDefault()).toLocalDate()} · ${setDescription(p.performance)}")
                    Text("Bands: ${p.bands.joinToString().ifBlank { "None" }} · RPE ${p.performance.rpe ?: "not recorded"}")
                    Text(p.notes.ifBlank { "No exercise notes" })
                }
                Text("PR weight: ${valid.mapNotNull { it.weightKg }.maxOrNull() ?: "—"} kg")
                Text("PR hold: ${valid.mapNotNull { it.holdMillis }.maxOrNull()?.div(1000.0) ?: "—"} sec")
                val best=points.maxByOrNull { it.score }
                Text("Best scored performance: ${best?.performance?.let(::setDescription).orEmpty()}")
                Text("Estimated 1RM: ${valid.filter { it.weightKg!=null && it.reps!=null }.maxOfOrNull { it.weightKg!!*(1+it.reps!!/30.0) } ?: "—"} kg")
                ActivityHeatmap(points.groupBy { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay() }.map { ActivityDay(it.key,it.value.size) }) { day -> selected=points.indexOfFirst { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()==day }.takeIf { it>=0 } }
            }
        }
        val ids=rows.filter { it.actualExerciseId==exerciseId && workouts.any { w -> w.id==it.workoutId && w.status=="FINISHED" } }.map { it.id }.toSet()
        val rom=sets.filter { it.workoutExerciseId in ids && it.romValue!=null }
        if(rom.isNotEmpty()) item { Text("Flexibility / ROM (separate from frequency)"); MiniChart(rom.sortedBy { it.loggedAt }.map { it.romValue!! }) { }; rom.forEach { Text("${it.romValue} ${it.romUnit}") } }
        items(points.reversed()) { p -> TextButton(onClick={selected=points.indexOf(p)}){Text("${Instant.ofEpochMilli(p.date).atZone(ZoneId.systemDefault()).toLocalDate()} · ${setDescription(p.performance)}")} }
    }
    if(picker)ExercisePicker(vm,{picker=false}){exerciseId=it.id;filter=null;selected=null;picker=false}
}

@Composable
fun MiniChart(values:List<Double>,smooth:List<Double?> = emptyList(),onSelect:(Int)->Unit) {
    val color=MaterialTheme.colorScheme.primary; val secondary=MaterialTheme.colorScheme.secondary
    Canvas(Modifier.fillMaxWidth().height(150.dp).pointerInput(values){detectTapGestures { if(values.isNotEmpty())onSelect(((it.x/size.width)*(values.size-1)).toInt().coerceIn(values.indices)) }}) {
        if(values.isEmpty())return@Canvas
        val min=values.minOrNull() ?: 0.0;val max=values.maxOrNull() ?: 1.0;val span=(max-min).coerceAtLeast(1.0)
        fun point(i:Int,v:Double)=Offset(if(values.size==1)size.width/2 else 8+(size.width-16)*i/(values.size-1),size.height-8-((v-min)/span*(size.height-16)).toFloat())
        values.forEachIndexed { i,v -> if(i>0)drawLine(color,point(i-1,values[i-1]),point(i,v),3f);drawCircle(color,5f,point(i,v)) }
        smooth.forEachIndexed { i,v -> if(i>0 && v!=null && smooth[i-1]!=null)drawLine(secondary,point(i-1,smooth[i-1]!!),point(i,v),5f) }
    }
}
