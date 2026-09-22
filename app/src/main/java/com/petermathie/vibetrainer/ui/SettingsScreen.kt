package com.petermathie.vibetrainer.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petermathie.vibetrainer.data.local.BodyMeasurementEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.io.File
import com.petermathie.vibetrainer.data.BackupPreferences

@Composable
fun SettingsScreen(vm:EditorViewModel,onStyle:()->Unit,onRemoveDemo:()->Unit) {
    val context=LocalContext.current;val prefs=context.getSharedPreferences("settings",0);val scope=rememberCoroutineScope()
    var message by remember { mutableStateOf("") };var last by remember { mutableStateOf(prefs.getLong("backup",0)) }
    var notices by remember { mutableStateOf<String?>(null) }
    var lb by remember { mutableStateOf(prefs.getBoolean("lb",false)) };var female by remember { mutableStateOf(prefs.getBoolean("female",false)) }
    var auto by remember { mutableStateOf(prefs.getBoolean("autoRest",false)) };var haptic by remember { mutableStateOf(prefs.getBoolean("haptic",true)) };var reduced by remember { mutableStateOf(prefs.getBoolean("reducedMotion",false)) }
    val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if(uri!=null)scope.launch { try { val text=BackupPreferences.attach(vm.exportJson(),prefs);withContext(Dispatchers.IO){requireNotNull(context.contentResolver.openOutputStream(uri)){"Cannot open backup destination"}.bufferedWriter().use{it.write(text)}};last=System.currentTimeMillis();prefs.edit().putLong("backup",last).apply();message="Backup saved" }catch(e:Exception){message=e.message.orEmpty()} } }
    var pendingImport by remember { mutableStateOf<String?>(null) }
    val restore=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if(uri!=null)scope.launch { try { pendingImport=withContext(Dispatchers.IO){context.contentResolver.openInputStream(uri)?.bufferedReader()?.use{it.readText()}} }catch(e:Exception){message=e.message.orEmpty()} } }
    val csv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri -> if(uri!=null)scope.launch { try {val text=vm.exportCsv();withContext(Dispatchers.IO){context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use{it.write(text)}};message="CSV saved"}catch(e:Exception){message=e.message.orEmpty()} } }
    val notify=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){message=if(it)"Timer notifications enabled" else "Notifications disabled"}
    var target by remember { mutableStateOf("60") };var bar by remember { mutableStateOf("20") }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        item {
            Text("Settings and data",style=MaterialTheme.typography.headlineSmall)
            TextButton(onClick=onStyle){Text("Colour palette")}
            SettingToggle("Pounds (lb)",lb){lb=it;prefs.edit().putBoolean("lb",it).apply()}
            SettingToggle("Female anatomy",female){female=it;prefs.edit().putBoolean("female",it).apply()}
            SettingToggle("Start rest automatically",auto){auto=it;prefs.edit().putBoolean("autoRest",it).apply()}
            SettingToggle("Haptics",haptic){haptic=it;prefs.edit().putBoolean("haptic",it).apply()}
            SettingToggle("Reduced motion",reduced){reduced=it;prefs.edit().putBoolean("reducedMotion",it).apply()}
            Text("Reduced motion disables touch ripples and app-owned animated transitions.")
            TextButton(onClick={if(Build.VERSION.SDK_INT>=33)notify.launch(Manifest.permission.POST_NOTIFICATIONS) else message="Notifications are enabled in Android settings"}){Text("Enable timer notifications")}
            if(Build.VERSION.SDK_INT>=31) TextButton(onClick={context.startActivity(android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,android.net.Uri.parse("package:${context.packageName}")))}){Text("Allow precise background timers")}
            if(Build.VERSION.SDK_INT>=31 && !context.getSystemService(android.app.AlarmManager::class.java).canScheduleExactAlarms()) Text("Without precise-timer permission, Android may delay background alerts.")
            TextButton(onClick={RestTimer.cancel(context);message="Timer cancelled"}){Text("Cancel rest timer")}
            Text("Plate calculator (${if(lb)"lb" else "kg"})")
            EditField("Total load",target){target=it};EditField("Bar weight",bar){bar=it}
            var remaining=((target.toDoubleOrNull() ?: 0.0)-(bar.toDoubleOrNull() ?: 0.0))/2
            val result=mutableListOf<String>()
            (if(lb)listOf(45.0,25.0,10.0,5.0,2.5)else listOf(25.0,20.0,15.0,10.0,5.0,2.5,1.25)).forEach { plate -> val n=(remaining/plate).toInt().coerceAtLeast(0);if(n>0){result+="$n × $plate";remaining-=n*plate} }
            Text("Per side: ${result.joinToString(" + ").ifEmpty { "No plates" }}${if(remaining>0.01)" · remainder $remaining" else ""}")
            Button(onClick={export.launch("vibe-trainer-backup.json")}){Text("JSON backup")}
            TextButton(onClick={restore.launch(arrayOf("application/json","text/plain"))}){Text("Restore / import structured JSON")}
            TextButton(onClick={csv.launch("vibe-trainer-workouts.csv")}){Text("CSV export")}
            Text("Last backup: ${if(last==0L)"Never" else Instant.ofEpochMilli(last)}")
            TextButton(onClick=onRemoveDemo){Text("Remove demo data")}
            TextButton(onClick={notices=context.assets.open("THIRD_PARTY_NOTICES.md").bufferedReader().use { it.readText() }}){Text("Open-source asset notices")}
            Text(message)
        }
    }
    notices?.let { text -> AlertDialog(onDismissRequest={notices=null},title={Text("Open-source assets")},text={LazyColumn { item { Text(text) } }},confirmButton={TextButton(onClick={notices=null}){Text("Close")}}) }
    if(pendingImport!=null)AlertDialog(onDismissRequest={pendingImport=null},title={Text("Import records?")},text={Text("Matching record IDs will be updated. Other records are retained. Make a backup first if you want to keep the previous values.")},confirmButton={TextButton(onClick={val text=pendingImport!!;pendingImport=null;scope.launch{try{val restored=BackupPreferences.validate(text);vm.importJson(text);BackupPreferences.restore(restored,prefs);lb=prefs.getBoolean("lb",false);female=prefs.getBoolean("female",false);auto=prefs.getBoolean("autoRest",false);haptic=prefs.getBoolean("haptic",true);reduced=prefs.getBoolean("reducedMotion",false);message="Import complete. Restored colours are active."}catch(e:Exception){message="Import failed: ${e.message}"}}}){Text("Import")}},dismissButton={TextButton(onClick={pendingImport=null}){Text("Cancel")}})
}

@Composable
private fun SettingToggle(label:String,value:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label,Modifier.weight(1f));Switch(value,onChange)}}

@Composable
fun MeasurementsScreen(vm:EditorViewModel) {
    val rows by vm.measurements.collectAsStateWithLifecycle();val context=LocalContext.current;val scope=rememberCoroutineScope()
    var metric by remember{mutableStateOf("Bodyweight")};var value by remember{mutableStateOf("")};var unit by remember{mutableStateOf("kg")};var note by remember{mutableStateOf("")};var refresh by remember{mutableStateOf(0)}
    var editing by remember { mutableStateOf<BodyMeasurementEntity?>(null) }
    var message by remember { mutableStateOf("") }
    val directory=File(context.filesDir,"progress-photos")
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)scope.launch{try{withContext(Dispatchers.IO){directory.mkdirs();requireNotNull(context.contentResolver.openInputStream(uri)).use{input->File(directory,"${System.currentTimeMillis()}.jpg").outputStream().use{input.copyTo(it)}}};refresh++;message="Photo added"}catch(e:Exception){message="Could not add photo: ${e.message}"}}}
    val exporter=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if(uri!=null) scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    requireNotNull(context.contentResolver.openOutputStream(uri)).use { output ->
                        ZipOutputStream(output).use { zip ->
                            directory.listFiles().orEmpty().forEach { file ->
                                zip.putNextEntry(ZipEntry(file.name))
                                file.inputStream().use { it.copyTo(zip) }
                                zip.closeEntry()
                            }
                        }
                    }
                }
                message="Photos exported"
            } catch(e:Exception) { message="Could not export photos: ${e.message}" }
        }
    }
    val photos=remember(refresh){directory.listFiles().orEmpty().sortedByDescending{it.name}}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp)) {
        item {
            Text("Measurements and photos",style=MaterialTheme.typography.headlineSmall)
            EditField("Metric",metric){metric=it};EditField("Value",value){value=it};EditField("Unit",unit){unit=it};EditField("Notes",note){note=it}
            Button(enabled=value.toDoubleOrNull()?.isFinite()==true&&metric.isNotBlank(),onClick={vm.save(BodyMeasurementEntity(newId(),System.currentTimeMillis(),metric,value.toDouble(),unit,note,false));value=""}){Text("Save measurement")}
            TextButton(onClick={picker.launch(arrayOf("image/*"))}){Text("Add progress photo")};TextButton(onClick={exporter.launch("progress-photos.zip")}){Text("Export photos separately")}
            Text(message)
        }
        items(photos,key={it.name}){file->
            val bitmap=remember(file){android.graphics.BitmapFactory.decodeFile(file.path,android.graphics.BitmapFactory.Options().apply{inSampleSize=4})}
            bitmap?.let { androidx.compose.foundation.Image(it.asImageBitmap(),contentDescription="Progress photo ${file.name}",modifier=Modifier.fillMaxWidth().height(240.dp)) }
            TextButton(onClick={if(file.delete()){refresh++;message="Photo removed"}else message="Could not remove photo"}){Text("Delete photo")}
        }
        items(rows,key={it.id}) { row ->
            Row {
                Text("${row.metric}: ${row.value} ${row.unit}",Modifier.weight(1f))
                TextButton(onClick={editing=row}) { Text("Edit") }
                TextButton(onClick={vm.removeMeasurement(row.id)}) { Text("Delete") }
            }
        }
    }
    editing?.let { row ->
        var amount by remember(row.id){mutableStateOf(row.value.toString())}
        var notes by remember(row.id){mutableStateOf(row.notes)}
        AlertDialog(onDismissRequest={editing=null},title={Text("${row.metric} (${row.unit})")},text={Column{EditField("Value",amount){amount=it};EditField("Notes",notes){notes=it}}},confirmButton={TextButton(enabled=amount.toDoubleOrNull()?.isFinite()==true,onClick={vm.save(row.copy(value=amount.toDouble(),notes=notes));editing=null}){Text("Save")}},dismissButton={TextButton(onClick={editing=null}){Text("Cancel")}})
    }
}
