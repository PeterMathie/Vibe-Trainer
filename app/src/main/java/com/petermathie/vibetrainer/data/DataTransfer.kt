package com.petermathie.vibetrainer.data

import com.petermathie.vibetrainer.data.local.VibeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Versioned, structured, portable records. Photos are deliberately separate. */
object DataTransfer {
    val tables=listOf("muscles","exercises","exercise_aliases","exercise_muscles","exercise_variations","bands","programmes","programme_days","programme_exercises","workouts","workout_exercises","workout_sets","workout_set_bands","trackers","tracker_fields","tracker_daily_values","body_measurements","seed_metadata")
    suspend fun export(db:VibeDatabase):String=withContext(Dispatchers.IO) {
        val root=JSONObject().put("format","vibe-trainer").put("version",1)
        val data=JSONObject()
        val sql=db.openHelper.readableDatabase
        sql.beginTransaction()
        try {
            tables.forEach { table ->
                val rows=JSONArray()
                sql.query("SELECT * FROM `$table`").use { cursor ->
                    while(cursor.moveToNext()) {
                        val row=JSONObject()
                        cursor.columnNames.forEachIndexed { i,name -> row.put(name,when(cursor.getType(i)) { 0->JSONObject.NULL;1->cursor.getLong(i);2->cursor.getDouble(i);else->cursor.getString(i) }) }
                        rows.put(row)
                    }
                };data.put(table,rows)
            }
            sql.setTransactionSuccessful()
        } finally { sql.endTransaction() }
        root.put("tables",data).toString(2)
    }
    /** Merge by stable ID; validate everything and roll back on any invalid record. */
    suspend fun import(db:VibeDatabase,text:String)=withContext(Dispatchers.IO) {
        require(text.length<50_000_000){"Import exceeds 50 MB"}
        val root=JSONObject(text)
        require(root.optString("format")=="vibe-trainer" && root.optInt("version")==1){"Unsupported backup format"}
        val data=root.getJSONObject("tables")
        require(data.keys().asSequence().all { it in tables }){"Unknown table in import"}
        val sql=db.openHelper.writableDatabase
        sql.beginTransaction()
        try {
            tables.forEach { table ->
                val rows=data.optJSONArray(table) ?: return@forEach
                val columns=mutableListOf<String>();val keys=mutableListOf<String>()
                sql.query("PRAGMA table_info(`$table`)").use { c -> while(c.moveToNext()){columns+=c.getString(c.getColumnIndexOrThrow("name"));if(c.getInt(c.getColumnIndexOrThrow("pk"))>0)keys+=c.getString(c.getColumnIndexOrThrow("name"))} }
                repeat(rows.length()) { i ->
                    val row=rows.getJSONObject(i)
                    require(row.keys().asSequence().toSet()==columns.toSet()) { "Invalid columns in $table record $i" }
                    val args=columns.map { if(row.isNull(it))null else row.get(it) }.toTypedArray()
                    sql.execSQL("INSERT OR IGNORE INTO `$table` (${columns.joinToString { "`$it`" }}) VALUES (${columns.joinToString { "?" }})",args)
                    val updateColumns=columns.filter { it !in keys }
                    if(updateColumns.isNotEmpty()) sql.execSQL("UPDATE `$table` SET ${updateColumns.joinToString { "`$it`=?" }} WHERE ${keys.joinToString(" AND ") { "`$it`=?" }}",(updateColumns+keys).map { if(row.isNull(it))null else row.get(it) }.toTypedArray())
                }
            }
            sql.query("PRAGMA foreign_key_check").use { require(!it.moveToFirst()){ "Import contains broken references" } }
            sql.setTransactionSuccessful()
        } finally { sql.endTransaction() }
        db.invalidationTracker.refreshVersionsAsync()
    }
    suspend fun csv(db:VibeDatabase):String=withContext(Dispatchers.IO) {
        fun quote(s:String)="\""+s.replace("\"","\"\"")+"\""
        val out=StringBuilder()
        db.openHelper.readableDatabase.query("SELECT w.name AS workout,w.finishedAt,e.canonicalName AS exercise,we.notes AS exerciseNotes,s.* FROM workout_sets s JOIN workout_exercises we ON we.id=s.workoutExerciseId JOIN exercises e ON e.id=we.actualExerciseId JOIN workouts w ON w.id=we.workoutId WHERE w.status='FINISHED' ORDER BY w.finishedAt,s.ordinal").use { c ->
            out.appendLine(c.columnNames.joinToString(",",transform=::quote))
            while(c.moveToNext())out.appendLine(c.columnNames.indices.joinToString(","){quote(if(c.isNull(it))"" else c.getString(it))})
        };out.toString()
    }
}
