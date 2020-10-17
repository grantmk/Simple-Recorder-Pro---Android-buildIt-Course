package com.gkmicro.audiorecorderpro

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recording")
    suspend fun getAll(): List<Recording>

    @Query("SELECT * FROM recording ORDER BY unixDateMillis DESC")
    fun recordingsByDate(): PagingSource<Int, Recording>

    @Update
    suspend fun update(recording: Recording): Int

    @Insert
    suspend fun insert(recording: Recording): Long

    @Delete
    suspend fun delete(recording: Recording)
}