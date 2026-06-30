package com.fixmystreet.ui.screens

import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fixmystreet.ui.components.BottomNavBar
import com.fixmystreet.ui.components.WavyTopShape
import com.fixmystreet.ui.components.WavyBottomShape
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*
import com.fixmystreet.ui.viewmodel.ReportState
import com.fixmystreet.ui.viewmodel.ReportViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

// Custom top wavy shape that goes down on the left and stays high on the right
val ReportWavyTopShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(navController: NavController, initialLat: Double? = null, initialLng: Double? = null, viewModel: ReportViewModel = viewModel()) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var lat by rememberSaveable { mutableStateOf(initialLat) }
    var lng by rememberSaveable { mutableStateOf(initialLng) }
    var category by rememberSaveable { mutableStateOf("Pothole") }
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showDescriptionFull by rememberSaveable { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val context = androidx.compose.ui.platform.LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        photoUri = uri
    }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    fun createImageFileUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(Date())
        val storageDir = context.cacheDir
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(
            context,
            "com.fixmystreet.fileprovider",
            file
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraUri != null) {
            photoUri = cameraUri
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                val uri = createImageFileUri()
                cameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    LaunchedEffect(initialLat, initialLng) {
        if (location.isBlank() && initialLat != null && initialLng != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(initialLat, initialLng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val name = address.locality ?: address.subAdminArea ?: address.getAddressLine(0) ?: "${initialLat}, ${initialLng}"
                        location = name
                    } else {
                        location = "${initialLat}, ${initialLng}"
                    }
                } catch(e: Exception) {
                    location = "${initialLat}, ${initialLng}"
                }
            }
        }
    }

    // Read location picked from LocationPickerScreen
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.let { handle ->
            handle.get<Double>("picked_lat")?.let { pickedLat ->
                handle.get<Double>("picked_lng")?.let { pickedLng ->
                    lat = pickedLat
                    lng = pickedLng
                    handle.remove<Double>("picked_lat")
                    handle.remove<Double>("picked_lng")
                }
            }
            handle.get<String>("picked_address")?.let { addr ->
                if (addr.isNotBlank()) {
                    location = addr
                    handle.remove<String>("picked_address")
                }
            }
            handle.get<String>("picked_photo_uri")?.let { uriStr ->
                if (uriStr.isNotBlank()) {
                    photoUri = Uri.parse(uriStr)
                    handle.remove<String>("picked_photo_uri")
                }
            }
        }
    }

    LaunchedEffect(state) {
        when (state) {
            is ReportState.Success -> {
                android.widget.Toast.makeText(context, "🛠️ Issue Reported successfully!", android.widget.Toast.LENGTH_LONG).show()
                navController.previousBackStackEntry?.savedStateHandle?.set("refresh_issues", true)
                viewModel.resetState()
                navController.popBackStack()
            }
            is ReportState.Error -> {
                android.widget.Toast.makeText(context, "Error: ${(state as ReportState.Error).message}", android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, Screen.Report.route) },
        containerColor = Color.White
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                // Top Blue Wavy Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(ReportWavyTopShape)
                        .background(PrimaryBlue)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = "Report Issue",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    // Issue Title Label + Field
                    Text("Issue Title", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Enter issue title", color = TextLight) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Background,
                            focusedContainerColor = Background,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Location Label + Field
                    Text("Location", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = { Text("Enter or pick from map", color = TextLight) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Background,
                            focusedContainerColor = Background,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Pick on Map",
                                tint = PrimaryDark,
                                modifier = Modifier.clickable {
                                    navController.navigate(Screen.LocationPicker.route)
                                }
                            )
                        },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Description Label + Field
                    Text("Description", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Describe the issue...", color = TextLight) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Background,
                            focusedContainerColor = Background,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Add Photos Clickable Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Screen.PhotoUpload.route)
                            }
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add Photos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryDark
                        )
                        if (photoUri != null) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { photoUri = null } // Click to remove
                            ) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = "Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp)
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Add Photos",
                                tint = PrimaryDark,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Border, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    if (state is ReportState.Loading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    val finalUserId = user?.uid ?: "mockUser"
                                    val finalUserName = user?.let { it.displayName ?: it.email?.substringBefore("@") } ?: "Anonymous"
                                    
                                    if (title.isBlank()) {
                                        android.widget.Toast.makeText(context, "Please enter a title", android.widget.Toast.LENGTH_SHORT).show()
                                    } else if (location.isBlank()) {
                                        android.widget.Toast.makeText(context, "Please enter or pick a location", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.submitReport(
                                            userId = finalUserId,
                                            userName = finalUserName,
                                            title = title,
                                            description = description,
                                            category = category,
                                            location = location,
                                            lat = lat,
                                            lng = lng,
                                            photoUri = photoUri
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                            ) {
                                Text("Submit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
