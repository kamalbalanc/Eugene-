package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.DiscourseRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscourseDao {
    @Query("SELECT * FROM discourse_entries WHERE predictionId = :predictionId ORDER BY postedAtEpochMs ASC")
    fun observeDiscourse(predictionId: String): Flow<List<DiscourseRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiscourseRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DiscourseRecord>)

    @Query("UPDATE discourse_entries SET helpfulCount = helpfulCount + 1 WHERE id = :id")
    suspend fun incrementHelpfulCount(id: String)

    @Query("UPDATE discourse_entries SET flagCount = flagCount + 1 WHERE id = :id")
    suspend fun incrementFlagCount(id: String)
}
