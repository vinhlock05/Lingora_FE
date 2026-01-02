package com.example.lingora_fe.auth.presentation

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.lingora_fe.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken
            val profilePictureUrl = credential.profilePictureUri?.toString()
            val username = credential.displayName
            
            android.util.Log.d("GoogleAuthClient", "signInWithIntent success: Token: ${googleIdToken?.take(10)}..., Name: $username")

            if(googleIdToken != null) {
                 SignInResult(
                     data = UserData(
                         userId = "", // Not known yet
                         username = username,
                         profilePictureUrl = profilePictureUrl
                     ),
                     errorMessage = null,
                     idToken = googleIdToken // IMPORTANT: send this to backend
                 )
            } else {
                 android.util.Log.e("GoogleAuthClient", "No Google ID Token found in credential")
                 SignInResult(
                     data = null,
                     errorMessage = "No Google ID Token found",
                     idToken = null
                 )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("GoogleAuthClient", "signInWithIntent Error: ${e.message}")
            // Check for specific ApiException codes if possible (e.g. 10: DeveloperError)
            if (e is com.google.android.gms.common.api.ApiException) {
                android.util.Log.e("GoogleAuthClient", "ApiException Status Code: ${e.statusCode}")
            }
            SignInResult(
                data = null,
                errorMessage = e.message,
                idToken = null
            )
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id)) // Will add this to strings.xml
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?,
    val idToken: String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)
