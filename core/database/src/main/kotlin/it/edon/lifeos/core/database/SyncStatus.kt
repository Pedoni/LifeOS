package it.edon.lifeos.core.database

/** Local-only synchronization state of a row; it is never sent to the backend. */
enum class SyncStatus {
    SYNCED,
    PENDING_UPLOAD,
    PENDING_DELETE,
    SYNC_ERROR
}
