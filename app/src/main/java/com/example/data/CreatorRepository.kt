package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CreatorRepository(private val database: AppDatabase) {
    private val creationDao = database.creationDao()
    private val userStatsDao = database.userStatsDao()

    val allHistory: Flow<List<CreationEntity>> = creationDao.getAllHistory()
    val savedProjects: Flow<List<CreationEntity>> = creationDao.getSavedProjects()
    val userStats: Flow<UserStatsEntity?> = userStatsDao.getUserStatsFlow()

    suspend fun getOrCreateUserStats(): UserStatsEntity {
        var stats = userStatsDao.getUserStats()
        if (stats == null) {
            stats = UserStatsEntity(
                id = "current_user",
                email = "chankikawde7@gmail.com",
                isLoggedIn = true,
                loginProvider = "Google",
                subscriptionPlan = "Free",
                creditsUsed = 0,
                totalCredits = 200,
                textGenerations = 0,
                imageGenerations = 0,
                videoGenerations = 0,
                audioGenerations = 0,
                businessGenerations = 0,
                isSystemApproved = true
            )
            userStatsDao.insertOrUpdate(stats)
        }
        return stats
    }

    suspend fun insertCreation(creation: CreationEntity): Long {
        val id = creationDao.insertCreation(creation)
        // Also update user usage statistics based on category
        val stats = getOrCreateUserStats()
        val isFree = when (creation.toolName.trim().lowercase()) {
            "ai chat studio", "script generator", "story generator", "title generator",
            "hashtag generator", "caption generator", "dialogues generator", "prompt generator", "thumbnail text ideas" -> true
            else -> false
        }
        val isPremium = when (creation.toolName.trim().lowercase()) {
            "ai website builder", "ai app builder", "ai presentation maker", "presentation maker", "ai presentationmaker" -> true
            else -> false
        }
        val cost = if (isPremium) {
            10
        } else if (isFree) {
            0
        } else {
            when (creation.category.uppercase()) {
                "TEXT" -> 1
                "IMAGE" -> 2
                "VIDEO" -> 5
                "AUDIO" -> 3
                "BUSINESS" -> 4
                else -> 1
            }
        }
        val updatedGenCount = when (creation.category.uppercase()) {
            "TEXT" -> stats.copy(textGenerations = stats.textGenerations + 1, creditsUsed = stats.creditsUsed + cost)
            "IMAGE" -> stats.copy(imageGenerations = stats.imageGenerations + 1, creditsUsed = stats.creditsUsed + cost)
            "VIDEO" -> stats.copy(videoGenerations = stats.videoGenerations + 1, creditsUsed = stats.creditsUsed + cost)
            "AUDIO" -> stats.copy(audioGenerations = stats.audioGenerations + 1, creditsUsed = stats.creditsUsed + cost)
            "BUSINESS" -> stats.copy(businessGenerations = stats.businessGenerations + 1, creditsUsed = stats.creditsUsed + cost)
            else -> stats.copy(creditsUsed = stats.creditsUsed + cost)
        }
        userStatsDao.insertOrUpdate(updatedGenCount)
        return id
    }

    suspend fun updateCreation(creation: CreationEntity) {
        creationDao.updateCreation(creation)
    }

    suspend fun deleteCreationById(id: Int) {
        creationDao.deleteById(id)
    }

    suspend fun clearHistory() {
        creationDao.clearAll()
    }

    suspend fun toggleSaveCreation(id: Int, isSaved: Boolean) {
        creationDao.updateSavedStatus(id, isSaved)
    }

    suspend fun updateAuth(loggedIn: Boolean, provider: String, email: String) {
        userStatsDao.updateAuthStatus(loggedIn, provider, email)
    }

    suspend fun updateSubscription(plan: String, totalCredits: Int) {
        userStatsDao.updateSubscription(plan, totalCredits)
    }

    suspend fun saveUserStats(stats: UserStatsEntity) {
        userStatsDao.insertOrUpdate(stats)
    }
}
