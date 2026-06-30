package com.fixmystreet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*

@Composable
fun VerifyOtpScreen(navController: NavController, email: String) {
    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

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
                text = "Verify OTP",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = PrimaryDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Please enter the 4-digit code sent to\n$email",
                color = TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // OTP Input Field
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Verification Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF4A4A4A),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                TextField(
                    value = otpCode,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
                        if (clean.length <= 4) {
                            otpCode = clean
                            errorMessage = null
                        }
                    },
                    placeholder = { Text("Enter 4-digit OTP", color = Color.LightGray, fontSize = 16.sp, textAlign = TextAlign.Center) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFFD3DFE8),
                        unfocusedIndicatorColor = Color(0xFFE0E0E0),
                        cursorColor = PrimaryBlue
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().widthIn(max = 240.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 4.sp
                    ),
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
                        if (otpCode.length != 4) {
                            errorMessage = "Please enter the full 4-digit code."
                            return@Button
                        }

                        isLoading = true
                        val expectedOtp = ForgotPasswordScreen.activeOtpMap[email]
                        
                        if (expectedOtp != null && expectedOtp == otpCode) {
                            // OTP Correct! Redirect to reset password
                            errorMessage = null
                            isLoading = false
                            navController.navigate(Screen.ResetPassword.createRoute(email))
                        } else {
                            errorMessage = "Invalid verification code. Please try again."
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Verify & Proceed",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
