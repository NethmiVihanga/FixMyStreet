package com.fixmystreet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fixmystreet.R
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.PrimaryBlue
import com.fixmystreet.ui.theme.SecondaryGreen
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val imageRes: Int,
    val subtitle: String,
    val buttonText: String,
    val isLastPage: Boolean
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = listOf(
        OnboardingPageData(
            imageRes = R.drawable.onboarding_1,
            subtitle = "Report infrastructure issues easily",
            buttonText = "Next",
            isLastPage = false
        ),
        OnboardingPageData(
            imageRes = R.drawable.onboarding_2,
            subtitle = "Add photos and exact location",
            buttonText = "Next",
            isLastPage = false
        ),
        OnboardingPageData(
            imageRes = R.drawable.onboarding_3,
            subtitle = "Authorities take action quickly",
            buttonText = "Get Started",
            isLastPage = true
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size + 1 }) // +1 for the Splash page
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { position ->
            if (position == 0) {
                // Page 0: The Splash/Logo Page
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
                                .size(150.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Report Today. Repair Tomorrow.",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Three dots at bottom
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 64.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4B7BEB)))
                    }
                }
            } else {
                // Pages 1, 2, 3: Onboarding Pages (Offset by 1)
                val pageIndex = position - 1
                val page = pages[pageIndex]
                
                Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
                    // Draw wavy backgrounds
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        // Top wavy shape (Dark Blue) - Shallower to avoid overlapping title text
                        val topPath = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(width, 0f)
                            lineTo(width, height * 0.08f)
                            cubicTo(
                                width * 0.7f, height * 0.02f,
                                width * 0.5f, height * 0.18f,
                                width * 0.25f, height * 0.12f
                            )
                            cubicTo(
                                width * 0.05f, height * 0.08f,
                                width * 0.25f, height * 0.22f,
                                0f, height * 0.25f
                            )
                            close()
                        }
                        drawPath(path = topPath, color = PrimaryBlue)

                        // Bottom wavy shape (Light grayish blue)
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

                    // Content
                    OnboardingPage(
                        page = page,
                        pageIndex = pageIndex
                    )

                    // Back Arrow (scrolls back to previous page)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(top = 48.dp, start = 24.dp)
                            .size(28.dp)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                    )

                    // Bottom Button
                    Button(
                        onClick = {
                            if (page.isLastPage) {
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 64.dp, start = 32.dp, end = 32.dp)
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (page.isLastPage) SecondaryGreen else PrimaryBlue
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = page.buttonText, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(page: OnboardingPageData, pageIndex: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 130.dp, bottom = 140.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Title logic based on page index to perfectly match screenshots
        val titleText = buildAnnotatedString {
            when (pageIndex) {
                0 -> {
                    // Line 1: "Report" (Green) + " Infrastructure" (Blue)
                    withStyle(style = SpanStyle(color = SecondaryGreen)) { append("Report ") }
                    withStyle(style = SpanStyle(color = PrimaryBlue)) { append("Infrastructure\n") }
                    // Line 2: "Issues" (Green) + " Easily" (Blue)
                    withStyle(style = SpanStyle(color = SecondaryGreen)) { append("Issues ") }
                    withStyle(style = SpanStyle(color = PrimaryBlue)) { append("Easily") }
                }
                1 -> {
                    // Line 1: "Add" (Green) + " Photos and" (Blue)
                    withStyle(style = SpanStyle(color = SecondaryGreen)) { append("Add ") }
                    withStyle(style = SpanStyle(color = PrimaryBlue)) { append("Photos and\n") }
                    // Line 2: "Exact" (Green) + " Location" (Blue)
                    withStyle(style = SpanStyle(color = SecondaryGreen)) { append("Exact ") }
                    withStyle(style = SpanStyle(color = PrimaryBlue)) { append("Location") }
                }
                2 -> {
                    // Line 1: "Authorities" (Green) + " Take" (Blue)
                    withStyle(style = SpanStyle(color = SecondaryGreen)) { append("Authorities ") }
                    withStyle(style = SpanStyle(color = PrimaryBlue)) { append("Take\n") }
                    // Line 2: "Action" (Green) + " Quickly" (Blue)
                    withStyle(style = SpanStyle(color = SecondaryGreen)) { append("Action ") }
                    withStyle(style = SpanStyle(color = PrimaryBlue)) { append("Quickly") }
                }
            }
        }
        
        Text(
            text = titleText,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp,
            modifier = Modifier.padding(top = 32.dp)
        )

        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = "Illustration",
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Fit
        )

        Text(
            text = page.subtitle,
            color = PrimaryBlue.copy(alpha = 0.8f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
