package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.AppealSubmissionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AppealSubmissionDao {
    @Query("SELECT * FROM appeal_submissions ORDER BY submittedAtEpochMs DESC")
    fun observeAppeals(): Flow<List<AppealSubmissionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appeal: AppealSubmissionRecord)
}
