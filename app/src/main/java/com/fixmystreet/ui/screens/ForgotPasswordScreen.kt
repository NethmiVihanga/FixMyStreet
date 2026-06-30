package com.fixmystreet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var generatedOtp by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var resolvedCanonicalEmail by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top and Bottom wavy accent backgrounds to match premium aesthetics
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

            // Bottom wavy shape
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
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 180.dp, bottom = 48.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Forgot Password",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = PrimaryDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enter your registered email address to receive a 4-digit verification code.",
                color = TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Email Form Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Email Address",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF4A4A4A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    placeholder = { Text("Type your email address", color = Color.LightGray, fontSize = 14.sp) },
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

            Spacer(modifier = Modifier.height(20.dp))

            // Error display
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Danger,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Submit Button
            if (isLoading) {
                CircularProgressIndicator(color = PrimaryBlue)
            } else {
                Button(
                    onClick = {
                        val trimmedEmail = email.trim()
                        if (trimmedEmail.isEmpty()) {
                            errorMessage = "Please enter your email address."
                            return@Button
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                            errorMessage = "Please enter a valid email address."
                            return@Button
                        }

                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val cleanEmail = trimmedEmail.lowercase()
                                val emailsToQuery = listOf(cleanEmail, trimmedEmail, email).distinct()
                                val userQuery = db.collection("users")
                                    .whereIn("email", emailsToQuery)
                                    .get()
                                    .await()

                                if (userQuery.isEmpty) {
                                    errorMessage = "This email is not registered."
                                } else {
                                    val doc = userQuery.documents[0]
                                    val canonicalEmail = doc.getString("email") ?: trimmedEmail
                                    resolvedCanonicalEmail = canonicalEmail
                                    
                                    // Generate and save 4 digit OTP in static map under canonical email
                                    val code = (1000..9999).random().toString()
                                    ForgotPasswordScreen.activeOtpMap[canonicalEmail] = code
                                    generatedOtp = code
                                    showSuccessDialog = true
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "An error occurred. Please try again."
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Send Code",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Modern Premium OTP Dialog Simulation
    if (showSuccessDialog && generatedOtp != null) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false 
                navController.navigate(Screen.VerifyOtp.createRoute(resolvedCanonicalEmail))
            },
            title = {
                Text(
                    text = "Verification Code Sent",
                    color = PrimaryDark,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "We have simulated sending an email with a 4-digit verification code to your email inbox.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimarySurface, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = generatedOtp!!,
                            color = PrimaryDark,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 8.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigate(Screen.VerifyOtp.createRoute(resolvedCanonicalEmail))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Verify Code", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// In-memory OTP store
object ForgotPasswordScreen {
    val activeOtpMap = mutableMapOf<String, String>()
}
