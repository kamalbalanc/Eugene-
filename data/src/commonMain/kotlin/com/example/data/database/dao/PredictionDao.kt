package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.PredictionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    @Query("SELECT * FROM predictions ORDER BY createdAtEpochMs DESC")
    fun observePredictions(): Flow<List<PredictionRecord>>

    @Query("SELECT * FROM predictions WHERE id = :id")
    fun observePredictionById(id: String): Flow<PredictionRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prediction: PredictionRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(predictions: List<PredictionRecord>)

    @Query("DELETE FROM predictions")
    suspend fun clearAll()
}
