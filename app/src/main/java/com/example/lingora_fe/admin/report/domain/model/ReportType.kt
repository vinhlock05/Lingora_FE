package com.example.lingora_fe.admin.report.domain.model

enum class ReportType(val value: String, val displayName: String) {
    SPAM("SPAM", "Spam / Quảng cáo"),
    HARASSMENT("HARASSMENT", "Quấy rối / Bắt nạt"),
    HATE_SPEECH("HATE_SPEECH", "Ngôn từ thù ghét"),
    INAPPROPRIATE("INAPPROPRIATE", "Nội dung không phù hợp"),
    MISINFORMATION("MISINFORMATION", "Thông tin sai lệch"),
    COPYRIGHT("COPYRIGHT", "Vi phạm bản quyền"),
    VIOLENCE("VIOLENCE", "Bạo lực / Nguy hiểm"),
    ADULT_CONTENT("ADULT_CONTENT", "Nội dung người lớn"),
    OTHER("OTHER", "Khác")
}
