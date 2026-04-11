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
     * Connect — token truyền qua auth, BE verify bằng io.use()
     * Không cần userId, BE tự lấy từ token.
     */
    fun connect(accessToken: String, baseUrl: String = com.example.lingora_fe.util.Constant.BASE_URL) {
        if (socket?.connected() == true) return

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
            setupListeners()
            socket?.connect()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        _connectionState.value = ConnectionState.Disconnected
    }

    fun isConnected(): Boolean = socket?.connected() == true

    // ─── Notification ───────────────────────────────

    fun notificationFlow(): Flow<Notification> = callbackFlow {
        val listener = io.socket.emitter.Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                try {
                    val jsonObject = args[0] as? JSONObject ?: return@Listener
                    val dto = gson.fromJson(jsonObject.toString(), NotificationDto::class.java)
                    trySend(dto.toDomain())
                } catch (_: Exception) { }
            }
        }
        socket?.on("notification", listener)
        awaitClose { socket?.off("notification", listener) }
    }

    // ─── Classroom Chat ───────────────────────────────

    /**
     * Join classroom chat room.
     * Gọi SAU khi đã load history qua REST.
     * @param onJoined callback khi join thành công
     * @param onError  callback khi có lỗi (không phải member, v.v.)
     */
    fun joinClassroom(
        classroomId: Int,
        onJoined: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        val payload = JSONObject().put("classroomId", classroomId)
        socket?.emit("classroom:join", payload)

        socket?.once("classroom:joined") { onJoined?.invoke() }
        socket?.once("classroom:error") { args ->
            val msg = (args.firstOrNull() as? JSONObject)?.optString("message") ?: "Error"
            onError?.invoke(msg)
        }
    }

    fun leaveClassroom(classroomId: Int) {
        socket?.emit("classroom:leave", JSONObject().put("classroomId", classroomId))
    }

    fun sendClassroomMessage(
        classroomId: Int,
        content: String,
        type: String = "TEXT",
        attachmentUrl: String? = null,
        repliedToId: Int? = null
    ) {
        val payload = JSONObject().apply {
            put("classroomId", classroomId)
            put("content", content)
            put("type", type)
            attachmentUrl?.let { put("attachmentUrl", it) }
            repliedToId?.let { put("repliedToId", it) }
        }
        socket?.emit("classroom:message", payload)
    }

    /**
     * Flow nhận tin nhắn mới trong room.
     * Tự hủy listener khi flow bị cancel (e.g. khi rời màn hình).
     */
    fun classroomMessageFlow(): Flow<JSONObject> = callbackFlow {
        val listener = io.socket.emitter.Emitter.Listener { args ->
            try {
                val data = args.firstOrNull() as? JSONObject ?: return@Listener
                val message = data.optJSONObject("message") ?: return@Listener
                trySend(message)
            } catch (_: Exception) { }
        }
        socket?.on("classroom:message", listener)
        awaitClose { socket?.off("classroom:message", listener) }
    }

    /**
     * Flow nhận thông báo khi được duyệt vào lớp (PENDING -> ACTIVE).
     */
    fun classroomApprovalFlow(): Flow<JSONObject> = callbackFlow {
        val listener = io.socket.emitter.Emitter.Listener { args ->
            try {
                val data = args.firstOrNull() as? JSONObject ?: return@Listener
                trySend(data)
            } catch (_: Exception) { }
        }
        socket?.on("classroom:approved", listener)
        awaitClose { socket?.off("classroom:approved", listener) }
    }

    // ─────────────────────────────────────────────────

    private fun setupListeners() {
        socket?.on(Socket.EVENT_CONNECT) {
            _connectionState.value = ConnectionState.Connected
        }
        socket?.on(Socket.EVENT_DISCONNECT) {
            _connectionState.value = ConnectionState.Disconnected
        }
        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            val error = args.firstOrNull()
            val msg = when (error) {
                is Exception -> error.message ?: "Connection error"
                is String -> error
                else -> "Connection error"
            }
            _connectionState.value = ConnectionState.Error(msg)
        }
    }
}

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
