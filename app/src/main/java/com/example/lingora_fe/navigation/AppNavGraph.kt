package com.example.lingora_fe.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.lingora_fe.admin.navigator.AdminNavigator
import com.example.lingora_fe.auth.domain.repository.AuthRepository
import com.example.lingora_fe.auth.presentation.AuthScreen
import com.example.lingora_fe.auth.presentation.OTPScreen
import com.example.lingora_fe.core.auth.SplashScreen
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.user.adaptivetest.presentation.AdaptiveTestScreen
import com.example.lingora_fe.user.navigator.UserNavigator

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    authRepository: AuthRepository,
    tokenManager: TokenManager
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen - validate token before navigation
        composable(route = Route.SplashScreen.route) {
            SplashScreen(
                navController = navController,
                authRepository = authRepository,
                tokenManager = tokenManager,
                onValidationComplete = { destination ->
                    navController.navigate(destination) {
                        // Clear back stack including SplashScreen
                        popUpTo(Route.SplashScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        
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

            composable(route = Route.AdaptiveTest.route) {
                AdaptiveTestScreen(navController = navController)
            }
        }
        
        // Admin Navigation
        navigation(
            route = Route.AdminNavigation.route,
            startDestination = Route.AdminNavigator.route
        ) {
            composable(route = Route.AdminNavigator.route) {
                AdminNavigator(rootNavController = navController)
            }
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
        
        // Adaptive Test (accessible from root for navigation from UserNavigator)
        composable(route = Route.AdaptiveTest.route) {
            AdaptiveTestScreen(navController = navController)
        }
    }
}