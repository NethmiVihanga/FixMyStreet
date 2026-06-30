package com.fixmystreet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fixmystreet.R
import com.fixmystreet.ui.components.PrimaryButton
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.PrimaryBlue
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun SplashScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    
    LaunchedEffect(Unit) {
        delay(2000)
        if (auth.currentUser != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(150.dp) // Slightly larger based on screenshot
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Text not needed since it's inside the logo in the design, but let's check
            // Actually, in the screenshot, the logo image itself has "FIX MY STREET"
            // And then there is a text "Report Today. Repair Tomorrow." below it.
            Text(text = "Report Today. Repair Tomorrow.", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        // The three dots at the bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444))) // Red
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981))) // Green
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4B7BEB))) // Light Blue
        }
    }
}
