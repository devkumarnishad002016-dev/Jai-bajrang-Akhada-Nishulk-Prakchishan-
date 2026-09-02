package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a ground running race / trial session (e.g. 1600m Army GD batch, 400m sprint, etc.)
 */
@Entity(
    tableName = "race_sessions",
    indices = [Index(value = ["date"]), Index(value = ["status"])]
)
data class RaceSession(
    @PrimaryKey val raceId: String = UUID.randomUUID().toString(),
    val batchName: String,
    val distanceMeters: Int = 1600,
    val date: String, // YYYY-MM-DD
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long? = null,
    val status: String = "READY", // "READY", "RUNNING", "PAUSED", "COMPLETED"
    val notes: String = "",
    val recordedBy: String = "TRAINER",
    val totalParticipants: Int = 0,
    val finishedCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Individual participant result in a live race session.
 */
@Entity(
    tableName = "race_results",
    indices = [
        Index(value = ["raceId", "studentId"], unique = true),
        Index(value = ["raceId"]),
        Index(value = ["studentId"])
    ]
)
data class RaceResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val raceId: String,
    val studentId: String,
    val studentName: String,
    val chestNumber: String = "",
    val village: String = "",
    val status: String = "RUNNING", // "RUNNING", "FINISHED", "DNF", "DNS"
    val finishTimestamp: Long? = null,
    val elapsedMillis: Long = 0L,
    val timeFormatted: String = "", // e.g. "05:14.28"
    val rank: Int = 0, // 1st, 2nd, 3rd (0 for unranked / DNF)
    val isPersonalBest: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
