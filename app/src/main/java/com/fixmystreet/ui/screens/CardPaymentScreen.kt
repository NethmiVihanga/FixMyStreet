package com.fixmystreet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fixmystreet.ui.components.BottomNavBar
import com.fixmystreet.ui.components.WavyBottomShape
import com.fixmystreet.ui.navigation.Screen
import com.fixmystreet.ui.theme.*
import com.fixmystreet.ui.viewmodel.DonationViewModel
import com.google.firebase.auth.FirebaseAuth

val CardPaymentWavyTopShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
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

// Visual Transformation to format Card Number visually with spaces without mutating raw digits
class CardNumberFilter : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        var out = ""
        for (i in text.text.indices) {
            out += text.text[i]
            if (i % 4 == 3 && i != 15) {
                out += " "
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset <= 4) return offset
                if (offset <= 8) return offset + 1
                if (offset <= 12) return offset + 2
                return (offset + 3).coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                return (offset - 3).coerceAtMost(text.text.length)
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

// Visual Transformation to format Expiry Date visually with a slash (MM/YY)
class ExpiryDateFilter : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        var out = ""
        for (i in text.text.indices) {
            out += text.text[i]
            if (i == 1) {
                out += "/"
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset <= 2) return offset
                return (offset + 1).coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset <= 2) return offset
                return (offset - 1).coerceAtMost(text.text.length)
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardPaymentScreen(
    navController: NavController,
    amount: Double,
    viewModel: DonationViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val isLoading by viewModel.isLoading.collectAsState()

    var cardHolderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Validation state
    var cardHolderError by remember { mutableStateOf(false) }
    var cardNumberError by remember { mutableStateOf(false) }
    var expiryDateError by remember { mutableStateOf(false) }
    var cvvError by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomNavBar(navController, Screen.Donate.route) },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Background wavy style matching other screens
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
                // Top Header wave
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(CardPaymentWavyTopShape)
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
                                text = "Card Payment",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    // Payment Amount Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .border(1.dp, Border, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total Donation Amount",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("$%.2f", amount),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Enter Card Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PrimaryDark,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Error Message Callout
                    errorMessage?.let {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .border(1.dp, Danger.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = it,
                                color = Danger,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Card Holder Name Input
                    OutlinedTextField(
                        value = cardHolderName,
                        onValueChange = {
                            cardHolderName = it
                            cardHolderError = false
                            errorMessage = null
                        },
                        label = { Text("Card Holder Name") },
                        isError = cardHolderError,
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Border,
                            errorBorderColor = Danger,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Card Number Input (Auto formatted using VisualTransformation to prevent cursor wrapping)
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length <= 16) {
                                cardNumber = digits
                                cardNumberError = false
                                errorMessage = null
                            }
                        },
                        label = { Text("Card Number") },
                        placeholder = { Text("XXXX XXXX XXXX XXXX") },
                        isError = cardNumberError,
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        visualTransformation = CardNumberFilter(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Border,
                            errorBorderColor = Danger,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Expiry Date and CVV side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Expiry Date Input (MM/YY)
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }
                                if (digits.length <= 4) {
                                    expiryDate = digits
                                    expiryDateError = false
                                    errorMessage = null
                                }
                            },
                            label = { Text("Expiry Date") },
                            placeholder = { Text("MM/YY") },
                            isError = expiryDateError,
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            visualTransformation = ExpiryDateFilter(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Border,
                                errorBorderColor = Danger,
                                focusedLabelColor = PrimaryBlue,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        // CVV Input (Exactly 3 digits)
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }
                                if (digits.length <= 3) {
                                    cvv = digits
                                    cvvError = false
                                    errorMessage = null
                                }
                            },
                            label = { Text("CVV") },
                            placeholder = { Text("123") },
                            isError = cvvError,
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Border,
                                errorBorderColor = Danger,
                                focusedLabelColor = PrimaryBlue,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Pay Now Button
                    Button(
                        onClick = {
                            var valid = true

                            // Validate Fields
                            if (cardHolderName.isBlank()) {
                                cardHolderError = true
                                valid = false
                            }
                            if (cardNumber.length != 16) {
                                cardNumberError = true
                                valid = false
                            }
                            if (expiryDate.length != 4 || (expiryDate.substring(0, 2).toIntOrNull() ?: 0) !in 1..12) {
                                expiryDateError = true
                                valid = false
                            }
                            if (cvv.length != 3) {
                                cvvError = true
                                valid = false
                            }

                            if (!valid) {
                                errorMessage = "Please correct the highlighted validation errors."
                                return@Button
                            }

                            // Mock Payment Check
                            val isHolderMatch = cardHolderName.trim().equals("John Doe", ignoreCase = true)
                            val isCardMatch = cardNumber == "1234567812345678"
                            val isCvvMatch = cvv == "123"
                            val isExpiryMatch = expiryDate.length == 4

                            if (isHolderMatch && isCardMatch && isCvvMatch && isExpiryMatch) {
                                if (user != null) {
                                    viewModel.submitDonation(
                                        userId = user.uid,
                                        userName = user.displayName ?: user.email?.substringBefore("@") ?: "Anonymous",
                                        amount = amount,
                                        message = "Donation of $${String.format(java.util.Locale.US, "%.2f", amount)} completed successfully via Card.",
                                        onSuccess = {
                                            showSuccessDialog = true
                                        }
                                    )
                                } else {
                                    errorMessage = "Error: User session not found. Please log in again."
                                }
                            } else {
                                errorMessage = "Payment failed: Invalid card details. Please use the mock payment account."
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
                            Text("Pay Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Success dialog after successful Firebase write
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.navigate(Screen.Donate.route) {
                    popUpTo(Screen.Donate.route) { inclusive = true }
                }
            },
            title = {
                Text(
                    text = "Payment Successful",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PrimaryDark
                )
            },
            text = {
                Text(
                    text = "Thank you for your generous donation of $${String.format(java.util.Locale.US, "%.2f", amount)}! Your payment has been processed successfully.",
                    fontSize = 15.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigate(Screen.Donate.route) {
                            popUpTo(Screen.Donate.route) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}
