package com.example.lingora_fe.user.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.navigation.Route

@Composable
fun ProfileScreen(
    rootNavController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Check if user can switch roles
    val canSwitchRoles = viewModel.canSwitchRoles()
    val activeRole = viewModel.getActiveRole()
    val allRoles = viewModel.getAllRoles()

    // Show error snackbar if there's an error
    LaunchedEffect(profileState.error) {
        if (profileState.error != null) {
            // Show error and clear it after some time
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        if (profileState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GradientStart)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Profile Header Card with Gradient
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box {
                        // Gradient background at top
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(GradientStart, GradientEnd)
                                    )
                                )
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(40.dp))
                            
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(92.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(GradientStart, GradientEnd)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = profileState.user?.username?.first()?.uppercase() ?: "U",
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Username
                            Text(
                                text = profileState.user?.username ?: "User",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MainText
                            )

                            // Email
                            Text(
                                text = profileState.user?.email ?: "",
                                fontSize = 14.sp,
                                color = NavBarText,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Status Badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = when (profileState.user?.status) {
                                    "ACTIVE" -> Color(0xFFD1FAE5)
                                    "INACTIVE" -> Color(0xFFFEF3C7)
                                    else -> Color(0xFFFEE2E2)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (profileState.user?.status) {
                                                    "ACTIVE" -> Color(0xFF065F46)
                                                    "INACTIVE" -> Color(0xFF92400E)
                                                    else -> Color(0xFF991B1B)
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = profileState.user?.status ?: "INACTIVE",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (profileState.user?.status) {
                                            "ACTIVE" -> Color(0xFF065F46)
                                            "INACTIVE" -> Color(0xFF92400E)
                                            else -> Color(0xFF991B1B)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // User Information Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Thông tin cá nhân",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MainText,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        ProfileInfoItem(
                            icon = Icons.Default.Person,
                            label = "Tên người dùng",
                            value = profileState.user?.username ?: ""
                        )

                        ProfileInfoItem(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = profileState.user?.email ?: ""
                        )

                        ProfileInfoItem(
                            icon = Icons.Default.School,
                            label = "Trình độ",
                            value = when (profileState.user?.proficiency) {
                                "BEGINNER" -> "Người mới bắt đầu"
                                "INTERMEDIATE" -> "Trung cấp"
                                "ADVANCED" -> "Nâng cao"
                                else -> profileState.user?.proficiency ?: "Chưa xác định"
                            }
                        )

                        ProfileInfoItem(
                            icon = Icons.Default.CalendarToday,
                            label = "Ngày tham gia",
                            value = profileState.user?.createdAt?.take(10) ?: "",
                            showDivider = false
                        )
                    }
                }

                // Roles Card
                if (!profileState.user?.roles.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Vai trò",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MainText,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            profileState.user?.roles?.forEach { role ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GradientStart.copy(alpha = 0.1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = GradientStart,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = when (role.name) {
                                                "ADMIN" -> "Quản trị viên"
                                                "LEARNER" -> "Người học"
                                                else -> role.name
                                            },
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MainText,
                                            modifier = Modifier.weight(1f)
                                        )
                                        // Show active indicator
                                        if (role.name == activeRole) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = GradientStart.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "Đang dùng",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GradientStart,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Role Switcher Card (only show if user has both ADMIN and LEARNER roles)
                if (canSwitchRoles) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SwapHoriz,
                                    contentDescription = null,
                                    tint = GradientStart,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Chuyển đổi vai trò",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MainText
                                )
                            }
                            
                            Text(
                                text = "Bạn có thể chuyển đổi giữa các vai trò của mình",
                                fontSize = 14.sp,
                                color = NavBarText,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Switch to Admin button
                            if (allRoles.contains("ADMIN") && activeRole != "ADMIN") {
                                Button(
                                    onClick = {
                                        viewModel.switchRole("ADMIN") { destination ->
                                            rootNavController.navigate(destination) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GradientStart
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AdminPanelSettings,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Chuyển sang chế độ Quản trị viên",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            // Switch to Learner button
                            if (allRoles.contains("LEARNER") && activeRole != "LEARNER") {
                                Button(
                                    onClick = {
                                        viewModel.switchRole("LEARNER") { destination ->
                                            rootNavController.navigate(destination) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GradientStart
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.School,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Chuyển sang chế độ Người học",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Logout Button
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFEE2E2)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !profileState.isLoggingOut
                ) {
                    if (profileState.isLoggingOut) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF991B1B)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Color(0xFF991B1B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Đăng xuất",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Error message
        profileState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFFFEE2E2),
                contentColor = Color(0xFF991B1B)
            ) {
                Text(error)
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = GradientStart,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Xác nhận đăng xuất",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            text = {
                Text(
                    "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?",
                    fontSize = 16.sp,
                    color = NavBarText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout {
                            // Navigate to login screen
                            rootNavController.navigate(Route.LoginScreen.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đăng xuất", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NavBarText
                    )
                ) {
                    Text("Hủy", fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GradientStart.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GradientStart,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = NavBarText,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MainText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                color = Color.Gray.copy(alpha = 0.1f),
                thickness = 1.dp
            )
        }
    }
}
