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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CreationEntity
import com.example.ui.CreatorViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

// Extra UI Colors
val GoldenAccent = Color(0xFFFBBF24)
val LuxuryEmerald = Color(0xFF10B981)
val SleekGray = Color(0xFF4B5563)
val DarkIceBlue = Color(0xFF0F172A)
val PremiumInk = Color(0xFF030712)
val NeonPink = Color(0xFFF43F5E)
val NeonGreen = Color(0xFF34D399)
val GradientPurple = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))

// Live Website States representable in workspace 
class EditableSection(
    val id: String,
    val type: String, // "hero", "about", "services", "features", "pricing", "team", "faq", "contact", "footer"
    initialTitle: String,
    initialSubtitle: String = "",
    initialContent: String = "",
    initialItems: List<String> = emptyList(),
    initialImageLabel: String = "💫 Tech Core",
    initialEnabled: Boolean = true
) {
    var title by mutableStateOf(initialTitle)
    var subtitle by mutableStateOf(initialSubtitle)
    var content by mutableStateOf(initialContent)
    val items = mutableStateListOf<String>().apply { addAll(initialItems) }
    var imageLabel by mutableStateOf(initialImageLabel)
    var isEnabled by mutableStateOf(initialEnabled)
}

class UserWebsite(
    val projectId: String,
    initialProjectName: String,
    initialType: String,
    initialDesign: String,
    initialTitle: String,
    initialHeading: String,
    initialSubheading: String,
    initialCta: String,
    initialPrimary: Color,
    initialSecondary: Color,
    initialAccent: Color,
    initialBg: Color,
    initialSeoTitle: String = "Smart Digital Workspace",
    initialMetaDesc: String = "Explore cutting edge tools designed to modernize enterprise delivery systems.",
    initialKeywords: String = "AI tool, high density, workspace, automation",
    initialCustomDomain: String = "",
    initialPublished: Boolean = false,
    initialSsl: Boolean = true,
    sectionsList: List<EditableSection> = emptyList()
) {
    var projectName by mutableStateOf(initialProjectName)
    var type by mutableStateOf(initialType)
    var designSelection by mutableStateOf(initialDesign)
    var mainTitle by mutableStateOf(initialTitle)
    var heroHeading by mutableStateOf(initialHeading)
    var heroSubheading by mutableStateOf(initialSubheading)
    var callToAction by mutableStateOf(initialCta)
    
    // Core color systems 
    var primaryColor by mutableStateOf(initialPrimary)
    var secondaryColor by mutableStateOf(initialSecondary)
    var accentColor by mutableStateOf(initialAccent)
    var backgroundColor by mutableStateOf(initialBg)

    // SEO Settings
    var seoTitle by mutableStateOf(initialSeoTitle)
    var metaDesc by mutableStateOf(initialMetaDesc)
    var keywords by mutableStateOf(initialKeywords)

    // Distribution
    var customDomain by mutableStateOf(initialCustomDomain)
    var isPublished by mutableStateOf(initialPublished)
    var sslEnabled by mutableStateOf(initialSsl)

    // Dynamic modular sections list
    val sections = mutableStateListOf<EditableSection>().apply { addAll(sectionsList) }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InteractiveWebsiteBuilderWorkspace(
    topic: String,
    format: String,
    generatedCode: String,
    viewModel: CreatorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val initialTopic = if (topic.isBlank()) "NextGen Digital Hub" else topic

    // 1. Manage List of projects dynamically for robust dashboard operations
    val myProjects = remember {
        mutableStateListOf(
            UserWebsite(
                projectId = "proj_01",
                initialProjectName = "NextGen Digital Hub Landing",
                initialType = "SaaS Website",
                initialDesign = "Futuristic",
                initialTitle = "NextGen Hub",
                initialHeading = "Synthesized Workspaces Built to Accelerate Your Enterprise",
                initialSubheading = "Engineered with intelligent auto-optimizing design models for flawless modern responsiveness.",
                initialCta = "Deploy Free Nodes 🚀",
                initialPrimary = NeonPurple,
                initialSecondary = NeonPink,
                initialAccent = NeonCyan,
                initialBg = Color(0xFF0C081A),
                initialCustomDomain = "nextgen-digital.in",
                initialPublished = true,
                sectionsList = listOf(
                    EditableSection("sec_hero", "hero", "Active Workspace Presets", "Accelerate Delivery Framework", "Build with neon grids"),
                    EditableSection("sec_features", "features", "Enterprise Capabilities", "Key modular units designed for maximum throughput", "", listOf("Instant Autonomous Pipelines", "Live Serverless Synchronization", "Decentralized Telemetry Blocks")),
                    EditableSection("sec_pricing", "pricing", "Transparent Workspace Plans", "Simple proportional scale pricing structured around your actual demands", "", listOf("Dev Tier - ₹0 (Free forever)", "Enterprise Core - ₹2,499/mo", "Nexus Ultra Custom")),
                    EditableSection("sec_faq", "faq", "Frequently Answered Intel", "Common inquiries processed through support channels", "", listOf("Q: Is custom code supported? \nA: Yes, advanced builders allow inserting injection logs.", "Q: Can I bind custom domains? \nA: SSL is provisioned instantly upon domain propagation.")),
                    EditableSection("sec_contact", "contact", "Enquire Early Beta Hub", "Submit details to reserve priority access", "Fill out secure contact cards below"),
                    EditableSection("sec_footer", "footer", "NextGen Hub Limited", "Generated with AI Site Creator Suite", "All content copyright preserved")
                )
            ),
            UserWebsite(
                projectId = "proj_02",
                initialProjectName = "Fresh harvest Delhi delivery",
                initialType = "Business Website",
                initialDesign = "Minimal",
                initialTitle = "Dehat Organics",
                initialHeading = "100% Organic Pesticide-Free Vegetable Supply Delivered Across Delhi NCR",
                initialSubheading = "Our local farms ensure pure traditional farm-to-table fresh harvests within six hours.",
                initialCta = "Explore Seasonal Box 🍏",
                initialPrimary = Color(0xFF10B981),
                initialSecondary = Color(0xFF34D399),
                initialAccent = Color(0xFFFBBF24),
                initialBg = Color(0xFF03140A),
                initialCustomDomain = "dehat-organics.co.in",
                initialPublished = false,
                sectionsList = listOf(
                    EditableSection("sec_hero", "hero", "Traditional Organic Greenery", "Fresh Pesticide-free Crop Delivery", "farm fresh"),
                    EditableSection("sec_about", "about", "Our Local Haryana Farms", "Pioneering eco-friendly sustainable agriculture formats in northern plains", "Over 200 acres dedicated to organic crop growth under expert supervision."),
                    EditableSection("sec_services", "services", "Delhi NCR Doorstep Delivery", "Flexible organic custom subscription box choices", "", listOf("Daily Vegetables Handpicked Delivery", "Cold-Pressed Mustard Oil & Honey", "E-Certificate Lab Reports For Purity")),
                    EditableSection("sec_pricing", "pricing", "Delhi Subscription Tiers", "Fresh boxes direct to households", "", listOf("Standard Basket - ₹599/week", "Mega Family Organic - ₹1,499/week", "Monthly Green Pass - ₹3,999")),
                    EditableSection("sec_contact", "contact", "Schedule a Farm Visit Today", "We welcome families for live organic tours", "Farm in Sonipat, Haryana"),
                    EditableSection("sec_footer", "footer", "Dehat Organics Delhi Ltd", "Cultivating pure health", "Since 2018")
                )
            )
        )
    }

    // 2. State of Currently Active Loaded Project
    var activeProject by remember { mutableStateOf(myProjects[0]) }

    // Dropdown selection States during generation in Dashboard
    var inputPromptText by remember { mutableStateOf(initialTopic) }
    var selectedWebType by remember { mutableStateOf("SaaS Website") }
    var selectedStylePreset by remember { mutableStateOf("Futuristic") }

    // Active workspace subtabs 
    // 0 = PROJECT MANAGER, 1 = VISUAL SECTION BUILDER, 2 = SEO & ROUTING, 3 = EXPORT & SCRIPTS, 4 = METRICS & ANALYTICS
    var activeWorkspaceTab by remember { mutableStateOf(1) }

    // Responsive Simulator Mode: "mobile", "tablet", "desktop"
    var responsiveMode by remember { mutableStateOf("desktop") }

    // AI Generation progress overlay states
    var isGeneratingWebsite by remember { mutableStateOf(false) }
    var generationProgressText by remember { mutableStateOf("") }
    var generationProgressPercent by remember { mutableStateOf(0f) }

    // Live AI Editor Image dialog states
    var showImageSelectorForSection by remember { mutableStateOf<EditableSection?>(null) }
    val mockImagePresets = listOf(
        "🛸 Cybernetic Telemetry Model",
        "⚡ Hyper-Scale Vector Illustration",
        "☘️ Pristine Eco Farm Harvest",
        "📈 Interactive Analytics Core",
        "📦 Modern Product Logistics Grid",
        "👤 Premium High-Quality Avatars",
        "💻 Elegant Minimal Code IDE"
    )

    // One Click Publish loader states
    var isPublishingToServer by remember { mutableStateOf(false) }
    var publishMessageStatus by remember { mutableStateOf("") }
    var publishProgressVal by remember { mutableStateOf(0f) }

    // Section Selector to Add dynamically
    var expandedAddSectionMenu by remember { mutableStateOf(false) }
    val sectionTypesToAdd = listOf("about", "services", "features", "pricing", "team", "faq", "contact")

    // Contact form inputs inside interactive simulator (Mobile/Desktop preview)
    var simNameInput by remember { mutableStateOf("") }
    var simEmailInput by remember { mutableStateOf("") }
    var simMessageInput by remember { mutableStateOf("") }
    var simSubmittedSuccess by remember { mutableStateOf(false) }

    // Helper functions for dynamic layout modifications
    val moveSectionUp: (Int) -> Unit = { index ->
        if (index > 0) {
            val element = activeProject.sections.removeAt(index)
            activeProject.sections.add(index - 1, element)
            Toast.makeText(context, "Moved section upstream!", Toast.LENGTH_SHORT).show()
        }
    }

    val moveSectionDown: (Int) -> Unit = { index ->
        if (index < activeProject.sections.size - 1) {
            val element = activeProject.sections.removeAt(index)
            activeProject.sections.add(index + 1, element)
            Toast.makeText(context, "Moved section downstream!", Toast.LENGTH_SHORT).show()
        }
    }

    // AI Website Content Compiler
    val processPromptToWebsiteBuild = {
        coroutineScope.launch {
            isGeneratingWebsite = true
            generationProgressPercent = 0.15f
            generationProgressText = "🛰️ Analyzing prompt semantic vectors..."
            delay(900)
            generationProgressPercent = 0.40f
            generationProgressText = "🎨 Compiling [${selectedStylePreset}] thematic style guidelines & system colors..."
            delay(1100)
            generationProgressPercent = 0.70f
            generationProgressText = "🧱 Structuring 9-section modular layout grid with responsive constraints..."
            delay(1200)
            generationProgressPercent = 0.90f
            generationProgressText = "✍️ Writing custom AI content titles, FAQ lists, and SEO descriptors..."
            delay(1000)

            // Content generator depending on Selected Type & Preset
            val generatedColors = when (selectedStylePreset) {
                "Futuristic" -> Triple(NeonPurple, NeonPink, NeonCyan)
                "Minimal" -> Triple(Color.White, SleekGray, NeonCyan)
                "Premium" -> Triple(GoldenAccent, Color(0xFFFBBF24), Color(0xFF34D399))
                "Glassmorphism" -> Triple(NeonCyan, NeonPurple, Color(0x80F43F5E))
                "Neumorphism" -> Triple(Color(0xFF818CF8), Color(0xFFC7D2FE), Color(0xFF34D399))
                "Dark Theme" -> Triple(Color(0xFF3B82F6), Color(0xFF60A5FA), NeonPurple)
                "Light Theme" -> Triple(Color(0xFF2563EB), Color(0xFF1D4ED8), Color(0xFFEF4444))
                else -> Triple(NeonBlue, NeonPurple, NeonGreen)
            }

            val generatedBg = when (selectedStylePreset) {
                "Minimal" -> Color(0xFF0C0C0E)
                "Premium" -> Color(0xFF0F0E0B)
                "Light Theme" -> Color(0xFFFAFAFA)
                "Futuristic" -> Color(0xFF050110)
                else -> Color(0xFF090913)
            }

            val dynamicTextHeading = when (selectedWebType) {
                "SaaS Website" -> "Scale Your Productivity Pipelines with $inputPromptText AI Systems"
                "Business Website" -> "Modern Professional Services Tailored for $inputPromptText"
                "Portfolio Website" -> "Exceptional UI/UX Engineering & Dynamic Portfolios for $inputPromptText"
                "AI Tool Website" -> "Harness Deep Learning Networks Built Exclusively for $inputPromptText"
                "Agency Website" -> "Full-Service Creative Engineering Strategies Powering $inputPromptText"
                "Startup Website" -> "Redefining High Growth Product Paradigms with $inputPromptText Operations"
                "Blog Website" -> "Thought Leadership Insights & Essential Guidelines for $inputPromptText"
                "E-commerce Website" -> "Buy Handcrafted Premium Products Direct on Indian $inputPromptText Store"
                else -> "The Ultimate Modern Landing Platform Engineered for $inputPromptText Solutions"
            }

            val dynamicSub = "Redefining the standard of execution using modern fully-responsive elements. Experience next-generation capabilities, fluid transitions, and lightning-fast optimization schemas."

            val imageLabel = when (selectedWebType) {
                "SaaS Website" -> "🛸 Cybernetic Telemetry Model"
                "AI Tool Website" -> "💻 Elegant Minimal Code IDE"
                "E-commerce Website" -> "📦 Modern Product Logistics Grid"
                else -> "⚡ Hyper-Scale Vector Illustration"
            }

            // Create customized modular sections
            val generatedSections = listOf(
                EditableSection("sec_hero", "hero", "Ready to Build Tomorrow", "Experience Instant Scale Platforms", "Hero elements styled elegantly", initialImageLabel = imageLabel),
                EditableSection("sec_about", "about", "Who we serve", "Engineering modern capabilities for high-throughput teams", "We solve critical bottlenecks through optimized pipeline automation frameworks."),
                EditableSection("sec_services", "services", "Our Key Capabilities", "Custom systems built for fast iterations", "", listOf("Serverless Pipeline Orchestration", "Dynamic Optimization Engines", "Integrated Live Telemetry")),
                EditableSection("sec_features", "features", "Highlights Catalog", "Why industry architects choose our framework", "", listOf("Sub-20ms edge database replication", "Instant SSL on custom domains", "99.99% Guaranteed sandbox availability")),
                EditableSection("sec_pricing", "pricing", "Transparent Tier Plans", "No credit card required. Cancel or upgrade anytime instantly.", "", listOf("Starter Core - ₹0/mo", "Nexus Developer - ₹1,299/mo", "Global Pro enterprise package")),
                EditableSection("sec_team", "team", "Meet the Engineering Pod", "Dynamic execution driven by elite designers", "", listOf("Aryan Sharma - Principal Architect", "Devika Iyer - Lead Content Specialist")),
                EditableSection("sec_faq", "faq", "Answers to Technical Specs", "Frequently asked user questions summarized", "", listOf("Q: How do exports work? \nA: Clean Tailwind HTML and scripts compile with one-click.")),
                EditableSection("sec_contact", "contact", "Connect with our Specialists", "Reserving premium beta passes instantly", "Enter your metrics below to initialize connection"),
                EditableSection("sec_footer", "footer", "$inputPromptText Limited", "Engineered live with Dora AI Generator", "All brand reserves bound beautifully.")
            )

            val newProjId = "proj_${System.currentTimeMillis()}"
            val newWeb = UserWebsite(
                projectId = newProjId,
                initialProjectName = "AI Project: $inputPromptText",
                initialType = selectedWebType,
                initialDesign = selectedStylePreset,
                initialTitle = inputPromptText,
                initialHeading = dynamicTextHeading,
                initialSubheading = dynamicSub,
                initialCta = "Explore Sandbox Features ✨",
                initialPrimary = generatedColors.first,
                initialSecondary = generatedColors.second,
                initialAccent = generatedColors.third,
                initialBg = generatedBg,
                initialSeoTitle = "$inputPromptText - Next-Generation Dynamic Web Platform",
                initialMetaDesc = "Explore optimized design frames created live for $inputPromptText using the AI website generator.",
                initialKeywords = "Dora AI, India workspace, $inputPromptText, reactive design",
                initialCustomDomain = "${inputPromptText.lowercase().filter { it.isLetterOrDigit() }.take(10)}-web.co.in",
                initialPublished = false,
                sectionsList = generatedSections
            )

            myProjects.add(0, newWeb)
            activeProject = newWeb
            generationProgressPercent = 1.0f
            isGeneratingWebsite = false
            Toast.makeText(context, "🎉 AI Site successfully built and loaded!", Toast.LENGTH_LONG).show()
        }
    }

    // Export ZIP simulation payload
    val generateZipDownloadSim = {
        val finalHtml = generateResponsiveHtmlTailwind(activeProject)
        val finalCss = generateRootCssProperties(activeProject)
        val finalJs = "document.addEventListener('DOMContentLoaded', () => { console.log('Successfully loaded index website for: ${activeProject.projectName}'); });"

        val pkgStructure = """
            =========================================
            ZIP PACKAGE CONSTRUCTED SUCCESSFULLY 🎉
            =========================================
            📁 Dist Root / 
              |-- 📄 index.html  (Fully compilable responsive code)
              |-- 📄 styles.css  (System palettes matching design guidelines)
              |-- 📄 script.js   (Simulated interface integrations)
              |-- 📄 robots.txt  (Configured SEO indexing vectors)
              |-- 📄 README.txt  (Deployment guidelines on Netlify / Vercel)
            
            [ZIP FILE GENERATION DETAILS]
            Primary Color Tag: #${Integer.toHexString(activeProject.primaryColor.value.toInt()).takeLast(6)}
            SEO Title Attribute: ${activeProject.seoTitle}
        """.trimIndent()

        val mockCreation = CreationEntity(
            id = (20000..99999).random(),
            toolName = "AI Website Generator ZIP",
            category = "Dora Web Exporter",
            inputPrompt = "ZIP Archive Package for ${activeProject.projectName}",
            outputText = pkgStructure,
            isSaved = true
        )
        viewModel.triggerMockDownload(mockCreation)
        Toast.makeText(context, "ZIP Package downloaded into saved database creations! 📦", Toast.LENGTH_LONG).show()
    }

    // Publish App Simulator execution
    val triggerServerDeploymentPipeline = {
        coroutineScope.launch {
            isPublishingToServer = true
            publishProgressVal = 0.15f
            publishMessageStatus = "Resolving secure sandboxed ports & DNS records..."
            delay(700)
            publishProgressVal = 0.45f
            publishMessageStatus = "Compiling customized Tailwind HTML and minifying responsive CSS grids..."
            delay(950)
            publishProgressVal = 0.75f
            publishMessageStatus = "Provisioning free SSL certificate via Let's Encrypt servers..."
            delay(1100)
            publishProgressVal = 0.90f
            publishMessageStatus = "Configuring custom domain routes: https://${activeProject.customDomain.ifBlank { "sandbox.aistudio.build/site" }}..."
            delay(800)
            publishProgressVal = 1.0f
            activeProject.isPublished = true
            isPublishingToServer = false
            Toast.makeText(context, "🚀 Website published successfully! URL is live now.", Toast.LENGTH_LONG).show()
        }
    }

    Column(modifier = modifier.fillMaxWidth().testTag("ai_website_generator_parent")) {
        // AI Website Generation Overlay Dialog
        if (isGeneratingWebsite) {
            AlertDialog(
                onDismissRequest = {},
                containerColor = Color(0xFF0F0F1D),
                tonalElevation = 6.dp,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = NeonPurple, modifier = Modifier.size(24.dp))
                        Text(
                            "Dora AI Website Creator",
                            color = NeonCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Please wait. Our neural layout planner is building elements, selecting styles, and writing copy modules...",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        LinearProgressIndicator(
                            progress = generationProgressPercent,
                            color = NeonPurple,
                            trackColor = Color(0xFF1E1E2F),
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )
                        Text(
                            text = generationProgressText,
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {}
            )
        }

        // Section Image Suggestion modal
        showImageSelectorForSection?.let { section ->
            AlertDialog(
                onDismissRequest = { showImageSelectorForSection = null },
                containerColor = Color(0xFF0D0D19),
                title = {
                    Text(
                        "AI Suggested Graphics Node Selector",
                        color = NeonCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "We selected these dynamic graphic illustration recommendations matching index parameters for: ${section.type}",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(mockImagePresets) { preset ->
                                Card(
                                    onClick = {
                                        section.imageLabel = preset
                                        showImageSelectorForSection = null
                                        Toast.makeText(context, "Layout graphic optimized!", Toast.LENGTH_SHORT).show()
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (section.imageLabel == preset) NeonCyan.copy(alpha = 0.5f) else Color.Transparent
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (section.imageLabel == preset) Color(0xFF1C1B33) else Color(0xFF070710)
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(preset, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        if (section.imageLabel == preset) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Active",
                                                tint = NeonCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showImageSelectorForSection = null }) {
                        Text("Dismiss", color = Color.Gray)
                    }
                }
            )
        }

        // One Click Publish Progress Dialog
        if (isPublishingToServer) {
            AlertDialog(
                onDismissRequest = {},
                containerColor = Color(0xFF070712),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.CloudUpload, contentDescription = null, tint = LuxuryEmerald)
                        Text("One Click Cloud Publisher Running...", color = LuxuryEmerald, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Connecting secure deployment server networks to deliver real-time operational visual frames securely.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        LinearProgressIndicator(
                            progress = publishProgressVal,
                            color = LuxuryEmerald,
                            trackColor = Color(0xFF1E1E2F),
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )
                        Text(publishMessageStatus, color = Color.LightGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                },
                confirmButton = {}
            )
        }

        // Workspace main card Container
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF060610)),
            border = BorderStroke(1.dp, Color(0x33A78BFA)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                
                // Top header controls with Live publish status indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (activeProject.isPublished) LuxuryEmerald else NeonPink)
                        )
                        Text(
                            text = if (activeProject.isPublished) "ONLINE DEPLOYMENT COLD ACTIVE" else "LOCAL SANDBOX DONT PROPAGATE",
                            color = if (activeProject.isPublished) LuxuryEmerald else Color.Gray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(GradientPurple, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "PREMIUM TIER STUDIO",
                            color = PureBlack,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Prompt description block - Instant creation portals
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F1E)),
                    border = BorderStroke(0.5.dp, Color.DarkGray.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "⚡ Dora AI Prompt-to-Website Compiler Engine",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = inputPromptText,
                                onValueChange = { inputPromptText = it },
                                placeholder = { Text("e.g. Organic dry fruit shop in Delhi NCR", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonPurple,
                                    unfocusedBorderColor = Color.DarkGray
                                ),
                                textStyle = TextStyle(fontSize = 12.sp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.5f).height(44.dp)
                            )

                            Button(
                                onClick = { processPromptToWebsiteBuild() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Text("AI BUILD ✨", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        // Dropdowns selectors for complete parameter bounds
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Dropdown 1: Website Type
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Website Type Selection:", color = Color.Gray, fontSize = 8.sp)
                                val webTypesList = listOf("SaaS Website", "Business Website", "Portfolio Website", "AI Tool Website", "Agency Website", "Startup Website", "Blog Website", "E-commerce Website", "Landing Page")
                                var expandedTypeMenu by remember { mutableStateOf(false) }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF070710), RoundedCornerShape(6.dp))
                                        .border(0.5.dp, Color.DarkGray, RoundedCornerShape(6.dp))
                                        .clickable { expandedTypeMenu = true }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Text(selectedWebType, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                    }
                                    DropdownMenu(
                                        expanded = expandedTypeMenu,
                                        onDismissRequest = { expandedTypeMenu = false },
                                        modifier = Modifier.background(Color(0xFF151528))
                                    ) {
                                        webTypesList.forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type, color = Color.White, fontSize = 11.sp) },
                                                onClick = {
                                                    selectedWebType = type
                                                    expandedTypeMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Dropdown 2: Style Selector
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Design & Theme Preset:", color = Color.Gray, fontSize = 8.sp)
                                val stylesList = listOf("Futuristic", "Minimal", "Premium", "Glassmorphism", "Neumorphism", "Dark Theme", "Light Theme")
                                var expandedStyleMenu by remember { mutableStateOf(false) }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF070710), RoundedCornerShape(6.dp))
                                        .border(0.5.dp, Color.DarkGray, RoundedCornerShape(6.dp))
                                        .clickable { expandedStyleMenu = true }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Text(selectedStylePreset, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                    }
                                    DropdownMenu(
                                        expanded = expandedStyleMenu,
                                        onDismissRequest = { expandedStyleMenu = false },
                                        modifier = Modifier.background(Color(0xFF151528))
                                    ) {
                                        stylesList.forEach { design ->
                                            DropdownMenuItem(
                                                text = { Text(design, color = Color.White, fontSize = 11.sp) },
                                                onClick = {
                                                    selectedStylePreset = design
                                                    expandedStyleMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom control tab bars for visual editing 
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0F24), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val tabs = listOf(
                        Triple(0, Icons.Filled.FolderZip, "MY PROJECTS"),
                        Triple(1, Icons.Filled.Layers, "VISUAL EDITOR"),
                        Triple(2, Icons.Filled.TravelExplore, "SEO-SSL ROUTING"),
                        Triple(3, Icons.Filled.Code, "EXPORT CODE"),
                        Triple(4, Icons.Filled.Assessment, "ANALYTICS STATS")
                    )

                    tabs.forEach { (idx, icon, title) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (activeWorkspaceTab == idx) Color(0xFF1E1E3F) else Color.Transparent)
                                .clickable { activeWorkspaceTab = idx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (activeWorkspaceTab == idx) NeonCyan else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = title,
                                    color = if (activeWorkspaceTab == idx) Color.White else Color.Gray,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // main Split Screen Workspace: Left is controls, Right is live simulation viewport
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // LEFT COLUMN CONTAINER - Dynamic workspace contents
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(480.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (activeWorkspaceTab) {
                            0 -> {
                                // PROJECT PORTFOLIO MANAGER
                                Text("📁 DRAFT SANDBOX PROJECTS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)

                                myProjects.forEach { proj ->
                                    val isCurrent = proj.projectId == activeProject.projectId
                                    Card(
                                        onClick = { activeProject = proj },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isCurrent) Color(0xFF151428) else Color(0xFF080812)
                                        ),
                                        border = BorderStroke(
                                            0.5.dp,
                                            if (isCurrent) NeonCyan.copy(alpha = 0.6f) else Color.DarkGray.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(proj.projectName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            if (proj.isPublished) LuxuryEmerald.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.12f),
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        if (proj.isPublished) "LIVE" else "DRAFT",
                                                        color = if (proj.isPublished) LuxuryEmerald else Color.Red,
                                                        fontSize = 7.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Type: ${proj.type} | Style Preset: ${proj.designSelection}", color = Color.Gray, fontSize = 9.sp)
                                            if (proj.customDomain.isNotBlank()) {
                                                Text("Domain binding: ${proj.customDomain}", color = NeonCyan, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("💡 Select any project to load into active Design Editor.", color = Color.Gray, fontSize = 9.sp, fontStyle = FontStyle.Italic)
                            }

                            1 -> {
                                // VISUAL EDITOR, LAYOUTS, CUSTOM IMAGES, SECTIONS DRAG & MOVE
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🧱 SECTION WIREFRAME TREE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)

                                    // Add Section Buttons Trigger
                                    Box {
                                        Button(
                                            onClick = { expandedAddSectionMenu = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E32)),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(10.dp))
                                                Text("ADD SECTION", color = NeonCyan, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = expandedAddSectionMenu,
                                            onDismissRequest = { expandedAddSectionMenu = false },
                                            modifier = Modifier.background(Color(0xFF121226))
                                        ) {
                                            sectionTypesToAdd.forEach { type ->
                                                DropdownMenuItem(
                                                    text = { Text("Add ${type.uppercase()} Section", color = Color.White, fontSize = 10.sp) },
                                                    onClick = {
                                                        val labelImage = when (type) {
                                                            "services" -> "🛸 Cybernetic Telemetry Model"
                                                            "pricing" -> "📈 Interactive Analytics Core"
                                                            "team" -> "👤 Premium High-Quality Avatars"
                                                            else -> "⚡ Hyper-Scale Vector Illustration"
                                                        }
                                                        val newSec = EditableSection(
                                                            id = "sec_${System.currentTimeMillis()}",
                                                            type = type,
                                                            initialTitle = "Newly Inserted ${type.replaceFirstChar { it.uppercase() }}",
                                                            initialSubtitle = "Double-click to customize properties instantly",
                                                            initialImageLabel = labelImage
                                                        )
                                                        activeProject.sections.add(activeProject.sections.size - 1, newSec)
                                                        expandedAddSectionMenu = false
                                                        Toast.makeText(context, "Added new section structure!", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Interactive Section blocks with order managers (Up/Down) & remove triggers
                                activeProject.sections.forEachIndexed { idx, section ->
                                    var isCollapsed by remember { mutableStateOf(idx != 0 && idx != SectionCounterState.count) }
                                    
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A13)),
                                        border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.25f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    IconButton(
                                                        onClick = { isCollapsed = !isCollapsed },
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isCollapsed) Icons.Filled.ChevronRight else Icons.Filled.ExpandMore,
                                                            contentDescription = null,
                                                            tint = NeonCyan,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }

                                                    Icon(
                                                        imageVector = when (section.type) {
                                                            "hero" -> Icons.Filled.Stars
                                                            "about" -> Icons.Filled.Info
                                                            "services" -> Icons.Filled.SettingsSuggest
                                                            "features" -> Icons.Filled.Category
                                                            "pricing" -> Icons.Filled.Payments
                                                            "team" -> Icons.Filled.People
                                                            "faq" -> Icons.Filled.HelpCenter
                                                            "contact" -> Icons.Filled.Mail
                                                            else -> Icons.Filled.SmartButton
                                                        },
                                                        contentDescription = null,
                                                        tint = NeonPurple,
                                                        modifier = Modifier.size(12.dp)
                                                    )

                                                    Text(
                                                        text = section.type.uppercase(),
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }

                                                // Section actions: Visibility toggle, move UP, move DOWN, Delete 
                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    // Toggle
                                                    IconButton(
                                                        onClick = { section.isEnabled = !section.isEnabled },
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (section.isEnabled) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                            contentDescription = null,
                                                            tint = if (section.isEnabled) NeonGreen else Color.Gray,
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                    }

                                                    // Up
                                                    IconButton(
                                                        onClick = { moveSectionUp(idx) },
                                                        enabled = idx > 0,
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = null, tint = if (idx > 0) Color.LightGray else Color.DarkGray, modifier = Modifier.size(11.dp))
                                                    }

                                                    // Down
                                                    IconButton(
                                                        onClick = { moveSectionDown(idx) },
                                                        enabled = idx < activeProject.sections.size - 1,
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = null, tint = if (idx < activeProject.sections.size - 1) Color.LightGray else Color.DarkGray, modifier = Modifier.size(11.dp))
                                                    }

                                                    // Trash Out
                                                    if (section.type != "hero" && section.type != "footer") {
                                                        IconButton(
                                                            onClick = {
                                                                activeProject.sections.removeAt(idx)
                                                                Toast.makeText(context, "Removed section!", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(20.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(11.dp))
                                                        }
                                                    }
                                                }
                                            }

                                            // Expanded interactive edit text elements
                                            if (!isCollapsed) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Divider(color = Color.DarkGray.copy(alpha = 0.2f))
                                                Spacer(modifier = Modifier.height(6.dp))

                                                OutlinedTextField(
                                                    value = section.title,
                                                    onValueChange = { section.title = it },
                                                    label = { Text("Section Headline", fontSize = 8.sp, color = Color.Gray) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedBorderColor = NeonCyan,
                                                        unfocusedBorderColor = Color.DarkGray
                                                    ),
                                                    textStyle = TextStyle(fontSize = 11.sp),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                                )

                                                if (section.type == "hero" || section.type == "about" || section.type == "pricing" || section.type == "contact") {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    OutlinedTextField(
                                                        value = section.subtitle,
                                                        onValueChange = { section.subtitle = it },
                                                        label = { Text("Secondary Text / Pitch Description", fontSize = 8.sp, color = Color.Gray) },
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = Color.White,
                                                            unfocusedTextColor = Color.White,
                                                            focusedBorderColor = NeonCyan,
                                                            unfocusedBorderColor = Color.DarkGray
                                                        ),
                                                        textStyle = TextStyle(fontSize = 11.sp),
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                                    )
                                                }

                                                // Dynamic Illustrative suggestions bindings 
                                                if (section.type == "hero" || section.type == "about" || section.type == "services") {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("Suggested AI Image Layout:", color = Color.Gray, fontSize = 8.sp)
                                                        Button(
                                                            onClick = { showImageSelectorForSection = section },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E2F)),
                                                            shape = RoundedCornerShape(4.dp),
                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(22.dp)
                                                        ) {
                                                            Text(section.imageLabel, color = NeonCyan, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }

                                                // Item lists management (Pricing, Service cards etc)
                                                if (section.items.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text("Modular Row Bullet Values:", color = Color.Gray, fontSize = 8.sp)
                                                    section.items.forEachIndexed { itemIdx, itemVal ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            OutlinedTextField(
                                                                value = itemVal,
                                                                onValueChange = { section.items[itemIdx] = it },
                                                                colors = OutlinedTextFieldDefaults.colors(
                                                                    focusedTextColor = Color.White,
                                                                    unfocusedTextColor = Color.White,
                                                                    focusedBorderColor = NeonPurple,
                                                                    unfocusedBorderColor = Color.DarkGray
                                                                ),
                                                                textStyle = TextStyle(fontSize = 10.sp),
                                                                shape = RoundedCornerShape(4.dp),
                                                                modifier = Modifier.weight(1f).height(38.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Interactive custom color scheme picker
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("🎨 FINE-TUNE BRAND COLORS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val paletteColors = listOf(
                                        Triple("Primary", activeProject.primaryColor) { c: Color -> activeProject.primaryColor = c },
                                        Triple("Secondary", activeProject.secondaryColor) { c: Color -> activeProject.secondaryColor = c },
                                        Triple("Accent", activeProject.accentColor) { c: Color -> activeProject.accentColor = c },
                                        Triple("Background", activeProject.backgroundColor) { c: Color -> activeProject.backgroundColor = c }
                                    )

                                    paletteColors.forEach { (label, color, setter) ->
                                        Card(
                                            border = BorderStroke(0.5.dp, Color.Gray),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F1A)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                        .clickable {
                                                            val colorLoopList = listOf(NeonPurple, NeonPink, NeonCyan, Color(0xFF10B981), Color(0xFFFBBF24), Color.White, Color(0xFF0F172A))
                                                            val nextPos = (colorLoopList.indexOf(color) + 1) % colorLoopList.size
                                                            setter(colorLoopList[nextPos])
                                                            Toast.makeText(context, "$label adjusted!", Toast.LENGTH_SHORT).show()
                                                        }
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(label, color = Color.White, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // SEO OPTIMIZATION & SSL DOMAIN PORTALS
                                Text("🔍 SEARCH RESULTS SEO ARCHITECTURE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)

                                OutlinedTextField(
                                    value = activeProject.seoTitle,
                                    onValueChange = { activeProject.seoTitle = it },
                                    label = { Text("SEO Target Title Tag", fontSize = 9.sp, color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.DarkGray
                                    ),
                                    textStyle = TextStyle(fontSize = 11.sp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                )

                                OutlinedTextField(
                                    value = activeProject.metaDesc,
                                    onValueChange = { activeProject.metaDesc = it },
                                    label = { Text("Meta Description (Max search excerpt snippet)", fontSize = 9.sp, color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.DarkGray
                                    ),
                                    textStyle = TextStyle(fontSize = 11.sp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(54.dp)
                                )

                                OutlinedTextField(
                                    value = activeProject.keywords,
                                    onValueChange = { activeProject.keywords = it },
                                    label = { Text("Search Keywords (Comma separated)", fontSize = 9.sp, color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.DarkGray
                                    ),
                                    textStyle = TextStyle(fontSize = 11.sp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                )

                                // SOCIAL BIND OG CARD MODEL (LATEST REVEALS)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("🌐 LINKEDIN / CHAT PREVIEW (OPEN GRAPH)", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF060D1E)),
                                    border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("aistudio.build/apps/${activeProject.mainTitle.lowercase().filter { it.isLetter() }.take(10)}", color = Color(0xFF22C55E), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        
                                        // Rich Card Box with simulated Canvas background
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Brush.linearGradient(listOf(Color(0xFF1E1B4B), Color(0xFF311042))))
                                                .padding(8.dp)
                                        ) {
                                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                                Text(activeProject.seoTitle, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                                Text(activeProject.metaDesc, color = Color.LightGray, fontSize = 8.sp, maxLines = 2, lineHeight = 10.sp)
                                                Text("🔗 Secure SSL Live Sandbox preview", color = NeonCyan, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // DOMAIN MANAGEMENT & SSL PORTALS
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("🖇️ CUSTOM DOMAIN CONFIGURATION", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = activeProject.customDomain,
                                        onValueChange = { activeProject.customDomain = it },
                                        placeholder = { Text("e.g. customdomain.com", fontSize = 10.sp, color = Color.Gray) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = NeonGreen,
                                            unfocusedBorderColor = Color.DarkGray
                                        ),
                                        textStyle = TextStyle(fontSize = 11.sp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    )

                                    Button(
                                        onClick = {
                                            activeProject.sslEnabled = !activeProject.sslEnabled
                                            Toast.makeText(context, "SSL State Toggled!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (activeProject.sslEnabled) LuxuryEmerald else Color.DarkGray
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(44.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text(
                                            if (activeProject.sslEnabled) "🔐 Let's Encrypt SSL" else "🔓 SSL DISABLED",
                                            color = PureBlack,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                // ONE-CLICK INSTANT PUBLISH ENGINE
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { triggerServerDeploymentPipeline() },
                                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryEmerald),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Filled.CloudUpload, contentDescription = null, tint = PureBlack, modifier = Modifier.size(14.dp))
                                        Text("ONE-CLICK INSTANT PUBLISH PRODUCTION SITE 🚀", color = PureBlack, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            3 -> {
                                // CODE SCRIPT COMPILERS & TEXT EXPORTERS
                                Text("💻 RESPONSIVE TAILWIND SOURCE CODE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)

                                var selectedSnippetType by remember { mutableStateOf("HTML") } // "HTML", "CSS", "JS"

                                Row(
                                    modifier = Modifier.fillMaxWidth().background(Color(0xFF060614)).padding(2.dp)
                                ) {
                                    listOf("HTML", "CSS", "JS").forEach { item ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (selectedSnippetType == item) Color(0xFF1C1C30) else Color.Transparent)
                                                .clickable { selectedSnippetType = item }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(item, color = if (selectedSnippetType == item) NeonCyan else Color.Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                val codeDisplaySource = when (selectedSnippetType) {
                                    "HTML" -> generateResponsiveHtmlTailwind(activeProject)
                                    "CSS" -> generateRootCssProperties(activeProject)
                                    else -> "document.addEventListener('DOMContentLoaded', () => {\n  console.log('Successfully bounded interactive client states!');\n});"
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp)
                                        .background(Color.Black, RoundedCornerShape(8.dp))
                                        .border(0.5.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = codeDisplaySource,
                                        color = NeonGreen,
                                        fontSize = 8.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 11.sp
                                    )
                                }

                                // Buttons to Copy or Export ZIP directly
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(codeDisplaySource))
                                            Toast.makeText(context, "$selectedSnippetType copied into native clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1F35)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                                            Text("COPY TO CLIPBOARD", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = { generateZipDownloadSim() },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(imageVector = Icons.Filled.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                            Text("DOWNLOAD ZIP ARCHIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            4 -> {
                                // METRICS PORTAL, TRAFFIC & COLLABORATION WORKFLOWS
                                Text("📊 WEB TRAFFIC ENGAGEMENT ANALYTICS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        Triple("Impressions", "4,820", Color.Green),
                                        Triple("Unique Visitors", "1,842", NeonCyan),
                                        Triple("Form Leads", "147", NeonPurple)
                                    ).forEach { (label, count, indicatorColor) ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0D1A)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(label, color = Color.Gray, fontSize = 8.sp, textAlign = TextAlign.Center)
                                                Text(count, color = indicatorColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                }

                                // Interactive Traffic Canvas Line Graph designed using precise guidelines
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Page Views Matrix (Last 7 Days):", color = Color.Gray, fontSize = 8.sp)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .background(Color.Black, RoundedCornerShape(8.dp))
                                        .border(0.5.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        // Draw horizontal grids
                                        val steps = 4
                                        val hStep = size.height / steps
                                        for (i in 0..steps) {
                                            val y = i * hStep
                                            drawLine(
                                                color = Color.DarkGray.copy(alpha = 0.3f),
                                                start = Offset(0f, y),
                                                end = Offset(size.width, y),
                                                strokeWidth = 1f
                                            )
                                        }

                                        // 7 coordinate metrics matching mock views
                                        val points = listOf(
                                            Offset(0f, size.height * 0.8f),
                                            Offset(size.width * 0.16f, size.height * 0.65f),
                                            Offset(size.width * 0.33f, size.height * 0.72f),
                                            Offset(size.width * 0.5f, size.height * 0.4f),
                                            Offset(size.width * 0.66f, size.height * 0.25f),
                                            Offset(size.width * 0.83f, size.height * 0.5f),
                                            Offset(size.width, size.height * 0.15f)
                                        )

                                        // Draw smooth Path
                                        val path = Path().apply {
                                            moveTo(points.first().x, points.first().y)
                                            for (i in 1 until points.size) {
                                                lineTo(points[i].x, points[i].y)
                                            }
                                        }

                                        drawPath(
                                            path = path,
                                            color = NeonPurple,
                                            style = Stroke(width = 3f)
                                        )

                                        // Draw nodes
                                        points.forEach { point ->
                                            drawCircle(
                                                color = NeonCyan,
                                                radius = 5f,
                                                center = point
                                            )
                                        }
                                    }
                                }

                                // TEAM COLLABORATORS MODULE (SSL SECURED)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("🤝 TEAM CLIENT COLLABORATORS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F1A)),
                                    border = BorderStroke(0.5.dp, Color.DarkGray),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(
                                            "Chanki Kawde (Creator/Owner)",
                                            "AI Studio Assistant Bot (Collaborator)"
                                        ).forEach { user ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NeonCyan))
                                                Text(user, color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // RIGHT COLUMN CONTAINER - Real scale mobile responsive Simulator Frame
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .height(480.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Responsive mode control triggers 
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF0C0C14), RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Triple("mobile", Icons.Filled.Smartphone, "Mobile Layout"),
                                Triple("tablet", Icons.Filled.TabletAndroid, "Tablet Layout"),
                                Triple("desktop", Icons.Filled.Computer, "Desktop Layout")
                            ).forEach { (mode, icon, accessibilityDesc) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (responsiveMode == mode) Color(0xFF2E2E4B) else Color.Transparent)
                                        .clickable { responsiveMode = mode }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = accessibilityDesc,
                                        tint = if (responsiveMode == mode) NeonCyan else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Framing Device Shell following the precise styling
                        val targetFrameWidth = when (responsiveMode) {
                            "mobile" -> 160.dp
                            "tablet" -> 220.dp
                            else -> 400.dp
                        }

                        Column(
                            modifier = Modifier
                                .width(targetFrameWidth)
                                .weight(1f)
                                .background(activeProject.backgroundColor, RoundedCornerShape(12.dp))
                                .border(
                                    2.dp,
                                    Brush.linearGradient(listOf(activeProject.primaryColor, activeProject.secondaryColor)),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(8.dp)
                        ) {
                            // Device header mock line
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.Red))
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.Yellow))
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.Green))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "site-compiler.aistudio",
                                        color = Color.LightGray.copy(alpha = 0.5f),
                                        fontSize = 5.5.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                if (activeProject.sslEnabled) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "SSL Secure link",
                                        tint = LuxuryEmerald,
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                            }

                            Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)

                            // Responsive scrollable simulated website viewport representation
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("interactive_live_iframe_scroller"),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Dynamically draw sections from class lists in memory
                                activeProject.sections.forEach { section ->
                                    if (section.isEnabled) {
                                        item {
                                            when (section.type) {
                                                "hero" -> {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 12.dp, horizontal = 4.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .background(activeProject.primaryColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = section.title.uppercase(),
                                                                color = activeProject.primaryColor,
                                                                fontSize = 6.5.sp,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Text(
                                                            text = activeProject.heroHeading,
                                                            color = Color.White,
                                                            fontSize = if (responsiveMode == "mobile") 11.sp else 14.sp,
                                                            fontWeight = FontWeight.Black,
                                                            textAlign = TextAlign.Center,
                                                            lineHeight = if (responsiveMode == "mobile") 13.sp else 16.sp
                                                        )

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Text(
                                                            text = activeProject.heroSubheading,
                                                            color = Color.LightGray,
                                                            fontSize = 8.sp,
                                                            textAlign = TextAlign.Center,
                                                            lineHeight = 11.sp
                                                        )

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        Button(
                                                            onClick = { Toast.makeText(context, "Call to action triggered in simulator!", Toast.LENGTH_SHORT).show() },
                                                            colors = ButtonDefaults.buttonColors(containerColor = activeProject.primaryColor),
                                                            shape = RoundedCornerShape(4.dp),
                                                            modifier = Modifier.height(24.dp),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(activeProject.callToAction, color = PureBlack, fontWeight = FontWeight.Bold, fontSize = 7.5.sp)
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        // Draw simulated vector art or illustration matching AI image selection
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(70.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Brush.linearGradient(listOf(activeProject.primaryColor.copy(alpha = 0.2f), activeProject.secondaryColor.copy(alpha = 0.2f))))
                                                                .border(0.5.dp, activeProject.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                Text("🖼️", fontSize = 16.sp)
                                                                Text(section.imageLabel, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }

                                                "about" -> {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
                                                            .padding(8.dp)
                                                    ) {
                                                        Text(section.title, color = activeProject.primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(section.subtitle, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(section.content, color = Color.Gray, fontSize = 8.sp, lineHeight = 11.sp)
                                                    }
                                                }

                                                "services" -> {
                                                    Column(modifier = Modifier.fillMaxWidth()) {
                                                        Text(section.title, color = activeProject.primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(section.subtitle, color = Color.White, fontSize = 8.sp)
                                                        Spacer(modifier = Modifier.height(6.dp))

                                                        // Service list layouts
                                                        section.items.forEach { feat ->
                                                            Card(
                                                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                                                                border = BorderStroke(0.3.dp, Color.Gray.copy(alpha = 0.2f)),
                                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(6.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = activeProject.accentColor, modifier = Modifier.size(8.dp))
                                                                    Text(feat, color = Color.White, fontSize = 7.5.sp)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                "features" -> {
                                                    Column(modifier = Modifier.fillMaxWidth()) {
                                                        Text(section.title, color = activeProject.primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        if (responsiveMode == "desktop") {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                section.items.take(3).forEach { core ->
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(4.dp))
                                                                            .border(0.3.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                                            .padding(4.dp)
                                                                    ) {
                                                                        Column {
                                                                            Icon(imageVector = Icons.Filled.Stars, contentDescription = null, tint = activeProject.accentColor, modifier = Modifier.size(8.dp))
                                                                            Spacer(modifier = Modifier.height(2.dp))
                                                                            Text(core, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold, lineHeight = 9.sp)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                                section.items.forEach { core ->
                                                                    Row(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(4.dp))
                                                                            .padding(6.dp),
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                    ) {
                                                                        Icon(imageVector = Icons.Filled.Stars, contentDescription = null, tint = activeProject.primaryColor, modifier = Modifier.size(8.dp))
                                                                        Text(core, color = Color.White, fontSize = 8.sp)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                "pricing" -> {
                                                    Column(modifier = Modifier.fillMaxWidth()) {
                                                        Text(section.title, color = activeProject.primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        section.items.forEach { plan ->
                                                            val isPremium = plan.contains("Premium") || plan.contains("Core") || plan.contains("Enterprise")
                                                            Card(
                                                                colors = CardDefaults.cardColors(
                                                                    containerColor = if (isPremium) activeProject.primaryColor.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f)
                                                                ),
                                                                border = BorderStroke(
                                                                    if (isPremium) 1.dp else 0.3.dp,
                                                                    if (isPremium) activeProject.primaryColor.copy(alpha = 0.5f) else Color.DarkGray
                                                                ),
                                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                                            ) {
                                                                Column(modifier = Modifier.padding(6.dp)) {
                                                                    Text(plan, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                                    Spacer(modifier = Modifier.height(2.dp))
                                                                    Text("Includes unlimited updates and responsive hosting layouts matching index.", color = Color.Gray, fontSize = 6.sp)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                "faq" -> {
                                                    Column(modifier = Modifier.fillMaxWidth()) {
                                                        Text(section.title, color = activeProject.primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        section.items.forEach { faq ->
                                                            val q = faq.substringBefore("\nA: ")
                                                            val a = faq.substringAfter("\nA: ")
                                                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                                                Text(q, color = Color.White, fontSize = 7.5.sp, fontWeight = FontWeight.Black)
                                                                Text(a, color = Color.LightGray, fontSize = 7.sp)
                                                            }
                                                        }
                                                    }
                                                }

                                                "contact" -> {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                                                            .padding(8.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(section.title, color = activeProject.primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(section.subtitle, color = Color.Gray, fontSize = 7.sp, textAlign = TextAlign.Center)

                                                        Spacer(modifier = Modifier.height(6.dp))

                                                        if (!simSubmittedSuccess) {
                                                            OutlinedTextField(
                                                                value = simNameInput,
                                                                onValueChange = { simNameInput = it },
                                                                placeholder = { Text("Your Name", fontSize = 7.sp, color = Color.Gray) },
                                                                colors = OutlinedTextFieldDefaults.colors(
                                                                    unfocusedBorderColor = Color.DarkGray,
                                                                    focusedBorderColor = activeProject.primaryColor
                                                                ),
                                                                textStyle = TextStyle(fontSize = 7.5.sp),
                                                                modifier = Modifier.fillMaxWidth().height(26.dp)
                                                            )
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            OutlinedTextField(
                                                                value = simEmailInput,
                                                                onValueChange = { simEmailInput = it },
                                                                placeholder = { Text("Email Contact", fontSize = 7.sp, color = Color.Gray) },
                                                                colors = OutlinedTextFieldDefaults.colors(
                                                                    unfocusedBorderColor = Color.DarkGray,
                                                                    focusedBorderColor = activeProject.primaryColor
                                                                ),
                                                                textStyle = TextStyle(fontSize = 7.5.sp),
                                                                modifier = Modifier.fillMaxWidth().height(26.dp)
                                                            )
                                                            Spacer(modifier = Modifier.height(6.dp))

                                                            Button(
                                                                onClick = {
                                                                    if (simEmailInput.isNotBlank()) {
                                                                        simSubmittedSuccess = true
                                                                        Toast.makeText(context, "Simulator form saved!", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = activeProject.primaryColor),
                                                                shape = RoundedCornerShape(4.dp),
                                                                modifier = Modifier.fillMaxWidth().height(18.dp),
                                                                contentPadding = PaddingValues(0.dp)
                                                            ) {
                                                                Text("Submit Inquiry 💬", color = PureBlack, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        } else {
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                                                                Icon(imageVector = Icons.Filled.CloudDone, contentDescription = null, tint = LuxuryEmerald, modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.height(2.dp))
                                                                Text("Inquiry Saved Locally", color = LuxuryEmerald, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }

                                                "footer" -> {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 12.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Divider(color = Color.Gray.copy(alpha = 0.1f), thickness = 0.5.dp)
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(section.title, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                        Text(section.subtitle, color = Color.Gray, fontSize = 6.sp)
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text("© ${Calendar.getInstance().get(Calendar.YEAR)} secure sandbox portal", color = Color.DarkGray, fontSize = 5.5.sp)
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
        }
    }
}

// Global scope counter keeping track of visual tree layouts
object SectionCounterState {
    var count by mutableStateOf(2)
}

// Real output responsive source generator for direct netlify/vercel copy pasting 
private fun generateResponsiveHtmlTailwind(project: UserWebsite): String {
    val themePreset = project.designSelection
    val primeColorHex = "#" + Integer.toHexString(project.primaryColor.value.toInt()).takeLast(6).uppercase()
    val secColorHex = "#" + Integer.toHexString(project.secondaryColor.value.toInt()).takeLast(6).uppercase()
    val bgColorHex = "#" + Integer.toHexString(project.backgroundColor.value.toInt()).takeLast(6).uppercase()

    val cssGradients = when (themePreset) {
        "Futuristic" -> "linear-gradient(135deg, $bgColorHex 0%, #030419 100%)"
        "Premium" -> "linear-gradient(135deg, $bgColorHex 0%, #151105 100%)"
        "Glassmorphism" -> "linear-gradient(135deg, #090214 0%, #1A0B2C 100%)"
        else -> "linear-gradient(135deg, $bgColorHex 0%, #0C0D11 100%)"
    }

    return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${project.seoTitle}</title>
        <meta name="description" content="${project.metaDesc}">
        <meta name="keywords" content="${project.keywords}">
        <script src="https://cdn.tailwindcss.com"></script>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background: $cssGradients;
                color: #F3F4F6;
                font-family: 'Inter', system-ui, sans-serif;
            }
        </style>
    </head>
    <body class="min-h-screen">
        <nav class="max-w-6xl mx-auto px-6 py-4 flex justify-between items-center border-b border-slate-900">
            <div class="flex items-center gap-2">
                <span class="text-xl font-black text-transparent bg-clip-text bg-gradient-to-r from-[${primeColorHex}] to-[${secColorHex}]">${project.mainTitle}</span>
            </div>
            <a href="#contact" class="text-xs font-bold px-4 py-2 rounded-full border border-slate-700 hover:border-slate-500 transition">Sandbox Beta</a>
        </nav>
        
        <!-- Modules Wireframes rendering -->
        ${project.sections.joinToString("\n") { sec ->
            if (sec.isEnabled) {
                when (sec.type) {
                    "hero" -> """
                    <header class="max-w-4xl mx-auto px-6 py-24 text-center">
                        <span class="inline-block px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider mb-6 bg-purple-950 text-purple-400 border border-purple-800">Operational Active Hub</span>
                        <h1 class="text-4xl md:text-6xl font-black tracking-tight mb-6 text-white">${project.heroHeading}</h1>
                        <p class="text-slate-400 text-lg md:text-xl max-w-2xl mx-auto font-light leading-relaxed mb-8">${project.heroSubheading}</p>
                        <div class="flex justify-center gap-4">
                            <a href="#contact" class="px-8 py-4 rounded-xl font-bold text-slate-950 transition hover:scale-105 active:scale-95" style="background-color: ${primeColorHex}">${project.callToAction}</a>
                        </div>
                    </header>
                    """
                    "about" -> """
                    <section class="max-w-4xl mx-auto px-6 py-16">
                        <h3 class="text-xs font-bold text-[${primeColorHex}] uppercase tracking-widest mb-2">${sec.title}</h3>
                        <h2 class="text-2xl md:text-3xl font-black text-white mb-4">${sec.subtitle}</h2>
                        <p class="text-slate-400 text-sm md:text-base leading-relaxed max-w-2xl">${sec.content}</p>
                    </section>
                    """
                    "services" -> """
                    <section class="max-w-4xl mx-auto px-6 py-16">
                        <h3 class="text-xs font-bold text-[${primeColorHex}] uppercase tracking-widest mb-8">${sec.title}</h3>
                        <div class="grid md:grid-cols-3 gap-6">
                            ${sec.items.joinToString("\n") { item -> """
                            <div class="p-6 bg-slate-900 bg-opacity-40 rounded-2xl border border-slate-800">
                                <span class="text-2xl mb-4 block"><i class="fas fa-check-circle text-[${secColorHex}]"></i></span>
                                <p class="text-white text-sm font-semibold">$item</p>
                            </div>
                            """ }}
                        </div>
                    </section>
                    """
                    "features" -> """
                    <section class="max-w-4xl mx-auto px-6 py-16 text-center">
                        <h3 class="text-xs font-bold text-[${primeColorHex}] uppercase tracking-widest mb-12">${sec.title}</h3>
                        <div class="grid md:grid-cols-3 gap-8">
                            ${sec.items.joinToString("\n") { core -> """
                            <div class="p-6 bg-slate-900 bg-opacity-30 rounded-xl border border-slate-800 hover:border-slate-700 transition">
                                <div class="w-10 h-10 rounded-full flex items-center justify-center bg-slate-800 text-white mb-4 mx-auto"><i class="fas fa-star text-[${primeColorHex}]"></i></div>
                                <h4 class="text-white text-sm font-bold mb-2">$core</h4>
                                <p class="text-slate-500 text-xs">High throughput cloud capability active on live servers.</p>
                            </div>
                            """ }}
                        </div>
                    </section>
                    """
                    "pricing" -> """
                    <section class="max-w-4xl mx-auto px-6 py-16">
                        <h3 class="text-xs font-bold text-[${primeColorHex}] uppercase tracking-widest mb-8 text-center">${sec.title}</h3>
                        <div class="grid md:grid-cols-3 gap-6">
                            ${sec.items.joinToString("\n") { plan -> """
                            <div class="p-6 bg-slate-900 bg-opacity-40 rounded-2xl border border-slate-800 text-center">
                                <h4 class="text-white text-base font-bold mb-4">$plan</h4>
                                <p class="text-slate-400 text-xs mb-6">Fully flexible responsive plan structured for you.</p>
                                <button class="w-full py-2 bg-slate-800 hover:bg-slate-700 rounded-xl font-semibold text-xs">Get Pass</button>
                            </div>
                            """ }}
                        </div>
                    </section>
                    """
                    "faq" -> """
                    <section class="max-w-4xl mx-auto px-6 py-16">
                        <h3 class="text-xs font-bold text-[${primeColorHex}] uppercase tracking-widest mb-8">${sec.title}</h3>
                        <div class="space-y-4">
                            ${sec.items.joinToString("\n") { faq -> """
                            <div class="p-5 bg-slate-900 bg-opacity-20 rounded-xl border border-slate-800">
                                <p class="font-bold text-white text-sm">${faq.substringBefore("\nA: ")}</p>
                                <p class="text-slate-400 text-xs mt-2">${faq.substringAfter("\nA: ")}</p>
                            </div>
                            """ }}
                        </div>
                    </section>
                    """
                    "contact" -> """
                    <section id="contact" class="max-w-xl mx-auto px-6 py-20 text-center bg-slate-950 rounded-3xl border border-slate-900">
                        <h3 class="text-2xl font-black text-white mb-2">${sec.title}</h3>
                        <p class="text-slate-500 text-sm mb-6">${sec.subtitle}</p>
                        <form onsubmit="event.preventDefault(); alert('Beta request registered!');" class="space-y-4">
                            <input type="text" placeholder="Your Name" required class="w-full px-4 py-3 bg-slate-900 border border-slate-800 rounded-xl outline-none focus:border-[${primeColorHex}] text-white text-sm" />
                            <input type="email" placeholder="contact@company.com" required class="w-full px-4 py-3 bg-slate-900 border border-slate-800 rounded-xl outline-none focus:border-[${primeColorHex}] text-white text-sm" />
                            <button type="submit" class="w-full py-3 rounded-xl font-bold text-slate-950 transition-transform active:scale-95" style="background-color: ${primeColorHex}">Submit Request</button>
                        </form>
                    </section>
                    """
                    "footer" -> """
                    <footer class="border-t border-slate-950 bg-black bg-opacity-40 py-12 text-center text-xs text-slate-600">
                        <p class="font-bold text-slate-400 mb-2">${sec.title}</p>
                        <p class="mb-4">${sec.subtitle}</p>
                        <p>© ${Calendar.getInstance().get(Calendar.YEAR)} secure developer sandbox. All rights reserved.</p>
                    </footer>
                    """
                    else -> ""
                }
            } else ""
        }}
    </body>
    </html>
    """.trimIndent()
}

// Generate theme layout CSS bindings
private fun generateRootCssProperties(project: UserWebsite): String {
    val primeColorHex = "#" + Integer.toHexString(project.primaryColor.value.toInt()).takeLast(6).uppercase()
    val secColorHex = "#" + Integer.toHexString(project.secondaryColor.value.toInt()).takeLast(6).uppercase()
    val bgColorHex = "#" + Integer.toHexString(project.backgroundColor.value.toInt()).takeLast(6).uppercase()

    return """
    :root {
        --color-primary: $primeColorHex;
        --color-secondary: $secColorHex;
        --color-accent: #${Integer.toHexString(project.accentColor.value.toInt()).takeLast(6).uppercase()};
        --color-bg: $bgColorHex;
        --font-display: 'Inter', sans-serif;
    }
    
    body {
        margin: 0;
        padding: 0;
        background-color: var(--color-bg);
        font-family: var(--font-display);
        color: #F3F4F6;
        scroll-behavior: smooth;
    }
    
    /* Custom Neon Scrollbars */
    ::-webkit-scrollbar {
        width: 8px;
    }
    ::-webkit-scrollbar-track {
        background: #03030F;
    }
    ::-webkit-scrollbar-thumb {
        background-color: var(--color-primary);
        border-radius: 4px;
        border: 2px solid #03030F;
    }
    """.trimIndent()
}
