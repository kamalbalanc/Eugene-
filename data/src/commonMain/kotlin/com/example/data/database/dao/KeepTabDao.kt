package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.database.entity.KeepTabRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface KeepTabDao {
    @Query("SELECT * FROM keep_tabs WHERE trackerUid = :trackerUid")
    fun observeTracked(trackerUid: String): Flow<List<KeepTabRecord>>

    @Query("SELECT EXISTS(SELECT 1 FROM keep_tabs WHERE trackerUid = :trackerUid AND trackedUid = :trackedUid)")
    fun isTracking(trackerUid: String, trackedUid: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keepTab: KeepTabRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keepTabs: List<KeepTabRecord>)

    @Query("DELETE FROM keep_tabs WHERE trackerUid = :trackerUid AND trackedUid = :trackedUid")
    suspend fun delete(trackerUid: String, trackedUid: String)

    @Query("DELETE FROM keep_tabs WHERE trackerUid = :trackerUid")
    suspend fun deleteForTracker(trackerUid: String)

    @Transaction
    suspend fun reconcile(trackerUid: String, remoteTabs: List<KeepTabRecord>) {
        deleteForTracker(trackerUid)
        insertAll(remoteTabs)
    }
}
