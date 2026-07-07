package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CreationEntity::class,
        UserStatsEntity::class,
        UserEntity::class,
        CourseEntity::class,
        ModuleEntity::class,
        LessonEntity::class,
        EnrollmentEntity::class,
        ProgressEntity::class,
        CertificateEntity::class,
        PaymentEntity::class,
        NotificationEntity::class,
        CouponEntity::class,
        QuizAttemptEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun creationDao(): CreationDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun lmsDao(): LmsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_creator_hub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
