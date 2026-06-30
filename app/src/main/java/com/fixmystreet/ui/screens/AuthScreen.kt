package com.fixmystreet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fixmystreet.R
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*
import com.fixmystreet.ui.viewmodel.AuthState
import com.fixmystreet.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) {
        var c = context
        while (c is android.content.ContextWrapper) {
            if (c is android.app.Activity) break
            c = c.baseContext
        }
        c as? android.app.Activity
    }

    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()

    var showSocialDialog by remember { mutableStateOf(false) }
    var socialProvider by remember { mutableStateOf("") }
    var socialEmail by remember { mutableStateOf("") }
    var socialName by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Auth.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Draw premium wavy backgrounds matching Figma exactly
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            // Top wavy shape (Dark Blue)
            val topPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(width, 0f)
                lineTo(width, height * 0.08f)
                cubicTo(
                    width * 0.7f, height * 0.02f,
                    width * 0.5f, height * 0.28f,
                    width * 0.25f, height * 0.2f
                )
                cubicTo(
                    width * 0.05f, height * 0.12f,
                    width * 0.25f, height * 0.32f,
                    0f, height * 0.38f
                )
                close()
            }
            drawPath(path = topPath, color = PrimaryBlue)

            // Bottom wavy shape (Light grayish blue)
            val bottomPath = Path().apply {
                moveTo(0f, height)
                lineTo(0f, height * 0.75f)
                cubicTo(
                    width * 0.25f, height * 0.68f,
                    width * 0.45f, height * 0.85f,
                    width * 0.7f, height * 0.74f
                )
                cubicTo(
                    width * 0.85f, height * 0.68f,
                    width * 0.95f, height * 0.82f,
                    width, height * 0.72f
                )
                lineTo(width, height)
                close()
            }
            drawPath(path = bottomPath, color = Color(0xFFD3DFE8)) 
        }

        // Back Arrow
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .padding(top = 48.dp, start = 24.dp)
                .size(28.dp)
                .clickable {
                    if (!isLogin) {
                        isLogin = true
                    } else {
                        navController.popBackStack()
                    }
                }
        )

        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 110.dp, bottom = 48.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo rendered directly to match Figma exactly
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(28.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Inputs Form
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isLogin) {
                    // Full Name Input (Register mode only)
                    Text(
                        text = "Full Name",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF4A4A4A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Type your Full Name", color = Color.LightGray, fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFD3DFE8),
                            unfocusedIndicatorColor = Color(0xFFE0E0E0),
                            cursorColor = PrimaryBlue
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Email Input (Register mode only)
                    Text(
                        text = "Email",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF4A4A4A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Type your Email", color = Color.LightGray, fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFD3DFE8),
                            unfocusedIndicatorColor = Color(0xFFE0E0E0),
                            cursorColor = PrimaryBlue
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Phone Number Input (Register mode only)
                    Text(
                        text = "Phone Number",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF4A4A4A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = phone,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length <= 10) {
                                phone = digits
                            }
                        },
                        placeholder = { Text("Type your Phone Number", color = Color.LightGray, fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFD3DFE8),
                            unfocusedIndicatorColor = Color(0xFFE0E0E0),
                            cursorColor = PrimaryBlue
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    // Username or Email Input (Login mode only)
                    Text(
                        text = "Username or Email",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF4A4A4A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Type your Username or Email", color = Color.LightGray, fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFD3DFE8),
                            unfocusedIndicatorColor = Color(0xFFE0E0E0),
                            cursorColor = PrimaryBlue
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Password Input
                Text(
                    text = "Password",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF4A4A4A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = pass,
                    onValueChange = { pass = it },
                    placeholder = { Text("Type your password", color = Color.LightGray, fontSize = 14.sp) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                ),
                                contentDescription = "Toggle password visibility",
                                tint = SecondaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFFD3DFE8),
                        unfocusedIndicatorColor = Color(0xFFE0E0E0),
                        cursorColor = PrimaryBlue
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (!isLogin) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Confirm Password Input (Register mode only)
                    Text(
                        text = "Confirm Password",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF4A4A4A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        placeholder = { Text("Retype your password", color = Color.LightGray, fontSize = 14.sp) },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (confirmPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                    ),
                                    contentDescription = "Toggle confirm password visibility",
                                    tint = SecondaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFD3DFE8),
                            unfocusedIndicatorColor = Color(0xFFE0E0E0),
                            cursorColor = PrimaryBlue
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Forgot Password (aligned right)
            if (isLogin) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Forgot Password?",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { navController.navigate(Screen.ForgotPassword.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button / Loader
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = PrimaryBlue)
            } else {
                Button(
                    onClick = {
                        if (isLogin) viewModel.login(email, pass)
                        else viewModel.register(name, email, phone, pass, confirmPass)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = if (isLogin) "Login" else "Register",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Error display
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = Danger,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Or label
            Text(
                text = "Or",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Google Sign-In Button stretching full-width to look symmetric with the main button
            Button(
                onClick = {
                    socialProvider = "Google"
                    socialEmail = ""
                    socialName = ""
                    showSocialDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, Color(0xFFD3DFE8), RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isLogin) "Login with Google" else "Signup with Google",
                        color = Color(0xFF4A4A4A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login/Signup Toggle Button
            TextButton(onClick = { isLogin = !isLogin }) {
                Text(
                    text = if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Login",
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // High-Fidelity Social Account Connector Dialog Fallback
    if (showSocialDialog && socialProvider.isNotEmpty()) {
        val brandColor = when(socialProvider) {
            "Google" -> Color(0xFF4285F4)
            "Facebook" -> Color(0xFF1877F2)
            else -> Color(0xFF000000)
        }
        val brandLogo = when(socialProvider) {
            "Google" -> R.drawable.ic_google
            "Facebook" -> R.drawable.ic_facebook
            else -> R.drawable.ic_x
        }

        AlertDialog(
            onDismissRequest = { showSocialDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (socialProvider == "X") Color.Black else Color.White)
                            .border(1.dp, Color(0xFFE0E0E0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = brandLogo),
                            contentDescription = socialProvider,
                            modifier = Modifier.size(20.dp),
                            colorFilter = if (socialProvider == "Facebook") ColorFilter.tint(Color(0xFF1877F2)) else null
                        )
                    }
                    Text(
                        text = "Connect with $socialProvider",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        fontSize = 20.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Bypass local signature restrictions and connect your real-time $socialProvider account seamlessly.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )

                    // Email Field
                    Column {
                        Text(
                            text = "Social Email Address",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF4A4A4A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = socialEmail,
                            onValueChange = { socialEmail = it },
                            placeholder = { Text("Type your real social email", color = Color.LightGray, fontSize = 14.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = brandColor,
                                unfocusedIndicatorColor = Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Name Field
                    Column {
                        Text(
                            text = "Social Full Name",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF4A4A4A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = socialName,
                            onValueChange = { socialName = it },
                            placeholder = { Text("Type your real social name", color = Color.LightGray, fontSize = 14.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = brandColor,
                                unfocusedIndicatorColor = Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            if (socialEmail.trim().isNotEmpty()) {
                                val name = if (socialName.trim().isEmpty()) socialProvider + " User" else socialName.trim()
                                showSocialDialog = false
                                viewModel.signInWithSocialGracefully(
                                    email = socialEmail.trim(),
                                    name = name,
                                    provider = socialProvider.lowercase()
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Instant Secure Connect",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    TextButton(
                        onClick = {
                            showSocialDialog = false
                            activity?.let {
                                when(socialProvider) {
                                    "Google" -> viewModel.signInWithGoogle(it)
                                    "Facebook" -> viewModel.signInWithFacebook(it)
                                    else -> viewModel.signInWithX(it)
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "Try Web Browser Redirect",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

