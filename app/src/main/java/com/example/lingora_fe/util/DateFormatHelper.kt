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
}