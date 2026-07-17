package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.UserRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun observeUser(uid: String): Flow<UserRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserRecord)

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun delete(uid: String)

    @Query("DELETE FROM users")
    suspend fun clear()
}
