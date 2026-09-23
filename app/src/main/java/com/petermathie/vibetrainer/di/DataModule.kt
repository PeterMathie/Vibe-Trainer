package com.petermathie.vibetrainer.di

import android.content.Context
import androidx.room.Room
import com.petermathie.vibetrainer.data.local.CatalogueDao
import com.petermathie.vibetrainer.data.local.ProgrammeDao
import com.petermathie.vibetrainer.data.local.TrackerDao
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.data.local.WorkoutDao
import com.petermathie.vibetrainer.data.local.MIGRATION_1_2
import com.petermathie.vibetrainer.data.local.MIGRATION_2_3
import com.petermathie.vibetrainer.data.local.MIGRATION_3_4
import com.petermathie.vibetrainer.data.local.MIGRATION_4_5
import com.petermathie.vibetrainer.data.local.MIGRATION_5_6
import com.petermathie.vibetrainer.data.local.MIGRATION_6_7
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): VibeDatabase =
        Room.databaseBuilder(context, VibeDatabase::class.java, "vibe-trainer.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .build()

    @Provides fun catalogueDao(db: VibeDatabase): CatalogueDao = db.catalogueDao()
    @Provides fun programmeDao(db: VibeDatabase): ProgrammeDao = db.programmeDao()
    @Provides fun workoutDao(db: VibeDatabase): WorkoutDao = db.workoutDao()
    @Provides fun trackerDao(db: VibeDatabase): TrackerDao = db.trackerDao()
}
