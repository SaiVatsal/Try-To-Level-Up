package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun CardColors(
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    disabledContainerColor: Color = Color.Unspecified,
    disabledContentColor: Color = Color.Unspecified
): CardColors {
    return CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object HUD : Screen("hud", "HUD", Icons.Filled.Dashboard)
    object Quests : Screen("quests", "Quests", Icons.Filled.Assignment)
    object Workout : Screen("workout", "Workout", Icons.Filled.FitnessCenter)
    object Nutrition : Screen("nutrition", "Nurture", Icons.Filled.Restaurant)
    object Focus : Screen("focus", "Focus", Icons.Filled.Timer)
    object Progress : Screen("progress", "Monarch", Icons.Filled.EmojiEvents)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AriseApp() {
    val navController = rememberNavController()
    val viewModel: AriseViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val levelUpActive by viewModel.isLevelUpScreenActive.collectAsState()
    val sysMessage by viewModel.questCompleteMessage.collectAsState()
    val stats by viewModel.playerStats.collectAsState()

    val activeDoomscrollApp by viewModel.doomscrollActiveApp.collectAsState()
    val doomscrollCountdown by viewModel.doomscrollProgressSeconds.collectAsState()

    val authState by com.example.network.FirebaseManager.authState.collectAsState()
    val isSystemMutedWithBugs by viewModel.isSystemMutedWithBugs.collectAsState()

    if (authState !is com.example.network.UserSessionState.LoggedIn) {
        AuthScreen(viewModel = viewModel)
    } else if (isSystemMutedWithBugs) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0407))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, SystemRed, RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Threat Detected",
                    tint = SystemRed,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "🚨 CORRUPTED SECTOR BLOCKED",
                    color = SystemRed,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "VULNERABILITY ID: CHANGER_SHIELD_SHADOW_SYS_403",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "A potential security exception or software bug has been detected inside the active subsystem compiled on May 26, 2026.",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "🛡️ SHIELD MITIGATION DEPLOYED:\n\n" +
                            "❌ Standard System UI: SUSPENDED\n" +
                            "❌ Camera Hardware Scanners: LOCKED DOWN\n" +
                            "❌ Voice Microphone Channels: BLOCKED\n" +
                            "❌ External File Sync: SAFE-ISOLATED",
                    color = EpicGold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Start,
                    lineHeight = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E0A0F))
                        .padding(14.dp)
                        .border(1.dp, SystemRed.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.triggerSystemBugCrashIsolation(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = SystemRed),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = "🛠️ OVERRIDE / EXCEPTION RE-SOLVE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    } else {
        Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = PitchBlack,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(1.dp, Brush.horizontalGradient(listOf(ElectricPurple, ElectricBlue)), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .navigationBarsPadding()
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    Screen.HUD,
                    Screen.Quests,
                    Screen.Workout,
                    Screen.Nutrition,
                    Screen.Focus,
                    Screen.Progress
                )

                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) ElectricBlue else Color.Gray.copy(alpha = 0.8f)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                color = if (isSelected) ElectricBlue else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = ElectricPurple.copy(alpha = 0.25f)
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PitchBlack)
                .padding(innerPadding)
        ) {
            NavHost(navController = navController, startDestination = Screen.HUD.route) {
                composable(Screen.HUD.route) { HudScreen(viewModel, navController) }
                composable(Screen.Quests.route) { QuestsScreen(viewModel) }
                composable(Screen.Workout.route) { WorkoutScreen(viewModel) }
                composable(Screen.Nutrition.route) { NutritionScreen(viewModel) }
                composable(Screen.Focus.route) { FocusScreen(viewModel) }
                composable(Screen.Progress.route) { ProgressScreen(viewModel) }
            }

            // --- System Alert Messages ---
            sysMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepDarkGlass),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .padding(horizontal = 24.dp)
                        .border(1.dp, ElectricBlue, RoundedCornerShape(8.dp))
                        .shadow(12.dp, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "System Note",
                            tint = ElectricBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // --- LEVEL UP SPELLS SCREEN ANIMATION ---
            AnimatedVisibility(
                visible = levelUpActive,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f))
                        .clickable { viewModel.isLevelUpScreenActive.value = false },
                    contentAlignment = Alignment.Center
                ) {
                    LevelUpAnimationContent(stats) {
                        viewModel.isLevelUpScreenActive.value = false
                    }
                }
            }

            // --- Doomscroll simulation Overlay ---
            activeDoomscrollApp?.let { appName ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.98f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                        modifier = Modifier
                            .padding(24.dp)
                            .border(2.dp, SystemRed, RoundedCornerShape(16.dp))
                            .shadow(20.dp, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Sealed",
                                tint = SystemRed,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "🔒 SYSTEM: ALARM TRIGGERED",
                                color = SystemRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Hunter Sai, you are attempting to break the Focus Seal for $appName. Reels and Shorts are toxic to your Monarch Mind.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Sealed Delay Countdown: $doomscrollCountdown s",
                                color = ElectricBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.dismissBlockedAppPrompt(earnReward = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "CLOSE APP & PROTECT MIND (+10 INT XP)",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { viewModel.dismissBlockedAppPrompt(earnReward = false) },
                                enabled = doomscrollCountdown == 0
                            ) {
                                Text(
                                    text = if (doomscrollCountdown > 0) "Seal active... wait" else "BYPASS PROTOCOLS ANYWAY",
                                    color = if (doomscrollCountdown > 0) Color.Gray else SystemRed,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

// ==========================================
// ⚔️ HUD / DASHBOARD SCREEN
// ==========================================
@Composable
fun HudScreen(viewModel: AriseViewModel, navController: NavController) {
    val stats by viewModel.playerStats.collectAsState()
    val quests by viewModel.todayQuests.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Logo Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Try Leveling UP",
                        color = ElectricBlue,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.drawBehind {
                            // Subtle shadow behind name
                        }
                    )
                    Text(
                        text = "SOLO LEVELING HUNTER INTERFACE",
                        color = ElectricPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Fire Streak Display
                    val streakVal = stats?.streak ?: 1
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricPurple.copy(alpha = 0.2f))
                            .border(1.dp, ElectricPurple, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Active Streak",
                            tint = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${streakVal}D STREAK",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Logout Icon Button
                    IconButton(
                        onClick = { com.example.network.FirebaseManager.logout() },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SystemRed.copy(alpha = 0.15f))
                            .border(1.dp, SystemRed.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Disconnect Gates",
                            tint = SystemRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Feature: RPG Character Card (Aura glow based on rank)
        stats?.let { charStats ->
            item {
                Card(
                    colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = when (charStats.rank) {
                                    "SSS", "SS" -> listOf(EpicGold, ElectricPurple)
                                    "S", "A" -> listOf(ElectricBlue, ElectricPurple)
                                    else -> listOf(ElectricBlue, Color.Gray.copy(alpha = 0.5f))
                                }
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Custom vector aura art paths
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val path = Path().apply {
                                moveTo(0f, size.height)
                                lineTo(size.width * 0.4f, size.height * 0.8f)
                                lineTo(size.width, size.height)
                                close()
                            }
                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, ElectricPurple.copy(alpha = 0.15f))
                                )
                            )
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = charStats.name.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Current Title: ${charStats.title}",
                                        color = EpicGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Class: Shadow Warrior // Day ${charStats.dayCount}",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                // Rank Shield Indicator
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    ElectricBlue.copy(alpha = 0.6f),
                                                    ElectricPurple.copy(alpha = 0.2f)
                                                )
                                            )
                                        )
                                        .border(2.dp, EpicGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = charStats.rank,
                                        color = EpicGold,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 19.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // XP Progress Slider
                            val xpPct = if (charStats.requiredXp > 0) charStats.xp.toFloat() / charStats.requiredXp.toFloat() else 0f
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "LVL ${charStats.level}",
                                    color = ElectricBlue,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "EXP: ${charStats.xp} / ${charStats.requiredXp} (${(xpPct * 100).toInt()}%)",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = xpPct,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = ElectricBlue,
                                trackColor = Color.DarkGray.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Stat RPG Panel (STR, AGI, VIT, END, INT)
            item {
                Card(
                    colors = CardColors(DeepDarkGlass.copy(alpha = 0.8f), Color.White, Color.Unspecified, Color.Unspecified),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📊 HUNTER STATUS & RPG ATTRIBUTES",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        StatHUDLine("STR (Compound Muscle Strength)", charStats.str, ElectricBlue, Icons.Filled.FitnessCenter)
                        StatHUDLine("AGI (Cardio Endurance speed)", charStats.agi, ElectricPurple, Icons.Filled.DirectionsRun)
                        StatHUDLine("VIT (Consistency & Streak Health)", charStats.vit, EpicGold, Icons.Filled.Shield)
                        StatHUDLine("END (Deep Durational stamina)", charStats.end, Color(0xFF00FFCC), Icons.Filled.HourglassTop)
                        StatHUDLine("INT (Nutrition & Deep focus)", charStats.intel, Color(0xFFE040FB), Icons.Filled.Psychology)
                    }
                }
            }
        }

        // Today's Quest Brief HUD Panel
        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📜 TODAY'S REAL-TIME ACTIVE QUESTS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        TextButton(onClick = { navController.navigate(Screen.Quests.route) }) {
                            Text(
                                "GO TO HUB",
                                fontSize = 11.sp,
                                color = ElectricBlue,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (quests.isEmpty()) {
                        Text(
                            text = "⚡ SYSTEM: No daily contracts detected. Click the button to regenerate contracts.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.forceResetQuests() },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("RE-SEED DEFAULTS", color = Color.Black, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                    } else {
                        quests.forEach { quest ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (quest.isCompleted) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.03f))
                                    .clickable {
                                        if (!quest.isCompleted) {
                                            viewModel.completeQuest(quest.id, quest.title)
                                        }
                                    }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = quest.title,
                                        color = if (quest.isCompleted) Color.Gray else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "${quest.description} (${quest.currentValue}/${quest.targetValue})",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Icon(
                                    imageVector = if (quest.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = "Status",
                                    tint = if (quest.isCompleted) ElectricBlue else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatHUDLine(label: String, valRaw: Int, color: Color, icon: ImageVector) {
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = label, color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Text(
                text = "$valRaw PT",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = (valRaw.toFloat() / 150f).coerceIn(0f..1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color.DarkGray.copy(alpha = 0.3f)
        )
    }
}

// ==========================================
// 🗓️ QUESTS TAB SCREEN
// ==========================================
@Composable
fun QuestsScreen(viewModel: AriseViewModel) {
    val quests by viewModel.todayQuests.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "HUNTER CONTRACTS",
                    color = ElectricPurple,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "⚡ SYSTEM ALERTS: Daily tasks reset at midnight. Penalty debuffs apply to uncompleted contracts.",
                    color = Color.LightGray.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        // System Alert box
        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SystemRed.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.NotificationImportant, contentDescription = "Penalty Warn", tint = SystemRed)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "🔒 SYSTEM ALERT: Social media limits are sealed with strict Regain timers. Maintain focus.",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (quests.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = { viewModel.forceResetQuests() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("RE-GENERATE DAILY CONTRACT BOARD", color = Color.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        } else {
            items(quests) { quest ->
                Card(
                    colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (quest.isCompleted) ElectricBlue.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (quest.type) {
                                        "Main" -> ElectricBlue.copy(alpha = 0.2f)
                                        "Side" -> EpicGold.copy(alpha = 0.2f)
                                        else -> ElectricPurple.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (quest.type) {
                                    "Main" -> Icons.Filled.FitnessCenter
                                    "Side" -> Icons.Filled.Restaurant
                                    "Focus" -> Icons.Filled.Timer
                                    else -> Icons.Filled.Warning
                                },
                                contentDescription = quest.type,
                                tint = when (quest.type) {
                                    "Main" -> ElectricBlue
                                    "Side" -> EpicGold
                                    else -> ElectricPurple
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = quest.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = quest.description,
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val pct = if (quest.targetValue > 0) quest.currentValue.toFloat() / quest.targetValue.toFloat() else 0f
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LinearProgressIndicator(
                                    progress = pct,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = when (quest.type) {
                                        "Main" -> ElectricBlue
                                        "Side" -> EpicGold
                                        else -> ElectricPurple
                                    },
                                    trackColor = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${quest.currentValue}/${quest.targetValue}",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(
                            onClick = {
                                if (!quest.isCompleted) {
                                    viewModel.completeQuest(quest.id, quest.title)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (quest.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = "Complete",
                                tint = if (quest.isCompleted) ElectricBlue else Color.DarkGray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Boss Quest module
        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EpicGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.WorkspacePremium, contentDescription = "Boss", tint = EpicGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚔️ WEEKLY BOSS SECTOR // GATE TRIAL",
                            color = EpicGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Contract: Complete a 150-minute elite focus block + heavy compound leg volume. Status: Reset on Saturday.",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.completeQuest(-99, "Weekly Boss Gate Trial Completed // Defeated Goliaths!") },
                        colors = ButtonDefaults.buttonColors(containerColor = EpicGold)
                    ) {
                        Text(
                            text = "CLAIM TRIUMPH (+1000 XP & Title)",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 🏋️ WORKOUT WORKER & PLAN SECTORS
// ==========================================
@Composable
fun WorkoutScreen(viewModel: AriseViewModel) {
    val workouts by viewModel.todayWorkouts.collectAsState()
    val isGenPlan by viewModel.isGeneratingAiPlan.collectAsState()
    val planText by viewModel.generatedAiPlanText.collectAsState()

    // Onboard Form inputs
    val goal by viewModel.onboardGoal.collectAsState()
    val equipment by viewModel.onboardEquipment.collectAsState()
    val fitness by viewModel.onboardFitnessLevel.collectAsState()

    // Push Up & Custom States
    val isPushUpActive by viewModel.isPushUpActive.collectAsState()
    val pushUpReps by viewModel.pushUpReps.collectAsState()
    val isPushUpResting by viewModel.isPushUpResting.collectAsState()
    val pushUpRestTimeLeft by viewModel.pushUpRestTimeLeft.collectAsState()
    val playerStats by viewModel.playerStats.collectAsState()

    val context = LocalContext.current
    val currentDay = playerStats?.dayCount ?: 1
    val pushUpTarget = 100 + currentDay - 1

    // Exercise form logging
    var workoutName by remember { mutableStateOf("") }
    var setsVal by remember { mutableStateOf("4") }
    var repsVal by remember { mutableStateOf("8") }
    var weightVal by remember { mutableStateOf("60") }

    if (isPushUpActive) {
        val selectedEx by viewModel.selectedExerciseType.collectAsState()
        val isCameraBlocked by viewModel.isCameraAccessBlockedByBug.collectAsState()
        
        // Immersive camera-assisted split-screen push-up panel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07080F))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "⚔️ SAITAMA'S ${selectedEx.uppercase()} TRIAL",
                            color = EpicGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "DAY $currentDay PROTOCOL // TARGET: $pushUpTarget REPS",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(
                        onClick = { viewModel.isPushUpActive.value = false }
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Exercise selection row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .border(1.dp, ElectricPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val exercises = listOf("Push-ups", "Pull-ups", "Squats", "Sit-ups")
                    exercises.forEach { ex ->
                        val isSel = selectedEx == ex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) ElectricPurple.copy(alpha = 0.3f) else Color.Transparent)
                                .border(1.dp, if (isSel) ElectricBlue else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable {
                                    viewModel.selectedExerciseType.value = ex
                                    viewModel.speak("Selected exercise target: $ex")
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ex.uppercase(),
                                color = if (isSel) Color.White else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Two sides block (using Row with weights)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Side: CAMERA VIEWPORT / SCANNER
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                            .border(2.dp, if (isCameraBlocked) SystemRed else ElectricBlue, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCameraBlocked) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Camera Locked Down",
                                    tint = SystemRed,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "🔒 SCANNERS ISOLATED",
                                    color = SystemRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Camera scanners are disabled to isolate software bugs inside the matrix.",
                                    color = Color.LightGray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // Camera granted. Render scifi AI joint scanner view
                            Box(modifier = Modifier.fillMaxSize()) {
                                val scanAnim = remember { androidx.compose.animation.core.Animatable(0f) }
                                LaunchedEffect(Unit) {
                                    scanAnim.animateTo(
                                        targetValue = 1f,
                                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                            animation = androidx.compose.animation.core.tween(2500, easing = androidx.compose.animation.core.LinearEasing),
                                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                                        )
                                    )
                                }

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val h = size.height
                                    val w = size.width
                                    
                                    drawRect(
                                        color = ElectricBlue.copy(alpha = 0.05f)
                                    )
                                    
                                    val lineY = h * scanAnim.value
                                    drawLine(
                                        color = ElectricBlue.copy(alpha = 0.8f),
                                        start = androidx.compose.ui.geometry.Offset(0f, lineY),
                                        end = androidx.compose.ui.geometry.Offset(w, lineY),
                                        strokeWidth = 3f
                                    )

                                    drawCircle(
                                        color = ElectricPurple.copy(alpha = 0.6f),
                                        radius = 8f,
                                        center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.3f)
                                    )
                                    drawCircle(
                                        color = ElectricBlue.copy(alpha = 0.6f),
                                        radius = 6f,
                                        center = androidx.compose.ui.geometry.Offset(w * 0.4f, h * 0.5f)
                                    )
                                    drawCircle(
                                        color = ElectricBlue.copy(alpha = 0.6f),
                                        radius = 6f,
                                        center = androidx.compose.ui.geometry.Offset(w * 0.6f, h * 0.5f)
                                    )
                                    
                                    drawLine(
                                        color = Color.LightGray.copy(alpha = 0.4f),
                                        start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.3f),
                                        end = androidx.compose.ui.geometry.Offset(w * 0.4f, h * 0.5f)
                                    )
                                    drawLine(
                                        color = Color.LightGray.copy(alpha = 0.4f),
                                        start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.3f),
                                        end = androidx.compose.ui.geometry.Offset(w * 0.6f, h * 0.5f)
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("REC ⏺️", color = SystemRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("AI TRACKER ACTIVE", color = ElectricBlue, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .border(1.dp, EpicGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .background(Color.Black.copy(alpha = 0.7f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("MESH CALIBRATION: ACTIVE (100%)", color = EpicGold, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    }

                                    Text(
                                        text = "Please position camera to capture the full range of movement.",
                                        color = Color.LightGray,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .padding(2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Right Side: TARGET COUNTER & EXHAUSTION CONTROLS (With Auto-detection buttons!)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Card(
                            colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ElectricPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "REPS DONE",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "$pushUpReps",
                                    color = ElectricBlue,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "/ $pushUpTarget",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (pushUpReps < pushUpTarget) {
                                            viewModel.pushUpReps.value++
                                            val currentReps = viewModel.pushUpReps.value
                                            if (currentReps == pushUpTarget) {
                                                viewModel.completePushUpProtocol(pushUpTarget)
                                            } else if (currentReps % 20 == 0) {
                                                viewModel.speak("Excellent threshold! Twenty reps logged. Rest if you need to, Hunter!")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("⚡ TAP TO LOG REP", fontSize = 9.sp, color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Auto-Sense Controls Card
                        val isAutoSensing by viewModel.isAutoSensingEnabled.collectAsState()
                        val isSandbox by viewModel.isSandboxSensorSimulationEnabled.collectAsState()
                        Card(
                            colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ElectricPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "📡 AUTO-SENSE CHANNELS",
                                    color = EpicGold,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isAutoSensing) ElectricBlue.copy(alpha = 0.15f) else Color.Black)
                                        .clickable {
                                            if (isAutoSensing) {
                                                viewModel.stopAutoSensing()
                                            } else {
                                                viewModel.startAutoSensing(context)
                                            }
                                        }
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isAutoSensing) "📡 HW SENSOR: ON" else "📡 HW SENSOR: OFF",
                                        color = if (isAutoSensing) ElectricBlue else Color.Gray,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSandbox) ElectricPurple.copy(alpha = 0.15f) else Color.Black)
                                        .clickable {
                                            viewModel.setSandboxSimulationEnabled(!isSandbox)
                                        }
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isSandbox) "🤖 AUTO-REPPING: ON" else "🤖 AUTO-REPPING: OFF",
                                        color = if (isSandbox) ElectricPurple else Color.Gray,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Recovery & Help
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startPushUpRest() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isPushUpResting,
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Restore, contentDescription = "Rest", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🚨 START REST TIMER", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { viewModel.completePushUpProtocol(pushUpTarget) },
                                colors = ButtonDefaults.buttonColors(containerColor = EpicGold),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("SYNC CLEAR (+50 XP)", fontSize = 8.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            // Rest Overloading Visual Overlay
            if (isPushUpResting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.92f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "🔋 ACTIVE SHADOW REST",
                            color = ElectricPurple,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "$pushUpRestTimeLeft",
                            color = Color.White,
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "SECONDS REMAINING",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "🔊 MOTIVATIONAL AI VOICE ACTIVE:",
                            color = EpicGold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "\"Sai! You can do it! Rep by rep, you are forging a shadow monarch's physique!\"",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(30.dp))
                        Button(
                            onClick = { viewModel.isPushUpResting.value = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text("SKIP COOLDOWN REST", color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "SHADOW PROTOCOLS",
                        color = ElectricBlue,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "🏋️ CHALLENGE YOUR TARGET LIMITS & LEVEL UP STR.",
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }

            // --- Saitama Challenge Card ---
            item {
                Card(
                    colors = CardColors(EpicGold.copy(alpha = 0.12f), Color.White, Color.Unspecified, Color.Unspecified),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.isPushUpActive.value = true }
                        .border(2.dp, EpicGold, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EpicGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera Pushup", tint = EpicGold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ TRIAL: 100+ PROGRESSIVE PUSH-UPS",
                                color = EpicGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "CAMERA FEED SPLIT // DAY $currentDay TARGET: $pushUpTarget REPS (+1/Day)",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Go", tint = EpicGold)
                    }
                }
            }

            // --- System Blocker / Ignore Battery Optimization Control Card ---
            item {
                var batteryExempted by remember { mutableStateOf(viewModel.isIgnoringBatteryOptimizations()) }
                
                Card(
                    colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ElectricPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🛡️ SHIELD CONTROL GATE",
                            color = ElectricPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Exempt application from battery optimization to secure absolute blocker shields over YouTube and gaming clients.",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Battery ignore optimization controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (batteryExempted) "🟢 CHIELD ENGINE ACTIVE" else "🔴 OPTIMIZED (SHIELD MAY SLEEP)",
                                color = if (batteryExempted) Color.Green else SystemRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            Button(
                                onClick = {
                                    if (!batteryExempted) {
                                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            val fall = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                            try { context.startActivity(fall) } catch(ex: Exception) {}
                                        }
                                    }
                                    batteryExempted = viewModel.isIgnoringBatteryOptimizations()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (batteryExempted) Color.DarkGray else ElectricPurple),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("EXEMPT LIMITS", fontSize = 10.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Block targets panel
                        Text(
                            text = "UNDER COOLDOWN SHIELD RECOVERY (YOUTUBE & GAMES):",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "YouTube / Shorts" to "Streaming and feeds",
                                "PUBG Mobile (Tencent)" to "Battle royale gameplay",
                                "Genshin Impact (miHoYo)" to "Open world active adventure",
                                "Roblox" to "Metaverse multiplayer engines"
                            ).forEach { (app, details) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(app, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text(details, color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(SystemRed.copy(alpha = 0.2f))
                                            .border(1.dp, SystemRed, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("SHOCK LOCKED", color = SystemRed, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }

        // Onboarding form for AI splitting
        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🤖 INITIALIZE AI PROGRESS PLANNER",
                        color = ElectricPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Goal selector buttons
                    Text("Select Target Objective:", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Bulk", "Cut", "Maintain").forEach { item ->
                            val active = goal == item
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) ElectricPurple else Color.Black)
                                    .border(1.dp, if (active) ElectricBlue else Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.onboardGoal.value = item }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Equipment selector buttons
                    Text("Select Gear Equipment:", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Gym", "Dumbbells", "Bodyweight").forEach { item ->
                            val active = equipment == item
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) ElectricPurple else Color.Black)
                                    .border(1.dp, if (active) ElectricBlue else Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.onboardEquipment.value = item }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.generateAiWorkoutPlan() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isGenPlan) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("⚡ ARISE // GENERATE INTEL PLAN", color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Display AI Plan Text output
        planText?.let { pText ->
            item {
                Card(
                    colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EpicGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚔️ RECEIVED INTEL: WEEKLY PROTOCOL",
                            color = EpicGold,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = pText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Animated Presets: Train Like Anime Hero
        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🌌 INTERSTELLAR HERO PROTOCOLS",
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.loadSelectedAnimePlan("Saitama Challenge") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Saitama 100", fontSize = 10.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { viewModel.loadSelectedAnimePlan("Sung Jin-Woo Master") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Jin-Woo", fontSize = 10.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { viewModel.loadSelectedAnimePlan("Gojo Satoru Explosive") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Gojo Satoru", fontSize = 10.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Interactive logger form
        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏋️ TRACK MANUAL LOG SETS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = workoutName,
                        onValueChange = { workoutName = it },
                        label = { Text("Exercise Name (e.g., Deadlift)", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color.DarkGray
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = setsVal,
                            onValueChange = { setsVal = it },
                            label = { Text("Sets", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = repsVal,
                            onValueChange = { repsVal = it },
                            label = { Text("Reps", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = weightVal,
                            onValueChange = { weightVal = it },
                            label = { Text("Weight (kg)", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (workoutName.isNotEmpty()) {
                                val s = setsVal.toIntOrNull() ?: 4
                                val r = repsVal.toIntOrNull() ?: 8
                                val w = weightVal.toFloatOrNull() ?: 60f
                                viewModel.logWorkout(workoutName, s, r, w, isPr = w > 80f, category = "STR")
                                workoutName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RECORD SET DATA & GAIN XP", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Workouts logged today list
        item {
            Text(
                text = "📝 LOGGED SESSIONS FOR TODAY",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (workouts.isEmpty()) {
            item {
                Text(
                    text = "No workouts logged today. Complete a preset or add sets manually.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(workouts) { log ->
                Card(
                    colors = CardColors(Color.Black.copy(alpha = 0.5f), Color.White, Color.Unspecified, Color.Unspecified),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = log.exerciseName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (log.isPr) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🏆 PR RECORD",
                                        color = EpicGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Text(
                                text = "${log.sets} sets x ${log.reps} reps // ${log.weightKg} kg (Multiplier: x2)",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(onClick = { viewModel.deleteWorkout(log.id) }) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = SystemRed)
                        }
                    }
                }
            }
        }
    }
}
}

// ==========================================
// 🥗 NUTRITION & macros screen
// ==========================================
@Composable
fun NutritionScreen(viewModel: AriseViewModel) {
    val foods by viewModel.todayFoodLogs.collectAsState()
    val waterCount by viewModel.todayWaterCount.collectAsState()

    var foodInput by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("20") }
    var calInput by remember { mutableStateOf("250") }

    val totalProtein = foods.sumOf { it.protein.toDouble() }.toFloat()
    val totalCalories = foods.sumOf { it.calories }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "HUNTER PROTOCOL: NOURISH",
                    color = EpicGold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "🥗 MACRO LIMITS & HYDRO HYDRATION BAR",
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        // Macro summary bar cards
        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EpicGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⭐ ACTIVE PROGRESS ATTRIBUTES",
                        color = EpicGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Protein Intake", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("$totalProtein G / 100 G", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Sum Calories", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("$totalCalories Kcal // Limit 2400", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = (totalProtein / 100f).coerceIn(0f..1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = EpicGold,
                        trackColor = Color.DarkGray
                    )
                }
            }
        }

        // Hydration tracker module
        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "💧 HYDRO CRYSO-VESSEL TRACKER",
                            color = ElectricBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Water Count: $waterCount / 8 Cups today",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = { viewModel.logWater() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("+ DRINK", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Quick Preset Indian protein foods list
        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🥩 PROTEIN PRESET MATRIX (INDIAN DEFAULTS)",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.logFood("Indian Paneer Subji", 25f, 10f, 18f, 320) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Paneer // 25g P", fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { viewModel.logFood("Whey Protein ISO", 26f, 2f, 1f, 120) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Whey // 26g P", fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { viewModel.logFood("Yellow Dal Tadka", 12f, 30f, 5f, 210) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Dal // 12g P", fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Custom manual logs
        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🥗 RECORD INDEPENDENT ALIMENT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = foodInput,
                        onValueChange = { foodInput = it },
                        label = { Text("Food Name Name (e.g., Boiled Eggs)", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = proteinInput,
                            onValueChange = { proteinInput = it },
                            label = { Text("Protein (g)", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = calInput,
                            onValueChange = { calInput = it },
                            label = { Text("Calories", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (foodInput.isNotEmpty()) {
                                val p = proteinInput.toFloatOrNull() ?: 20f
                                val c = calInput.toIntOrNull() ?: 250
                                viewModel.logFood(foodInput, p, 15f, 6f, c)
                                foodInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EpicGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RECORD NUTRITION DATA", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Current meals log table
        item {
            Text("📝 SAVED FEED HISTORY", color = Color.LightGray, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        if (foods.isEmpty()) {
            item {
                Text(
                    text = "No meals saved today.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(foods) { food ->
                Card(
                    colors = CardColors(Color.Black.copy(alpha = 0.4f), Color.White, Color.Unspecified, Color.Unspecified),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = food.foodName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${food.protein}g Protein // ${food.calories} calories rec.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(onClick = { viewModel.deleteFood(food.id) }) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = SystemRed)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 🧠 FOCUS TIMER & TIMER ACTIONS
// ==========================================
@Composable
fun FocusScreen(viewModel: AriseViewModel) {
    val status by viewModel.timerStatus.collectAsState()
    val secRemain by viewModel.timeLeftSeconds.collectAsState()
    val isSoundLofi by viewModel.isLofiActive.collectAsState()

    val blockApps by viewModel.blockedApps.collectAsState()

    val mins = secRemain / 60
    val secs = secRemain % 60
    val formatStr = String.format("%02d:%02d", mins, secs)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "REGAIN CORE TIMER",
                    color = ElectricPurple,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "🧠 COMBAT DOOMSCROLLING BY SEALING PROTOCOLS",
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        // Circular dynamic countdown HUD clock
        item {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(DeepDarkGlass)
                    .border(
                        2.dp,
                        Brush.sweepGradient(listOf(ElectricBlue, ElectricPurple, ElectricBlue)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Outer sonic glow ripple effect
                if (status == AriseViewModel.TimerStatus.WORKING) {
                    val inf = rememberInfiniteTransition()
                    val sc by inf.animateFloat(
                        180f, 210f,
                        animationSpec = infiniteRepeatable(
                            tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(sc.dp)
                            .border(1.dp, ElectricBlue.copy(alpha = 0.2f), CircleShape)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FOCUS SEAL",
                        fontSize = 10.sp,
                        color = ElectricPurple,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatStr,
                        fontSize = 32.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = status.name,
                        fontSize = 11.sp,
                        color = if (status == AriseViewModel.TimerStatus.WORKING) ElectricBlue else Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Play controllers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (status != AriseViewModel.TimerStatus.WORKING) {
                    Button(
                        onClick = { viewModel.startFocusTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier.width(110.dp)
                    ) {
                        Text("START", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.pauseFocusTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = SystemRed),
                        modifier = Modifier.width(110.dp)
                    ) {
                        Text("PAUSE", color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = { viewModel.resetFocusTimer() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.width(110.dp)
                ) {
                    Text("RESET", color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Lofi sound control card
        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.MusicNote, contentDescription = "Audio", tint = ElectricPurple)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("SCIENCE-BACKED LO-FI STREAM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("Acoustic rain frequency synthesis active", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Switch(
                        checked = isSoundLofi,
                        onCheckedChange = { viewModel.isLofiActive.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ElectricBlue, checkedTrackColor = ElectricPurple)
                    )
                }
            }
        }

        // App blocker interactive list (Regain companion)
        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SystemRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔒 SEALED APPS (INTERACTIVE SIMULATOR)",
                        color = SystemRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Attempting to launch these apps triggers the Doomscroll Interrupter.",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    blockApps.forEach { blockApp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { viewModel.attemptOpenBlockedApp(blockApp.appPackage) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.Lock, contentDescription = "Lock", tint = SystemRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(blockApp.appDisplayName, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            Text("TEST OPEN", color = ElectricBlue, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 🏆 PROGRESS & SHAREABLE CARD SCREEN
// ==========================================
@Composable
fun ProgressScreen(viewModel: AriseViewModel) {
    val workouts by viewModel.todayWorkouts.collectAsState()
    val stats by viewModel.playerStats.collectAsState()

    // Mock Grid list of badges
    val badges = listOf(
        "First Quest" to Icons.Filled.WorkspacePremium,
        "30-Day Streak" to Icons.Filled.LocalFireDepartment,
        "S-Rank Status" to Icons.Filled.Shield,
        "Monarch Mind" to Icons.Filled.Psychology,
        "Golden Chalice" to Icons.Filled.FitnessCenter,
        "Timer Titan" to Icons.Filled.HourglassTop
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "MONARCH CHRONICLES",
                    color = ElectricPurple,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "🏆 ALL-TIME LOG ARCHIVES & INVENTORIES",
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        // Visual Shareable Screenshot Card Box
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "📱 INSTAGRAM SHAREABLE AVATAR CARD",
                    color = ElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // High fidelity aesthetic sharing container
                Card(
                    colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Brush.horizontalGradient(listOf(ElectricBlue, ElectricPurple)), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        DeepDarkGlass,
                                        PitchBlack
                                    )
                                )
                            )
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ARISE HUNTER SYSTEM // LOG",
                            color = ElectricBlue,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Large circular level layout
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(ElectricPurple.copy(alpha = 0.2f))
                                .border(3.dp, EpicGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "LVL",
                                    fontSize = 10.sp,
                                    color = EpicGold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${stats?.level ?: 1}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stats?.name?.uppercase() ?: "SAI VATSAL",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "CLASS: ${stats?.title ?: "SHADOW WARRIOR"}",
                            color = ElectricBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Sharing values table
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("RANK", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(stats?.rank ?: "E", color = EpicGold, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("STREAK", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text("${stats?.streak ?: 1} Days", color = ElectricPurple, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("VOLUME", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                val volume = workouts.sumOf { (it.sets * it.reps * 10).toDouble() }.toInt()
                                Text("${volume} XP", color = ElectricBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "⚡ RATED SS FOCUS MIND ACTIVATE ⚡",
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Achievements Badge inventory
        item {
            Text(
                text = "🏆 ACQUIRED ELITE MONARCH BADGES",
                color = EpicGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        item {
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(badges) { badge ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElectricPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = badge.second,
                                    contentDescription = badge.first,
                                    tint = EpicGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = badge.first,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // SYSTEM HEALTH & EMERGENCY VULNERABILITY MITIGATION VALVE
        item {
            val isSecMuted by viewModel.isSystemMutedWithBugs.collectAsState()
            Card(
                colors = CardColors(DeepDarkGlass, Color.White, Color.Unspecified, Color.Unspecified),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isSecMuted) SystemRed.copy(alpha = 0.5f) else ElectricPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🛠️ SUBSYSTEM HEALTH & BUG DEFENSE MITIGATION",
                        color = if (isSecMuted) SystemRed else ElectricBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Simulate software exceptions or bugs to deploy an automatic defensive shield. This blocks access to cameras, microphones, and screens for safety.",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSecMuted) SystemRed.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.5f))
                            .clickable {
                                viewModel.triggerSystemBugCrashIsolation(!isSecMuted)
                            }
                            .border(1.dp, if (isSecMuted) SystemRed else Color.Transparent, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSecMuted) "🔒 SYSTEM SHIELD ISOLATED (BUG TRIGGERED)" else "🟢 SHIELD HEALTHY (ALL GATES SECURED)",
                                color = if (isSecMuted) SystemRed else Color.Green,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isSecMuted) "Subsystems are locked down: mic, camera, and HUD are offline." else "Normal operations clear. Subsystem channels healthy.",
                                color = Color.LightGray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Switch(
                            checked = isSecMuted,
                            onCheckedChange = { active ->
                                viewModel.triggerSystemBugCrashIsolation(active)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SystemRed,
                                checkedTrackColor = SystemRed.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.Black
                            )
                        )
                    }
                }
            }
        }
    }
}

// Custom level up dynamic dialog content
@Composable
fun LevelUpAnimationContent(stats: PlayerStats?, onClose: () -> Unit) {
    val level = stats?.level ?: 1
    val title = stats?.title ?: "Shadow Warrior"
    val rank = stats?.rank ?: "E"

    Column(
        modifier = Modifier
            .padding(24.dp)
            .border(2.dp, EpicGold, RoundedCornerShape(16.dp))
            .background(DeepDarkGlass)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.OfflineBolt,
            contentDescription = "Bolt",
            tint = EpicGold,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "⚡ LEVEL UP! ⚡",
            color = EpicGold,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "SYSTEM ASSIGNED STAT INCREASE",
            color = ElectricBlue,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "NEW RANK ACHIEVED: $rank",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "ACTIVE LEVEL: $level",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Current Title: $title",
            color = EpicGold,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "All RPG stats (STR, AGI, VIT, END, INT) have gained +1 limits. Defeat your limits, Hunter.",
            color = Color.LightGray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = EpicGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ARISE NOW",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
