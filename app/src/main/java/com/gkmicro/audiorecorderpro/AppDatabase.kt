package com.gkmicro.audiorecorderpro

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Recording::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao
}