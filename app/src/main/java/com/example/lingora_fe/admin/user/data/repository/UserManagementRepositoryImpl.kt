package com.example.lingora_fe.admin.user.data.repository

import arrow.core.Either
import com.example.lingora_fe.admin.user.data.remote.api.UserManagementApiService
import com.example.lingora_fe.admin.user.data.remote.dto.UpdateProficiencyRequest
import com.example.lingora_fe.admin.user.data.remote.dto.toDomain
import com.example.lingora_fe.admin.user.data.remote.dto.toDto
import com.example.lingora_fe.admin.user.domain.model.AdminUser
import com.example.lingora_fe.admin.user.domain.model.CreateUserData
import com.example.lingora_fe.admin.user.domain.model.UpdateUserData
import com.example.lingora_fe.admin.user.domain.model.UserFilterOptions
import com.example.lingora_fe.admin.user.domain.model.UserListMetadata
import com.example.lingora_fe.admin.user.domain.repository.UserManagementRepository
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.di.qualifier.AuthApiClient
import com.example.lingora_fe.util.Constant
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class UserManagementRepositoryImpl @Inject constructor(
    private val apiService: UserManagementApiService,
    @AuthApiClient private val okHttpClient: OkHttpClient
) : UserManagementRepository {

    override suspend fun getAllUsers(
        token: String,
        filterOptions: UserFilterOptions
    ): Either<AppFailure, UserListMetadata> {
        return Either.catch {
            val response = apiService.getAllUsers(
                limit = filterOptions.limit,
                page = filterOptions.page,
                search = filterOptions.search,
                proficiency = filterOptions.proficiency,
                status = filterOptions.status,
                sort = filterOptions.sort
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getUserById(
        token: String,
        userId: Int
    ): Either<AppFailure, AdminUser> {
        return Either.catch {
            val response = apiService.getUserById(userId)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun createUser(
        token: String,
        userData: CreateUserData
    ): Either<AppFailure, AdminUser> {
        return Either.catch {
            val response = apiService.createUser(userData.toDto())
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateUser(
        token: String,
        userId: Int,
        userData: UpdateUserData
    ): Either<AppFailure, AdminUser> {
        return Either.catch {
            val response = apiService.updateUser(userId, userData.toDto())
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }
    
    /**
     * Update only proficiency field - uses OkHttp directly to control request body
     * This ensures only proficiency field is sent, avoiding null/empty fields for other properties
     */
    suspend fun updateProficiencyOnly(
        token: String,
        userId: Int,
        proficiency: String
    ): Either<AppFailure, AdminUser> = withContext(Dispatchers.IO) {
        Either.catch {
            // Create JSON body with only proficiency field using Gson without serializeNulls
            val gsonWithoutNulls = GsonBuilder()
                .setLenient()
                .create() // Don't serialize nulls
            
            val requestBody = UpdateProficiencyRequest(proficiency = proficiency)
            val jsonBody = gsonWithoutNulls.toJson(requestBody)
            
            // Create request body
            val mediaType = "application/json".toMediaType()
            val body = jsonBody.toRequestBody(mediaType)
            
            // Build HTTP request
            val request = Request.Builder()
                .url("${Constant.BASE_URL}users/$userId")
                .patch(body)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()
            
            // Execute request
            val response = okHttpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                throw Exception("Update failed: ${response.code} - $errorBody")
            }
            
            // Parse response
            val responseBody = response.body?.string() ?: throw Exception("Empty response body")
            val gson = GsonBuilder().setLenient().create()
            
            // Parse ApiResponse wrapper first
            val jsonElement = gson.fromJson(responseBody, com.google.gson.JsonObject::class.java)
            val message = jsonElement.get("message")?.asString ?: "Unknown error"
            
            // Parse metaData (AdminUserDto)
            val metaDataElement = jsonElement.get("metaData")
            if (metaDataElement == null || metaDataElement.isJsonNull) {
                throw Exception(message)
            }
            
            val userDto = gson.fromJson(
                metaDataElement,
                com.example.lingora_fe.admin.user.data.remote.dto.AdminUserDto::class.java
            )
            
            userDto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun restoreUser(
        token: String,
        userId: Int
    ): Either<AppFailure, AdminUser> {
        return Either.catch {
            val response = apiService.restoreUser(userId)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteUser(
        token: String,
        userId: Int
    ): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteUser(userId)
            Unit
        }.mapLeft { it.toAppFailure() }
    }
}

