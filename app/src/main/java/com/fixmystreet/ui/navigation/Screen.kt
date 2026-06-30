package com.fixmystreet.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Report : Screen("report?lat={lat}&lng={lng}") {
        fun createRoute(lat: Double, lng: Double) = "report?lat=$lat&lng=$lng"
    }
    object Detail : Screen("detail/{issueId}") {
        fun createRoute(issueId: String) = "detail/$issueId"
    }
    object Comments : Screen("comments/{issueId}") {
        fun createRoute(issueId: String) = "comments/$issueId"
    }
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object AboutUs : Screen("aboutus")
    object LocationPicker : Screen("locationpicker")
    object PhotoUpload : Screen("photoupload")
    object Donate : Screen("donate")
    object Updates : Screen("updates")
    object CardPayment : Screen("cardpayment/{amount}") {
        fun createRoute(amount: Double) = "cardpayment/$amount"
    }
    object EditProfile : Screen("editprofile")
    object ForgotPassword : Screen("forgotpassword")
    object VerifyOtp : Screen("verifyotp/{email}") {
        fun createRoute(email: String) = "verifyotp/$email"
    }
    object ResetPassword : Screen("resetpassword/{email}") {
        fun createRoute(email: String) = "resetpassword/$email"
    }
}
