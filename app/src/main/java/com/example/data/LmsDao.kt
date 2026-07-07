package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LmsDao {
    // ==========================================
    // USER OPERATIONS
    // ==========================================
    @Query("SELECT * FROM lms_users WHERE role = 'Student'")
    fun getAllStudentsFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM lms_users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM lms_users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE lms_users SET isBlocked = :isBlocked WHERE id = :userId")
    suspend fun setBlockedStatus(userId: String, isBlocked: Boolean)


    // ==========================================
    // COURSE OPERATIONS
    // ==========================================
    @Query("SELECT * FROM lms_courses")
    fun getAllCoursesFlow(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM lms_courses WHERE isPublished = 1")
    fun getPublishedCoursesFlow(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM lms_courses WHERE id = :courseId LIMIT 1")
    suspend fun getCourseById(courseId: String): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)


    // ==========================================
    // MODULE OPERATIONS
    // ==========================================
    @Query("SELECT * FROM lms_modules WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getModulesForCourseFlow(courseId: String): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM lms_modules WHERE courseId = :courseId ORDER BY orderIndex ASC")
    suspend fun getModulesForCourseDirect(courseId: String): List<ModuleEntity>

    @Query("SELECT * FROM lms_modules WHERE id = :id LIMIT 1")
    suspend fun getModuleById(id: String): ModuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: ModuleEntity)

    @Update
    suspend fun updateModule(module: ModuleEntity)

    @Query("DELETE FROM lms_modules WHERE id = :moduleId")
    suspend fun deleteModuleById(moduleId: String)

    @Query("DELETE FROM lms_lessons WHERE moduleId = :moduleId")
    suspend fun deleteLessonsForModule(moduleId: String)


    // ==========================================
    // LESSON OPERATIONS
    // ==========================================
    @Query("SELECT * FROM lms_lessons WHERE moduleId = :moduleId ORDER BY orderIndex ASC")
    fun getLessonsForModuleFlow(moduleId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lms_lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Query("DELETE FROM lms_lessons WHERE id = :lessonId")
    suspend fun deleteLessonById(lessonId: String)


    // ==========================================
    // ENROLLMENT OPERATIONS
    // ==========================================
    @Query("SELECT * FROM lms_enrollments WHERE userId = :userId")
    fun getEnrollmentsByStudentFlow(userId: String): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM lms_enrollments WHERE userId = :userId AND courseId = :courseId LIMIT 1")
    suspend fun getEnrollment(userId: String, courseId: String): EnrollmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: EnrollmentEntity)

    @Query("DELETE FROM lms_enrollments WHERE userId = :userId AND courseId = :courseId")
    suspend fun unenrollStudent(userId: String, courseId: String)

    @Query("SELECT * FROM lms_enrollments")
    fun getAllEnrollmentsFlow(): Flow<List<EnrollmentEntity>>


    // ==========================================
    // PROGRESS TRACKING
    // ==========================================
    @Query("SELECT * FROM lms_progress WHERE userId = :userId AND courseId = :courseId")
    fun getCourseProgressFlow(userId: String, courseId: String): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM lms_progress WHERE userId = :userId AND courseId = :courseId")
    suspend fun getCourseProgressDirect(userId: String, courseId: String): List<ProgressEntity>

    @Query("SELECT * FROM lms_progress WHERE userId = :userId AND lessonId = :lessonId LIMIT 1")
    suspend fun getLessonProgress(userId: String, lessonId: String): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)


    // ==========================================
    // CERTIFICATES
    // ==========================================
    @Query("SELECT * FROM lms_certificates WHERE userId = :userId")
    fun getCertificatesFlow(userId: String): Flow<List<CertificateEntity>>

    @Query("SELECT * FROM lms_certificates WHERE userId = :userId AND courseId = :courseId LIMIT 1")
    suspend fun getCertificateForCourse(userId: String, courseId: String): CertificateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: CertificateEntity)


    // ==========================================
    // PAYMENTS & REVENUE
    // ==========================================
    @Query("SELECT * FROM lms_payments ORDER BY timestamp DESC")
    fun getAllPaymentsFlow(): Flow<List<PaymentEntity>>

    @Query("SELECT SUM(amount) FROM lms_payments WHERE status = 'Success'")
    fun getTotalRevenueFlow(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)


    // ==========================================
    // NOTIFICATIONS
    // ==========================================
    @Query("SELECT * FROM lms_notifications WHERE userId = :userId OR userId = 'All' ORDER BY timestamp DESC")
    fun getNotificationsForUserFlow(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE lms_notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsAsRead(userId: String)


    // ==========================================
    // COUPON OPERATIONS
    // ==========================================
    @Query("SELECT * FROM lms_coupons")
    fun getAllCouponsFlow(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM lms_coupons WHERE code = :code AND isActive = 1 LIMIT 1")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Query("DELETE FROM lms_coupons WHERE id = :id")
    suspend fun deleteCouponById(id: String)


    // ==========================================
    // QUIZ ATTEMPTS
    // ==========================================
    @Query("SELECT * FROM lms_quiz_attempts WHERE userId = :userId")
    fun getQuizAttemptsForUserFlow(userId: String): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizAttempt(attempt: QuizAttemptEntity)
}
