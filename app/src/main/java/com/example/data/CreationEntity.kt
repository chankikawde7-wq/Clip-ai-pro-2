package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "creations")
data class CreationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val toolName: String,
    val category: String,
    val inputPrompt: String,
    val outputText: String,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
