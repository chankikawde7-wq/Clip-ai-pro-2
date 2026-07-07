package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: String = "current_user",
    val email: String = "chankikawde7@gmail.com",
    val isLoggedIn: Boolean = false,
    val loginProvider: String = "None", // "Google", "Email", "None"
    val subscriptionPlan: String = "Free", // "Free", "Monthly Pro", "Yearly Pro Plan", "Pro", "Agency"
    val creditsUsed: Int = 0,
    val totalCredits: Int = 50,
    val textGenerations: Int = 0,
    val imageGenerations: Int = 0,
    val videoGenerations: Int = 0,
    val audioGenerations: Int = 0,
    val businessGenerations: Int = 0,
    val isSystemApproved: Boolean = true, // Admin moderation block check
    val language: String = "English",
    val theme: String = "System",
    val voice: String = "Default Voice",
    val aiModel: String = "Gemini 2.5 Flash",
    val personalization: Boolean = true
) {
    val isProPlan: Boolean
        get() = subscriptionPlan == "Pro" || subscriptionPlan == "Agency" || subscriptionPlan == "Monthly Pro" || subscriptionPlan == "Yearly Pro Plan"
}
