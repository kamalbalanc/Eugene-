package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.SecondingSnapshotRecord
import com.example.data.database.entity.SecondingSnapshotOutcomeRecord
import com.example.data.database.entity.SecondingSnapshotDownsampledRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SecondingSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: SecondingSnapshotRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshotOutcomes(outcomes: List<SecondingSnapshotOutcomeRecord>)

    @Query("SELECT * FROM seconding_snapshots WHERE predictionId = :predictionId ORDER BY timestamp ASC")
    fun observeSnapshots(predictionId: String): Flow<List<SecondingSnapshotRecord>>

    @Query("SELECT * FROM seconding_snapshot_outcomes WHERE predictionId = :predictionId ORDER BY timestamp ASC")
    fun observeSnapshotOutcomes(predictionId: String): Flow<List<SecondingSnapshotOutcomeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownsampled(downsampled: List<SecondingSnapshotDownsampledRecord>)

    @Query("SELECT * FROM seconding_snapshot_downsampled WHERE predictionId = :predictionId ORDER BY bucketStart ASC")
    fun observeDownsampled(predictionId: String): Flow<List<SecondingSnapshotDownsampledRecord>>
}
