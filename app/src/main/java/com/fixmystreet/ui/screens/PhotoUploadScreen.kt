package com.fixmystreet.ui.screens

import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fixmystreet.ui.components.BottomNavBar
import com.fixmystreet.ui.components.WavyBottomShape
import com.fixmystreet.ui.components.WavyTopShape
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// Custom top wavy shape that goes down on the left and stays high on the right
val PhotoUploadWavyTopShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
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
fun PhotoUploadScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val option1Url = "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?q=80&w=600&auto=format&fit=crop"
    val option2Url = "https://images.unsplash.com/photo-1584852959828-090c291bd670?q=80&w=600&auto=format&fit=crop"
    val option3Url = "https://images.unsplash.com/photo-1598463952796-088f11b2b8d0?q=80&w=600&auto=format&fit=crop"

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
        }
    }

    fun createImageFileUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.cacheDir
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(
            context,
            "com.fixmystreet.fileprovider",
            file
        )
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraUri != null) {
            selectedUri = cameraUri
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

    Scaffold(
        bottomBar = { BottomNavBar(navController, Screen.Report.route) },
        containerColor = Color.White
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
                // Blue Wavy Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(PhotoUploadWavyTopShape)
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
                            text = "Photo Upload",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Choose from gallery title
                    Text(
                        text = "Choose from gallery",
                        color = PrimaryDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Slot 1: Unsplash Image 1
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp)
                                .clickable { selectedUri = Uri.parse(option1Url) },
                            shape = RoundedCornerShape(12.dp),
                            border = if (selectedUri?.toString() == option1Url) BorderStroke(3.dp, PrimaryBlue) else null
                        ) {
                            AsyncImage(
                                model = option1Url,
                                contentDescription = "Gallery Option 1",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Slot 2: Unsplash Image 2
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp)
                                .clickable { selectedUri = Uri.parse(option2Url) },
                            shape = RoundedCornerShape(12.dp),
                            border = if (selectedUri?.toString() == option2Url) BorderStroke(3.dp, PrimaryBlue) else null
                        ) {
                            AsyncImage(
                                model = option2Url,
                                contentDescription = "Gallery Option 2",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Slot 3: Unsplash Image 3 + Circular Add Overlay / Custom Selected Image
                        val isCustomSelected = selectedUri != null && selectedUri?.toString() != option1Url && selectedUri?.toString() != option2Url
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp)
                                .clickable { galleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            border = if (isCustomSelected) BorderStroke(3.dp, PrimaryBlue) else null
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = if (isCustomSelected) selectedUri else option3Url,
                                    contentDescription = "Gallery Option 3",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Circle Plus Icon overlay in the center
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .align(Alignment.Center)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.8f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Pick Custom Photo",
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Capture a photo section sandwiched between separators
                    HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Border)

                    Row(
                        modifier = Modifier
                             .fillMaxWidth()
                             .clickable {
                                 val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                     context,
                                     android.Manifest.permission.CAMERA
                                 ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                 if (hasCameraPermission) {
                                     try {
                                         val uri = createImageFileUri()
                                         cameraUri = uri
                                         cameraLauncher.launch(uri)
                                     } catch (e: Exception) {
                                         // ignore
                                     }
                                 } else {
                                     cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                 }
                             }
                             .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Capture a photo",
                            color = PrimaryDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Capture Photo",
                            tint = PrimaryDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Border)

                    Spacer(modifier = Modifier.height(48.dp))

                    // Center-aligned Add Button matching mockup width
                    Button(
                        onClick = {
                            selectedUri?.let { uri ->
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("picked_photo_uri", uri.toString())
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .width(180.dp)
                            .height(48.dp)
                            .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                        enabled = selectedUri != null
                    ) {
                        Text("Add", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
