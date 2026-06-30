package com.fixmystreet.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fixmystreet.R
import com.fixmystreet.ui.components.BottomNavBar
import com.fixmystreet.ui.components.WavyBottomShape
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*
import com.fixmystreet.ui.viewmodel.DonationViewModel
import com.google.firebase.auth.FirebaseAuth

// Custom top wavy shape that goes down on the left and stays high on the right
val DonateWavyTopShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.25f)
    cubicTo(
        size.width * 0.75f, size.height * 0.2f,
        size.width * 0.35f, size.height * 0.95f,
        0f, size.height * 0.75f
    )
    close()
}

@Composable
fun DonateScreen(navController: NavController, viewModel: DonationViewModel = viewModel()) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedAmount by remember { mutableStateOf(10) }
    var otherAmount by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (user != null) viewModel.loadDonations(user.uid)
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, Screen.Donate.route) },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Light blue wavy background at bottom matching mockup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .align(Alignment.BottomCenter)
                    .clip(WavyBottomShape)
                    .background(PrimarySurface)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header area with overlapping illustration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    // Blue Wavy Header Background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(DonateWavyTopShape)
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
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Text(
                                text = "Donate to Us",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Hand holding coin illustration centered overlapping the wave
                    Card(
                        modifier = Modifier
                            .size(160.dp)
                            .align(Alignment.BottomCenter),
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        border = BorderStroke(4.dp, Color.White)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?q=80&w=600&auto=format&fit=crop",
                            contentDescription = "Donation Illustration",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "Support Our Free App!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = PrimaryDark,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Help us improve and grow by donating to our development fund",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Preset Amount Selector Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        listOf(5, 10, 20).forEach { amount ->
                            val isSelected = selectedAmount == amount
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryDark else PrimarySurface)
                                    .clickable {
                                        selectedAmount = amount
                                        otherAmount = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$$amount",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isSelected) 26.sp else 20.sp,
                                    color = if (isSelected) Color.White else PrimaryDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Other Amount Row with Left & Right Dividers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Border)
                        Text(
                            text = "Other Amount",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryDark,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Border)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom input field with leading currency prefix block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, Border, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(48.dp)
                                .background(Color(0xFFE9EFF8))
                                .border(1.dp, Border, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryDark)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        androidx.compose.foundation.text.BasicTextField(
                            value = otherAmount,
                            onValueChange = {
                                otherAmount = it
                                if (it.isNotBlank()) selectedAmount = -1
                            },
                            modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                color = PrimaryDark,
                                fontWeight = FontWeight.Bold
                            ),
                            decorationBox = { innerTextField ->
                                if (otherAmount.isEmpty()) {
                                    Text("Enter Amount", color = TextLight, fontSize = 16.sp)
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Why Donate Centered Section Title
                    Text(
                        text = "Why Donate?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PrimaryDark,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Branded Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        WhyDonateCard(painterResource(R.drawable.ic_features), "Enhance\nFeatures")
                        WhyDonateCard(painterResource(R.drawable.ic_bug), "Bug Fixes\n& Issues")
                        WhyDonateCard(painterResource(R.drawable.ic_rocket), "Keep It\nFree")
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Donate Now Button
                    Button(
                        onClick = {
                            val amt = if (selectedAmount > 0) selectedAmount.toDouble()
                                      else otherAmount.toDoubleOrNull() ?: 0.0
                            if (user != null && amt > 0) {
                                navController.navigate(Screen.CardPayment.createRoute(amt))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Donate Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.WhyDonateCard(painter: Painter, label: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDark,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}
