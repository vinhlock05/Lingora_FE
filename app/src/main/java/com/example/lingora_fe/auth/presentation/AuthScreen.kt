package com.example.lingora_fe.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.R
import com.example.lingora_fe.core.ui.theme.*
import com.example.lingora_fe.navigation.Route
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import android.widget.Toast
import android.app.Activity

@Composable
fun AuthScreen(
    navController: NavController,
    initialTab: String = "login",
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    
    // Google Auth Setup
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Use the injected client from ViewModel instead of creating a new one
    val googleAuthClient = viewModel.googleAuthClient

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            android.util.Log.d("AuthScreen", "Google Sign-In Result Code: ${result.resultCode}")
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch {
                    val signInResult = googleAuthClient.signInWithIntent(
                        intent = result.data ?: return@launch
                    )
                    signInResult.idToken?.let { token ->
                        android.util.Log.d("AuthScreen", "Got ID Token, calling ViewModel")
                        viewModel.googleLogin(token)
                    } ?: run {
                        android.util.Log.e("AuthScreen", "No ID Token found in result")
                        Toast.makeText(
                            context,
                            "Google Sign In Failed: ${signInResult.errorMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                android.util.Log.e("AuthScreen", "Google Sign-In Failed or Cancelled. Result Code: ${result.resultCode}")
                // Common failure: 0 (CANCELED) often means SHA-1 mismatch or network issue
                if (result.resultCode == 0) {
                     android.util.Log.e("AuthScreen", "Possible causes: SHA-1 mismatch, missing internet, or user cancelled.")
                }
            }
        }
    )
    
    var activeTab by remember { mutableStateOf(initialTab) }
    
    // Login states
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    
    // Register states
    var registerEmail by remember { mutableStateOf("") }
    var registerUsername by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerConfirmPassword by remember { mutableStateOf("") }
    var registerPasswordVisible by remember { mutableStateOf(false) }
    var registerConfirmPasswordVisible by remember { mutableStateOf(false) }

    var registrationEmail by remember { mutableStateOf<String?>(null) }
    
    // Track previous auth state to avoid re-navigation
    var previousAuthState by remember { mutableStateOf(authState.isAuthenticated) }
    
    LaunchedEffect(authState.isAuthenticated, registrationEmail, authState.user, authState.token, authState.isLoading) {
        // Check if registration was successful (has user, token, but NOT authenticated yet)
        // This means user registered but hasn't verified OTP yet
        if (registrationEmail != null && 
            authState.user != null && 
            authState.token != null && 
            !authState.isAuthenticated &&
            !authState.isLoading &&
            activeTab == "register") {
            // Registration complete - navigate to OTP screen
            // Clear registrationEmail to prevent re-navigation
            val email = registrationEmail
            registrationEmail = null
            navController.navigate(Route.otpScreen(email!!))
            return@LaunchedEffect
        }
        
        // Check if login was successful but user is NOT verified (status != ACTIVE)
        // This happens when user logs in but hasn't verified their email
        if (authState.user != null && 
            authState.token != null && 
            !authState.isAuthenticated &&
            !authState.isLoading &&
            activeTab == "login" &&
            authState.user?.status != "ACTIVE") {
            // User logged in but not verified - navigate to OTP screen
            navController.navigate(Route.otpScreen(authState.user!!.email))
            return@LaunchedEffect
        }
        
        // Login/Google Sign-In successful - navigate when authenticated
        if (authState.isAuthenticated && 
            authState.user != null && 
            authState.token != null && 
            !authState.isLoading &&
            !previousAuthState) {  // Only navigate when transitioning from not authenticated -> authenticated
            // Login successful - check proficiency first
            val tokenManager = viewModel.tokenManager
            val activeRole = tokenManager.getActiveRole() ?: tokenManager.getUserRole()
            
            previousAuthState = true
            
            // Check if user is ADMIN - admins don't need proficiency test
            if (activeRole == "ADMIN") {
                navController.navigate(Route.AdminNavigation.route) {
                    popUpTo(Route.AuthNavigation.route) { inclusive = true }
                }
            } else {
                // For LEARNER role, check proficiency
                val userProficiency = authState.user?.proficiency
                if (userProficiency.isNullOrBlank()) {
                    // User has no proficiency - redirect to adaptive test
                    navController.navigate(Route.AdaptiveTest.route) {
                        popUpTo(Route.AuthNavigation.route) { inclusive = true }
                    }
                } else {
                    // User has proficiency - navigate to UserNavigation
                    navController.navigate(Route.UserNavigation.route) {
                        popUpTo(Route.AuthNavigation.route) { inclusive = true }
                    }
                }
            }
        }
        
        // Update previous auth state
        if (!authState.isAuthenticated) {
            previousAuthState = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize().imePadding()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Box to contain Card with overlapping Logo
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Card Container with Shadow
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 40.dp)
                        .shadow(
                            elevation = 15.dp,
                            spotColor = Color(0x1A000000),
                            ambientColor = Color(0x1A000000),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xCCFFFFFF)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Welcome Text
                        Text(
                            text = "Chào mừng đến với",
                            fontSize = 20.sp,
                            color = Color(0xFF00A63E),
                            fontFamily = ArimoFontFamily
                        )

                        // LINGORA Title with Gradient Colors
                        Text(
                            text = "LINGORA",
                            fontSize = 48.sp,
                            fontWeight = FontWeight(400),
                            fontFamily = TitanOneFontFamily,
                            brush = Brush.linearGradient(
                                colors = listOf(GradientStart700, GradientEnd700)
                            ),
                            letterSpacing = 3.sp,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    // Tab Buttons with Gradient
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { activeTab = "login" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (activeTab == "login") {
                                        Modifier.background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(GradientStart, GradientEnd)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    } else {
                                        Modifier.background(
                                            color = Color.White,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                )
                        ) {
                            Text(
                                text = "Đăng nhập",
                                color = if (activeTab == "login") Color.White else NavBarText,
                                fontFamily = ArimoFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = { activeTab = "register" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (activeTab == "register") {
                                        Modifier.background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(GradientStart, GradientEnd)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    } else {
                                        Modifier.background(
                                            color = Color.White,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                )
                        ) {
                            Text(
                                text = "Đăng ký",
                                color = if (activeTab == "register") Color.White else NavBarText,
                                fontFamily = ArimoFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Content based on active tab
                    when (activeTab) {
                        "login" -> LoginContent(
                            identifier = loginIdentifier,
                            onIdentifierChange = { loginIdentifier = it },
                            password = loginPassword,
                            onPasswordChange = { loginPassword = it },
                            passwordVisible = loginPasswordVisible,
                            onPasswordVisibilityChange = { loginPasswordVisible = !loginPasswordVisible },
                            onLoginClick = {
                                if (loginIdentifier.isNotEmpty() && loginPassword.isNotEmpty()) {
                                    viewModel.login(loginIdentifier, loginPassword)
                                }
                            },
                            
                            onForgotPasswordClick = {
                                navController.navigate(Route.ForgotPassword.route)
                            },
                            onGoogleLoginClick = {
                                scope.launch {
                                    val signInIntentSender = googleAuthClient.signIn()
                                    if(signInIntentSender != null) {
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender
                                            ).build()
                                        )
                                    } else {
                                        Toast.makeText(context, "Google Sign In Init Failed", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            isLoading = authState.isLoading,
                            error = authState.error
                        )
                        "register" -> RegisterContent(
                            username = registerUsername,
                            onUsernameChange = { registerUsername = it },
                            email = registerEmail,
                            onEmailChange = { registerEmail = it },
                            password = registerPassword,
                            onPasswordChange = { registerPassword = it },
                            confirmPassword = registerConfirmPassword,
                            onConfirmPasswordChange = { registerConfirmPassword = it },
                            passwordVisible = registerPasswordVisible,
                            onPasswordVisibilityChange = { registerPasswordVisible = !registerPasswordVisible },
                            confirmPasswordVisible = registerConfirmPasswordVisible,
                            onConfirmPasswordVisibilityChange = { registerConfirmPasswordVisible = !registerConfirmPasswordVisible },
                            onRegisterClick = {
                                if (registerEmail.isNotEmpty() && registerUsername.isNotEmpty() && 
                                    registerPassword.isNotEmpty() && registerPassword == registerConfirmPassword) {
                                    viewModel.register(registerEmail, registerUsername, registerPassword)
                                    // Set email for OTP navigation
                                    registrationEmail = registerEmail
                                }
                            },
                            onGoogleLoginClick = {
                                scope.launch {
                                    val signInIntentSender = googleAuthClient.signIn()
                                    if(signInIntentSender != null) {
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender
                                            ).build()
                                        )
                                    } else {
                                        Toast.makeText(context, "Google Sign In Init Failed", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            isLoading = authState.isLoading,
                            error = authState.error
                        )
                    }
                }
            }
                
                // Logo overlapping the card (half outside, half inside)
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Lingora Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun LoginContent(
    identifier: String,
    onIdentifierChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Email Input
        OutlinedTextField(
            value = identifier,
            onValueChange = onIdentifierChange,
            label = { Text("Tên đăng nhập", fontFamily = ArimoFontFamily) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GradientStart,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Mật khẩu", fontFamily = ArimoFontFamily) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GradientStart,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        // Forgot Password Link - Right aligned below password field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-8).dp), // Move up to sit closer to password field
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onForgotPasswordClick,
                contentPadding = PaddingValues(0.dp) // Remove default padding
            ) {
                Text(
                    text = "Quên mật khẩu?",
                    color = GradientStart,
                    fontSize = 14.sp,
                    fontFamily = ArimoFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Error Message
        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                fontFamily = ArimoFontFamily,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Login Button with Gradient
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Đăng nhập",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ArimoFontFamily,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.Gray.copy(alpha = 0.3f)
            )
            Text(
                text = "Hoặc",
                modifier = Modifier.padding(horizontal = 8.dp),
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = ArimoFontFamily
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.Gray.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Login Button
        Button(
            onClick = onGoogleLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
            enabled = !isLoading
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google_ic),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified // Preserve original colors
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tiếp tục với Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ArimoFontFamily,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun RegisterContent(
    username: String,
    onUsernameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Full Name Input
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Tên đăng nhập", fontFamily = ArimoFontFamily) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GradientStart,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email của bạn", fontFamily = ArimoFontFamily) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GradientStart,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Mật khẩu", fontFamily = ArimoFontFamily) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GradientStart,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Confirm Password Input
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Xác nhận mật khẩu", fontFamily = ArimoFontFamily) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onConfirmPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GradientStart,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        // Error Message
        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                fontFamily = ArimoFontFamily,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Validation Error
        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
            Text(
                text = "Mật khẩu không khớp",
                color = Color.Red,
                fontSize = 12.sp,
                fontFamily = ArimoFontFamily,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Register Button with Gradient
        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Tạo tài khoản",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ArimoFontFamily,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.Gray.copy(alpha = 0.3f)
            )
            Text(
                text = "Hoặc",
                modifier = Modifier.padding(horizontal = 8.dp),
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = ArimoFontFamily
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.Gray.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Register Button
        Button(
            onClick = onGoogleLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
            enabled = !isLoading
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google_ic),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified // Preserve original colors
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tiếp tục với Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ArimoFontFamily,
                    color = Color.Black
                )
            }
        }
    }
}

// Extension function to apply gradient to text
@Composable
fun Text(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    brush: Brush,
    letterSpacing: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        modifier = modifier,
        style = androidx.compose.ui.text.TextStyle(
            brush = brush
        )
    )
}

