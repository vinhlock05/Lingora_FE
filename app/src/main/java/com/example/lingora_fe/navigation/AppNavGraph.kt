package com.example.lingora_fe.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
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
            startDestination = Route.LoginScreen.route // TODO: Update with actual login route
        ) {
            // TODO: Add auth screens (Login, Register, OTP, ForgotPassword)
        }
        
        // Admin Navigation
        navigation(
            route = Route.AdminNavigation.route,
            startDestination = Route.AdminNavigator.route // TODO: Update with actual admin route
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