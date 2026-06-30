package com.fixmystreet.ui.screens

import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fixmystreet.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var searchQuery by remember { mutableStateOf("") }
    var resolvedAddress by remember { mutableStateOf("Move the map to select a location") }
    var isGeocoding by remember { mutableStateOf(false) }
    var geocodeJob by remember { mutableStateOf<Job?>(null) }

    val defaultLocation = LatLng(6.9271, 79.8612)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14f)
    }

    // When camera stops moving, reverse geocode the center
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val center = cameraPositionState.position.target
            geocodeJob?.cancel()
            geocodeJob = coroutineScope.launch {
                delay(400) // debounce
                isGeocoding = true
                withContext(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(center.latitude, center.longitude, 1)
                        val name = addresses?.firstOrNull()?.let {
                            it.thoroughfare?.let { road -> "$road, ${it.locality ?: it.subAdminArea ?: ""}" }
                                ?: it.locality
                                ?: it.subAdminArea
                                ?: it.getAddressLine(0)
                        } ?: "${String.format("%.5f", center.latitude)}, ${String.format("%.5f", center.longitude)}"
                        withContext(Dispatchers.Main) {
                            resolvedAddress = name
                            isGeocoding = false
                        }
                    } catch (e: Exception) {
                        val center2 = cameraPositionState.position.target
                        withContext(Dispatchers.Main) {
                            resolvedAddress = "${String.format("%.5f", center2.latitude)}, ${String.format("%.5f", center2.longitude)}"
                            isGeocoding = false
                        }
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Search Bar on blue background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlue)
                .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search location...", color = TextLight) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
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
                                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                                        }
                                    }
                                } catch (e: Exception) { /* ignore */ }
                            }
                        }
                    })
                )
            }
        }

        // Map with center-pin overlay
        Box(modifier = Modifier.weight(1f)) {
            // The Map — moves freely
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(zoomControlsEnabled = true, scrollGesturesEnabled = true)
            )

            // Fixed center pin overlay (stays at center while map moves underneath)
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Shadow / label bubble
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (isGeocoding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = PrimaryBlue,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = resolvedAddress,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryDark,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                // The big red pin icon
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Pin",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(48.dp).offset(y = (-4).dp) // slight overlap with bubble
                )
                // Pin shadow dot
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 4.dp)
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(50))
                )
            }

            // Hint text at bottom of map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(
                    text = "📍 Drag the map to move the pin",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Bottom Address Bar + Add Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Show selected address
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isGeocoding) "Finding location..." else resolvedAddress,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
            }

            Button(
                onClick = {
                    val center = cameraPositionState.position.target
                    navController.previousBackStackEntry?.savedStateHandle?.set("picked_lat", center.latitude)
                    navController.previousBackStackEntry?.savedStateHandle?.set("picked_lng", center.longitude)
                    navController.previousBackStackEntry?.savedStateHandle?.set("picked_address", resolvedAddress)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                enabled = !isGeocoding
            ) {
                Text("Add", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}
