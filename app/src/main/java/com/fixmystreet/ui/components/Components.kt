package com.fixmystreet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp)),
            color = PrimaryBlue
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    selected = currentRoute == Screen.Home.route,
                    onClick = { navController.navigate(Screen.Home.route) { launchSingleTop = true; popUpTo(Screen.Home.route) } }
                )
                NavItem(
                    icon = Icons.Default.Download,
                    label = "Updates",
                    selected = currentRoute == Screen.Updates.route,
                    onClick = { navController.navigate(Screen.Updates.route) { launchSingleTop = true } }
                )
                NavItem(
                    icon = Icons.Default.Upload,
                    label = "Upload",
                    selected = currentRoute?.startsWith("report") == true,
                    onClick = { navController.navigate("report") { launchSingleTop = true } }
                )
                NavItem(
                    icon = Icons.Default.Notifications,
                    label = "Notifications",
                    selected = currentRoute == Screen.Notifications.route,
                    onClick = { navController.navigate(Screen.Notifications.route) { launchSingleTop = true } }
                )
                NavItem(
                    icon = Icons.Default.Person,
                    label = "Account",
                    selected = currentRoute == Screen.Profile.route,
                    onClick = { navController.navigate(Screen.Profile.route) { launchSingleTop = true } }
                )
            }
        }
    }
}

@Composable
fun NavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    // In the design, all icons and text in the bottom bar are white, no selected state coloring is explicitly shown,
    // but maybe we can make selected white and unselected semi-transparent white
    val color = if (selected) Color.White else Color.White.copy(alpha = 0.6f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
        shape = CircleShape
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
