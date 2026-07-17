package com.example.data.repository

import com.example.data.database.dao.UserDao
import com.example.data.database.entity.UserRecord
import com.example.data.local.PlatformSettings
import com.example.data.remote.EugeneApiService
import com.example.data.remote.mapper.toDomain
import com.example.domain.model.Session
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val apiService: EugeneApiService,
    private val platformSettings: PlatformSettings
) : AuthRepository {

    private val KEY_ACTIVE_UID = "active_uid"
    private val KEY_ACTIVE_EMAIL = "active_email"

    private val activeUidFlow = MutableStateFlow<String?>(platformSettings.getString(KEY_ACTIVE_UID))

    override fun observeSession(): Flow<Session> {
        return activeUidFlow.flatMapLatest { uid ->
            if (uid == null) {
                flowOf(Session.Guest)
            } else {
                userDao.observeUser(uid).map { record ->
                    if (record == null) {
                        Session.Guest
                    } else {
                        Session.Authenticated(
                            uid = record.uid,
                            email = platformSettings.getString(KEY_ACTIVE_EMAIL) ?: record.email,
                            name = record.name,
                            handle = record.handle,
                            avatarUrl = record.avatarUrl,
                            accuracy = record.accuracy,
                            reputation = record.reputation,
                            resolvedPredictionCount = record.resolvedPredictionCount
                        )
                    }
                }
            }
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Session.Authenticated> {
        return try {
            val userDto = apiService.signInWithEmail(email, password)
            val domainUser = userDto.toDomain()
            saveSessionLocally(domainUser, email)
            Result.success(domainUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, name: String, handle: String): Result<Session.Authenticated> {
        return try {
            val userDto = apiService.signUpWithEmail(email, password, name, handle)
            val domainUser = userDto.toDomain()
            saveSessionLocally(domainUser, email)
            Result.success(domainUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Session.Authenticated> {
        return try {
            val userDto = apiService.signInWithGoogle(idToken)
            val domainUser = userDto.toDomain()
            saveSessionLocally(domainUser, domainUser.email)
            Result.success(domainUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            val uid = activeUidFlow.value
            if (uid != null) {
                userDao.delete(uid)
            }
            platformSettings.remove(KEY_ACTIVE_UID)
            platformSettings.remove(KEY_ACTIVE_EMAIL)
            activeUidFlow.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(name: String, handle: String): Result<Unit> {
        return try {
            apiService.updateProfile(name, handle)
            val uid = activeUidFlow.value
            if (uid != null) {
                val current = userDao.observeUser(uid).first()
                if (current != null) {
                    val updated = current.copy(name = name, handle = handle)
                    userDao.insert(updated)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveSessionLocally(user: Session.Authenticated, email: String) {
        val record = UserRecord(
            uid = user.uid,
            email = email,
            name = user.name,
            handle = user.handle,
            avatarUrl = user.avatarUrl,
            accuracy = user.accuracy,
            reputation = user.reputation,
            resolvedPredictionCount = user.resolvedPredictionCount
        )
        userDao.insert(record)
        platformSettings.setString(KEY_ACTIVE_UID, user.uid)
        platformSettings.setString(KEY_ACTIVE_EMAIL, email)
        activeUidFlow.value = user.uid
    }
}
