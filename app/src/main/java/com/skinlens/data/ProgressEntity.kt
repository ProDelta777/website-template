package com.skinlens.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress_table")
data class ProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val photoUri: String,
    val overallScore: Int,
    val oiliness: String,
    val dryness: String,
    val redness: String,
    val texture: String,
    val darkSpots: String,
    val blemishLike: String
)
