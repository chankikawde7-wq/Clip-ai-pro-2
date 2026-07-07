@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.views

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.AuthUiState
import com.example.ui.LmsViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

// ------------------------------------------
// DESIGN SYSTEM DESIGN TOKENS
// ------------------------------------------
val LmsDarkBg = Color(0xFF0F172A)      // Rich slate/midnight
val LmsCardBg = Color(0xFF1E293B)      // Card surface
val LmsAccentBlue = Color(0xFF0284C7)  // Sapphire Blue
val LmsAccentTeal = Color(0xFF10B981)  // Success/Mint
val LmsAccentGold = Color(0xFFF59E0B)  // Premium Gold
val LmsAccentCoral = Color(0xFFEF4444) // Error/Coral
val LmsTextPrimary = Color(0xFFF8FAFC) // Slate 50
val LmsTextSecondary = Color(0xFF94A3B8)// Slate 400

sealed class LmsScreen {
    object Auth : LmsScreen()
    object StudentHome : LmsScreen()
    object AdminHome : LmsScreen()
    data class CourseDetails(val courseId: String) : LmsScreen()
    data class CourseLearningRoom(val courseId: String, val initialLessonId: String? = null) : LmsScreen()
}

@Composable
fun CreatorHubApp(
    viewModel: LmsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authState by viewModel.authUiState.collectAsStateWithLifecycle()

    // Interactive custom state navigation stack to allow back-presses beautifully
    val navStack = remember { mutableStateListOf<LmsScreen>(LmsScreen.Auth) }
    val currentScreen = navStack.lastOrNull() ?: LmsScreen.Auth

    fun navigateTo(screen: LmsScreen) {
        navStack.add(screen)
    }

    fun navigateBack() {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.lastIndex)
        }
    }

    // Synchronize current user login state to route appropriately
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navStack.clear()
            if (currentUser?.role == "Admin") {
                navStack.add(LmsScreen.AdminHome)
            } else {
                navStack.add(LmsScreen.StudentHome)
            }
        } else {
            navStack.clear()
            navStack.add(LmsScreen.Auth)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LmsDarkBg)
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                is LmsScreen.Auth -> {
                    AuthScreen(
                        viewModel = viewModel,
                        state = authState,
                        onLoginSuccess = { }
                    )
                }
                is LmsScreen.StudentHome -> {
                    StudentHomeScreen(
                        viewModel = viewModel,
                        student = currentUser ?: UserEntity("student_mock","Jane Doe","student@lms.com","Student","",0L,false,""),
                        onNavigateToCourse = { courseId -> navigateTo(LmsScreen.CourseDetails(courseId)) },
                        onNavigateToLearning = { courseId, lessonId -> navigateTo(LmsScreen.CourseLearningRoom(courseId, lessonId)) }
                    )
                }
                is LmsScreen.AdminHome -> {
                    AdminHomeScreen(
                        viewModel = viewModel,
                        onNavigateToCourse = { courseId -> navigateTo(LmsScreen.CourseDetails(courseId)) }
                    )
                }
                is LmsScreen.CourseDetails -> {
                    CourseDetailsScreen(
                        courseId = screen.courseId,
                        viewModel = viewModel,
                        currentUserId = currentUser?.id ?: "",
                        currentUserRole = currentUser?.role ?: "",
                        onBack = { navigateBack() },
                        onStartLearning = { courseId, lessonId -> navigateTo(LmsScreen.CourseLearningRoom(courseId, lessonId)) }
                    )
                }
                is LmsScreen.CourseLearningRoom -> {
                    CourseLearningRoomScreen(
                        courseId = screen.courseId,
                        initialLessonId = screen.initialLessonId,
                        viewModel = viewModel,
                        currentUserId = currentUser?.id ?: "",
                        onBack = { navigateBack() }
                    )
                }
            }
        }
    }
}

// ==========================================
// 1. AUTHENTICATION SCREEN
// ==========================================
@Composable
fun AuthScreen(
    viewModel: LmsViewModel,
    state: AuthUiState,
    onLoginSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(true) }
    var selectedRole by remember { mutableStateOf("Student") } // "Student" or "Admin"
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var forgotPasswordActive by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(state) {
        if (state is AuthUiState.Error) {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .background(LmsCardBg, RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(24.dp))
                .padding(32.dp)
        ) {
            // Icon / Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White.copy(0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = "Logo",
                    tint = LmsAccentBlue,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "(welcome to ClipAI)",
                color = LmsAccentBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp).testTag("welcome_clipai_text")
            )
            Text(
                text = "LMS Academy",
                color = LmsTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Professional Education Portal",
                color = LmsTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Quick Fill Help Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.04f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Quick Fill Demo Credentials:", color = LmsAccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                isSignUp = false
                                email = "student@lms.com"
                                password = "student123"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue.copy(alpha = 0.15f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Student Accounts", color = LmsAccentBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                isSignUp = false
                                email = "admin@lms.com"
                                password = "admin123"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LmsAccentGold.copy(alpha = 0.15f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Admin Accounts", color = LmsAccentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inputs
            if (isSignUp) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = LmsTextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LmsTextPrimary,
                        unfocusedTextColor = LmsTextPrimary,
                        focusedBorderColor = LmsAccentBlue,
                        unfocusedBorderColor = Color.White.copy(0.12f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_name_input")
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Surname", color = LmsTextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LmsTextPrimary,
                        unfocusedTextColor = LmsTextPrimary,
                        focusedBorderColor = LmsAccentBlue,
                        unfocusedBorderColor = Color.White.copy(0.12f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_surname_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", color = LmsTextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LmsTextPrimary,
                    unfocusedTextColor = LmsTextPrimary,
                    focusedBorderColor = LmsAccentBlue,
                    unfocusedBorderColor = Color.White.copy(0.12f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = LmsTextSecondary) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LmsTextPrimary,
                    unfocusedTextColor = LmsTextPrimary,
                    focusedBorderColor = LmsAccentBlue,
                    unfocusedBorderColor = Color.White.copy(0.12f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isSignUp) {
                // Role Selector
                Text(
                    text = "Register Account Type:",
                    color = LmsTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Student", "Admin").forEach { role ->
                        val active = selectedRole == role
                        Button(
                            onClick = { selectedRole = role },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (active) LmsAccentBlue else Color.White.copy(0.06f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(role, color = if (active) Color.White else LmsTextSecondary, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // CTA Button
            val textAction = if (isSignUp) "Register Securely" else "Log In"
            Button(
                onClick = {
                    if (isSignUp) {
                        viewModel.signup("$name $surname".trim(), email, password, selectedRole)
                    } else {
                        viewModel.login(email, password)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("auth_submit_btn")
            ) {
                if (state is AuthUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(textAction, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { isSignUp = !isSignUp }) {
                    Text(
                        text = if (isSignUp) "Already have an account? Login" else "Create profile",
                        color = LmsAccentBlue,
                        fontSize = 11.sp
                    )
                }

                TextButton(onClick = { forgotPasswordActive = true }) {
                    Text("Forgot Password?", color = LmsTextSecondary, fontSize = 11.sp)
                }
            }

            // Google Sign In Mock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clickable {
                        Toast
                            .makeText(context, "Google Sign-In: Simulation Complete", Toast.LENGTH_SHORT)
                            .show()
                        viewModel.login("student@lms.com", "student123")
                    }
                    .background(Color.White.copy(0.04f), RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccountBalanceWallet, "Wallet/G", tint = LmsAccentTeal, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock with Google Identity", color = LmsTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (forgotPasswordActive) {
        AlertDialog(
            onDismissRequest = { forgotPasswordActive = false },
            containerColor = LmsCardBg,
            title = { Text("Simulate Dynamic Recovery", color = LmsTextPrimary) },
            text = {
                Column {
                    Text("Specify registration email to broadcast recovery parameters.", color = LmsTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = LmsTextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    forgotPasswordActive = false
                    Toast.makeText(context, "Secure password reset dynamic dispatch sent!", Toast.LENGTH_SHORT).show()
                }, colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue)) {
                    Text("Transmit Link", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { forgotPasswordActive = false }) {
                    Text("Cancel", color = LmsTextSecondary)
                }
            }
        )
    }
}

// ==========================================
// 2. STUDENT DASHBOARD SCREEN
// ==========================================
@Composable
fun StudentHomeScreen(
    viewModel: LmsViewModel,
    student: UserEntity,
    onNavigateToCourse: (String) -> Unit,
    onNavigateToLearning: (String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Mobile Engineering", "Artificial Intelligence", "Software Architecture")

    val publishedCourses by viewModel.publishedCourses.collectAsStateWithLifecycle()
    val wishlist by viewModel.courseWishlist.collectAsStateWithLifecycle()
    val studentEnrollments by viewModel.getStudentEnrollments(student.id).collectAsStateWithLifecycle(emptyList())
    val certificates by viewModel.studentCertificates.collectAsStateWithLifecycle(emptyList())
    val quizAttempts by viewModel.studentQuizAttempts.collectAsStateWithLifecycle(emptyList())
    val notifications by viewModel.studentNotifications.collectAsStateWithLifecycle(emptyList())

    val context = LocalContext.current
    var profileDialogOpen by remember { mutableStateOf(false) }
    var securityDialogOpen by remember { mutableStateOf(false) }
    var showNotifDrawer by remember { mutableStateOf(false) }
    var showAchievementsTab by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = LmsDarkBg,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LmsCardBg)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { profileDialogOpen = true }
                    ) {
                        AsyncImage(
                            model = student.profileImg,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, LmsAccentBlue, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Welcome, ${student.name} 👋", color = LmsTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("LMS Student Workspace", color = LmsTextSecondary, fontSize = 10.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { showAchievementsTab = !showAchievementsTab }) {
                            Icon(
                                imageVector = if (showAchievementsTab) Icons.Filled.School else Icons.Outlined.School,
                                contentDescription = "Achievements",
                                tint = if (showAchievementsTab) LmsAccentGold else LmsTextSecondary
                            )
                        }
                        IconButton(onClick = { showNotifDrawer = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Alerts",
                                tint = LmsTextSecondary
                            )
                        }
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Filled.Logout, "Exit", tint = LmsAccentCoral)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                if (showAchievementsTab) {
                    // ==========================================
                    // CERTIFICATES & RESULTS PANEL
                    // ==========================================
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Your Academic Dashboard", color = LmsTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            TextButton(onClick = { showAchievementsTab = false }) {
                                Text("Back to Study", color = LmsAccentBlue, fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        Text("Earned Certifications", color = LmsAccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (certificates.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(LmsCardBg, RoundedCornerShape(14.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.WorkspacePremium, "Cert", tint = LmsTextSecondary, modifier = Modifier.size(40.dp))
                                    Text("No Certificates Unlocked", color = LmsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                                    Text("Complete 100% of any paid or free course syllabus, and system certificates are unlocked automatically.", color = LmsTextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    } else {
                        items(certificates) { cert ->
                            // Look up course title
                            val c = publishedCourses.find { it.id == cert.courseId }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LmsCardBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, LmsAccentGold, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(LmsAccentGold.copy(0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.WorkspacePremium, "Premium", tint = LmsAccentGold, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(c?.title ?: "Certified Course Graduate", color = LmsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("Credential: ${cert.verificationCode}", color = LmsAccentGold, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                        Text("Date Issued: Dec 2026", color = LmsTextSecondary, fontSize = 10.sp)
                                    }
                                    IconButton(onClick = {
                                        Toast.makeText(context, "Certificate metadata downloaded securely! Key: ${cert.verificationCode}", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Filled.Download, "Grab", tint = LmsTextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Recent Quiz Scores", color = LmsTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (quizAttempts.isEmpty()) {
                        item {
                            Text("No quiz submissions evaluated yet.", color = LmsTextSecondary, fontSize = 11.sp)
                        }
                    } else {
                        items(quizAttempts) { qa ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LmsCardBg),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Quiz Submission", color = LmsTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Timestamp: Dec 14", color = LmsTextSecondary, fontSize = 9.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(LmsAccentTeal.copy(0.12f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Score: ${qa.score}/${qa.totalQuestions}", color = LmsAccentTeal, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // ==========================================
                    // REGULAR HOME: CATALOG & ENROLLMENTS
                    // ==========================================
                    item {
                        // Quick Profile Management Trigger Cards
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { profileDialogOpen = true },
                                colors = CardDefaults.cardColors(containerColor = LmsCardBg)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Edit, "Edit", tint = LmsAccentBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Edit Credentials", color = LmsTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { securityDialogOpen = true },
                                colors = CardDefaults.cardColors(containerColor = LmsCardBg)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Lock, "Lock", tint = LmsAccentGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Change Password", color = LmsTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // SEARCH BOX
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search course tags or subjects...", color = LmsTextSecondary) },
                            leadingIcon = { Icon(Icons.Filled.Search, "Find", tint = LmsTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LmsTextPrimary,
                                unfocusedTextColor = LmsTextPrimary,
                                focusedBorderColor = LmsAccentBlue,
                                unfocusedBorderColor = Color.White.copy(0.12f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // SELECTION CATEGORIES
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val selected = selectedCategory == cat
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        labelColor = LmsTextSecondary,
                                        selectedLabelColor = Color.White,
                                        selectedContainerColor = LmsAccentBlue
                                    )
                                )
                            }
                        }
                    }

                    // CONTINUING / ENROLLED SECTION
                    item {
                        Text("My Current Enrolled Courses", color = LmsTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    val enrolledIds = studentEnrollments.map { it.courseId }
                    val currentEnrolledCourses = publishedCourses.filter { enrolledIds.contains(it.id) }

                    if (currentEnrolledCourses.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LmsCardBg),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("You aren't enrolled in any courses yet.", color = LmsTextSecondary, fontSize = 12.sp)
                                    TextButton(onClick = { selectedCategory = "All" }) {
                                        Text("Explore catalog below and mark enroll!", color = LmsAccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        items(currentEnrolledCourses) { course ->
                            // Custom progress fetcher
                            val progressFloat by viewModel.getCourseCompletionFlow(student.id, course.id, course.lessonsCount).collectAsStateWithLifecycle(0f)
                            val percent = (progressFloat * 100).toInt()

                            Card(
                                colors = CardDefaults.cardColors(containerColor = LmsCardBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToCourse(course.id) }
                            ) {
                                Row(modifier = Modifier.padding(12.dp)) {
                                    AsyncImage(
                                        model = course.thumbnail,
                                        contentDescription = "Thumbnail",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(course.category, color = LmsAccentBlue, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                        Text(course.title, color = LmsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            LinearProgressIndicator(
                                                progress = { progressFloat },
                                                color = if (percent == 100) LmsAccentTeal else LmsAccentBlue,
                                                trackColor = Color.White.copy(0.1f),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(6.dp)
                                                    .clip(CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("$percent%", color = if (percent == 100) LmsAccentTeal else LmsTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = { onNavigateToCourse(course.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("Resume Lessons", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // EXPLORE COURSE LABELS
                    item {
                        Text("Explore Academic Courses", color = LmsTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    val filterList = publishedCourses.filter {
                        (selectedCategory == "All" || it.category == selectedCategory) &&
                                (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.instructor.contains(searchQuery, ignoreCase = true))
                    }

                    if (filterList.isEmpty()) {
                        item {
                            Text("No courses found matching criteria.", color = LmsTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        items(filterList) { course ->
                            val isWished = wishlist.contains(course.id)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LmsCardBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToCourse(course.id) }
                            ) {
                                Column {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        AsyncImage(
                                            model = course.thumbnail,
                                            contentDescription = "Cover",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                        )
                                        IconButton(
                                            onClick = { viewModel.toggleWishlist(course.id) },
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        ) {
                                            Icon(
                                                imageVector = if (isWished) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = "Wished",
                                                tint = if (isWished) LmsAccentCoral else Color.White
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(8.dp)
                                                .background(LmsAccentBlue, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(if (course.isFree) "FREE" else "$${course.price}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(course.category, color = LmsAccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(course.title, color = LmsTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Star, "Star", tint = LmsAccentGold, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("${course.rating} (${course.ratingCount})", color = LmsTextSecondary, fontSize = 11.sp)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Timer, "Clock", tint = LmsTextSecondary, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(course.duration, color = LmsTextSecondary, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // NOTIFICATION SLIDE DRAWER
            if (showNotifDrawer) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.6f))
                        .clickable { showNotifDrawer = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.85f)
                            .background(LmsCardBg)
                            .align(Alignment.CenterEnd)
                            .clickable(enabled = false) { }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Student Notifications", color = LmsTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showNotifDrawer = false }) {
                                Icon(Icons.Filled.Close, "Dismiss", tint = LmsTextPrimary)
                            }
                        }

                        Button(
                            onClick = { viewModel.clearStudentNotifications() },
                            colors = ButtonDefaults.buttonColors(containerColor = LmsLightCardOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mark All Read", color = LmsTextPrimary, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (notifications.isEmpty()) {
                            Text("No alerts found.", color = LmsTextSecondary, modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                        }

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(notifications) { notif ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(0.02f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row {
                                        val icon = when (notif.type) {
                                            "Enrollment" -> Icons.Default.Payments
                                            "Update" -> Icons.Default.Upcoming
                                            "Promotion" -> Icons.Default.Campaign
                                            else -> Icons.Default.Announcement
                                        }
                                        Icon(icon, "Type", tint = LmsAccentBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(notif.title, color = LmsTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(notif.message, color = LmsTextSecondary, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // PROFILE DIALOG
    if (profileDialogOpen) {
        var profileName by remember { mutableStateOf(student.name) }
        var profileAvatar by remember { mutableStateOf(student.profileImg) }
        AlertDialog(
            onDismissRequest = { profileDialogOpen = false },
            containerColor = LmsCardBg,
            title = { Text("Update Academic Profile", color = LmsTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text("Enter Name") }
                    )
                    OutlinedTextField(
                        value = profileAvatar,
                        onValueChange = { profileAvatar = it },
                        label = { Text("Avatar URL") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.editProfile(profileName, profileAvatar)
                    profileDialogOpen = false
                }, colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue)) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileDialogOpen = false }) { Text("Cancel") }
            }
        )
    }

    // CHANGE PASSWORD DIALOG
    if (securityDialogOpen) {
        var op by remember { mutableStateOf("") }
        var np by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { securityDialogOpen = false },
            containerColor = LmsCardBg,
            title = { Text("Secure Password Change", color = LmsTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = op,
                        onValueChange = { op = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = np,
                        onValueChange = { np = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.changePassword(op, np,
                        onSuccess = {
                            securityDialogOpen = false
                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                    )
                }, colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue)) {
                    Text("Apply Change", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { securityDialogOpen = false }) { Text("Discard") }
            }
        )
    }
}

val LmsLightCardOutline = Color(0xFF334155)

// ==========================================
// 3. ADMIN HOMEPAGE SCREEN
// ==========================================
@Composable
fun AdminHomeScreen(
    viewModel: LmsViewModel,
    onNavigateToCourse: (String) -> Unit
) {
    val publishedCourses by viewModel.publishedCourses.collectAsStateWithLifecycle()
    val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()
    val students by viewModel.adminStudentsFlow.collectAsStateWithLifecycle(emptyList())
    val enrollments by viewModel.adminEnrollmentsFlow.collectAsStateWithLifecycle(emptyList())
    val payments by viewModel.adminPaymentsFlow.collectAsStateWithLifecycle(emptyList())
    val revenue by viewModel.adminRevenueFlow.collectAsStateWithLifecycle(0.0)
    val coupons by viewModel.adminCouponsFlow.collectAsStateWithLifecycle(emptyList())

    val context = LocalContext.current
    var adminActiveTab by remember { mutableStateOf("Metrics") } // "Metrics", "Courses", "Students", "Promotions"

    // Creation States
    var addCourseOpen by remember { mutableStateOf(false) }
    var addCouponOpen by remember { mutableStateOf(false) }
    var pushNoticeOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = LmsDarkBg,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LmsCardBg)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("LMS Admin Control", color = LmsTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("System Administration Portal", color = LmsAccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = LmsAccentCoral),
                        onClick = { viewModel.logout() },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Logout", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        },
        bottomBar = {
            // Admin bottom navigator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LmsCardBg)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Metrics", "Courses", "Students", "Promotions").forEach { tab ->
                        val active = adminActiveTab == tab
                        val icon = when (tab) {
                            "Metrics" -> Icons.Default.Analytics
                            "Courses" -> Icons.Default.Book
                            "Students" -> Icons.Default.Groups
                            else -> Icons.Default.Campaign
                        }
                        IconButton(onClick = { adminActiveTab = tab }) {
                            Icon(icon, tab, tint = if (active) LmsAccentBlue else LmsTextSecondary)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when (adminActiveTab) {
                "Metrics" -> {
                    // ==========================================
                    // ANALYTICS & REVENUE GRAPHS MODULE
                    // ==========================================
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        item { Spacer(modifier = Modifier.height(6.dp)) }
                        item { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(colors = CardDefaults.cardColors(containerColor = LmsCardBg), modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("LMS Revenue", color = LmsTextSecondary, fontSize = 10.sp)
                                    Text("$${"%.2f".format(revenue)}", color = LmsAccentTeal, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text("Success state metrics", color = LmsTextSecondary, fontSize = 8.sp)
                                }
                            }
                            Card(colors = CardDefaults.cardColors(containerColor = LmsCardBg), modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Syllabus Catalog", color = LmsTextSecondary, fontSize = 10.sp)
                                    Text("${allCourses.size} Courses", color = LmsAccentBlue, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text("${publishedCourses.size} published live", color = LmsTextSecondary, fontSize = 8.sp)
                                }
                            }
                        } }

                        item { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(colors = CardDefaults.cardColors(containerColor = LmsCardBg), modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Total Registrants", color = LmsTextSecondary, fontSize = 10.sp)
                                    Text("${students.size} students", color = LmsTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(colors = CardDefaults.cardColors(containerColor = LmsCardBg), modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Active Enrollments", color = LmsTextSecondary, fontSize = 10.sp)
                                    Text("${enrollments.size} enrolls", color = LmsTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } }

                        // STYLISH CUSTOM BAR GRAPH REPRESENTING COURSE POPULARITY
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LmsCardBg),
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(0.04f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Course Enrollment Distribution", color = LmsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    if (allCourses.isEmpty()) {
                                        Text("No course statistics available.", color = LmsTextSecondary, fontSize = 11.sp)
                                    } else {
                                        allCourses.forEach { c ->
                                            val count = enrollments.count { it.courseId == c.id }
                                            val max = if (enrollments.isEmpty()) 1 else enrollments.size
                                            val fraction = count.toFloat() / max.toFloat()

                                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(c.title, color = LmsTextPrimary, fontSize = 11.sp, maxLines = 1, modifier = Modifier.weight(0.7f), overflow = TextOverflow.Ellipsis)
                                                    Text("$count students", color = LmsAccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(10.dp)
                                                        .background(Color.White.copy(0.05f), RoundedCornerShape(4.dp))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(fraction.coerceAtLeast(0.05f))
                                                            .fillMaxHeight()
                                                            .background(
                                                                Brush.horizontalGradient(listOf(LmsAccentBlue, LmsAccentTeal)),
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // HISTORIC PAYMENTS LIST
                        item {
                            Text("Realtime Payment Logs", color = LmsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        if (payments.isEmpty()) {
                            item { Text("No purchases tracked in database.", color = LmsTextSecondary, fontSize = 11.sp) }
                        } else {
                            items(payments) { pay ->
                                Card(colors = CardDefaults.cardColors(containerColor = LmsCardBg), modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(pay.transactionId, color = LmsTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Syllabus access: ${pay.courseId}", color = LmsTextSecondary, fontSize = 9.sp)
                                        }
                                        Text("$${"%.2f".format(pay.amount)}", color = LmsAccentTeal, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                "Courses" -> {
                    // ==========================================
                    // COURSE MANAGEMENT MANAGER LIST
                    // ==========================================
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("LMS Curriculum Designer", color = LmsTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { addCourseOpen = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Filled.Add, "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Course", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }

                        items(allCourses) { course ->
                            Card(colors = CardDefaults.cardColors(containerColor = LmsCardBg), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = course.thumbnail,
                                        contentDescription = "Cover",
                                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(course.title, color = LmsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text("${course.category} • ${if (course.isFree) "Free" else "$${course.price}"}", color = LmsTextSecondary, fontSize = 10.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                            Button(
                                                onClick = { onNavigateToCourse(course.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Text("Edit Syllabus", color = Color.White, fontSize = 10.sp)
                                            }
                                            Button(
                                                onClick = { viewModel.adminToggleCoursePublish(course) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (course.isPublished) LmsAccentTeal.copy(0.12f) else LmsAccentCoral.copy(0.12f)
                                                ),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Text(
                                                    text = if (course.isPublished) "Published" else "Unpublished",
                                                    color = if (course.isPublished) LmsAccentTeal else LmsAccentCoral,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                    IconButton(onClick = { viewModel.adminDeleteCourse(course) }) {
                                        Icon(Icons.Filled.Delete, "Delete", tint = LmsAccentCoral)
                                    }
                                }
                            }
                        }
                    }
                }

                "Students" -> {
                    // ==========================================
                    // STUDENTS MANAGEMENT
                    // ==========================================
                    var searchStudQuery by remember { mutableStateOf("") }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Text("Registered Student Directories", color = LmsTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        item {
                            OutlinedTextField(
                                value = searchStudQuery,
                                onValueChange = { searchStudQuery = it },
                                placeholder = { Text("Search by name, email...", color = LmsTextSecondary) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        val filteredStudents = students.filter {
                            searchStudQuery.isEmpty() || it.name.contains(searchStudQuery, ignoreCase = true) || it.email.contains(searchStudQuery, ignoreCase = true)
                        }

                        items(filteredStudents) { stud ->
                            Card(colors = CardDefaults.cardColors(containerColor = LmsCardBg), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = stud.profileImg,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.size(44.dp).clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(stud.name, color = LmsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(stud.email, color = LmsTextSecondary, fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.adminToggleBlockStudent(stud.id, stud.isBlocked) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (stud.isBlocked) LmsAccentTeal else LmsAccentCoral
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text(if (stud.isBlocked) "Unblock" else "Block", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                "Promotions" -> {
                    // ==========================================
                    // COUPONS & PUSH NOTIFICATION CONTROLLER
                    // ==========================================
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Coupon Campaign Desk", color = LmsTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Button(onClick = { addCouponOpen = true }, colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue)) {
                                    Text("New Coupon", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }

                        items(coupons) { cp ->
                            Card(colors = CardDefaults.cardColors(containerColor = LmsCardBg), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(cp.code, color = LmsTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                        Text("Discount Magnitude: ${cp.discountPercent}% Off", color = LmsTextSecondary, fontSize = 11.sp)
                                    }
                                    IconButton(onClick = { viewModel.adminDeleteCoupon(cp.id) }) {
                                        Icon(Icons.Filled.Close, "Kill", tint = LmsAccentCoral)
                                    }
                                }
                            }
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("FCM Interactive Announcements", color = LmsTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Button(onClick = { pushNoticeOpen = true }, colors = ButtonDefaults.buttonColors(containerColor = LmsAccentGold)) {
                                    Text("FCM Broadcaster", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }

                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = LmsCardBg), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Push Notification Engine", color = LmsTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Submits Firebase Cloud Messages instantly. Active Student boards will pull real-time feeds dynamically.", color = LmsTextSecondary, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE COURSE MODEL
    if (addCourseOpen) {
        var t by remember { mutableStateOf("") }
        var d by remember { mutableStateOf("") }
        var c by remember { mutableStateOf("Mobile Engineering") }
        var dur by remember { mutableStateOf("10 Hours") }
        var pr by remember { mutableStateOf("") }
        var fr by remember { mutableStateOf(true) }
        var thumb by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { addCourseOpen = false },
            containerColor = LmsCardBg,
            title = { Text("Publish New Syllabus Course", color = LmsTextPrimary) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = t, onValueChange = { t = it }, label = { Text("Course Title") })
                    OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("Course Description") })
                    OutlinedTextField(value = dur, onValueChange = { dur = it }, label = { Text("Total Duration (e.g. 15 Hours)") })
                    OutlinedTextField(value = thumb, onValueChange = { thumb = it }, label = { Text("Thumbnail URL (or empty)") })

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = fr, onCheckedChange = { fr = it })
                        Text("This is a FREE Course", color = LmsTextPrimary, fontSize = 11.sp)
                    }

                    if (!fr) {
                        OutlinedTextField(
                            value = pr,
                            onValueChange = { pr = it },
                            label = { Text("Price in USD ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (t.isNotEmpty() && d.isNotEmpty()) {
                        viewModel.adminAddCourse(t, d, c, dur, pr.toDoubleOrNull() ?: 0.0, fr, thumb)
                        addCourseOpen = false
                        Toast.makeText(context, "Syllabus created & announced via FCM!", Toast.LENGTH_SHORT).show()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue)) {
                    Text("Announce Course", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { addCourseOpen = false }) { Text("Dismiss") }
            }
        )
    }

    // CREATE COUPON
    if (addCouponOpen) {
        var cod by remember { mutableStateOf("") }
        var per by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { addCouponOpen = false },
            containerColor = LmsCardBg,
            title = { Text("Deploy Campaign Coupon", color = LmsTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = cod, onValueChange = { cod = it }, label = { Text("Promo Code (e.g. HALFOFF)") })
                    OutlinedTextField(value = per, onValueChange = { per = it }, label = { Text("Markdown Percent (e.g. 50)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val p = per.toIntOrNull() ?: 0
                    if (cod.isNotEmpty() && p > 0) {
                        viewModel.adminAddCoupon(cod, p)
                        addCouponOpen = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue)) {
                    Text("Deploy Campaign")
                }
            }
        )
    }

    // BROADCAST PUSH DIALOG
    if (pushNoticeOpen) {
        var pt by remember { mutableStateOf("") }
        var pm by remember { mutableStateOf("") }
        var cat by remember { mutableStateOf("Announcement") } // "Announcement", "Update", "Promotion"
        AlertDialog(
            onDismissRequest = { pushNoticeOpen = false },
            containerColor = LmsCardBg,
            title = { Text("FCM Notification Broadcaster", color = LmsTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Transmits a direct notification package triggering a sync with all student devices.", color = LmsTextSecondary, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Announcement", "Promo", "Update").forEach { chip ->
                            val selected = cat == (if (chip == "Promo") "Promotion" else chip)
                            FilterChip(selected = selected, onClick = { cat = (if (chip == "Promo") "Promotion" else chip) }, label = { Text(chip) })
                        }
                    }
                    OutlinedTextField(value = pt, onValueChange = { pt = it }, label = { Text("Alert Title") })
                    OutlinedTextField(value = pm, onValueChange = { pm = it }, label = { Text("Body Message") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (pt.isNotEmpty() && pm.isNotEmpty()) {
                        viewModel.adminBroadcastPush(pt, pm, cat)
                        pushNoticeOpen = false
                        Toast.makeText(context, "FCM payload distributed!", Toast.LENGTH_SHORT).show()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = LmsAccentGold)) {
                    Text("Broadcast Link", color = Color.White)
                }
            }
        )
    }
}

// ==========================================
// 4. COURSE DETAILS SCREEN (SYLLABUS & PURCHASE)
// ==========================================
@Composable
fun CourseDetailsScreen(
    courseId: String,
    viewModel: LmsViewModel,
    currentUserId: String,
    currentUserRole: String,
    onBack: () -> Unit,
    onStartLearning: (String, String) -> Unit
) {
    val context = LocalContext.current
    var course by remember { mutableStateOf<CourseEntity?>(null) }
    var modules by remember { mutableStateOf<List<ModuleEntity>>(emptyList()) }

    // Fetch details dynamically of the course
    LaunchedEffect(courseId) {
        course = viewModel.allCourses.value.find { it.id == courseId }
        viewModel.loadCourseModules(courseId)
    }

    val activeModules by viewModel.activeCourseModules.collectAsStateWithLifecycle()
    val checkEnrollFlow by viewModel.getStudentEnrollments(currentUserId).collectAsStateWithLifecycle(emptyList())
    val isEnrolled = checkEnrollFlow.any { it.courseId == courseId }

    // Coupon Checkout
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    var promoInput by remember { mutableStateOf("") }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    // Admin Creation
    var createModuleOpen by remember { mutableStateOf(false) }
    var createLessonOpen by remember { mutableStateOf(false) }
    var activeModuleIdForLesson by remember { mutableStateOf("") }

    val activeCourse = course ?: return

    Scaffold(
        containerColor = LmsDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Course Detail Spec", color = LmsTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = LmsTextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LmsCardBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Image Card
            item {
                Card(colors = CardDefaults.cardColors(containerColor = LmsCardBg)) {
                    Column {
                        AsyncImage(
                            model = activeCourse.thumbnail,
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(LmsAccentBlue.copy(0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(activeCourse.category, color = LmsAccentBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(activeCourse.duration, color = LmsTextSecondary, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(activeCourse.title, color = LmsTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text("Instructor: ${activeCourse.instructor}", color = LmsAccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(activeCourse.description, color = LmsTextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }

            // CTA ENROLLMENT BOX
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LmsAccentBlue.copy(0.08f)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, LmsAccentBlue.copy(0.2f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (currentUserRole == "Admin") {
                            Text("Administrator Syllabus Settings", color = LmsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("As an admin, you can alter lessons, configure text-books, or append quiz sheets.", color = LmsTextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { createModuleOpen = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("New Module", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        } else if (isEnrolled) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Text("Classroom Enrollment Active!", color = LmsAccentTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Access curriculum modules below.", color = LmsTextSecondary, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { onStartLearning(courseId, "") },
                                    colors = ButtonDefaults.buttonColors(containerColor = LmsAccentTeal)
                                ) {
                                    Text("Go to Class", color = Color.White)
                                }
                            }
                        } else {
                            // Purchase flow
                            val finalPrice = if (activeCourse.isFree) 0.0 else {
                                val baseVal = activeCourse.price * (1.0 - activeCourse.discountPercent / 100.0)
                                if (appliedCoupon != null) baseVal * (1.0 - appliedCoupon!!.discountPercent / 100.0) else baseVal
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Syllabus Price:", color = LmsTextSecondary, fontSize = 11.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (activeCourse.isFree) {
                                            Text("FREE COURSE", color = LmsAccentTeal, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                        } else {
                                            Text("$${"%.2f".format(finalPrice)}", color = LmsTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                            if (activeCourse.discountPercent > 0 || appliedCoupon != null) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("$${"%.2f".format(activeCourse.price)}", color = LmsTextSecondary, fontSize = 12.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (activeCourse.isFree) {
                                            viewModel.handleFreeEnrollment(currentUserId, courseId, activeCourse.title)
                                            Toast.makeText(context, "Syllabus unlocked! Classroom Active.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            showCheckoutDialog = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue)
                                ) {
                                    Text(if (activeCourse.isFree) "Enroll Instantly" else "Buy Course Package")
                                }
                            }

                            // COUPON INJECTION ROW FOR PAID COURSES
                            if (!activeCourse.isFree) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = promoInput,
                                        onValueChange = { promoInput = it },
                                        placeholder = { Text("Code: WELCOME50", fontSize = 10.sp, color = LmsTextSecondary) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = LmsTextPrimary,
                                            unfocusedTextColor = LmsTextPrimary,
                                            focusedBorderColor = LmsAccentBlue,
                                            unfocusedBorderColor = Color.White.copy(0.12f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.applyCouponCode(promoInput) { ok, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.05f)),
                                        modifier = Modifier.height(44.dp)
                                    ) {
                                        Text("Apply", color = LmsTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (appliedCoupon != null) {
                                    Text("Campaign Coupon Applied: ${appliedCoupon?.discountPercent}% Extra Discount!", color = LmsAccentTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // COURSE CURRICULUM SYLLABUS HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Course Curriculum Modules", color = LmsTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("${activeModules.size} Modules", color = LmsTextSecondary, fontSize = 11.sp)
                }
            }

            if (activeModules.isEmpty()) {
                item {
                    Text("Class modules are currently being prioritized by instructor.", color = LmsTextSecondary, fontSize = 12.sp)
                }
            } else {
                items(activeModules) { mod ->
                    // Fetch lessons under module
                    val lessonsFlow by viewModel.getLessonsForModuleFlow(mod.id).collectAsStateWithLifecycle(emptyList())

                    Card(
                        colors = CardDefaults.cardColors(containerColor = LmsCardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(mod.title, color = LmsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                if (currentUserRole == "Admin") {
                                    Row {
                                        IconButton(onClick = {
                                            activeModuleIdForLesson = mod.id
                                            createLessonOpen = true
                                        }) {
                                            Icon(Icons.Filled.AddCircle, "Add lesson", tint = LmsAccentTeal)
                                        }
                                        IconButton(onClick = { viewModel.adminDeleteModule(mod.id) }) {
                                            Icon(Icons.Filled.DeleteOutline, "Del module", tint = LmsAccentCoral)
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(0.04f), modifier = Modifier.padding(vertical = 8.dp))

                            if (lessonsFlow.isEmpty()) {
                                Text("Empty module folder.", color = LmsTextSecondary, fontSize = 11.sp)
                            } else {
                                lessonsFlow.forEach { lesson ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clickable(enabled = isEnrolled) { onStartLearning(courseId, lesson.id) },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val icon = when (lesson.lessonType) {
                                                "Video" -> Icons.Default.PlayCircleOutline
                                                "PDF" -> Icons.Default.LibraryBooks
                                                "Quiz" -> Icons.Default.Quiz
                                                else -> Icons.Default.Assignment
                                            }
                                            Icon(icon, "Type", tint = LmsAccentBlue, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(lesson.title, color = if (isEnrolled) LmsTextPrimary else LmsTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                Text("${lesson.durationMin} mins • ${lesson.lessonType} lesson", color = LmsTextSecondary, fontSize = 10.sp)
                                            }
                                        }

                                        if (currentUserRole == "Admin") {
                                            IconButton(onClick = { viewModel.adminDeleteLesson(courseId, lesson) }) {
                                                Icon(Icons.Filled.Delete, "Delete Lesson", tint = LmsAccentCoral, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CHECKOUT DIALOG
    if (showCheckoutDialog) {
        var cardNum by remember { mutableStateOf("") }
        var cardExpiry by remember { mutableStateOf("") }
        var cardCvv by remember { mutableStateOf("") }

        val finalPrice = if (activeCourse.isFree) 0.0 else {
            val baseVal = activeCourse.price * (1.0 - activeCourse.discountPercent / 100.0)
            if (appliedCoupon != null) baseVal * (1.0 - appliedCoupon!!.discountPercent / 100.0) else baseVal
        }

        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            containerColor = LmsCardBg,
            title = { Text("Checkout Course Package", color = LmsTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("LMS Gateway Services integration simulated successfully.\nEnter test parameters to unlock.", color = LmsTextSecondary, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Class package price:", color = LmsTextSecondary)
                        Text("$${"%.2f".format(finalPrice)} USD", color = LmsAccentTeal, fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(
                        value = cardNum,
                        onValueChange = { cardNum = it },
                        label = { Text("16-Digit Card Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cardExpiry,
                            onValueChange = { cardExpiry = it },
                            label = { Text("MM/YY") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cardCvv,
                            onValueChange = { cardCvv = it },
                            label = { Text("CVV") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (cardNum.isNotEmpty() && cardExpiry.isNotEmpty() && cardCvv.isNotEmpty()) {
                        viewModel.handlePaidEnrollment(currentUserId, courseId, activeCourse.title, finalPrice, appliedCoupon?.code ?: "")
                        showCheckoutDialog = false
                    } else {
                        Toast.makeText(context, "Fill test credit card inputs.", Toast.LENGTH_SHORT).show()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = LmsAccentTeal)) {
                    Text("Secure Purchase", color = Color.White)
                }
            }
        )
    }

    // CREATE MODULE DIALOG
    if (createModuleOpen) {
        var mTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { createModuleOpen = false },
            containerColor = LmsCardBg,
            title = { Text("Create Course Module Block", color = LmsTextPrimary) },
            text = {
                OutlinedTextField(value = mTitle, onValueChange = { mTitle = it }, label = { Text("Module Topic Title") })
            },
            confirmButton = {
                Button(onClick = {
                    if (mTitle.isNotEmpty()) {
                        viewModel.adminAddModule(courseId, mTitle)
                        createModuleOpen = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue)) {
                    Text("Save Module Folder")
                }
            }
        )
    }

    // CREATE LESSON DIALOG
    if (createLessonOpen) {
        var lTitle by remember { mutableStateOf("") }
        var lDesc by remember { mutableStateOf("") }
        var lType by remember { mutableStateOf("Video") } // Video, PDF, Text, Assignment, Quiz
        var videoUrl by remember { mutableStateOf("") }
        var pdfUrl by remember { mutableStateOf("") }
        var textBody by remember { mutableStateOf("") }
        var quizQuestionsJson by remember { mutableStateOf("") }
        var assignTaskText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { createLessonOpen = false },
            containerColor = LmsCardBg,
            title = { Text("Add Lecture Lesson", color = LmsTextPrimary) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Media Type:", color = LmsTextSecondary, fontSize = 11.sp)
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Video", "PDF", "Text", "Assignment", "Quiz").forEach { pt ->
                            val match = lType == pt
                            FilterChip(selected = match, onClick = { lType = pt }, label = { Text(pt) })
                        }
                    }

                    OutlinedTextField(value = lTitle, onValueChange = { lTitle = it }, label = { Text("Lesson Title") })
                    OutlinedTextField(value = lDesc, onValueChange = { lDesc = it }, label = { Text("Short Subtitle Description") })

                    when (lType) {
                        "Video" -> OutlinedTextField(value = videoUrl, onValueChange = { videoUrl = it }, label = { Text("Playable Video URL") })
                        "PDF" -> OutlinedTextField(value = pdfUrl, onValueChange = { pdfUrl = it }, label = { Text("Secure Document PDF URL") })
                        "Text" -> OutlinedTextField(value = textBody, onValueChange = { textBody = it }, label = { Text("Syllabus Book Review Text") })
                        "Assignment" -> OutlinedTextField(value = assignTaskText, onValueChange = { assignTaskText = it }, label = { Text("Assignment Task Submissions Description") })
                        "Quiz" -> {
                            Text("Provide JSON parameters array cards:", color = LmsTextSecondary, fontSize = 9.sp)
                            OutlinedTextField(
                                value = quizQuestionsJson,
                                onValueChange = { quizQuestionsJson = it },
                                label = { Text("Questions JSON structure") },
                                placeholder = { Text("[{\"question\":\"Q?\",\"options\":[\"A\",\"B\"],\"answer\":\"A\"}]") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (lTitle.isNotEmpty()) {
                        viewModel.adminAddLesson(
                            courseId = courseId,
                            moduleId = activeModuleIdForLesson,
                            title = lTitle,
                            description = lDesc,
                            type = lType,
                            videoUrl = videoUrl,
                            pdfUrl = pdfUrl,
                            textBody = textBody,
                            quizQuestions = quizQuestionsJson.ifEmpty { "[]" },
                            assignmentTask = assignTaskText,
                        )
                        createLessonOpen = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue)) {
                    Text("Commit Lesson", color = Color.White)
                }
            }
        )
    }
}

// ==========================================
// 5. CLASSROOM CHROME & LEARNING ROOM
// ==========================================
@Composable
fun CourseLearningRoomScreen(
    courseId: String,
    initialLessonId: String?,
    viewModel: LmsViewModel,
    currentUserId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var course by remember { mutableStateOf<CourseEntity?>(null) }
    var allLessons by remember { mutableStateOf<List<LessonEntity>>(emptyList()) }
    var activeLesson by remember { mutableStateOf<LessonEntity?>(null) }

    // Navigation and Modules
    val activeModules by viewModel.activeCourseModules.collectAsStateWithLifecycle()
    val checkProgressMap by viewModel.activeLessonCompletionStates.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    // Retrieve modules and query syllabus database
    LaunchedEffect(courseId) {
        course = viewModel.allCourses.value.find { it.id == courseId }
        viewModel.loadCourseModules(courseId)
        viewModel.loadCompletedLessons(currentUserId, courseId)
    }

    // Observe activeModules and aggregate lessons list
    LaunchedEffect(activeModules) {
        val list = mutableListOf<LessonEntity>()
        activeModules.forEach { mod ->
            viewModel.getLessonsForModuleFlow(mod.id).collect { mLessons ->
                list.addAll(mLessons)
                allLessons = list.toList()

                if (activeLesson == null) {
                    if (initialLessonId?.isNotEmpty() == true) {
                        activeLesson = allLessons.find { it.id == initialLessonId }
                    }
                    if (activeLesson == null && allLessons.isNotEmpty()) {
                        activeLesson = allLessons.firstOrNull()
                    }
                }
            }
        }
    }

    val lesson = activeLesson ?: return

    Scaffold(
        containerColor = LmsDarkBg,
        topBar = {
            TopAppBar(
                title = { Text(course?.title ?: "Learning Room", color = LmsTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = LmsTextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LmsCardBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lecture Player Area (Interactive Media Player Console)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                when (lesson.lessonType) {
                    "Video" -> {
                        // Dynamic Playback Simulation Controller Layout
                        var isPlaying by remember { mutableStateOf(false) }
                        var sliderProgress by remember { mutableStateOf(0.3f) }
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = course?.thumbnail,
                                contentDescription = "Sim Video Blur",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().alpha(0.3f)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("NOW PLAYING VIDEO STREAM", color = LmsAccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                IconButton(
                                    onClick = { isPlaying = !isPlaying },
                                    modifier = Modifier.size(54.dp).background(Color.White.copy(0.12f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = "Sim play",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Slider(
                                    value = sliderProgress,
                                    onValueChange = { sliderProgress = it },
                                    colors = SliderDefaults.colors(thumbColor = LmsAccentBlue, activeTrackColor = LmsAccentBlue)
                                )
                            }
                        }
                    }
                    "PDF" -> {
                        var pageIndex by remember { mutableStateOf(1) }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.DarkGray)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("DOCUMENT BOOKLET VIEWER", color = LmsAccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Lecture Chapter Study Material Overview. Fully persistent reference books containing compose blueprints and room diagrams.", color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = { if (pageIndex > 1) pageIndex-- },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.12f))
                                ) {
                                    Text("Previous", color = Color.White, fontSize = 10.sp)
                                }
                                Text("Page $pageIndex / 24", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { pageIndex++ },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.12f))
                                ) {
                                    Text("Next Page", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    "Text" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2C3E50)).padding(16.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Description, "Txt", tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                    }
                    "Assignment" -> {
                        var assignSubmText by remember { mutableStateOf("") }
                        var uploadedFile by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ASSIGNMENT SPECIFICATION DETAILS", color = LmsTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(lesson.assignmentDesc.ifEmpty { "Build compile target elements matching course guidelines." }, color = LmsTextSecondary, fontSize = 10.sp)
                            OutlinedTextField(
                                value = assignSubmText,
                                onValueChange = { assignSubmText = it },
                                placeholder = { Text("Write submit note description...", fontSize = 9.sp) },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            )
                            Button(
                                onClick = {
                                    uploadedFile = true
                                    Toast.makeText(context, "Assignment uploaded successfully to storage bucket!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LmsAccentBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (uploadedFile) "Update Submission (Success)" else "Attach Solution Document", fontSize = 10.sp)
                            }
                        }
                    }
                    "Quiz" -> {
                        var activeQuizIndex by remember { mutableStateOf(0) }
                        var activeScoreCount by remember { mutableStateOf(0) }
                        var quizOver by remember { mutableStateOf(false) }

                        // Parse the JSON questions
                        val questions = remember(lesson.quizQuestions) {
                            try {
                                val clean = lesson.quizQuestions.trim()
                                if (clean.startsWith("[")) {
                                    // Parse mock-like structure rather than importing heavy moshi adapters
                                    val regex = """\{"question":"([^"]*)","options":\["([^"]*)","([^"]*)","([^"]*)","([^"]*)"\],"answer":"([^"]*)"\}""".toRegex()
                                    regex.findAll(clean).map { match ->
                                        val q = match.groupValues[1]
                                        val op1 = match.groupValues[2]
                                        val op2 = match.groupValues[3]
                                        val op3 = match.groupValues[4]
                                        val op4 = match.groupValues[5]
                                        val ans = match.groupValues[6]
                                        QuizCard(q, listOf(op1, op2, op3, op4), ans)
                                    }.toList()
                                } else emptyList()
                            } catch (e: Exception) {
                                emptyList()
                            }
                        }

                        if (questions.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Quiz questions currently being finalized by reviewer.", color = Color.White)
                            }
                        } else if (quizOver) {
                            Column(
                                modifier = Modifier.fillMaxSize().background(LmsCardBg).padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Filled.EmojiEvents, "Icon", tint = LmsAccentGold, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.weight(1f))
                                Text("Interactive Quiz Evaluated!", color = LmsTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Score: $activeScoreCount / ${questions.size}", color = LmsAccentTeal, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.weight(1f))
                                Button(
                                    onClick = {
                                        viewModel.submitQuizScore(currentUserId, lesson.id, activeScoreCount, questions.size)
                                        quizOver = false
                                        activeQuizIndex = 0
                                        activeScoreCount = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LmsAccentTeal)
                                ) {
                                    Text("Record Attempt Score", color = Color.White)
                                }
                            }
                        } else {
                            val activeQ = questions[activeQuizIndex]
                            Column(
                                modifier = Modifier.fillMaxSize().background(LmsCardBg).padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Quiz Question ${activeQuizIndex + 1} of ${questions.size}", color = LmsAccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("Active score: $activeScoreCount", color = LmsAccentTeal, fontSize = 10.sp)
                                }

                                Text(activeQ.question, color = LmsTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)

                                activeQ.options.forEach { opt ->
                                    Button(
                                        onClick = {
                                            if (opt == activeQ.answer) {
                                                activeScoreCount++
                                            }
                                            if (activeQuizIndex + 1 < questions.size) {
                                                activeQuizIndex++
                                            } else {
                                                quizOver = true
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.06f)),
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(opt, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Left)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Lesson Details and Completed Controller
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(lesson.title, color = LmsTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(lesson.description, color = LmsTextSecondary, fontSize = 12.sp)

                HorizontalDivider(color = Color.White.copy(0.04f))

                // Lesson text bodies
                if (lesson.lessonType == "Text") {
                    Text(lesson.textBody.ifEmpty { "Detailed summary textbooks to read." }, color = LmsTextPrimary, fontSize = 12.sp, lineHeight = 18.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mark lesson computed checkbox
                val isCompleted = checkProgressMap[lesson.id] == true
                Surface(
                    color = if (isCompleted) LmsAccentTeal.copy(0.08f) else LmsCardBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.markLessonProgress(
                                studentId = currentUserId,
                                courseId = courseId,
                                lessonId = lesson.id,
                                isCompleted = !isCompleted,
                                totalLessonsCount = course?.lessonsCount ?: 1,
                                courseName = course?.title ?: ""
                            )
                        }
                        .border(1.dp, if (isCompleted) LmsAccentTeal.copy(0.3f) else Color.White.copy(0.04f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                tint = if (isCompleted) LmsAccentTeal else LmsTextSecondary,
                                contentDescription = "Tick",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Mark this lesson completed", color = LmsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Updates your progress meters instantly.", color = LmsTextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Module and syllabus lists drawer inside room
                Text("Room Module Outline Selector", color = LmsTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                activeModules.forEach { mod ->
                    val mSyllabusLessons by viewModel.getLessonsForModuleFlow(mod.id).collectAsStateWithLifecycle(emptyList())
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LmsCardBg, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(mod.title, color = LmsAccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        mSyllabusLessons.forEach { les ->
                            val complete = checkProgressMap[les.id] == true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { activeLesson = les },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (complete) Icons.Filled.CheckCircle else Icons.Outlined.PlayCircleFilled,
                                        tint = if (complete) LmsAccentTeal else LmsTextSecondary,
                                        contentDescription = "Tick",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(les.title, color = if (les.id == lesson.id) LmsAccentBlue else LmsTextPrimary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

data class QuizCard(
    val question: String,
    val options: List<String>,
    val answer: String
)
