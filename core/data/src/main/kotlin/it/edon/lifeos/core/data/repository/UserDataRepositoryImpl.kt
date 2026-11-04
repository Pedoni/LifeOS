package it.edon.lifeos.core.data.repository

import it.edon.lifeos.core.database.dao.CategoryDao
import it.edon.lifeos.core.domain.repository.UserDataRepository
import javax.inject.Inject

/** Every table that holds user-owned rows must be cleared here when it is added. */
internal class UserDataRepositoryImpl @Inject constructor(private val categoryDao: CategoryDao) :
    UserDataRepository {

    override suspend fun deleteAllFor(userId: String) {
        categoryDao.deleteAllForUser(userId)
    }
}
