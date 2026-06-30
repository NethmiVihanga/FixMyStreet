package com.fixmystreet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fixmystreet.ui.components.BottomNavBar
import com.fixmystreet.ui.components.WavyTopShape
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*
import com.fixmystreet.ui.viewmodel.NotificationViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NotificationsScreen(navController: NavController, viewModel: NotificationViewModel = viewModel()) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    LaunchedEffect(Unit) {
        viewModel.loadNotifications(user?.uid ?: "mockUser")
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, Screen.Notifications.route) },
        containerColor = Background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Blue Wavy Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(WavyTopShape)
                    .background(PrimaryBlue)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Notifications",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 48.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notifications yet.", color = TextSecondary, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications) { notif ->
                        val isUnread = !notif.read
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(
                                    topStart = if (isUnread) 0.dp else 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp
                                ))
                                .background(if (isUnread) PrimaryBlue else Color.White)
                                .clickable {
                                    if (!notif.read) viewModel.markRead(notif.id, user?.uid ?: "mockUser")
                                    if (notif.issueId != null) navController.navigate(Screen.Detail.createRoute(notif.issueId))
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = notif.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isUnread) Color.White else PrimaryDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.message,
                                    fontSize = 13.sp,
                                    color = if (isUnread) Color.White.copy(alpha = 0.85f) else TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                    // "..." more indicator like mockup
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("• • •", color = TextLight, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}
