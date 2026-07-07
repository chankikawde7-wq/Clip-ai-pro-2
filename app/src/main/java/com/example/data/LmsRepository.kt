package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class LmsRepository(private val lmsDao: LmsDao) {

    // Users
    val allStudents: Flow<List<UserEntity>> = lmsDao.getAllStudentsFlow()
    suspend fun getUserById(userId: String) = lmsDao.getUserById(userId)
    suspend fun getUserByEmail(email: String) = lmsDao.getUserByEmail(email)
    suspend fun insertUser(user: UserEntity) = lmsDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = lmsDao.updateUser(user)
    suspend fun setBlockedStatus(userId: String, isBlocked: Boolean) = lmsDao.setBlockedStatus(userId, isBlocked)

    // Courses
    val allCourses: Flow<List<CourseEntity>> = lmsDao.getAllCoursesFlow()
    val publishedCourses: Flow<List<CourseEntity>> = lmsDao.getPublishedCoursesFlow()
    suspend fun getCourseById(courseId: String) = lmsDao.getCourseById(courseId)
    suspend fun insertCourse(course: CourseEntity) = lmsDao.insertCourse(course)
    suspend fun updateCourse(course: CourseEntity) = lmsDao.updateCourse(course)
    suspend fun deleteCourse(course: CourseEntity) = lmsDao.deleteCourse(course)

    // Modules
    fun getModulesForCourse(courseId: String): Flow<List<ModuleEntity>> = lmsDao.getModulesForCourseFlow(courseId)
    suspend fun getModulesForCourseDirect(courseId: String): List<ModuleEntity> = lmsDao.getModulesForCourseDirect(courseId)
    suspend fun insertModule(module: ModuleEntity) = lmsDao.insertModule(module)
    suspend fun updateModule(module: ModuleEntity) = lmsDao.updateModule(module)
    suspend fun deleteModule(moduleId: String) {
        lmsDao.deleteLessonsForModule(moduleId)
        lmsDao.deleteModuleById(moduleId)
    }

    // Lessons
    fun getLessonsForModule(moduleId: String): Flow<List<LessonEntity>> = lmsDao.getLessonsForModuleFlow(moduleId)
    suspend fun getLessonById(lessonId: String) = lmsDao.getLessonById(lessonId)
    suspend fun insertLesson(lesson: LessonEntity) = lmsDao.insertLesson(lesson)
    suspend fun updateLesson(lesson: LessonEntity) = lmsDao.updateLesson(lesson)
    suspend fun deleteLesson(lessonId: String) = lmsDao.deleteLessonById(lessonId)

    // Enrollments
    fun getEnrollmentsByStudent(userId: String): Flow<List<EnrollmentEntity>> = lmsDao.getEnrollmentsByStudentFlow(userId)
    val allEnrollments: Flow<List<EnrollmentEntity>> = lmsDao.getAllEnrollmentsFlow()
    suspend fun isEnrolled(userId: String, courseId: String): Boolean {
        return lmsDao.getEnrollment(userId, courseId) != null
    }
    suspend fun enrollStudent(userId: String, courseId: String) {
        val id = UUID.randomUUID().toString()
        lmsDao.insertEnrollment(EnrollmentEntity(id, userId, courseId))
    }
    suspend fun unenrollStudent(userId: String, courseId: String) {
        lmsDao.unenrollStudent(userId, courseId)
    }

    // Progress
    fun getCourseProgressFlow(userId: String, courseId: String): Flow<List<ProgressEntity>> =
        lmsDao.getCourseProgressFlow(userId, courseId)

    suspend fun getCourseProgressDirect(userId: String, courseId: String): List<ProgressEntity> =
        lmsDao.getCourseProgressDirect(userId, courseId)

    suspend fun markLessonCompleted(userId: String, courseId: String, lessonId: String, isCompleted: Boolean) {
        val id = "${userId}_$lessonId"
        lmsDao.insertProgress(
            ProgressEntity(
                id = id,
                userId = userId,
                courseId = courseId,
                lessonId = lessonId,
                isCompleted = isCompleted,
                lastAccessedTimestamp = System.currentTimeMillis()
            )
        )
    }

    // Certificates
    fun getCertificates(userId: String): Flow<List<CertificateEntity>> = lmsDao.getCertificatesFlow(userId)
    suspend fun getCertificateForCourse(userId: String, courseId: String) = lmsDao.getCertificateForCourse(userId, courseId)
    suspend fun issueCertificate(userId: String, courseId: String, courseName: String) {
        val verificationCode = "CERT-" + UUID.randomUUID().toString().uppercase().take(8)
        val id = "${userId}_$courseId"
        lmsDao.insertCertificate(
            CertificateEntity(
                id = id,
                userId = userId,
                courseId = courseId,
                issueDate = System.currentTimeMillis(),
                certificateUrl = "https://images.unsplash.com/photo-1578301978693-85fa9c0320b9?auto=format&fit=crop&q=80&w=600",
                verificationCode = verificationCode
            )
        )
    }

    // Payments
    val allPaymentsFlow: Flow<List<PaymentEntity>> = lmsDao.getAllPaymentsFlow()
    val totalRevenue: Flow<Double?> = lmsDao.getTotalRevenueFlow()
    suspend fun recordPayment(userId: String, courseId: String, amount: Double, couponCode: String = "") {
        val id = "TXN-" + UUID.randomUUID().toString().uppercase().take(12)
        lmsDao.insertPayment(
            PaymentEntity(
                id = id,
                userId = userId,
                courseId = courseId,
                amount = amount,
                transactionId = id,
                timestamp = System.currentTimeMillis(),
                status = "Success",
                couponCodeUsed = couponCode
            )
        )
    }

    // Notifications
    fun getNotifications(userId: String): Flow<List<NotificationEntity>> = lmsDao.getNotificationsForUserFlow(userId)
    suspend fun sendNotification(userId: String, title: String, message: String, type: String = "Announcement") {
        lmsDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                isRead = false,
                type = type
            )
        )
    }
    suspend fun markAllNotificationsAsRead(userId: String) = lmsDao.markAllNotificationsAsRead(userId)

    // Coupons
    val allCoupons: Flow<List<CouponEntity>> = lmsDao.getAllCouponsFlow()
    suspend fun getCouponByCode(code: String) = lmsDao.getCouponByCode(code)
    suspend fun insertCoupon(coupon: CouponEntity) = lmsDao.insertCoupon(coupon)
    suspend fun deleteCoupon(id: String) = lmsDao.deleteCouponById(id)

    // Quiz Attempts
    fun getQuizAttempts(userId: String): Flow<List<QuizAttemptEntity>> = lmsDao.getQuizAttemptsForUserFlow(userId)
    suspend fun saveQuizAttempt(userId: String, lessonId: String, score: Int, totalQuestions: Int) {
        val id = UUID.randomUUID().toString()
        lmsDao.insertQuizAttempt(
            QuizAttemptEntity(
                id = id,
                userId = userId,
                lessonId = lessonId,
                score = score,
                totalQuestions = totalQuestions,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Seed Data
    suspend fun seedDataIfNeeded() {
        // Seed default users if empty
        val studentUser = lmsDao.getUserById("student_mock")
        if (studentUser == null) {
            lmsDao.insertUser(
                UserEntity(
                    id = "student_mock",
                    name = "Jane Doe",
                    email = "student@lms.com",
                    role = "Student",
                    profileImg = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=150",
                    passwordHash = "student123"
                )
            )
        }

        val adminUser = lmsDao.getUserById("admin_mock")
        if (adminUser == null) {
            lmsDao.insertUser(
                UserEntity(
                    id = "admin_mock",
                    name = "Prof. Robert Alistair",
                    email = "admin@lms.com",
                    role = "Admin",
                    profileImg = "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=150",
                    passwordHash = "admin123"
                )
            )
        }

        // Seed modern, detailed courses if database is empty
        val courses = lmsDao.getAllCoursesFlow().firstOrNull() ?: emptyList()
        if (courses.isEmpty()) {
            val course1Id = "c1_android"
            val course2Id = "c2_gemini"
            val course3Id = "c3_uisystem"

            // Insert Courses
            lmsDao.insertCourse(
                CourseEntity(
                    id = course1Id,
                    title = "Jetpack Compose Craftsmanship: Beginner to Advanced",
                    description = "Take your Android layouts to the next level. Master Canvas rendering, edge-to-edge custom setups, spring animations, dynamic color schemers, and robust MVVM state architectures.",
                    instructor = "Prof. Robert Alistair",
                    category = "Mobile Engineering",
                    duration = "22 Hours",
                    lessonsCount = 4,
                    rating = 4.9,
                    ratingCount = 384,
                    price = 49.99,
                    isFree = false,
                    discountPercent = 20,
                    isPublished = true,
                    thumbnail = "https://images.unsplash.com/photo-1607799279861-4dd421887fb3?auto=format&fit=crop&q=80&w=600",
                    couponCode = "WELCOME50"
                )
            )

            lmsDao.insertCourse(
                CourseEntity(
                    id = course2Id,
                    title = "Generative AI Systems: Building with Gemini & LLMs",
                    description = "Learn how to orchestrate Gemini API in production-ready apps. Dive deep into system instructs, multimodal zero-shot prompts, advanced tool/function calling, and conversational UI state.",
                    instructor = "Dr. Elena Rostova",
                    category = "Artificial Intelligence",
                    duration = "14 Hours",
                    lessonsCount = 3,
                    rating = 4.8,
                    ratingCount = 212,
                    price = 129.99,
                    isFree = false,
                    isPublished = true,
                    thumbnail = "https://images.unsplash.com/photo-1677442136019-21780efad99a?auto=format&fit=crop&q=80&w=600"
                )
            )

            lmsDao.insertCourse(
                CourseEntity(
                    id = course3Id,
                    title = "Database Systems: Room SQLite Deep Dive",
                    description = "Step-by-step master course on Android Room Database persistence. Master KSP annotation processing, writing advanced multi-table DAO queries, utilizing reactive Flows, and building dynamic caches.",
                    instructor = "Prof. Robert Alistair",
                    category = "Software Architecture",
                    duration = "8 Hours",
                    lessonsCount = 2,
                    rating = 4.7,
                    ratingCount = 145,
                    price = 0.0,
                    isFree = true,
                    isPublished = true,
                    thumbnail = "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?auto=format&fit=crop&q=80&w=600"
                )
            )

            // Seed Modules & Lessons
            // Course 1 Modules & Lessons
            val m1 = "m_android_1"
            val m2 = "m_android_2"
            lmsDao.insertModule(ModuleEntity(m1, course1Id, "Module 1: Edge-to-Edge & Foundations", 1))
            lmsDao.insertModule(ModuleEntity(m2, course1Id, "Module 2: Advanced Dynamic UI", 2))

            lmsDao.insertLesson(
                LessonEntity(
                    id = "l_compose_1",
                    moduleId = m1,
                    title = "Understanding System Insets & edgeToEdge()",
                    description = "A visual exploration on managing top status bars, notch cutouts, bottom navigation safe area offsets, and insets configurations.",
                    orderIndex = 1,
                    lessonType = "Video",
                    videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
                    durationMin = 15
                )
            )
            lmsDao.insertLesson(
                LessonEntity(
                    id = "l_compose_2",
                    moduleId = m1,
                    title = "Dynamic Colors & Design Tokens in Compose",
                    description = "How to style high-fidelity cards and containers natively utilizing MaterialTheme.colorScheme tokens instead of hardcoded hex colors.",
                    orderIndex = 2,
                    lessonType = "PDF",
                    pdfUrl = "https://unec.edu.az/application/uploads/2014/12/pdf-sample.pdf",
                    durationMin = 10
                )
            )
            lmsDao.insertLesson(
                LessonEntity(
                    id = "l_compose_3",
                    moduleId = m2,
                    title = "Compose State Controllers Quiz",
                    description = "Test your grasp on StateFlow, UI state sealed definitions, and keeping recompositions low and lightweight.",
                    orderIndex = 1,
                    lessonType = "Quiz",
                    quizQuestions = """[
                        {"question":"Which keyword enables reactively monitoring database updates flow in compose?","options":["collectAsStateWithLifecycle()","rememberSaveable()","getValue()","by state"],"answer":"collectAsStateWithLifecycle()"},
                        {"question":"How do we prevent unnecessary expensive calculations on recomposition?","options":["Use remember { }","Use derivedStateOf { }","Use LaunchEffect","Call invalidate()"],"answer":"Use remember { }"},
                        {"question":"What Compose layout handles scrollable vertical grids and lists easily?","options":["LazyColumn / LazyVerticalGrid","Column with verticalScroll","RecyclerView","ListView"],"answer":"LazyColumn / LazyVerticalGrid"}
                    ]""",
                    durationMin = 8
                )
            )
            lmsDao.insertLesson(
                LessonEntity(
                    id = "l_compose_4",
                    moduleId = m2,
                    title = "Assignment: Build an Edge-to-edge Form",
                    description = "Implement an interactive sign-in form with professional transitions, text fields utilizing filled styling, and custom adaptive sizes.",
                    orderIndex = 2,
                    lessonType = "Assignment",
                    assignmentDesc = "Task: Write a simple Compose Screen that implements an input form. Ensure touch targets are at least 48dp, and write tests if requested.",
                    durationMin = 12
                )
            )

            // Course 2 Modules & Lessons
            val m3 = "m_gemini_1"
            lmsDao.insertModule(ModuleEntity(m3, course2Id, "Module 1: Large Language Model Orchestrations", 1))

            lmsDao.insertLesson(
                LessonEntity(
                    id = "l_gemini_1",
                    moduleId = m3,
                    title = "Introduction to Gemini REST Architecture",
                    description = "We explore model endpoint configurations. Learn how system instructions dictate personas and guardrails in chat systems.",
                    orderIndex = 1,
                    lessonType = "Text",
                    textBody = "Gemini is Google's next-generation multimodal model series. In Android development, we fetch outputs securely from server-side configurations. Always make sure to isolate keys in local build properties or secrets so that client binary distributions are safe.",
                    durationMin = 8
                )
            )
            lmsDao.insertLesson(
                LessonEntity(
                    id = "l_gemini_2",
                    moduleId = m3,
                    title = "Gemini System Prompts Quiz",
                    description = "Quick assessment verifying knowledge of system instructions.",
                    orderIndex = 2,
                    lessonType = "Quiz",
                    quizQuestions = """[
                        {"question":"Where should you store sensitive Gemini API keys in Android?","options":["Secrets Panel Configured through BuildConfig","AndroidManifest.xml","In string.xml","Hardcoded as val apiKey"],"answer":"Secrets Panel Configured through BuildConfig"},
                        {"question":"What role specifies system instructions in model setup?","options":["system_instruction","user","model","instruction"],"answer":"system_instruction"}
                    ]""",
                    durationMin = 5
                )
            )
            lmsDao.insertLesson(
                LessonEntity(
                    id = "l_gemini_3",
                    moduleId = m3,
                    title = "Assignment: Create a Zero-Shot Prompt Card",
                    description = "Submit your multimodal architectural design overview outlining how zero-shot triggers work inside Android viewholders.",
                    orderIndex = 3,
                    lessonType = "Assignment",
                    assignmentDesc = "Submit a brief 300-word essay explaining zero-shot prompting best-practices overlaying edge-to-edge layouts.",
                    durationMin = 15
                )
            )

            // Seed initial notifications
            lmsDao.insertNotification(
                NotificationEntity(
                    "n_init_1",
                    "All",
                    "Welcome to Smart LMS!",
                    "Start exploring world-class courses designed by elite architectural instructors. Get standard access to Compose, AI engineering, databases and more.",
                    System.currentTimeMillis() - 100000,
                    false,
                    "Announcement"
                )
            )

            lmsDao.insertNotification(
                NotificationEntity(
                    "n_init_2",
                    "All",
                    "New Course Launched!",
                    "Check out 'Generative AI Systems: Building with Gemini & LLMs' to expand your future-proof mobile development skills now.",
                    System.currentTimeMillis() - 500000,
                    false,
                    "Promotion"
                )
            )

            // Seed Coupons
            lmsDao.insertCoupon(CouponEntity("cp1", "WELCOME50", 50, true))
            lmsDao.insertCoupon(CouponEntity("cp2", "FREEAI", 100, true))

            // Seed some historic payments to make the admin metrics look gorgeous
            lmsDao.insertPayment(PaymentEntity("pm1", "student_mock", "c1_android", 39.99, "TXN-ComposeMasterCheck", System.currentTimeMillis() - 86400000 * 2, "Success"))
            lmsDao.insertPayment(PaymentEntity("pm2", "student_mock", "c2_gemini", 129.99, "TXN-GeminiMasterInt", System.currentTimeMillis() - 86400000 * 1, "Success"))
        }
    }
}
