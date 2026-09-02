package com.example.data.cloud

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Supported Entity types for synchronization.
 */
enum class SyncEntityType {
    STUDENT,
    ATTENDANCE,
    TRAINING_RECORD,
    TEST_ATTEMPT,
    MOCK_TEST,
    NOTICE,
    RECRUITMENT,
    SUBJECT,
    TOPIC,
    QUESTION,
    RACE_SESSION,
    RACE_RESULT,
    CHAPTER,
    NOTIFICATION
}

enum class SyncOperation {
    UPSERT,
    DELETE
}

enum class SyncState {
    PENDING,
    IN_PROGRESS,
    SYNCED,
    TRANSIENT_FAILURE,
    PERMANENT_FAILURE
}

/**
 * Item in the offline-first Outbox queue for Cloud Firestore sync.
 * Guarantees idempotency and prevents duplicate tasks.
 */
data class SyncOutboxItem(
    val id: String = UUID.randomUUID().toString(),
    val entityType: SyncEntityType,
    val localRecordId: String,
    val firestoreDocId: String,
    val operation: SyncOperation = SyncOperation.UPSERT,
    val studentId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val syncState: SyncState = SyncState.PENDING,
    val lastError: String? = null,
    val nextRetryTime: Long = 0L
) {
    val isPermanent: Boolean get() = syncState == SyncState.PERMANENT_FAILURE
}

data class SyncDiagnostics(
    val lastSyncTimestamp: Long = 0L,
    val pendingCount: Int = 0,
    val failedCount: Int = 0,
    val syncedCount: Int = 0,
    val permanentFailureCount: Int = 0,
    val activeRole: String = "UNAUTHENTICATED",
    val activeStudentId: String? = null,
    val lastError: String? = null
)

/**
 * Manages the offline-first Sync Outbox queue.
 * - Thread-safe state with Mutex.
 * - Deduplicates pending items with identical firestoreDocId.
 * - Calculates bounded exponential backoff on transient errors.
 * - Halts infinite retries on permanent failures (Permission Denied, Malformed, Security Rule Rejection).
 */
class SyncOutboxManager {
    private val mutex = Mutex()
    private val queue = mutableMapOf<String, SyncOutboxItem>() // Key: entityType + "_" + firestoreDocId

    private var lastSyncTime: Long = 0L
    private var totalSyncedCount: Int = 0

    private val _diagnostics = MutableStateFlow(SyncDiagnostics())
    val diagnostics: StateFlow<SyncDiagnostics> = _diagnostics.asStateFlow()

    suspend fun enqueue(
        entityType: SyncEntityType,
        localRecordId: String,
        firestoreDocId: String,
        operation: SyncOperation = SyncOperation.UPSERT,
        studentId: String? = null
    ): SyncOutboxItem = mutex.withLock {
        val deduplicationKey = "${entityType.name}_$firestoreDocId"
        val existing = queue[deduplicationKey]

        val item = if (existing != null && existing.syncState != SyncState.SYNCED) {
            // Update existing pending item with latest operation/timestamp without creating duplicate
            existing.copy(
                operation = operation,
                timestamp = System.currentTimeMillis(),
                syncState = SyncState.PENDING,
                studentId = studentId ?: existing.studentId
            )
        } else {
            SyncOutboxItem(
                entityType = entityType,
                localRecordId = localRecordId,
                firestoreDocId = firestoreDocId,
                operation = operation,
                studentId = studentId,
                timestamp = System.currentTimeMillis(),
                syncState = SyncState.PENDING
            )
        }

        queue[deduplicationKey] = item
        updateDiagnosticsLocked()
        item
    }

    suspend fun getPendingItems(
        currentStudentId: String?,
        isAdmin: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<SyncOutboxItem> = mutex.withLock {
        queue.values.filter { item ->
            if (item.syncState == SyncState.SYNCED || item.syncState == SyncState.PERMANENT_FAILURE) {
                return@filter false
            }
            if (item.nextRetryTime > currentTime) {
                return@filter false // Wait for backoff interval
            }

            // Security boundary filter: Students can only sync their own records
            if (isAdmin) {
                true // Admins can sync all queues
            } else if (currentStudentId != null) {
                // Cadet can only process own student items or unowned public items
                item.studentId == null || item.studentId == currentStudentId
            } else {
                // Unauthenticated users cannot sync private items
                item.studentId == null && (item.entityType == SyncEntityType.NOTICE || item.entityType == SyncEntityType.RECRUITMENT)
            }
        }.sortedBy { it.timestamp }
    }

    suspend fun markInProgress(item: SyncOutboxItem) = mutex.withLock {
        val key = "${item.entityType.name}_${item.firestoreDocId}"
        queue[key] = item.copy(syncState = SyncState.IN_PROGRESS)
        updateDiagnosticsLocked()
    }

    suspend fun markSuccess(item: SyncOutboxItem) = mutex.withLock {
        val key = "${item.entityType.name}_${item.firestoreDocId}"
        queue[key] = item.copy(
            syncState = SyncState.SYNCED,
            lastError = null
        )
        lastSyncTime = System.currentTimeMillis()
        totalSyncedCount++
        updateDiagnosticsLocked()
    }

    suspend fun markFailure(
        item: SyncOutboxItem,
        error: Throwable,
        isPermanent: Boolean = false
    ) = mutex.withLock {
        val key = "${item.entityType.name}_${item.firestoreDocId}"
        val newRetry = item.retryCount + 1

        val finalPermanent = isPermanent || isPermanentError(error) || newRetry >= MAX_RETRY_ATTEMPTS

        // Bounded exponential backoff: 1s, 2s, 4s, 8s, 16s, max 60s
        val backoffDelayMs = if (finalPermanent) {
            0L
        } else {
            val exponent = (newRetry - 1).coerceIn(0, 6)
            minOf(MAX_BACKOFF_MS, INITIAL_BACKOFF_MS * (1L shl exponent))
        }

        val updated = item.copy(
            retryCount = newRetry,
            syncState = if (finalPermanent) SyncState.PERMANENT_FAILURE else SyncState.TRANSIENT_FAILURE,
            lastError = error.localizedMessage ?: error.message ?: "Unknown Error",
            nextRetryTime = if (finalPermanent) 0L else (System.currentTimeMillis() + backoffDelayMs)
        )

        queue[key] = updated
        updateDiagnosticsLocked(error.message)
    }

    suspend fun clearCompleted() = mutex.withLock {
        val iterator = queue.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.syncState == SyncState.SYNCED) {
                iterator.remove()
            }
        }
        updateDiagnosticsLocked()
    }

    suspend fun getQueueSnapshot(): List<SyncOutboxItem> = mutex.withLock {
        queue.values.toList()
    }

    private fun isPermanentError(error: Throwable): Boolean {
        val msg = (error.message ?: "").lowercase()
        return msg.contains("permission_denied") ||
                msg.contains("permission denied") ||
                msg.contains("forbidden") ||
                msg.contains("unauthenticated") ||
                msg.contains("invalid-argument") ||
                msg.contains("cross_student_forbidden")
    }

    private fun updateDiagnosticsLocked(lastErrorMessage: String? = null) {
        val pending = queue.values.count { it.syncState == SyncState.PENDING || it.syncState == SyncState.IN_PROGRESS }
        val transientFailed = queue.values.count { it.syncState == SyncState.TRANSIENT_FAILURE }
        val permFailed = queue.values.count { it.syncState == SyncState.PERMANENT_FAILURE }

        _diagnostics.value = SyncDiagnostics(
            lastSyncTimestamp = lastSyncTime,
            pendingCount = pending,
            failedCount = transientFailed,
            syncedCount = totalSyncedCount,
            permanentFailureCount = permFailed,
            lastError = lastErrorMessage ?: _diagnostics.value.lastError
        )
    }

    companion object {
        const val INITIAL_BACKOFF_MS = 1000L
        const val MAX_BACKOFF_MS = 60000L
        const val MAX_RETRY_ATTEMPTS = 5
    }
}
