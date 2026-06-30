package com.fixmystreet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fixmystreet.R
import com.fixmystreet.ui.components.BottomNavBar
import com.fixmystreet.ui.components.WavyBottomShape
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*

// Custom top wavy shape that goes down on the left and stays high on the right
val AboutUsWavyTopShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
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
fun AboutUsScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController, Screen.Profile.route) },
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
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header and Text Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // Blue Wavy Shape Background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(AboutUsWavyTopShape)
                            .background(PrimaryBlue)
                    ) {
                        // Title and Back button
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
                                text = "About Us",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Text "Find more information about us." aligned to right & bottom of this region
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 12.dp, end = 24.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Find more\ninformation\nabout\nus.",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark,
                            textAlign = TextAlign.End,
                            lineHeight = 42.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Social Media and Contact List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ContactRow(
                        painter = painterResource(id = R.drawable.ic_email),
                        text = "fixmystreet@gmail.com"
                    )
                    ContactRow(
                        painter = painterResource(id = R.drawable.ic_youtube),
                        text = "FixMyStreet"
                    )
                    ContactRow(
                        painter = painterResource(id = R.drawable.ic_facebook),
                        text = "FixMyStreet"
                    )
                    ContactRow(
                        painter = painterResource(id = R.drawable.ic_instagram),
                        text = "FixMyStreet"
                    )
                    ContactRow(
                        painter = painterResource(id = R.drawable.ic_whatsapp),
                        text = "0761234567"
                    )
                }
            }
        }
    }
}

@Composable
fun ContactRow(
    painter: Painter,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )
    }
}
