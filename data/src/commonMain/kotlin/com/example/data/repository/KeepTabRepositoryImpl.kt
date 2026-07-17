package com.example.data.repository

import com.example.data.database.dao.KeepTabDao
import com.example.data.database.entity.KeepTabRecord
import com.example.data.local.PlatformSettings
import com.example.data.local.mapper.toDomain
import com.example.data.remote.EugeneApiService
import com.example.domain.model.KeepTab
import com.example.domain.repository.KeepTabRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class KeepTabRepositoryImpl(
    private val keepTabDao: KeepTabDao,
    private val apiService: EugeneApiService,
    private val platformSettings: PlatformSettings
) : KeepTabRepository {

    private val KEY_ACTIVE_UID = "active_uid"

    override fun observeTracked(trackerUid: String): Flow<List<KeepTab>> {
        return keepTabDao.observeTracked(trackerUid).map { records ->
            records.map { it.toDomain() }
        }
    }

    override fun isTracking(trackerUid: String, trackedUid: String): Flow<Boolean> {
        return keepTabDao.isTracking(trackerUid, trackedUid)
    }

    override suspend fun track(trackerUid: String, trackedUid: String): Result<Unit> {
        return try {
            val id = "tab_${trackerUid}_${trackedUid}"
            val record = KeepTabRecord(
                id = id,
                trackerUid = trackerUid,
                trackedUid = trackedUid,
                createdAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                syncedAt = null
            )
            keepTabDao.insert(record)
            
            try {
                apiService.trackUser(trackerUid, trackedUid)
                keepTabDao.insert(record.copy(syncedAt = Clock.System.now().toEpochMilliseconds()))
            } catch (e: Exception) {
                // Ignore remote exception to preserve optimistic tracking
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun untrack(trackerUid: String, trackedUid: String): Result<Unit> {
        return try {
            keepTabDao.delete(trackerUid, trackedUid)
            
            try {
                apiService.untrackUser(trackerUid, trackedUid)
            } catch (e: Exception) {
                // Ignore remote exception to preserve optimistic untracking
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sync(): Result<Unit> {
        return try {
            val trackerUid = platformSettings.getString(KEY_ACTIVE_UID) ?: return Result.success(Unit)
            
            val localTabs = keepTabDao.observeTracked(trackerUid).first()
            
            localTabs.filter { it.syncedAt == null }.forEach { tab ->
                try {
                    apiService.trackUser(trackerUid, tab.trackedUid)
                } catch (e: Exception) {
                    // Ignore individual failures
                }
            }
            
            val remoteTabsDto = apiService.getKeepTabs(trackerUid)
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val remoteRecords = remoteTabsDto.map { dto ->
                KeepTabRecord(
                    id = dto.id,
                    trackerUid = dto.trackerUid,
                    trackedUid = dto.trackedUid,
                    createdAtEpochMs = dto.createdAt.toEpochMilliseconds(),
                    syncedAt = nowMs
                )
            }
            
            keepTabDao.reconcile(trackerUid, remoteRecords)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
