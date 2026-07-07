package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.FirebaseRetrofitClient
import com.example.api.FirebaseAuthRequest
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: UserEntity) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class LmsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = LmsRepository(database.lmsDao())

    // ------------------------------------------
    // STATIC / SEED DATA TRIGGER
    // ------------------------------------------
    init {
        viewModelScope.launch {
            repository.seedDataIfNeeded()
        }
    }

    // ------------------------------------------
    // AUTHENTICATION STATE
    // ------------------------------------------
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private fun parseFirebaseError(e: Exception): String {
        if (e is retrofit2.HttpException) {
            val errorJson = e.response()?.errorBody()?.string()
            if (errorJson != null) {
                try {
                    if (errorJson.contains("EMAIL_EXISTS")) {
                        return "This email address is already in use by another account."
                    }
                    if (errorJson.contains("WEAK_PASSWORD")) {
                        return "Password should be at least 6 characters."
                    }
                    if (errorJson.contains("INVALID_EMAIL")) {
                        return "Please enter a valid email address."
                    }
                    if (errorJson.contains("EMAIL_NOT_FOUND") || errorJson.contains("INVALID_PASSWORD") || errorJson.contains("INVALID_LOGIN_ATTEMPT")) {
                        return "Invalid email or password. Please try again."
                    }
                    if (errorJson.contains("TOO_MANY_ATTEMPTS_TRY_LATER")) {
                        return "We have temporarily blocked all requests from this device due to unusual activity. Try again later."
                    }
                    if (errorJson.contains("USER_DISABLED")) {
                        return "This user account has been disabled by an administrator."
                    }
                } catch (ex: Exception) {
                    // ignore
                }
            }
        }
        return e.localizedMessage ?: "Network connection error. Please try again."
    }

    fun login(email: String, passwordHash: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            if (email.isEmpty() || passwordHash.isEmpty()) {
                _authUiState.value = AuthUiState.Error("All fields are required.")
                return@launch
            }

            val apiKey = "AIzaSyC5m8-UaQKSyTa6r9JmXxVb-wZZDhrcBmo"

            try {
                val fbResponse = FirebaseRetrofitClient.service.signIn(
                    apiKey = apiKey,
                    request = FirebaseAuthRequest(
                        email = email.trim(),
                        password = passwordHash
                    )
                )

                val fbUid = fbResponse.localId ?: ""
                
                var user = repository.getUserByEmail(email.trim().lowercase())
                if (user == null) {
                    user = UserEntity(
                        id = fbUid.ifEmpty { "user_" + UUID.randomUUID().toString().take(10) },
                        name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = email.trim().lowercase(),
                        role = "Student",
                        profileImg = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=150",
                        passwordHash = passwordHash
                    )
                    repository.insertUser(user)
                } else {
                    if (user.passwordHash != passwordHash) {
                        user = user.copy(passwordHash = passwordHash)
                        repository.updateUser(user)
                    }
                }

                if (user.isBlocked) {
                    _authUiState.value = AuthUiState.Error("This account is blocked by the Administrator.")
                    return@launch
                }

                _currentUser.value = user
                _authUiState.value = AuthUiState.Success(user)
                repository.sendNotification(user.id, "Welcome back, ${user.name}!", "Successfully signed in via Firebase account.", "Update")
            } catch (e: Exception) {
                Log.e("FirebaseLogin", "Firebase login failed", e)
                _authUiState.value = AuthUiState.Error(parseFirebaseError(e))
            }
        }
    }

    fun signup(name: String, email: String, passwordHash: String, role: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            if (name.isEmpty() || email.isEmpty() || passwordHash.isEmpty()) {
                _authUiState.value = AuthUiState.Error("All fields are required.")
                return@launch
            }

            val apiKey = "AIzaSyC5m8-UaQKSyTa6r9JmXxVb-wZZDhrcBmo"

            try {
                val fbResponse = FirebaseRetrofitClient.service.signUp(
                    apiKey = apiKey,
                    request = FirebaseAuthRequest(
                        email = email.trim(),
                        password = passwordHash
                    )
                )

                val fbUid = fbResponse.localId ?: ("user_" + UUID.randomUUID().toString().take(10))

                val user = UserEntity(
                    id = fbUid,
                    name = name,
                    email = email.trim().lowercase(),
                    role = role,
                    profileImg = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=150",
                    passwordHash = passwordHash
                )
                repository.insertUser(user)
                _currentUser.value = user
                _authUiState.value = AuthUiState.Success(user)
                repository.sendNotification(user.id, "Welcome to LMS Academy!", "Thank you for registering on Firebase database. Start learning today!", "Announcement")
            } catch (e: Exception) {
                Log.e("FirebaseSignup", "Firebase signup failed", e)
                _authUiState.value = AuthUiState.Error(parseFirebaseError(e))
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _authUiState.value = AuthUiState.Idle
    }

    fun editProfile(newName: String, newAvatarUrl: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(name = newName, profileImg = newAvatarUrl)
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }

    fun changePassword(oldPass: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = _currentUser.value ?: return
        if (user.passwordHash != oldPass) {
            onError("Incorrect current password.")
            return
        }
        if (newPass.length < 4) {
            onError("Password must be at least 4 characters.")
            return
        }
        viewModelScope.launch {
            val updated = user.copy(passwordHash = newPass)
            repository.updateUser(updated)
            _currentUser.value = updated
            onSuccess()
        }
    }

    // ------------------------------------------
    // GENERAL COURSE LISTING
    // ------------------------------------------
    val allCourses: StateFlow<List<CourseEntity>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedCourses: StateFlow<List<CourseEntity>> = repository.publishedCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ------------------------------------------
    // WISHLIST MAP
    // ------------------------------------------
    private val _courseWishlist = MutableStateFlow<Set<String>>(emptySet())
    val courseWishlist: StateFlow<Set<String>> = _courseWishlist.asStateFlow()

    fun toggleWishlist(courseId: String) {
        val currentSet = _courseWishlist.value
        if (currentSet.contains(courseId)) {
            _courseWishlist.value = currentSet - courseId
        } else {
            _courseWishlist.value = currentSet + courseId
        }
    }

    // ------------------------------------------
    // STUDENT VIEW DATA & DETAILS
    // ------------------------------------------
    // Reactive mapping of user enrollments
    fun getStudentEnrollments(studentId: String): Flow<List<EnrollmentEntity>> {
        return repository.getEnrollmentsByStudent(studentId)
    }

    // Get specific completion details of course
    fun getCourseCompletionFlow(studentId: String, courseId: String, totalLessons: Int): Flow<Float> {
        if (totalLessons <= 0) return flowOf(0f)
        return repository.getCourseProgressFlow(studentId, courseId).map { progressEntities ->
            val completedCount = progressEntities.count { it.isCompleted }
            completedCount.toFloat() / totalLessons.toFloat()
        }
    }

    // Active course structures for playback
    private val _activeCourseModules = MutableStateFlow<List<ModuleEntity>>(emptyList())
    val activeCourseModules = _activeCourseModules.asStateFlow()

    fun loadCourseModules(courseId: String) {
        viewModelScope.launch {
            repository.getModulesForCourse(courseId).collect {
                _activeCourseModules.value = it
            }
        }
    }

    fun getLessonsForModuleFlow(moduleId: String): Flow<List<LessonEntity>> {
        return repository.getLessonsForModule(moduleId)
    }

    fun getLessonProgressFlow(userId: String, lessonId: String): Flow<ProgressEntity?> {
        return database.lmsDao().getLessonsForModuleFlow("").map { null } // unused mock wrapper, let's load dynamically
    }

    // ------------------------------------------
    // PROGRESS SYSTEM & ENROLLMENT ACTIONS
    // ------------------------------------------
    private val _activeLessonCompletionStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val activeLessonCompletionStates = _activeLessonCompletionStates.asStateFlow()

    fun loadCompletedLessons(studentId: String, courseId: String) {
        viewModelScope.launch {
            repository.getCourseProgressFlow(studentId, courseId).collect { list ->
                val map = list.associate { it.lessonId to it.isCompleted }
                _activeLessonCompletionStates.value = map
            }
        }
    }

    fun markLessonProgress(studentId: String, courseId: String, lessonId: String, isCompleted: Boolean, totalLessonsCount: Int, courseName: String) {
        viewModelScope.launch {
            repository.markLessonCompleted(studentId, courseId, lessonId, isCompleted)
            
            // Check if course is fully completed
            val progressRaw = repository.getCourseProgressDirect(studentId, courseId)
            val completedCount = progressRaw.count { it.isCompleted }
            
            if (completedCount >= totalLessonsCount && totalLessonsCount > 0) {
                // Ensure certificate not already issued
                val existingCert = repository.getCertificateForCourse(studentId, courseId)
                if (existingCert == null) {
                    repository.issueCertificate(studentId, courseId, courseName)
                    repository.sendNotification(
                        userId = studentId,
                        title = "Course Certified! 🎓",
                        message = "Congratulations! You completed '$courseName' successfully. Your completion certificate has been unlocked and saved to your profile.",
                        type = "Announcement"
                    )
                }
            }
        }
    }

    // ------------------------------------------
    // ENROLLMENT PURCHASE FLOW & COUPONS
    // ------------------------------------------
    private val _appliedCoupon = MutableStateFlow<CouponEntity?>(null)
    val appliedCoupon = _appliedCoupon.asStateFlow()

    fun applyCouponCode(code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val coupon = repository.getCouponByCode(code.trim().uppercase())
            if (coupon != null) {
                _appliedCoupon.value = coupon
                onResult(true, "Applied ${coupon.discountPercent}% Discount Coupon!")
            } else {
                _appliedCoupon.value = null
                onResult(false, "Invalid or Expired Coupon.")
            }
        }
    }

    fun clearCoupon() {
        _appliedCoupon.value = null
    }

    fun handleFreeEnrollment(studentId: String, courseId: String, courseName: String) {
        viewModelScope.launch {
            repository.enrollStudent(studentId, courseId)
            repository.sendNotification(
                userId = studentId,
                title = "Enrolled Successfully!",
                message = "Welcome to '$courseName'. Press start learning to access the course syllabus and modules.",
                type = "Enrollment"
            )
        }
    }

    fun handlePaidEnrollment(studentId: String, courseId: String, courseName: String, price: Double, couponCodeUsed: String) {
        viewModelScope.launch {
            repository.recordPayment(studentId, courseId, price, couponCodeUsed)
            repository.enrollStudent(studentId, courseId)
            repository.sendNotification(
                userId = studentId,
                title = "Course Enrollment Successful! 💳",
                message = "Payment of $${"%.2f".format(price)} confirmed! You are now enrolled in '$courseName'.",
                type = "Enrollment"
            )
            clearCoupon()
        }
    }

    // ------------------------------------------
    // QUIZ SCORER
    // ------------------------------------------
    fun submitQuizScore(userId: String, lessonId: String, score: Int, totalQuestions: Int) {
        viewModelScope.launch {
            repository.saveQuizAttempt(userId, lessonId, score, totalQuestions)
            repository.sendNotification(userId, "Quiz completed!", "You scored $score/$totalQuestions on our quiz.", "Update")
        }
    }

    val studentQuizAttempts: Flow<List<QuizAttemptEntity>>
        get() = _currentUser.value?.let { repository.getQuizAttempts(it.id) } ?: flowOf(emptyList())

    // ------------------------------------------
    // CERTIFICATES FLOw
    // ------------------------------------------
    val studentCertificates: Flow<List<CertificateEntity>>
        get() = _currentUser.value?.let { repository.getCertificates(it.id) } ?: flowOf(emptyList())

    // ------------------------------------------
    // NOTIFICATIONS FLOW
    // ------------------------------------------
    val studentNotifications: Flow<List<NotificationEntity>>
        get() = _currentUser.value?.let { repository.getNotifications(it.id) } ?: flowOf(emptyList())

    fun clearStudentNotifications() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(user.id)
        }
    }

    // ------------------------------------------
    // ADMIN DASHBOARD ANALYTICS & METRICS
    // ------------------------------------------
    val adminStudentsFlow: Flow<List<UserEntity>> = repository.allStudents
    val adminEnrollmentsFlow: Flow<List<EnrollmentEntity>> = repository.allEnrollments
    val adminPaymentsFlow: Flow<List<PaymentEntity>> = repository.allPaymentsFlow
    val adminRevenueFlow: Flow<Double> = repository.totalRevenue.map { it ?: 0.0 }

    // ------------------------------------------
    // COURSE CREATION / EDITING (ADMIN PANEL)
    // ------------------------------------------
    fun adminAddCourse(title: String, description: String, category: String, duration: String, price: Double, isFree: Boolean, thumbnail: String) {
        viewModelScope.launch {
            val course = CourseEntity(
                id = "course_" + UUID.randomUUID().toString().take(8),
                title = title,
                description = description,
                instructor = _currentUser.value?.name ?: "Administrator",
                category = category,
                duration = duration,
                lessonsCount = 0,
                rating = 5.0,
                ratingCount = 0,
                price = if (isFree) 0.0 else price,
                isFree = isFree,
                thumbnail = thumbnail.ifEmpty { "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&q=80&w=600" }
            )
            repository.insertCourse(course)
            repository.sendNotification("All", "New Course Available!", "Admin has published the course: '$title'. Enroll today!", "Announcement")
        }
    }

    fun adminEditCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.updateCourse(course)
        }
    }

    fun adminDeleteCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.deleteCourse(course)
        }
    }

    fun adminToggleCoursePublish(course: CourseEntity) {
        viewModelScope.launch {
            val updated = course.copy(isPublished = !course.isPublished)
            repository.updateCourse(updated)
        }
    }

    // ------------------------------------------
    // MODULE MANAGEMENT (ADMIN PANEL)
    // ------------------------------------------
    fun adminAddModule(courseId: String, title: String) {
        viewModelScope.launch {
            val modules = repository.getModulesForCourseDirect(courseId)
            val nextIndex = modules.size + 1
            val mod = ModuleEntity(
                id = "module_" + UUID.randomUUID().toString().take(8),
                courseId = courseId,
                title = title,
                orderIndex = nextIndex
            )
            repository.insertModule(mod)
        }
    }

    fun adminEditModule(module: ModuleEntity, newTitle: String) {
        viewModelScope.launch {
            val mod = module.copy(title = newTitle)
            repository.updateModule(mod)
        }
    }

    fun adminDeleteModule(moduleId: String) {
        viewModelScope.launch {
            repository.deleteModule(moduleId)
        }
    }

    // ------------------------------------------
    // LESSON MANAGEMENT (ADMIN PANEL)
    // ------------------------------------------
    fun adminAddLesson(
        courseId: String,
        moduleId: String,
        title: String,
        description: String,
        type: String,
        videoUrl: String = "",
        pdfUrl: String = "",
        textBody: String = "",
        quizQuestions: String = "",
        assignmentTask: String = "",
        duration: Int = 10
    ) {
        viewModelScope.launch {
            val lesson = LessonEntity(
                id = "lesson_" + UUID.randomUUID().toString().take(8),
                moduleId = moduleId,
                title = title,
                description = description,
                orderIndex = 11, // simple end appending
                lessonType = type,
                videoUrl = videoUrl,
                pdfUrl = pdfUrl,
                textBody = textBody,
                quizQuestions = quizQuestions,
                assignmentDesc = assignmentTask,
                durationMin = duration
            )
            repository.insertLesson(lesson)

            // Dynamic update course lesson count increment
            val course = repository.getCourseById(courseId)
            if (course != null) {
                repository.updateCourse(course.copy(lessonsCount = course.lessonsCount + 1))
            }
        }
    }

    fun adminDeleteLesson(courseId: String, lesson: LessonEntity) {
        viewModelScope.launch {
            repository.deleteLesson(lesson.id)
            val course = repository.getCourseById(courseId)
            if (course != null && course.lessonsCount > 0) {
                repository.updateCourse(course.copy(lessonsCount = course.lessonsCount - 1))
            }
        }
    }

    // ------------------------------------------
    // STUDENT MANAGEMENT (ADMIN PANEL)
    // ------------------------------------------
    fun adminToggleBlockStudent(userId: String, isCurrentlyBlocked: Boolean) {
        viewModelScope.launch {
            repository.setBlockedStatus(userId, !isCurrentlyBlocked)
        }
    }

    // ------------------------------------------
    // NOTIFICATION BROADCAST (ADMIN PANEL)
    // ------------------------------------------
    fun adminBroadcastPush(title: String, message: String, category: String) {
        viewModelScope.launch {
            repository.sendNotification(
                userId = "All",
                title = "$category: $title",
                message = message,
                type = category
            )
        }
    }

    // ------------------------------------------
    // COUPON OFFERS (ADMIN PANEL)
    // ------------------------------------------
    val adminCouponsFlow: Flow<List<CouponEntity>> = repository.allCoupons

    fun adminAddCoupon(code: String, percent: Int) {
        viewModelScope.launch {
            val coupon = CouponEntity(
                id = "coupon_" + UUID.randomUUID().toString().take(8),
                code = code.trim().uppercase(),
                discountPercent = percent,
                isActive = true
            )
            repository.insertCoupon(coupon)
        }
    }

    fun adminDeleteCoupon(couponId: String) {
        viewModelScope.launch {
            repository.deleteCoupon(couponId)
        }
    }
}
