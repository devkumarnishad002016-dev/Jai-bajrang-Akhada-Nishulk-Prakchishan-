package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persistent Outbox Queue Entity for Offline-First Cloud Firestore Synchronization.
 * Stores pending and in-flight operations locally in Room SQLite database.
 * Survives app crashes, force-stops, device reboots, and prolonged offline periods.
 */
@Entity(
    tableName = "outbox_items",
    indices = [
        Index(value = ["deduplicationKey"], unique = true),
        Index(value = ["syncState"]),
        Index(value = ["studentId"]),
        Index(value = ["timestamp"])
    ]
)
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deduplicationKey: String, // format: ${entityType}_${firestoreDocId}
    val entityType: String, // STUDENT, ATTENDANCE, TRAINING_RECORD, etc.
    val localRecordId: String,
    val firestoreDocId: String,
    val operation: String = "UPSERT", // UPSERT or DELETE
    val studentId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val syncState: String = "PENDING", // PENDING, IN_PROGRESS, SYNCED, TRANSIENT_FAILURE, PERMANENT_FAILURE
    val lastError: String? = null,
    val nextRetryTime: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long = 0L
)
