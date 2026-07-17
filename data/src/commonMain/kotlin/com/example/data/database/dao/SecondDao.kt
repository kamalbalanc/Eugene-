package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.SecondRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SecondDao {
    @Query("SELECT * FROM seconds ORDER BY castAtEpochMs DESC")
    fun observeSeconds(): Flow<List<SecondRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(second: SecondRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(seconds: List<SecondRecord>)
}
