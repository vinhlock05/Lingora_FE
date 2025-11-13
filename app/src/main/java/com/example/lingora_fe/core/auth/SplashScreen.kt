package com.example.lingora_fe.core.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.lingora_fe.auth.domain.repository.AuthRepository
import com.example.lingora_fe.core.network.TokenManager

/**
 * Splash screen để validate token khi app khởi động
 */
@Composable
fun SplashScreen(
    navController: NavController,
    authRepository: AuthRepository,
    tokenManager: TokenManager,
    onValidationComplete: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
        
        TokenValidationHandler(
            authRepository = authRepository,
            tokenManager = tokenManager,
            onValidationComplete = onValidationComplete
        )
    }
}

