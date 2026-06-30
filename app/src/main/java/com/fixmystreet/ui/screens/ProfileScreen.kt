package com.fixmystreet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fixmystreet.ui.components.SmartAsyncImage
import com.fixmystreet.ui.components.BottomNavBar
import com.fixmystreet.ui.components.WavyBottomShape
import com.fixmystreet.ui.components.WavyTopShape
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*
import com.fixmystreet.ui.viewmodel.AuthViewModel
import com.fixmystreet.ui.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val userProfile by profileViewModel.userProfile.collectAsState()

    var showDeactivateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (user != null) profileViewModel.loadProfile(user.uid)
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, Screen.Profile.route) },
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // Bottom-right background decoration wave to match Figma background accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .align(Alignment.BottomEnd)
                    .clip(WavyBottomShape)
                    .background(Color(0xFFCFD8DC).copy(alpha = 0.5f))
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Blue Wavy Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(WavyTopShape)
                        .background(PrimaryBlue)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Profile",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Profile Info Row (Left aligned avatar + details horizontally)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val photo = userProfile?.photoURL.orEmpty()
                    if (photo.isNotEmpty()) {
                        SmartAsyncImage(
                            photoUrl = photo,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        // Silhouette inside solid black circle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = userProfile?.name ?: user?.displayName ?: user?.email?.substringBefore("@") ?: "User Name",
                            color = PrimaryDark,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Account",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Divider line below profile avatar row
                HorizontalDivider(color = Border, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))

                Spacer(modifier = Modifier.height(8.dp))

                // Menu Items List (Flat style with divider lines)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Settings,
                        label = "Settings",
                        iconTint = PrimaryDark,
                        onClick = { navController.navigate(Screen.EditProfile.route) }
                    )
                    HorizontalDivider(color = Border, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))

                    ProfileMenuItem(
                        icon = Icons.Outlined.Info,
                        label = "About Us",
                        iconTint = PrimaryDark,
                        onClick = { navController.navigate(Screen.AboutUs.route) }
                    )
                    HorizontalDivider(color = Border, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))

                    ProfileMenuItem(
                        icon = Icons.Outlined.MonetizationOn,
                        label = "Donate Us",
                        iconTint = Color(0xFFFFA000), // Yellow/Orange accent matching Figma
                        onClick = { navController.navigate(Screen.Donate.route) }
                    )
                    HorizontalDivider(color = Border, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))

                    ProfileMenuItem(
                        icon = Icons.Outlined.Person,
                        label = "Sign Out",
                        iconTint = PrimaryDark,
                        onClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                    HorizontalDivider(color = Border, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
                }
            }
        }

        if (showDeactivateDialog) {
            AlertDialog(
                onDismissRequest = { showDeactivateDialog = false },
                title = { Text("Deactivate Account") },
                text = { Text("Are you sure?", color = Danger) },
                confirmButton = {
                    TextButton(onClick = { showDeactivateDialog = false }) { Text("Confirm", color = Danger) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeactivateDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, iconTint: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = PrimaryDark,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Go",
            tint = PrimaryDark,
            modifier = Modifier.size(24.dp)
        )
    }
}
