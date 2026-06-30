package com.fixmystreet.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fixmystreet.ui.components.WavyTopShape
import com.fixmystreet.ui.theme.*
import com.fixmystreet.ui.viewmodel.DetailViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    navController: NavController,
    issueId: String,
    viewModel: DetailViewModel = viewModel()
) {
    val comments by viewModel.comments.collectAsState()
    var newCommentText by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(issueId) {
        viewModel.loadIssueDetails(issueId)
    }

    Scaffold(
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top Header
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
                        text = "Comments",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 48.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Comments List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (comments.isEmpty()) {
                    item {
                        Text(
                            text = "No comments yet. Be the first to comment!",
                            color = TextSecondary,
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                items(comments) { comment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = comment.userName.ifEmpty { "User" },
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDark,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                val dateStr = comment.createdAt?.toDate()?.let { sdf.format(it) } ?: ""
                                Text(
                                    text = dateStr,
                                    color = TextLight,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = comment.text,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Add Comment Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("Write a comment...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Border,
                        focusedBorderColor = PrimaryBlue
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        if (newCommentText.isNotBlank() && currentUser != null) {
                            viewModel.addComment(
                                issueId = issueId,
                                userId = currentUser.uid,
                                userName = currentUser.email?.substringBefore("@") ?: "Anonymous",
                                text = newCommentText.trim()
                            )
                            newCommentText = ""
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Post")
                }
            }
        }
    }
}
