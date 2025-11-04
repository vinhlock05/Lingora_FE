package com.example.lingora_fe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.core.ui.theme.Lingora_FETheme
import com.example.lingora_fe.navigation.AppNavGraph
import com.example.lingora_fe.navigation.Route
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var tokenManager: TokenManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check if user is already logged in and get user role
        val accessToken = tokenManager.getAccessToken()
        val userRole = tokenManager.getUserRole()
        
        val startDestination = if (accessToken != null) {
            // Route based on user role
            if (userRole == "ADMIN") {
                Route.AdminNavigation.route
            } else {
                Route.UserNavigation.route
            }
        } else {
            Route.AuthNavigation.route
        }
        
        setContent {
            Lingora_FETheme(darkTheme = false) {
                val isSystemInDarkMode = isSystemInDarkTheme()
                val systemUiColor = rememberSystemUiController()
                SideEffect {
                    systemUiColor.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = !isSystemInDarkMode
                    )
                }
                val navController = rememberNavController()
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                ) {
                    AppNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}