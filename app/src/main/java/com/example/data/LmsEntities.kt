package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lms_users")
data class UserEntity(
    @PrimaryKey val id: String, // Firebase UUID or custom generated unique string
    val name: String,
    val email: String,
    val role: String, // "Admin" or "Student"
    val profileImg: String,
    val joinedDate: Long = System.currentTimeMillis(),
    val isBlocked: Boolean = false,
    val passwordHash: String // Local secure storage for simulated firebase dynamic auth verification
)

@Entity(tableName = "lms_courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val instructor: String,
    val category: String,
    val duration: String,
    val lessonsCount: Int,
    val rating: Double,
    val ratingCount: Int,
    val price: Double,
    val isFree: Boolean,
    val discountPercent: Int = 0,
    val isPublished: Boolean = true,
    val thumbnail: String,
    val couponCode: String = "",
    val certificateAvailable: Boolean = true
)

@Entity(tableName = "lms_modules")
data class ModuleEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val orderIndex: Int
)

@Entity(tableName = "lms_lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val moduleId: String,
    val title: String,
    val description: String,
    val orderIndex: Int,
    val lessonType: String, // "Video", "PDF", "Text", "Assignment", "Quiz"
    val videoUrl: String = "", // Firebase Storage url or simulation url
    val pdfUrl: String = "",
    val textBody: String = "",
    val quizQuestions: String = "", // JSON string containing quiz question cards
    val assignmentDesc: String = "",
    val durationMin: Int = 10
)

@Entity(tableName = "lms_enrollments")
data class EnrollmentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val courseId: String,
    val enrolledDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "lms_progress")
data class ProgressEntity(
    @PrimaryKey val id: String, // userId + "_" + lessonId
    val userId: String,
    val courseId: String,
    val lessonId: String,
    val isCompleted: Boolean = false,
    val lastAccessedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "lms_certificates")
data class CertificateEntity(
    @PrimaryKey val id: String, // userId + "_" + courseId
    val userId: String,
    val courseId: String,
    val issueDate: Long = System.currentTimeMillis(),
    val certificateUrl: String = "",
    val verificationCode: String
)

@Entity(tableName = "lms_payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val courseId: String,
    val amount: Double,
    val transactionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "Success", "Pending", "Failed"
    val couponCodeUsed: String = ""
)

@Entity(tableName = "lms_notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String, // "All" or a specific user UUID
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String // "Enrollment", "Update", "Promotion", "Announcement"
)

@Entity(tableName = "lms_coupons")
data class CouponEntity(
    @PrimaryKey val id: String,
    val code: String,
    val discountPercent: Int,
    val isActive: Boolean = true
)

@Entity(tableName = "lms_quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val lessonId: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)
