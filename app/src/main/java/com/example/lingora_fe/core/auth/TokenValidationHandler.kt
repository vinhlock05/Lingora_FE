package com.example.lingora_fe.core.auth

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.lingora_fe.auth.domain.repository.AuthRepository
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.navigation.Route

@Composable
fun TokenValidationHandler(
    authRepository: AuthRepository,
    tokenManager: TokenManager,
    onValidationComplete: (String) -> Unit
) {
    var validationAttempted by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (validationAttempted) return@LaunchedEffect
        validationAttempted = true
        
        val accessToken = tokenManager.getAccessToken()
        
        if (accessToken == null) {
            // Không có token, redirect về login
            Log.d("TokenValidationHandler", "No token found, redirecting to login")
            onValidationComplete(Route.AuthNavigation.route)
            return@LaunchedEffect
        }
        
        // Có token, validate bằng cách gọi getProfile
        // Nếu token expired, TokenAuthenticator sẽ tự động refresh
        // Nếu refresh fail, getProfile sẽ fail và chúng ta sẽ redirect về login        

        try {
            val result = authRepository.getProfile(accessToken)
            result.fold(
                ifLeft = { failure ->
                    // Token invalid hoặc refresh fail
                    Log.e("TokenValidationHandler", "Token validation failed: ${failure.message}")
                    // Clear tokens và redirect về login
                    tokenManager.clearTokens()
                    onValidationComplete(Route.AuthNavigation.route)
                },
                ifRight = { user ->
                    // Token valid (có thể đã được refresh)
                    // Check if user is verified (status = ACTIVE)
                    if (user.status != "ACTIVE") {
                        // User has token but hasn't verified email yet
                        // Redirect to OTP screen
                        Log.d("TokenValidationHandler", "User not verified (status: ${user.status}), redirecting to OTP")
                        onValidationComplete(Route.otpScreen(user.email))
                        return@fold
                    }
                    
                    // User is verified, proceed to app
                    val newToken = tokenManager.getAccessToken()
                    Log.d("TokenValidationHandler", "Token validation successful, token length: ${newToken?.length}")
                    val activeRole = tokenManager.getActiveRole() ?: tokenManager.getUserRole()
                    val destination = if (activeRole == "ADMIN") {
                        Route.AdminNavigation.route
                    } else {
                        Route.UserNavigation.route
                    }
                    onValidationComplete(destination)
                }
            )
        } catch (e: Exception) {
            Log.e("TokenValidationHandler", "Token validation exception: ${e.message}", e)
            tokenManager.clearTokens()
            onValidationComplete(Route.AuthNavigation.route)
        }
    }
}

