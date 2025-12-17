package com.example.lingora_fe.admin.report.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lingora_fe.admin.report.domain.model.ReportStatus
import com.example.lingora_fe.admin.report.domain.model.ReportType

@Composable
fun ReportTypeBadge(reportType: ReportType) {
    val (backgroundColor, textColor) = when (reportType) {
        ReportType.SPAM -> Color(0xFFA7D1EF) to Color(0xFF1976D2)
        ReportType.HARASSMENT -> Color(0xFFFFA2AF) to Color(0xFFC62828)
        ReportType.HATE_SPEECH -> Color(0xFFFA9FAE) to Color(0xFFB71C1C)
        ReportType.INAPPROPRIATE -> Color(0xFFF2BFFA) to Color(0xFF7B1FA2)
        ReportType.MISINFORMATION -> Color(0xFFFDE46F) to Color(0xFFD76A09)
        ReportType.COPYRIGHT -> Color(0xFF8ED8FA) to Color(0xFF0277BD)
        ReportType.VIOLENCE -> Color(0xFFFA7B8E) to Color(0xFFB71C1C)
        ReportType.ADULT_CONTENT -> Color(0xFFFDA0C0) to Color(0xFFC2185B)
        ReportType.OTHER -> Color(0xFFDED6D6) to Color(0xFF616161)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = reportType.displayName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ReportStatusBadge(status: ReportStatus) {
    val (backgroundColor, textColor) = when (status) {
        ReportStatus.PENDING -> Color(0xFFE1CA75) to Color(0xFFB25B0E)
        ReportStatus.ACCEPTED -> Color(0xFFAEFFB3) to Color(0xFF2E7D32)
        ReportStatus.REJECTED -> Color(0xFFFAA2AF) to Color(0xFFC62828)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}