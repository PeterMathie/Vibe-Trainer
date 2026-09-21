package com.petermathie.vibetrainer.data.seed

import android.content.Context
import androidx.room.withTransaction
import com.petermathie.vibetrainer.BuildConfig
import com.petermathie.vibetrainer.data.local.BandEntity
import com.petermathie.vibetrainer.data.local.ExerciseAliasEntity
import com.petermathie.vibetrainer.data.local.ExerciseEntity
import com.petermathie.vibetrainer.data.local.ExerciseMuscleEntity
import com.petermathie.vibetrainer.data.local.ExerciseVariationEntity
import com.petermathie.vibetrainer.data.local.MuscleEntity
import com.petermathie.vibetrainer.data.local.ProgrammeDayEntity
import com.petermathie.vibetrainer.data.local.ProgrammeEntity
import com.petermathie.vibetrainer.data.local.ProgrammeExerciseEntity
import com.petermathie.vibetrainer.data.local.SeedMetadataEntity
import com.petermathie.vibetrainer.data.local.TrackerDailyValueEntity
import com.petermathie.vibetrainer.data.local.TrackerEntity
import com.petermathie.vibetrainer.data.local.TrackerFieldEntity
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.data.local.WorkoutEntity
import com.petermathie.vibetrainer.data.local.WorkoutExerciseEntity
import com.petermathie.vibetrainer.data.local.WorkoutSetEntity
import com.petermathie.vibetrainer.domain.model.ExerciseTag
import com.petermathie.vibetrainer.domain.model.MuscleRole
import com.petermathie.vibetrainer.domain.model.SetResult
import com.petermathie.vibetrainer.domain.model.SetType
import com.petermathie.vibetrainer.domain.model.TrackingType
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.domain.model.WorkoutStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.io.SequenceInputStream
import java.util.Collections
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray

@Singleton
class DatabaseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: VibeDatabase,
) {
    suspend fun seedIfNeeded() {
        database.withTransaction {
            if ((database.metadataDao().version(CATALOGUE_KEY) ?: 0) < CATALOGUE_VERSION) {
                seedCatalogue()
                database.metadataDao().put(SeedMetadataEntity(CATALOGUE_KEY, CATALOGUE_VERSION))
            }
            if (BuildConfig.DEBUG && (database.metadataDao().version(DEMO_KEY) ?: 0) < DEMO_VERSION) {
                seedDemo()
                database.metadataDao().put(SeedMetadataEntity(DEMO_KEY, DEMO_VERSION))
            }
            if (BuildConfig.DEBUG && database.metadataDao().version("progress_demo") == null) {
                if(database.workoutDao().demoCount() > 0) seedProgressDemo()
                database.metadataDao().put(SeedMetadataEntity("progress_demo",1))
            }
            // Demo records use the same historical snapshots as real workouts.
            val sql=database.openHelper.writableDatabase
            sql.execSQL("INSERT OR IGNORE INTO workout_muscles SELECT we.id,em.muscleId,em.role FROM workout_exercises we JOIN exercise_muscles em ON em.exerciseId=we.actualExerciseId WHERE we.exerciseName=''")
            sql.execSQL("UPDATE workout_exercises SET exerciseName=(SELECT canonicalName FROM exercises WHERE id=actualExerciseId),trackingType=(SELECT trackingType FROM exercises WHERE id=actualExerciseId) WHERE exerciseName=''")
        }
    }

    private suspend fun seedCatalogue() {
        database.catalogueDao().insertMuscles(MUSCLES)
        database.catalogueDao().insertBands(BANDS)

        val parts = context.assets.list("").orEmpty()
            .filter { it.startsWith("free_exercises.json.gz.part") }
            .sorted()
            .map { context.assets.open(it) }
        check(parts.isNotEmpty()) { "Bundled exercise catalogue is missing" }
        val raw = GZIPInputStream(SequenceInputStream(Collections.enumeration(parts)))
            .bufferedReader()
            .use { it.readText() }
        val array = JSONArray(raw)
        val exercises = mutableListOf<ExerciseEntity>()
        val aliases = mutableListOf<ExerciseAliasEntity>()
        val mappings = mutableMapOf<Pair<String, String>, ExerciseMuscleEntity>()

        repeat(array.length()) { index ->
            val item = array.getJSONObject(index)
            val sourceId = item.getString("id")
            val id = "free:$sourceId"
            val name = item.getString("name")
            val category = item.optString("category")
            val equipment = item.optString("equipment").takeIf { it.isNotBlank() && it != "null" }
            val tag = if (category == "stretching") ExerciseTag.STRETCHING else ExerciseTag.STRENGTH
            val tracking = inferTrackingType(name, equipment, tag)
            val instructionsArray = item.optJSONArray("instructions")
            val instructions = instructionsArray?.let { values ->
                buildList { repeat(values.length()) { add(values.getString(it)) } }.joinToString("\n")
            }
            exercises += ExerciseEntity(
                id = id,
                canonicalName = name,
                tag = tag.name,
                trackingType = tracking.name,
                equipment = equipment,
                instructions = instructions,
                source = "free-exercise-db",
                isCustom = false,
            )
            aliasCandidates(name).forEach { alias ->
                aliases += ExerciseAliasEntity(id, alias, alias.normalized())
            }
            addMappings(mappings, id, name, item.optJSONArray("primaryMuscles"), MuscleRole.PRIMARY)
            addMappings(mappings, id, name, item.optJSONArray("secondaryMuscles"), MuscleRole.SECONDARY)
        }

        CURATED_EXERCISES.forEach { seed ->
            exercises.removeAll { it.id == seed.id }
            exercises += seed
        }
        CURATED_ALIASES.forEach { (exerciseId, values) ->
            values.forEach { aliases += ExerciseAliasEntity(exerciseId, it, it.normalized()) }
        }
        CURATED_MAPPINGS.forEach { mapping ->
            mappings[mapping.exerciseId to mapping.muscleId] = mapping
        }

        database.catalogueDao().insertExercises(exercises)
        database.catalogueDao().insertAliases(aliases.distinctBy { it.exerciseId to it.normalizedAlias })
        database.catalogueDao().insertExerciseMuscles(mappings.values.toList())
        database.catalogueDao().insertVariations(SKILL_VARIATIONS)
    }

    private fun addMappings(
        output: MutableMap<Pair<String, String>, ExerciseMuscleEntity>,
        exerciseId: String,
        exerciseName: String,
        sourceMuscles: JSONArray?,
        role: MuscleRole,
    ) {
        if (sourceMuscles == null) return
        repeat(sourceMuscles.length()) { index ->
            val source = sourceMuscles.getString(index)
            canonicalMuscles(source, exerciseName).forEach { muscleId ->
                val key = exerciseId to muscleId
                val previous = output[key]
                if (previous == null || role == MuscleRole.PRIMARY) {
                    output[key] = ExerciseMuscleEntity(exerciseId, muscleId, role.name)
                }
            }
        }
    }

    private suspend fun seedDemo() {
        database.programmeDao().insertProgrammes(DEMO_PROGRAMMES)
        database.programmeDao().insertDays(DEMO_DAYS)
        database.programmeDao().insertProgrammeExercises(DEMO_PROGRAMME_EXERCISES)
        database.trackerDao().insertTrackers(DEMO_TRACKERS)
        database.trackerDao().insertFields(DEMO_TRACKER_FIELDS)

        val now = System.currentTimeMillis()
        val dayMillis = 86_400_000L
        val workouts = listOf(
            demoWorkout("demo-workout-push-1", "Monday — Planche + Push", "demo-day-push", now - dayMillis, TrainingMode.STRENGTH),
            demoWorkout("demo-workout-legs-1", "Wednesday — Legs + Mobility", "demo-day-legs", now - 3 * dayMillis, TrainingMode.STRENGTH),
            demoWorkout("demo-workout-pull-1", "Saturday — Muscle-up + Pull", "demo-day-pull", now - 8 * dayMillis, TrainingMode.STRENGTH),
            demoWorkout("demo-stretch-1", "Front Splits", "demo-day-front-splits", now - 2 * dayMillis, TrainingMode.STRETCHING),
        )
        workouts.forEach { database.workoutDao().insertWorkout(it) }

        val workoutExercises = mutableListOf<WorkoutExerciseEntity>()
        val sets = mutableListOf<WorkoutSetEntity>()
        fun add(workoutId: String, exerciseId: String, position: Int, reps: Int? = null, hold: Long? = null, weight: Double? = null) {
            val weId = "$workoutId:$exerciseId"
            workoutExercises += WorkoutExerciseEntity(
                id = weId,
                workoutId = workoutId,
                plannedExerciseId = exerciseId,
                actualExerciseId = exerciseId,
                position = position,
                notes = if (position == 0) "Demo note: technique felt controlled" else "",
                restSeconds = 120,
                supersetGroup = null,
            )
            repeat(3) { ordinal ->
                sets += WorkoutSetEntity(
                    id = "$weId:$ordinal",
                    workoutExerciseId = weId,
                    ordinal = ordinal + 1,
                    setType = SetType.WORKING.name,
                    result = SetResult.COMPLETED.name,
                    variationId = null,
                    weightKg = weight,
                    reps = reps,
                    holdMillis = hold,
                    leftReps = null,
                    rightReps = null,
                    leftHoldMillis = null,
                    rightHoldMillis = null,
                    addedWeightKg = null,
                    assistanceKg = null,
                    rpe = 7.0 + ordinal * 0.5,
                    romValue = null,
                    romUnit = null,
                    notes = if (ordinal == 2) "Last set moved slowly" else "",
                    loggedAt = now - dayMillis,
                    updatedAt = now - dayMillis,
                )
            }
        }
        add("demo-workout-push-1", "core:handstand", 0, hold = 8_000)
        add("demo-workout-push-1", "core:planche", 1, hold = 6_000)
        add("demo-workout-push-1", "core:bench-press", 2, reps = 4, weight = 70.0)
        add("demo-workout-legs-1", "core:squat", 0, reps = 6, weight = 90.0)
        add("demo-workout-legs-1", "core:cossack-squat", 1, reps = 6, weight = 16.0)
        add("demo-workout-pull-1", "core:pull-up", 0, reps = 6)
        add("demo-workout-pull-1", "core:overhead-press", 1, reps = 5, weight = 45.0)
        add("demo-stretch-1", "core:front-split", 0, hold = 30_000)
        database.workoutDao().insertWorkoutExercises(workoutExercises)
        sets.forEach { database.workoutDao().insertSet(it) }

        val today = LocalDate.now().toEpochDay()
        listOf(
            TrackerDailyValueEntity("demo-piano-minutes", today - 1, 35.0, null, null, "", now),
            TrackerDailyValueEntity("demo-meditation-minutes", today - 1, 12.0, null, null, "", now),
            TrackerDailyValueEntity("demo-protein-grams", today - 2, 122.0, null, null, "", now),
        ).forEach { database.trackerDao().upsertValue(it) }
    }

    private suspend fun seedProgressDemo() {
        val now=System.currentTimeMillis()
        repeat(8) { index ->
            val finished=now-(8-index)*7*86_400_000L
            val id="demo-progress-$index"
            database.workoutDao().insertWorkout(demoWorkout(id,"Demo — Push progression","demo-day-push",finished,TrainingMode.STRENGTH))
            listOf("core:bench-press","core:planche").forEachIndexed { position, exercise ->
                val row=WorkoutExerciseEntity("$id:$exercise",id,exercise,exercise,position,"Demo session ${index+1}: ${if(index%3==0)"Harder than usual" else "Controlled technique"}",180,null)
                database.workoutDao().insertWorkoutExercises(listOf(row))
                repeat(3) { ordinal ->
                    val set=WorkoutSetEntity("${row.id}:$ordinal",row.id,ordinal+1,"WORKING","COMPLETED",if(position==1)"planche-tuck" else null,if(position==0)50.0+index*2.5 else null,if(position==0)6-ordinal else null,if(position==1)(6000L+index*1000-ordinal*500) else null,null,null,null,null,null,null,7.0+ordinal*.5,null,null,"",finished-600000+position*180000+ordinal*60000,finished)
                    database.workoutDao().insertSet(set)
                    if(position==1)database.workoutDao().insertSetBands(listOf(com.petermathie.vibetrainer.data.local.WorkoutSetBandEntity(set.id,BANDS[if(index<4)2 else 1].id,0)))
                }
            }
        }
    }

    private fun demoWorkout(id: String, name: String, dayId: String, finishedAt: Long, mode: TrainingMode) = WorkoutEntity(
        id = id,
        programmeDayId = dayId,
        name = name,
        mode = mode.name,
        status = WorkoutStatus.FINISHED.name,
        startedAt = finishedAt - 3_600_000L,
        finishedAt = finishedAt,
        notes = "Representative demo history",
        bodyweightKg = 76.0,
        isDemo = true,
    )

    private fun inferTrackingType(name: String, equipment: String?, tag: ExerciseTag): TrackingType = when {
        tag == ExerciseTag.STRETCHING -> TrackingType.HOLD
        name.contains("hold", ignoreCase = true) -> TrackingType.HOLD
        equipment == "body only" -> TrackingType.BODYWEIGHT_REPS
        else -> TrackingType.WEIGHT_REPS
    }

    private fun aliasCandidates(name: String): List<String> = buildList {
        val withoutPunctuation = name.replace(Regex("[^A-Za-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()
        if (!withoutPunctuation.equals(name, ignoreCase = true)) add(withoutPunctuation)
    }

    private fun canonicalMuscles(source: String, exerciseName: String): List<String> = when (source) {
        "abdominals" -> if (exerciseName.contains(Regex("oblique|side|twist", RegexOption.IGNORE_CASE))) listOf("CORE", "OBLIQUES") else listOf("CORE")
        "abductors" -> listOf("ABDUCTORS")
        "adductors" -> listOf("ADDUCTORS")
        "biceps" -> listOf("BICEPS")
        "calves" -> listOf("CALVES")
        "chest" -> listOf("CHEST")
        "forearms" -> listOf("FOREARMS")
        "glutes" -> listOf("GLUTES")
        "hamstrings" -> listOf("HAMSTRINGS")
        "lats" -> listOf("LATS")
        "lower back" -> listOf("BACK_LOWER")
        "middle back" -> listOf("RHOMBOIDS")
        "neck", "traps" -> listOf("TRAPEZIUS")
        "quadriceps" -> listOf("QUADS")
        "shoulders" -> when {
            exerciseName.contains(Regex("rear|reverse", RegexOption.IGNORE_CASE)) -> listOf("SHOULDERS_REAR")
            exerciseName.contains(Regex("lateral|side", RegexOption.IGNORE_CASE)) -> listOf("SHOULDERS_SIDE")
            exerciseName.contains(Regex("press|front", RegexOption.IGNORE_CASE)) -> listOf("SHOULDERS_FRONT", "SHOULDERS_SIDE")
            else -> listOf("SHOULDERS_FRONT", "SHOULDERS_SIDE", "SHOULDERS_REAR")
        }
        "triceps" -> listOf("TRICEPS")
        else -> emptyList()
    }

    private fun String.normalized(): String = lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    companion object {
        private const val CATALOGUE_KEY = "exercise-catalogue"
        private const val CATALOGUE_VERSION = 1
        private const val DEMO_KEY = "debug-demo"
        private const val DEMO_VERSION = 1

        private val MUSCLES = listOf(
            "ABDUCTORS" to "Abductors", "ADDUCTORS" to "Adductors", "BACK_LOWER" to "Lower back",
            "BICEPS" to "Biceps", "CALVES" to "Calves", "CHEST" to "Chest", "CORE" to "Abdominals",
            "FOREARMS" to "Forearms", "GLUTES" to "Glutes", "HAMSTRINGS" to "Hamstrings",
            "LATS" to "Lats", "OBLIQUES" to "Obliques", "QUADS" to "Quadriceps",
            "RHOMBOIDS" to "Rhomboids / middle back", "SHOULDERS_FRONT" to "Front deltoids",
            "SHOULDERS_REAR" to "Rear deltoids", "SHOULDERS_SIDE" to "Side deltoids",
            "TRAPEZIUS" to "Trapezius", "TRICEPS" to "Triceps",
        ).map { (id, name) -> MuscleEntity(id, name, id) }

        private val BANDS = listOf(
            BandEntity("band-yellow", "Yellow", 0.6, 0xFFFFD84DL),
            BandEntity("band-red", "Red", 1.2, 0xFFE94747L),
            BandEntity("band-black", "Black", 2.2, 0xFF1A1A1AL),
            BandEntity("band-purple", "Purple", 3.1, 0xFF7446B8L),
        )

        private fun curated(id: String, name: String, tracking: TrackingType, tag: ExerciseTag = ExerciseTag.STRENGTH) =
            ExerciseEntity(id, name, tag.name, tracking.name, null, null, "vibe-trainer", false)

        private val CURATED_EXERCISES = listOf(
            curated("core:handstand", "Handstand", TrackingType.SKILL_HOLD),
            curated("core:planche", "Planche", TrackingType.SKILL_HOLD),
            curated("core:muscle-up", "Muscle-up", TrackingType.ASSISTED_REPS),
            curated("core:bench-press", "Bench press", TrackingType.WEIGHT_REPS),
            curated("core:dip", "Dip", TrackingType.BODYWEIGHT_REPS),
            curated("core:leg-raise", "Leg raise", TrackingType.BODYWEIGHT_REPS),
            curated("core:squat", "Back squat", TrackingType.WEIGHT_REPS),
            curated("core:lunge", "Lunge", TrackingType.WEIGHT_REPS),
            curated("core:cossack-squat", "Cossack squat", TrackingType.WEIGHT_REPS),
            curated("core:jefferson-curl", "Jefferson curl", TrackingType.WEIGHT_REPS),
            curated("core:pull-up", "Pull-up", TrackingType.ASSISTED_REPS),
            curated("core:lat-pulldown", "Lat pulldown", TrackingType.WEIGHT_REPS),
            curated("core:overhead-press", "Overhead press", TrackingType.WEIGHT_REPS),
            curated("core:back-extension", "Back extension", TrackingType.WEIGHT_REPS),
            curated("core:front-split", "Front split", TrackingType.ROM_MEASUREMENT, ExerciseTag.STRETCHING),
            curated("core:forward-fold", "Forward fold", TrackingType.ROM_MEASUREMENT, ExerciseTag.STRETCHING),
            curated("core:side-split", "Side split", TrackingType.ROM_MEASUREMENT, ExerciseTag.STRETCHING),
            curated("core:bridge", "Bridge", TrackingType.ROM_MEASUREMENT, ExerciseTag.STRETCHING),
        )

        private val CURATED_ALIASES = mapOf(
            "core:bench-press" to listOf("bench", "barbell bench"),
            "core:dip" to listOf("dips"),
            "core:pull-up" to listOf("pullup", "pullups", "pull ups", "negative pull-up"),
            "core:overhead-press" to listOf("OHP", "shoulder press"),
            "core:lat-pulldown" to listOf("lat pull down", "pulldown"),
            "core:cossack-squat" to listOf("cossack"),
        )

        private fun mapping(exerciseId: String, muscleId: String, role: MuscleRole) = ExerciseMuscleEntity(exerciseId, muscleId, role.name)
        private val CURATED_MAPPINGS = listOf(
            mapping("core:handstand", "SHOULDERS_FRONT", MuscleRole.PRIMARY), mapping("core:handstand", "TRICEPS", MuscleRole.SECONDARY), mapping("core:handstand", "CORE", MuscleRole.SECONDARY),
            mapping("core:planche", "SHOULDERS_FRONT", MuscleRole.PRIMARY), mapping("core:planche", "CHEST", MuscleRole.PRIMARY), mapping("core:planche", "TRICEPS", MuscleRole.SECONDARY), mapping("core:planche", "CORE", MuscleRole.SECONDARY),
            mapping("core:muscle-up", "LATS", MuscleRole.PRIMARY), mapping("core:muscle-up", "BICEPS", MuscleRole.SECONDARY), mapping("core:muscle-up", "TRICEPS", MuscleRole.SECONDARY), mapping("core:muscle-up", "CHEST", MuscleRole.SECONDARY),
            mapping("core:bench-press", "CHEST", MuscleRole.PRIMARY), mapping("core:bench-press", "TRICEPS", MuscleRole.SECONDARY), mapping("core:bench-press", "SHOULDERS_FRONT", MuscleRole.SECONDARY),
            mapping("core:dip", "CHEST", MuscleRole.PRIMARY), mapping("core:dip", "TRICEPS", MuscleRole.PRIMARY), mapping("core:dip", "SHOULDERS_FRONT", MuscleRole.SECONDARY),
            mapping("core:leg-raise", "CORE", MuscleRole.PRIMARY), mapping("core:leg-raise", "QUADS", MuscleRole.SECONDARY),
            mapping("core:squat", "QUADS", MuscleRole.PRIMARY), mapping("core:squat", "GLUTES", MuscleRole.PRIMARY), mapping("core:squat", "HAMSTRINGS", MuscleRole.SECONDARY),
            mapping("core:lunge", "QUADS", MuscleRole.PRIMARY), mapping("core:lunge", "GLUTES", MuscleRole.PRIMARY),
            mapping("core:cossack-squat", "ADDUCTORS", MuscleRole.PRIMARY), mapping("core:cossack-squat", "QUADS", MuscleRole.PRIMARY), mapping("core:cossack-squat", "GLUTES", MuscleRole.SECONDARY),
            mapping("core:jefferson-curl", "BACK_LOWER", MuscleRole.PRIMARY), mapping("core:jefferson-curl", "HAMSTRINGS", MuscleRole.SECONDARY),
            mapping("core:pull-up", "LATS", MuscleRole.PRIMARY), mapping("core:pull-up", "BICEPS", MuscleRole.SECONDARY), mapping("core:pull-up", "FOREARMS", MuscleRole.SECONDARY),
            mapping("core:lat-pulldown", "LATS", MuscleRole.PRIMARY), mapping("core:lat-pulldown", "BICEPS", MuscleRole.SECONDARY),
            mapping("core:overhead-press", "SHOULDERS_FRONT", MuscleRole.PRIMARY), mapping("core:overhead-press", "SHOULDERS_SIDE", MuscleRole.PRIMARY), mapping("core:overhead-press", "TRICEPS", MuscleRole.SECONDARY),
            mapping("core:back-extension", "BACK_LOWER", MuscleRole.PRIMARY), mapping("core:back-extension", "GLUTES", MuscleRole.SECONDARY), mapping("core:back-extension", "HAMSTRINGS", MuscleRole.SECONDARY),
            mapping("core:front-split", "HAMSTRINGS", MuscleRole.PRIMARY), mapping("core:front-split", "QUADS", MuscleRole.SECONDARY),
            mapping("core:forward-fold", "HAMSTRINGS", MuscleRole.PRIMARY), mapping("core:forward-fold", "BACK_LOWER", MuscleRole.SECONDARY),
            mapping("core:side-split", "ADDUCTORS", MuscleRole.PRIMARY), mapping("core:side-split", "HAMSTRINGS", MuscleRole.SECONDARY),
            mapping("core:bridge", "SHOULDERS_FRONT", MuscleRole.PRIMARY), mapping("core:bridge", "BACK_LOWER", MuscleRole.PRIMARY), mapping("core:bridge", "QUADS", MuscleRole.SECONDARY),
        )

        private val SKILL_VARIATIONS = listOf(
            ExerciseVariationEntity("handstand-wall", "core:handstand", "Wall handstand", 10, true),
            ExerciseVariationEntity("handstand-free", "core:handstand", "Freestanding handstand", 20, true),
            ExerciseVariationEntity("planche-tuck", "core:planche", "Tuck planche", 10, true),
            ExerciseVariationEntity("planche-advanced-tuck", "core:planche", "Advanced tuck planche", 20, true),
            ExerciseVariationEntity("planche-straddle", "core:planche", "Straddle planche", 30, true),
            ExerciseVariationEntity("planche-full", "core:planche", "Full planche", 40, true),
        )

        private val DEMO_PROGRAMMES = listOf(
            ProgrammeEntity("demo-programme-push", "Planche + Push", TrainingMode.STRENGTH.name, true),
            ProgrammeEntity("demo-programme-legs", "Legs + Mobility", TrainingMode.STRENGTH.name, true),
            ProgrammeEntity("demo-programme-pull", "Muscle-up + Pull", TrainingMode.STRENGTH.name, true),
            ProgrammeEntity("demo-programme-stretch", "Stretching", TrainingMode.STRETCHING.name, true),
        )
        private val DEMO_DAYS = listOf(
            ProgrammeDayEntity("demo-day-push", "demo-programme-push", "Monday — Planche + Push", 0),
            ProgrammeDayEntity("demo-day-legs", "demo-programme-legs", "Wednesday — Legs + Mobility", 0),
            ProgrammeDayEntity("demo-day-pull", "demo-programme-pull", "Saturday — Muscle-up + Pull", 0),
            ProgrammeDayEntity("demo-day-front-splits", "demo-programme-stretch", "Front Splits", 0),
            ProgrammeDayEntity("demo-day-forward-fold", "demo-programme-stretch", "Forward Fold", 1),
            ProgrammeDayEntity("demo-day-side-splits", "demo-programme-stretch", "Side Splits", 2),
            ProgrammeDayEntity("demo-day-bridge", "demo-programme-stretch", "Bridge", 3),
        )

        private fun programmeExercise(day: String, exercise: String, position: Int, rest: Int = 120) = ProgrammeExerciseEntity(
            id = "$day:$exercise",
            programmeDayId = day,
            exerciseId = exercise,
            position = position,
            targetSets = 3,
            targetRepsMin = null,
            targetRepsMax = null,
            targetHoldSeconds = null,
            restSeconds = rest,
            targetRpe = null,
            notes = "",
            supersetGroup = null,
        )

        private val DEMO_PROGRAMME_EXERCISES = buildList {
            listOf("core:handstand", "core:planche", "core:muscle-up", "core:bench-press", "core:dip", "core:leg-raise").forEachIndexed { i, id -> add(programmeExercise("demo-day-push", id, i)) }
            listOf("core:handstand", "core:squat", "core:lunge", "core:cossack-squat", "core:jefferson-curl").forEachIndexed { i, id -> add(programmeExercise("demo-day-legs", id, i)) }
            listOf("core:handstand", "core:muscle-up", "core:planche", "core:pull-up", "core:overhead-press", "core:back-extension").forEachIndexed { i, id -> add(programmeExercise("demo-day-pull", id, i)) }
            add(programmeExercise("demo-day-front-splits", "core:front-split", 0, 60))
            add(programmeExercise("demo-day-forward-fold", "core:forward-fold", 0, 60))
            add(programmeExercise("demo-day-side-splits", "core:side-split", 0, 60))
            add(programmeExercise("demo-day-bridge", "core:bridge", 0, 60))
        }

        private val DEMO_TRACKERS = listOf(
            TrackerEntity("demo-piano", "Piano", true),
            TrackerEntity("demo-meditation", "Meditation", true),
            TrackerEntity("demo-protein", "Protein", true),
        )
        private val DEMO_TRACKER_FIELDS = listOf(
            TrackerFieldEntity("demo-piano-minutes", "demo-piano", "Duration", "DURATION", "min", null, null, 0),
            TrackerFieldEntity("demo-meditation-minutes", "demo-meditation", "Duration", "DURATION", "min", "AT_LEAST", 10.0, 0),
            TrackerFieldEntity("demo-protein-grams", "demo-protein", "Protein", "NUMBER", "g", "AT_LEAST", 120.0, 0),
        )
    }
}
