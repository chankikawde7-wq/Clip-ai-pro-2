package com.example.ui.views

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.CreatorViewModel

private val ProfileNeonPurple = Color(0xFFA78BFA)
private val ProfileNeonCyan = Color(0xFF22D3EE)
private val ProfileNeonBlue = Color(0xFF60A5FA)
private val ProfileGold = Color(0xFFFBBF24)
private val ProfileCardBg = Color(0xFF0D0D1E)
private val ProfileDarkGrey = Color(0xFF111122)

@Composable
fun ProfileScreenView(
    viewModel: CreatorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userStats by viewModel.userStats.collectAsState()
    
    // Dialog states for interactive settings
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var showPersonalizationDialog by remember { mutableStateOf(false) }
    
    // Dialog states for ABOUT section
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    
    // Destructive action confirmation dialogs
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteChatsDialog by remember { mutableStateOf(false) }
    
    // Setting values from Room database flow
    val selectedLanguage = userStats?.language ?: "English"
    val selectedTheme = userStats?.theme ?: "System"
    val selectedVoice = userStats?.voice ?: "Default Voice"
    val selectedModel = userStats?.aiModel ?: "Gemini 2.5 Flash"
    val personalizationEnabled = userStats?.personalization ?: true

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- SCREEN TITLE ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = ProfileNeonPurple,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "My Profile Settings",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }

        // --- AVATAR & HEADER ---
        userStats?.let { stats ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(ProfileNeonPurple, ProfileNeonCyan))),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ProfileNeonPurple, ProfileNeonCyan)))
                            .padding(2.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF050510)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stats.email.take(1).uppercase(),
                                color = ProfileNeonCyan,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stats.email,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ProfileNeonCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = ProfileNeonCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stats.subscriptionPlan,
                            color = ProfileNeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .background(ProfileCardBg, RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Not logged in", color = Color.LightGray, fontSize = 14.sp)
            }
        }

        // --- PREFERENCES GROUP ---
        Text(
            text = "PREFERENCES",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column {
                // 1. Language Option
                SettingsRow(
                    icon = Icons.Outlined.Translate,
                    title = "Language",
                    valueToken = selectedLanguage,
                    onClick = { showLanguageDialog = true }
                )
                Divider(color = Color.White.copy(alpha = 0.07f), thickness = 0.5.dp)

                // 2. Theme Option
                SettingsRow(
                    icon = Icons.Outlined.Palette,
                    title = "Theme",
                    valueToken = selectedTheme,
                    onClick = { showThemeDialog = true }
                )
                Divider(color = Color.White.copy(alpha = 0.07f), thickness = 0.5.dp)

                // 3. Voice Option
                SettingsRow(
                    icon = Icons.Outlined.RecordVoiceOver,
                    title = "Voice",
                    valueToken = selectedVoice,
                    onClick = { showVoiceDialog = true }
                )
                Divider(color = Color.White.copy(alpha = 0.07f), thickness = 0.5.dp)

                // 4. Model Option
                SettingsRow(
                    icon = Icons.Outlined.SmartToy,
                    title = "Model",
                    valueToken = selectedModel,
                    onClick = { showModelDialog = true }
                )
                Divider(color = Color.White.copy(alpha = 0.07f), thickness = 0.5.dp)

                // 5. Personalization Option
                SettingsRow(
                    icon = Icons.Outlined.Tune,
                    title = "Personalization",
                    valueToken = if (personalizationEnabled) "On" else "Off",
                    onClick = { showPersonalizationDialog = true }
                )
            }
        }

        // --- ABOUT GROUP ---
        Text(
            text = "ABOUT",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column {
                // Terms of Service
                SettingsRow(
                    icon = Icons.Outlined.Description,
                    title = "Terms of Service",
                    onClick = { showTermsDialog = true }
                )
                Divider(color = Color.White.copy(alpha = 0.07f), thickness = 0.5.dp)

                // Privacy Policy
                SettingsRow(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "Privacy Policy",
                    onClick = { showPrivacyDialog = true }
                )
                Divider(color = Color.White.copy(alpha = 0.07f), thickness = 0.5.dp)

                // About Info
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    title = "About",
                    onClick = { showAboutDialog = true }
                )
                Divider(color = Color.White.copy(alpha = 0.07f), thickness = 0.5.dp)

                // Contact Us
                SettingsRow(
                    icon = Icons.Outlined.Email,
                    title = "Contact Us",
                    onClick = { showContactDialog = true }
                )
            }
        }

        // --- ACTIONS SECTION ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Column {
                // Log Out
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoutDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log out",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Log out",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.07f), thickness = 0.5.dp)

                // Delete All Chats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteChatsDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete all chats",
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Delete all chats",
                        color = Color(0xFFF87171),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // App licensing details in bottom bounds
        Text(
            text = "CLIP AI Hub v2.4.0\nSecure Local Sandboxed Database Active",
            color = Color.Gray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }

    // ==========================================
    // PREFERENCE POPUP DIALOGS
    // ==========================================

    // Language Dialog
    if (showLanguageDialog) {
        SelectionDialog(
            title = "Select Language",
            options = listOf("English", "Hindi (हिन्दी)", "Spanish (Español)", "Marathi (मराठी)"),
            selectedValue = selectedLanguage,
            onDismiss = { showLanguageDialog = false },
            onSelect = {
                viewModel.updateLanguage(it)
                showLanguageDialog = false
                Toast.makeText(context, "Language changed to $it", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        SelectionDialog(
            title = "Select App Theme",
            options = listOf("System", "Dark Theme", "Light Theme", "Future Neon (Amoled)"),
            selectedValue = selectedTheme,
            onDismiss = { showThemeDialog = false },
            onSelect = {
                viewModel.updateTheme(it)
                showThemeDialog = false
                Toast.makeText(context, "$it applied", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Voice Dialog
    if (showVoiceDialog) {
        SelectionDialog(
            title = "Select AI Voice",
            options = listOf("Default Voice", "Sophia (Warm)", "Alex (Polished Male)", "Rohan (Indian Accent)", "Elena (Cheerful)"),
            selectedValue = selectedVoice,
            onDismiss = { showVoiceDialog = false },
            onSelect = {
                viewModel.updateVoice(it)
                showVoiceDialog = false
                Toast.makeText(context, "Voice changed to $it", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Model Dialog
    if (showModelDialog) {
        SelectionDialog(
            title = "Active Generative Model",
            options = listOf("Gemini 2.5 Flash", "Gemini 1.5 Pro", "Gemma 7B (Local Model)", "Claude 3.5 Sonnet"),
            selectedValue = selectedModel,
            onDismiss = { showModelDialog = false },
            onSelect = {
                viewModel.updateAiModel(it)
                showModelDialog = false
                Toast.makeText(context, "$it activated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Personalization Dialog
    if (showPersonalizationDialog) {
        Dialog(onDismissRequest = { showPersonalizationDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("AI Personalization", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Help Clip AI improve its response accuracy by customizing suggestions according to your historical work preferences.",
                        color = Color.LightGray, fontSize = 12.sp, lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Personalized Prompts", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = personalizationEnabled,
                            onCheckedChange = { viewModel.updatePersonalization(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ProfileNeonCyan,
                                checkedTrackColor = ProfileNeonCyan.copy(alpha = 0.4f)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showPersonalizationDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ProfileNeonPurple),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Options", color = Color.White)
                    }
                }
            }
        }
    }

    // ==========================================
    // ABOUT POPUP DIALOGS
    // ==========================================

    // Terms of Service
    if (showTermsDialog) {
        AboutInfoDialog(
            title = "Terms of Service",
            content = "Welcome to CLIP AI Hub!\n\nThese terms outline the rules and regulations for using Clip AI's applications and tools. By using this service, you consent to secure, offline-first sandbox data tracking. We cache generated image and video metadata directly onto your secure SQLite Room instance. Billing operations run as a mock ledger sandbox for test purchases.\n\nAll AI generated content is royalty-free is owned exclusively by you.",
            onDismiss = { showTermsDialog = false }
        )
    }

    // Privacy Policy
    if (showPrivacyDialog) {
        AboutInfoDialog(
            title = "Privacy Policy",
            content = "Privacy is our highest directive.\n\nCLIP AI Hub retains zero data on remote servers unless explicitly requested. Every single text-to-image request, video prompt, or speech synthesis file is generated in direct collaboration with our secure gateway API and indexed safely on your localized private cache.\n\nWe do not compile telemetry or share user account identifiers with foreign marketing structures.",
            onDismiss = { showPrivacyDialog = false }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AboutInfoDialog(
            title = "About CLIP AI Hub",
            content = "CLIP AI Hub is a premium, high-integrity generative creation platform tailored for futuristic image scaling, video generation, and voice-over synthesis.\n\nBuilt securely using Kotlin, Jetpack Compose, and Room local databases.\n\nDeveloped by Google AI Studio.",
            onDismiss = { showAboutDialog = false }
        )
    }

    // Contact Us Diallog
    if (showContactDialog) {
        var contactQuery by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showContactDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Contact Us & Feedback", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Support Email: support@clipai.hub", color = ProfileNeonCyan, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = contactQuery,
                        onValueChange = { contactQuery = it },
                        placeholder = { Text("What feedback or questions do you have?", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ProfileNeonCyan,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showContactDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                        Button(
                            onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:")
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf("support@clipai.hub"))
                                    putExtra(Intent.EXTRA_SUBJECT, "Clip AI Hub Support & Feedback")
                                    putExtra(Intent.EXTRA_TEXT, contactQuery)
                                }
                                try {
                                    context.startActivity(Intent.createChooser(emailIntent, "Open Mail App..."))
                                    showContactDialog = false
                                    Toast.makeText(context, "Opening email application...", Toast.LENGTH_SHORT).show()
                                } catch (ex: Exception) {
                                    Toast.makeText(context, "No email app found. Please send directly to support@clipai.hub", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ProfileNeonPurple),
                            enabled = contactQuery.isNotBlank(),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Send", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DESTRUCTIVE ACTION DIALOGS
    // ==========================================

    // Log Out Dialog
    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "Log out",
            message = "Are you sure you want to log out from CLIP AI Hub?",
            confirmText = "Log out",
            confirmColor = Color(0xFFEF4444),
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete All Chats Dialog
    if (showDeleteChatsDialog) {
        ConfirmationDialog(
            title = "Delete all chats?",
            message = "This will permanently clear your safe generation logs, cached images, and workspaces history. This action is irreversible.",
            confirmText = "Delete Permanently",
            confirmColor = Color(0xFFEF4444),
            onDismiss = { showDeleteChatsDialog = false },
            onConfirm = {
                showDeleteChatsDialog = false
                viewModel.clearAllHistory()
                Toast.makeText(context, "Deletions completed. All history cleared!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

// --- COMPOSE SUB-COMPONENTS FOR REUSABILITY ---

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    valueToken: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        if (valueToken != null) {
            Text(
                text = valueToken,
                color = ProfileNeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                options.forEach { option ->
                    val isSelected = option.startsWith(selectedValue) || option == selectedValue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ProfileNeonCyan,
                                unselectedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = option,
                            color = if (isSelected) ProfileNeonCyan else Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AboutInfoDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = content,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ProfileNeonBlue),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Got it", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = message,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
                    ) {
                        Text(confirmText, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
