package com.example.lingora_fe.user.notification.data.socket

import com.example.lingora_fe.user.notification.data.remote.dto.NotificationDto
import com.example.lingora_fe.user.notification.data.remote.dto.toDomain
import com.example.lingora_fe.user.notification.domain.model.Notification
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSocketManager @Inject constructor(
    private val gson: Gson
) {

    private var socket: Socket? = null
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /**
     * Connect to Socket.IO server with authentication token
     */
    fun connect(accessToken: String, baseUrl: String = com.example.lingora_fe.util.Constant.BASE_URL, userId: Int? = null) {
        if (socket?.connected() == true) {
            return
        }

        try {
            val options = IO.Options().apply {
                auth = mapOf("token" to accessToken)
                reconnection = true
                reconnectionDelay = 1000
                reconnectionAttempts = 5
                transports = arrayOf("websocket")
            }

            val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
            socket = IO.socket(normalizedUrl, options)
            setupListeners(userId)
            socket?.connect()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
        }
    }

    /**
     * Disconnect from Socket.IO server
     */
    fun disconnect() {
        socket?.disconnect()
        socket = null
        _connectionState.value = ConnectionState.Disconnected
    }

    /**
     * Flow of new notifications from Socket.IO
     */
    fun notificationFlow(): Flow<Notification> = callbackFlow {
        val listener = io.socket.emitter.Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                try {
                    val jsonObject = args[0] as? JSONObject
                    if (jsonObject != null) {
                        val notificationDto = gson.fromJson(jsonObject.toString(), NotificationDto::class.java)
                        val notification = notificationDto.toDomain()
                        trySend(notification)
                    }
                } catch (e: Exception) {
                    // Handle parsing error
                }
            }
        }

        socket?.on("notification", listener)

        awaitClose {
            socket?.off("notification", listener)
        }
    }

    private fun setupListeners(userId: Int?) {
        socket?.on(Socket.EVENT_CONNECT) {
            _connectionState.value = ConnectionState.Connected
            if (userId != null) {
                try {
                    socket?.emit("register", userId.toString())
                } catch (_: Exception) { }
            }
        }

        socket?.on(Socket.EVENT_DISCONNECT) {
            _connectionState.value = ConnectionState.Disconnected
        }

        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            val error = args.firstOrNull()
            val errorMessage = when (error) {
                is Exception -> error.message ?: "Connection error"
                is String -> error
                else -> "Connection error"
            }
            _connectionState.value = ConnectionState.Error(errorMessage)
        }
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }
}

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

