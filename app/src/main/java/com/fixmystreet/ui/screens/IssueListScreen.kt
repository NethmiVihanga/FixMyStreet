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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fixmystreet.ui.components.SmartAsyncImage
import com.fixmystreet.data.model.Issue
import com.fixmystreet.ui.components.BottomNavBar
import com.fixmystreet.ui.components.WavyBottomShape
import com.fixmystreet.ui.components.WavyTopShape
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*
import com.fixmystreet.ui.viewmodel.HomeViewModel

@Composable
fun IssueListScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val issues by viewModel.issues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val refreshIssues by savedStateHandle?.getStateFlow<Boolean?>("refresh_issues", null)?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.loadIssues()
    }

    LaunchedEffect(refreshIssues) {
        if (refreshIssues == true) {
            viewModel.loadIssues()
            savedStateHandle?.set("refresh_issues", false)
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, Screen.Updates.route) },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Light blue wavy background at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .align(Alignment.BottomCenter)
                    .clip(WavyBottomShape)
                    .background(PrimarySurface) // Light blue
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Blue Wavy Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
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
                            text = "Issue List",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 48.dp) // to center it relative to the row
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // List of Issues
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(issues) { issue ->
                            CompactIssueCard(issue) {
                                navController.navigate(Screen.Detail.createRoute(issue.id))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactIssueCard(issue: Issue, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmartAsyncImage(
                photoUrl = issue.photoURL,
                contentDescription = "Issue Image",
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.35f)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = issue.title,
                    color = PrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = issue.location,
                    color = PrimaryDark.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
        }
    }
}
