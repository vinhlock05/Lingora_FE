package com.example.lingora_fe.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object DateFormatHelper {
    private val chatTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm - dd/MM", Locale.forLanguageTag("vi-VN"))
    private val sessionTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"))
    private val isoFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    )

    fun formatChatTimestamp(raw: String, includeDate: Boolean = false): String {
        return try {
            val instant = Instant.parse(raw)
            val zonedDateTime = instant.atZone(ZoneId.systemDefault())
            if (includeDate) {
                zonedDateTime.format(sessionTimeFormatter)
            } else {
                zonedDateTime.format(chatTimeFormatter)
            }
        } catch (e: DateTimeParseException) {
            raw
        }
    }

    fun formatDateAsChatTime(date: java.util.Date?, includeDate: Boolean = false): String {
        if (date == null) return ""
        return try {
            val instant = date.toInstant()
            val zonedDateTime = instant.atZone(ZoneId.systemDefault())
            if (includeDate) {
                zonedDateTime.format(sessionTimeFormatter)
            } else {
                zonedDateTime.format(chatTimeFormatter)
            }
        } catch (e: Exception) {
            date.toString()
        }
    }

    fun formatTimeAgo(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return try {
            val instant = Instant.parse(raw)
            val now = Instant.now()
            val diffMillis = now.toEpochMilli() - instant.toEpochMilli()
            val seconds = diffMillis / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            when {
                days > 0 -> "$days ngày trước"
                hours > 0 -> "$hours giờ trước"
                minutes > 0 -> "$minutes phút trước"
                else -> "Vừa xong"
            }
        } catch (_: Exception) {
            try {
                val formatter = java.text.SimpleDateFormat(isoFormats.first(), java.util.Locale.getDefault()).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val date = formatter.parse(raw)
                if (date != null) {
                    val now = java.util.Date()
                    val diff = now.time - date.time
                    val seconds = diff / 1000
                    val minutes = seconds / 60
                    val hours = minutes / 60
                    val days = hours / 24
                    when {
                        days > 0 -> "$days ngày trước"
                        hours > 0 -> "$hours giờ trước"
                        minutes > 0 -> "$minutes phút trước"
                        else -> "Vừa xong"
                    }
                } else raw
            } catch (_: Exception) {
                raw
            }
        }
    }
}
