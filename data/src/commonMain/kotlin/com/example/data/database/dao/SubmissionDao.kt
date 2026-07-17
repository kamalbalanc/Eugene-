package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.SubmissionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SubmissionDao {
    @Query("SELECT * FROM submissions ORDER BY submittedAtEpochMs DESC")
    fun observeSubmissions(): Flow<List<SubmissionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(submission: SubmissionRecord)

    @Query("UPDATE submissions SET state = :state WHERE id = :id")
    suspend fun updateState(id: String, state: String)
}
