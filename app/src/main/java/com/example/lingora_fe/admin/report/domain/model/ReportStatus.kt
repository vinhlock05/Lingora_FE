package com.example.lingora_fe.admin.report.domain.model

enum class ReportStatus(val value: String, val displayName: String) {
    PENDING("PENDING", "Pending"),
    ACCEPTED("ACCEPTED", "Accepted"),
    REJECTED("REJECTED", "Rejected")
}
