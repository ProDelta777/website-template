package com.skinlens.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress_table ORDER BY timestamp DESC")
    fun getAllProgress(): Flow<List<ProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)

    @Query("DELETE FROM progress_table")
    suspend fun deleteAllProgress()
}
