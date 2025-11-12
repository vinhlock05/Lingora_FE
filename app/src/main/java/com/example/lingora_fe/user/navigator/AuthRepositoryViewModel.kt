package com.example.lingora_fe.user.navigator

import androidx.lifecycle.ViewModel
import com.example.lingora_fe.auth.domain.repository.AuthRepository
import com.example.lingora_fe.core.network.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthRepositoryViewModel @Inject constructor(
    val authRepository: AuthRepository,
    val tokenManager: TokenManager
) : ViewModel()

