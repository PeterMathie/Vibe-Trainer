package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petermathie.vibetrainer.data.local.*
import com.petermathie.vibetrainer.domain.model.TrackingType

@Composable
fun ExerciseEditor(vm: EditorViewModel) {
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    val muscles by vm.muscles.collectAsStateWithLifecycle()
    val mappings by vm.mappings.collectAsStateWithLifecycle()
    val aliases by vm.aliases.collectAsStateWithLifecycle()
    val variations by vm.variations.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<ExerciseEntity?>(null) }
    var variation by remember { mutableStateOf<String?>(null) }
    val matchingMuscles=muscles.filter { it.displayName.contains(query,true) }.map { it.id }.toSet()
    val matchingIds=mappings.filter { it.muscleId in matchingMuscles }.map { it.exerciseId }.toSet()+aliases.filter { it.alias.contains(query,true) }.map { it.exerciseId }
    LazyColumn(Modifier.fillMaxSize(), contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        item { Text("Exercises",style=MaterialTheme.typography.headlineSmall); EditField("Name, alias or muscle",query){query=it}; Button(onClick={selected=ExerciseEntity(newId(),"","STRENGTH","WEIGHT_REPS",null,null,"custom",true)}){Text("Custom exercise")} }
        items(exercises.filter { !it.isArchived && (it.canonicalName.contains(query,true)||it.id in matchingIds) }.take(100),key={it.id}) { e ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                Text(e.canonicalName,style=MaterialTheme.typography.titleMedium)
                Text(mappings.filter { it.exerciseId==e.id }.joinToString { m -> "${muscles.find { it.id==m.muscleId }?.displayName} (${m.role.lowercase()})" },style=MaterialTheme.typography.bodySmall)
                Row {
                    TextButton(onClick={selected=if(e.isCustom)e else e.copy(id=newId(),canonicalName=e.canonicalName+" (custom)",isCustom=true,source=e.id)}) { Text(if(e.isCustom)"Edit" else "Duplicate") }
                    TextButton(onClick={variation=e.id}){Text("Add variation")}
                }
                if(e.isCustom) TextButton(onClick={vm.saveExercise(e.copy(isArchived=true),aliases.filter { it.exerciseId==e.id }.map { it.alias },mappings.filter { it.exerciseId==e.id }.associate { it.muscleId to it.role })}){Text("Archive")}
                variations.filter { it.exerciseId==e.id }.sortedBy { it.progressionRank }.forEach { v ->
                    Row {
                        Text(v.name,Modifier.weight(1f))
                        if(!v.isSeeded) {
                            TextButton(onClick={vm.moveVariation(v.id,-1)}){Text("Up")}
                            TextButton(onClick={vm.moveVariation(v.id,1)}){Text("Down")}
                        }
                    }
                }
            } }
        }
    }
    variation?.let { id -> NameDialog("Variation name","",{variation=null}) { vm.save(ExerciseVariationEntity(newId(),id,it,(variations.filter { it.exerciseId==id }.maxOfOrNull { it.progressionRank } ?: 0)+1,false));variation=null } }
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
