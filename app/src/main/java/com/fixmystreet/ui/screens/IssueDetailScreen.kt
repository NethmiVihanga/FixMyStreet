package com.fixmystreet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.fixmystreet.ui.components.BottomNavBar
import com.fixmystreet.ui.components.WavyBottomShape
import com.fixmystreet.ui.components.WavyTopShape
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*
import com.fixmystreet.ui.viewmodel.DetailViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun IssueDetailScreen(
    navController: NavController,
    issueId: String,
    viewModel: DetailViewModel = viewModel()
) {
    val issue by viewModel.issue.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Current signed-in user
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }

    LaunchedEffect(issueId) {
        viewModel.loadIssueDetails(issueId)
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, Screen.Detail.route) },
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
                    .background(PrimarySurface)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
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
                            text = "Issue Details",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        // spacer to keep title centered
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }

                if (isLoading && issue == null) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else if (issue != null) {
                    val i = issue!!

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .offset(y = (-40).dp)
                    ) {
                        // Hero Image
                        SmartAsyncImage(
                            photoUrl = i.photoURL,
                            contentDescription = "Issue Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Title Section
                        Text(text = "Title", color = PrimaryDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoBox(text = i.title)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Location Section
                        Text(text = "Location", color = PrimaryDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoBox(text = i.location)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description Section
                        Text(text = "Description", color = PrimaryDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoBox(text = i.description, showArrow = true)

                        Spacer(modifier = Modifier.height(32.dp))

                        // Whether the signed-in user owns this report
                        val isOwner = currentUserId.isNotEmpty() && currentUserId == i.userId

                        // Action Buttons
                        if (isOwner) {
                            // Owner sees Edit + Delete + Comments
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        editTitle = i.title
                                        editLocation = i.location
                                        editDescription = i.description
                                        showEditDialog = true
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight.copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Edit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Button(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Button(
                                    onClick = { navController.navigate(Screen.Comments.createRoute(issueId)) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Comments", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        } else {
                            // Non-owner sees Comments only
                            Button(
                                onClick = { navController.navigate(Screen.Comments.createRoute(issueId)) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Comments", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Edit dialog (owner only)
            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("Edit Issue") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                label = { Text("Title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editLocation,
                                onValueChange = { editLocation = it },
                                label = { Text("Location") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editDescription,
                                onValueChange = { editDescription = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.updateIssueDetails(issueId, editTitle, editDescription, editLocation)
                            showEditDialog = false
                        }) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // Delete confirmation dialog (owner only)
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Report") },
                    text = { Text("Are you sure you want to permanently delete this report? This cannot be undone.", color = Danger) },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            viewModel.deleteIssue(issueId) { navController.popBackStack() }
                        }) { Text("Delete", color = Danger, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
fun InfoBox(text: String, showArrow: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .then(if (showArrow) Modifier.clickable { expanded = !expanded } else Modifier)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                color = PrimaryDark,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                maxLines = if (showArrow && !expanded) 1 else Int.MAX_VALUE
            )
            if (showArrow) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = TextLight
                )
            }
        }
    }
}
