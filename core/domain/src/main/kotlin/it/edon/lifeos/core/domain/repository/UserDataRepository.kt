package it.edon.lifeos.core.domain.repository

/** The data kept on this device for the people who signed in. */
interface UserDataRepository {
    /** Removes everything stored locally for [userId], including what was not synchronized. */
    suspend fun deleteAllFor(userId: String)
}
