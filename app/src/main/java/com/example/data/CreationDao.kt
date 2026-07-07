package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CreationDao {
    @Query("SELECT * FROM creations ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CreationEntity>>

    @Query("SELECT * FROM creations WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedProjects(): Flow<List<CreationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreation(creation: CreationEntity): Long

    @Update
    suspend fun updateCreation(creation: CreationEntity)

    @Query("DELETE FROM creations WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM creations")
    suspend fun clearAll()

    @Query("UPDATE creations SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateSavedStatus(id: Int, isSaved: Boolean)
}
