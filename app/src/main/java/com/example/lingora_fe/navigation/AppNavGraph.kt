package com.example.lingora_fe.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.lingora_fe.auth.presentation.AuthScreen
import com.example.lingora_fe.auth.presentation.OTPScreen
import com.example.lingora_fe.navigation.Route.Companion.otpScreen
import com.example.lingora_fe.user.navigator.UserNavigator

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Navigation
        navigation(
            route = Route.AuthNavigation.route,
            startDestination = Route.AuthScreen.route
        ) {
            composable(route = Route.AuthScreen.route) {
                AuthScreen(navController = navController, initialTab = "login")
            }
            
            // Deprecated routes - kept for backwards compatibility
            composable(route = Route.LoginScreen.route) {
                AuthScreen(navController = navController, initialTab = "login")
            }
            
            composable(route = Route.RegisterScreen.route) {
                AuthScreen(navController = navController, initialTab = "register")
            }
            
            composable(
                route = Route.OTPScreen.route,
                arguments = listOf(
                    navArgument("email") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                OTPScreen(navController = navController, email = email)
            }
        }
        
        // Admin Navigation
        navigation(
            route = Route.AdminNavigation.route,
            startDestination = Route.AdminNavigator.route
        ) {
            // TODO: Add admin screens
        }
        
        // User Navigation
        navigation(
            route = Route.UserNavigation.route,
            startDestination = Route.UserNavigator.route
        ) {
            composable(route = Route.UserNavigator.route) {
                UserNavigator(rootNavController = navController)
            }
        }
    }
}