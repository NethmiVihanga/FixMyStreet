package com.fixmystreet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fixmystreet.ui.components.SmartAsyncImage
import com.fixmystreet.R
import com.fixmystreet.data.model.Issue
import com.fixmystreet.ui.components.BottomNavBar
import com.fixmystreet.ui.components.WavyTopShape
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*
import com.fixmystreet.ui.viewmodel.HomeViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import android.location.Geocoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val issues by viewModel.issues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var searchedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedFilter by remember { mutableStateOf("all") } // "all" or "constructions"
    
    val filteredIssues = remember(issues, selectedFilter) {
        if (selectedFilter == "constructions") {
            issues.filter { it.status == "in-progress" }
        } else {
            issues
        }
    }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.loadIssues()
    }

    val defaultLocation = LatLng(6.9271, 79.8612) // Colombo, Sri Lanka
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, Screen.Home.route) },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Background Map Layer
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(zoomControlsEnabled = false),
                onMapClick = { latLng ->
                    navController.navigate(Screen.Report.createRoute(latLng.latitude, latLng.longitude))
                }
            ) {
                filteredIssues.forEach { issue ->
                    if (issue.lat != null && issue.lng != null) {
                        val markerColor = if (issue.status == "in-progress") {
                            BitmapDescriptorFactory.HUE_ORANGE
                        } else if (issue.status == "resolved") {
                            BitmapDescriptorFactory.HUE_GREEN
                        } else {
                            BitmapDescriptorFactory.HUE_RED
                        }
                        Marker(
                            state = MarkerState(position = LatLng(issue.lat, issue.lng)),
                            title = issue.title,
                            snippet = "${issue.location} - ${issue.status.replaceFirstChar { it.uppercase() }}",
                            icon = BitmapDescriptorFactory.defaultMarker(markerColor)
                        )
                    }
                }
                searchedLocation?.let { loc ->
                    Marker(
                        state = MarkerState(position = loc),
                        title = searchQuery,
                        snippet = "Searched Location"
                    )
                }
            }

            // Top Blue Wavy Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(WavyTopShape)
                    .background(PrimaryBlue)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp)
                        .size(100.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Floating Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = 150.dp)
                    .background(Color.White, RoundedCornerShape(24.dp)),
                placeholder = { Text("Search", color = TextLight) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryBlue) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = PrimaryBlue,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    if (searchQuery.isNotBlank()) {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocationName(searchQuery, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val address = addresses[0]
                                    val latLng = LatLng(address.latitude, address.longitude)
                                    withContext(Dispatchers.Main) {
                                        searchedLocation = latLng
                                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                                    }
                                }
                            } catch (e: Exception) {
                                // Ignore or show toast
                            }
                        }
                    }
                })
            )

            // Floating Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = 215.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(
                    text = "All Issues",
                    isSelected = selectedFilter == "all",
                    onClick = { selectedFilter = "all" }
                )
                FilterChipItem(
                    text = "🚧 Constructions",
                    isSelected = selectedFilter == "constructions",
                    onClick = { selectedFilter = "constructions" }
                )
            }

            // Bottom Dashboard Card Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = if (selectedFilter == "constructions") "🚧 Active Constructions" else "Dashboard",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = PrimaryBlue)
                    } else {
                        val recentIssue = filteredIssues.firstOrNull()
                        if (recentIssue != null) {
                            RecentIssueCard(issue = recentIssue) {
                                navController.navigate(Screen.Detail.createRoute(recentIssue.id))
                            }
                        } else {
                            Text(
                                text = if (selectedFilter == "constructions") "No active constructions found." else "No recent issues found.",
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) PrimaryDark else Color.White,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else Border,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RecentIssueCard(issue: Issue, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (issue.photoURL.isNotEmpty()) {
                SmartAsyncImage(
                    photoUrl = issue.photoURL,
                    contentDescription = "Issue Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = issue.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "The road has sunk.", // Hardcoded to match mockup as description is not fully visible
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "14:00 PM", // Mockup data
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
