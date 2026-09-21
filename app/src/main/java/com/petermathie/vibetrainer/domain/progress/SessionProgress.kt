package com.petermathie.vibetrainer.domain.progress

import com.petermathie.vibetrainer.data.local.*

data class ProgressPoint(val date: Long, val score: Double, val performance: WorkoutSetEntity, val notes: String, val variation: String?, val bands: List<String>, val index: Double? = null, val trend: Double? = null)

object SessionProgress {
    fun valid(s: WorkoutSetEntity): Boolean = s.setType == "WORKING" && s.result == "COMPLETED" && s.romValue == null && ((s.reps ?: 0)>0 || (s.holdMillis ?: 0)>0 || (s.leftReps ?: 0)>0 || (s.rightReps ?: 0)>0 || (s.leftHoldMillis ?: 0)>0 || (s.rightHoldMillis ?: 0)>0)
    fun score(s: WorkoutSetEntity, bodyweight: Double?, width: Double): Double? {
        if(!valid(s)) return null
        val reps = s.reps ?: listOfNotNull(s.leftReps,s.rightReps).minOrNull() ?: 0
        val hold = s.holdMillis ?: listOfNotNull(s.leftHoldMillis,s.rightHoldMillis).minOrNull()
        if(hold != null && hold>0) return ProgressScorer.assistedHold(hold,width)
        if(width>0) return ProgressScorer.assistedReps(reps,width)
        val load = s.weightKg ?: bodyweight?.let { (it+(s.addedWeightKg ?: 0.0)-(s.assistanceKg ?: 0.0)).coerceAtLeast(0.0) }
        return if(load != null && load>0) ProgressScorer.weightedReps(load,reps) else reps.toDouble()
    }
    /** Assistance is normalised only within a variation. Never compare band force between movements. */
    fun points(exerciseId:String, workouts:List<WorkoutEntity>, exercises:List<WorkoutExerciseEntity>, sets:List<WorkoutSetEntity>, links:List<WorkoutSetBandEntity>, bands:List<BandEntity>, filter:String?=null):List<ProgressPoint> {
        val raw=workouts.filter { it.status=="FINISHED" }.sortedBy { it.finishedAt }.mapNotNull { w ->
            val rows=exercises.filter { it.workoutId==w.id && it.actualExerciseId==exerciseId }
            val results=sets.filter { s -> rows.any { it.id==s.workoutExerciseId } && (filter==null || (s.variationId ?: "default")==filter) }
            results.mapNotNull { s ->
                val used=links.filter { it.setId==s.id }.mapNotNull { l -> bands.find { it.id==l.bandId } }
                score(s,w.bodyweightKg,used.sumOf { it.widthCentimetres })?.let { ProgressPoint(w.finishedAt ?: w.startedAt,it,s,rows.find { it.id==s.workoutExerciseId }?.notes.orEmpty(),s.variationId,used.map { it.name }) }
            }.maxByOrNull { it.score }
        }
        val indexed=raw.map { point ->
            val comparable=raw.filter { it.variation==point.variation }
            point.copy(index=if(comparable.size<3)null else ProgressScorer.progressIndex(point.score,comparable.take(3).map { it.score }))
        }
        return indexed.mapIndexed { i,p -> val values=indexed.take(i+1).takeLast(3).mapNotNull { it.index }; p.copy(trend=if(values.size==3)values.average() else null) }
    }
}
