package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.FirebaseManager
import com.example.network.UserSessionState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: AriseViewModel) {
    val authState by FirebaseManager.authState.collectAsState()
    val isConfigured by FirebaseManager.isConfigured.collectAsState()
    val focusManager = LocalFocusManager.current

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    
    var showPassword by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Visual animation effects
    val infiniteTransition = rememberInfiniteTransition(label = "Aura")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Epic Background Glow Aura
        Box(
            modifier = Modifier
                .size(340.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElectricPurple.copy(alpha = 0.22f),
                                ElectricBlue.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 1.1f * glowScale
                    )
                }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Gamified System Logo Block
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.linearGradient(listOf(ElectricPurple, ElectricBlue)),
                        RoundedCornerShape(24.dp)
                    )
                    .border(2.dp, EpicGold, RoundedCornerShape(24.dp))
                    .shadow(16.dp, RoundedCornerShape(24.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.MilitaryTech,
                    contentDescription = "System Emblem",
                    tint = EpicGold,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SYSTEM GATEWAY",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "LEVEL UP YOUR FITNESS & PRODUCTIVITY",
                fontSize = 11.sp,
                color = ElectricBlue,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card Body (Glassmorphism Styled Form)
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepDarkGlass),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(ElectricPurple, ElectricBlue)),
                        RoundedCornerShape(16.dp)
                    )
                    .shadow(12.dp, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isLoginMode) "INITIALIZE AUTHENTICATION" else "CREATE SYSTEM ACCOUNT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )

                    // Error Panel
                    AnimatedVisibility(visible = statusMessage != null || authState is UserSessionState.Error) {
                        val displayMsg = statusMessage ?: (authState as? UserSessionState.Error)?.message ?: ""
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SystemRed.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .border(1.dp, SystemRed, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error icon",
                                    tint = SystemRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = displayMsg,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Fields
                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Hunter Real Name", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = ElectricPurple) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = PitchBlack,
                                unfocusedContainerColor = PitchBlack,
                                focusedIndicatorColor = ElectricPurple,
                                unfocusedIndicatorColor = Color.DarkGray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("System Email Address", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Outlined.Mail, contentDescription = null, tint = ElectricBlue) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = PitchBlack,
                            unfocusedContainerColor = PitchBlack,
                            focusedIndicatorColor = ElectricBlue,
                            unfocusedIndicatorColor = Color.DarkGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Access Password", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = ElectricPurple) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color.Gray
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = PitchBlack,
                            unfocusedContainerColor = PitchBlack,
                            focusedIndicatorColor = ElectricPurple,
                            unfocusedIndicatorColor = Color.DarkGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )

                    // Core Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            statusMessage = null
                            if (email.isEmpty() || password.isEmpty()) {
                                statusMessage = "Required fields cannot be left empty"
                                return@Button
                            }
                            if (!isLoginMode && displayName.isEmpty()) {
                                statusMessage = "Display name cannot be left empty"
                                return@Button
                            }

                            if (isLoginMode) {
                                FirebaseManager.loginWithEmail(email, password) { success, err ->
                                    if (!success) statusMessage = err
                                }
                            } else {
                                FirebaseManager.registerWithEmail(email, password, displayName) { success, err ->
                                    if (!success) statusMessage = err
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(ElectricPurple, ElectricBlue)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (authState is UserSessionState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = if (isLoginMode) "SIGN IN TO SYSTEM" else "REGISTER AS HUNTER",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Switch Mode Click
                    TextButton(onClick = { isLoginMode = !isLoginMode }) {
                        Text(
                            text = if (isLoginMode) "New Hunter? Register Credentials" else "Existing Account? Sign In Instead",
                            color = ElectricBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // External Social Sign On Providers (Google & Facebook)
            Text(
                text = "— OR SIGN IN WITH OVER-GATE SEALS —",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Google Login Button
                OutlinedButton(
                    onClick = {
                        // Triggers programmatic Google entry log
                        FirebaseManager.signInWithGoogleToken("google_oauth2_mock_token") { success, err ->
                            if (!success) statusMessage = err
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.DarkGray)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = "Google Sign In",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Facebook Login Button
                OutlinedButton(
                    onClick = {
                        // Triggers Facebook OAuth access log
                        FirebaseManager.signInWithFacebookToken("facebook_access_token") { success, err ->
                            if (!success) statusMessage = err
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.DarkGray)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Facebook,
                            contentDescription = "Facebook Sign In",
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Facebook", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Firebase Connection Live status information (Mandate 4)
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isConfigured) DeepDarkGlass else DeepDarkGlass.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isConfigured) ElectricBlue.copy(alpha = 0.5f) else EpicGold.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isConfigured) ElectricBlue else EpicGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isConfigured) "LIVE FIREBASE GATE CONNECTED" else "LOCAL SANDBOX GATE ACTIVE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConfigured) ElectricBlue else EpicGold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isConfigured) {
                            "Your gamified stats, quest goals, nutrition records, and focus logs will automatically keep synchronized in clear-time with your Cloud Firestore database!"
                        } else {
                            "To activate live database synchronization inside your Firebase instance, configure FIREBASE_API_KEY, FIREBASE_PROJECT_ID, and FIREBASE_APP_ID securely in the AI Studio Secrets panel. Enter Sandbox mode below to start playing instantly!"
                        },
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (!isConfigured) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                FirebaseManager.loginWithEmail("saivatsal.sandbox@arise.io", "sandbox_bypass_pass") { _, _ -> }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EpicGold.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, EpicGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("ENTER GATE SOLO (SANDBOX BYPASS)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EpicGold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
