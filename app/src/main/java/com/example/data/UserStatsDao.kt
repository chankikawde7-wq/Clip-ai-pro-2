package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 'current_user' LIMIT 1")
    fun getUserStatsFlow(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 'current_user' LIMIT 1")
    suspend fun getUserStats(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStatsEntity)

    @Query("UPDATE user_stats SET isLoggedIn = :loggedIn, loginProvider = :provider, email = :email WHERE id = 'current_user'")
    suspend fun updateAuthStatus(loggedIn: Boolean, provider: String, email: String)

    @Query("UPDATE user_stats SET subscriptionPlan = :plan, totalCredits = :credits WHERE id = 'current_user'")
    suspend fun updateSubscription(plan: String, credits: Int)

    @Query("UPDATE user_stats SET creditsUsed = creditsUsed + 1 WHERE id = 'current_user'")
    suspend fun incrementUsage()
}
