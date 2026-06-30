package com.fixmystreet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fixmystreet.ui.screens.*

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(route = Screen.Auth.route) {
            AuthScreen(navController = navController)
        }
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(
            route = Screen.Report.route,
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("lng") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()
            ReportScreen(navController = navController, initialLat = lat, initialLng = lng)
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("issueId") { type = NavType.StringType })
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getString("issueId") ?: return@composable
            IssueDetailScreen(navController = navController, issueId = issueId)
        }
        composable(
            route = Screen.Comments.route,
            arguments = listOf(navArgument("issueId") { type = NavType.StringType })
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getString("issueId") ?: return@composable
            CommentsScreen(navController = navController, issueId = issueId)
        }
        composable(route = Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
        composable(route = Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(route = Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
        composable(route = Screen.AboutUs.route) {
            AboutUsScreen(navController = navController)
        }
        composable(route = Screen.LocationPicker.route) {
            LocationPickerScreen(navController = navController)
        }
        composable(route = Screen.PhotoUpload.route) {
            PhotoUploadScreen(navController = navController)
        }
        composable(route = Screen.Donate.route) {
            DonateScreen(navController = navController)
        }
        composable(route = Screen.Updates.route) {
            IssueListScreen(navController = navController)
        }
        composable(
            route = Screen.CardPayment.route,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val amountStr = backStackEntry.arguments?.getString("amount") ?: "0.0"
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            CardPaymentScreen(navController = navController, amount = amount)
        }
        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(
            route = Screen.VerifyOtp.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyOtpScreen(navController = navController, email = email)
        }
        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(navController = navController, email = email)
        }
    }
}
