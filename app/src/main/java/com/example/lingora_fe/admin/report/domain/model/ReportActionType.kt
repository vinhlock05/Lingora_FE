package com.example.lingora_fe.admin.report.domain.model

enum class ReportActionType(val value: String, val displayName: String) {
    DELETE_CONTENT("DELETE_CONTENT", "Delete Content"),
    WARN_USER("WARN_USER", "Warn User"),
    SUSPEND_USER("SUSPEND_USER", "Suspend User"),
    BAN_USER("BAN_USER", "Ban User")
}
