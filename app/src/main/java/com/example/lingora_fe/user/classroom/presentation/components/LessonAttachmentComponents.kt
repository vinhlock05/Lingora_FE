package com.example.lingora_fe.user.classroom.presentation.components

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonAttachment
import com.example.lingora_fe.user.classroom.presentation.LessonDetailState
import com.example.lingora_fe.user.classroom.util.LessonAttachmentRole
import com.example.lingora_fe.user.classroom.util.LessonAttachmentType

// ─── Limits ────────────────────────────────────────────────────────────────────

const val MAX_INLINE_ATTACHMENTS = 3
const val MAX_TOTAL_ATTACHMENTS  = 20

// ─── File-type helpers ─────────────────────────────────────────────────────────

/** Returns an icon matching the file type. */
@Composable
fun attachmentTypeIcon(fileType: LessonAttachmentType): ImageVector = when (fileType) {
    LessonAttachmentType.VIDEO    -> Icons.Outlined.VideoFile
    LessonAttachmentType.AUDIO    -> Icons.Outlined.AudioFile
    LessonAttachmentType.PDF      -> Icons.Outlined.PictureAsPdf
    LessonAttachmentType.DOCUMENT -> Icons.Outlined.Description
    LessonAttachmentType.IMAGE    -> Icons.Outlined.Image
    LessonAttachmentType.OTHER    -> Icons.Default.AttachFile
}

/** Background colour for the file-type icon badge. */
fun attachmentTypeColor(fileType: LessonAttachmentType): Color = when (fileType) {
    LessonAttachmentType.VIDEO    -> Color(0xFFEDE9FE) // violet-100
    LessonAttachmentType.AUDIO    -> Color(0xFFE0F2FE) // sky-100
    LessonAttachmentType.PDF      -> Color(0xFFFEE2E2) // red-100
    LessonAttachmentType.DOCUMENT -> Color(0xFFDCFCE7) // green-100
    LessonAttachmentType.IMAGE    -> Color(0xFFFEF9C3) // yellow-100
    LessonAttachmentType.OTHER    -> Color(0xFFF3F4F6) // gray-100
}

/** Foreground / tint colour for the file-type icon. */
fun attachmentTypeIconTint(fileType: LessonAttachmentType): Color = when (fileType) {
    LessonAttachmentType.VIDEO    -> Color(0xFF7C3AED)
    LessonAttachmentType.AUDIO    -> Color(0xFF0284C7)
    LessonAttachmentType.PDF      -> Color(0xFFDC2626)
    LessonAttachmentType.DOCUMENT -> Color(0xFF16A34A)
    LessonAttachmentType.IMAGE    -> Color(0xFFCA8A04)
    LessonAttachmentType.OTHER    -> Color(0xFF6B7280)
}

// ─── AttachmentSectionHeader ───────────────────────────────────────────────────

/** Small sub-header row separating INLINE vs DOWNLOAD attachment groups. */
@Composable
fun AttachmentSectionHeader(icon: ImageVector, label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ClassroomColors.BrandPrimaryStrong,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = ClassroomColors.BrandPrimaryStrong
        )
        Text(
            text = "($count)",
            style = MaterialTheme.typography.labelSmall,
            color = ClassroomColors.TextMuted
        )
    }
}

// ─── AttachmentItem ────────────────────────────────────────────────────────────

@Composable
fun AttachmentItem(
    attachment: ClassroomLessonAttachment,
    isTeacher: Boolean,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showImageDialog by remember { mutableStateOf(false) }
    var showVideoDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }

    val sizeLabel = attachment.fileSizeBytes?.let { bytes ->
        when {
            bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / 1024.0 / 1024.0)
            bytes >= 1024        -> "%.0f KB".format(bytes / 1024.0)
            else                 -> "$bytes B"
        }
    }
    val durationLabel = attachment.durationSeconds?.let { secs ->
        val m = secs / 60; val s = secs % 60
        if (m > 0) "${m}p${s.toString().padStart(2, '0')}s" else "${s}s"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable {
                when {
                    // INLINE Image → fullscreen Coil preview
                    attachment.fileType == LessonAttachmentType.IMAGE &&
                    attachment.role    == LessonAttachmentRole.INLINE -> showImageDialog = true

                    // INLINE Video → in-app VideoView dialog
                    attachment.fileType == LessonAttachmentType.VIDEO &&
                    attachment.role    == LessonAttachmentRole.INLINE -> showVideoDialog = true

                    // INLINE Audio → in-app audio player dialog
                    attachment.fileType == LessonAttachmentType.AUDIO &&
                    attachment.role    == LessonAttachmentRole.INLINE -> showAudioDialog = true

                    // DOWNLOAD Video/Audio → open with external media player
                    attachment.fileType == LessonAttachmentType.VIDEO ||
                    attachment.fileType == LessonAttachmentType.AUDIO -> {
                        runCatching {
                            val mime = attachment.mimeType
                                ?: if (attachment.fileType == LessonAttachmentType.VIDEO) "video/*" else "audio/*"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(attachment.fileUrl), mime)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }
                    }

                    // PDF / Doc / Image (DOWNLOAD) / Other → generic browser/app open
                    else -> {
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(attachment.fileUrl))
                            context.startActivity(intent)
                        }
                    }
                }
            },
        colors = CardDefaults.cardColors(containerColor = ClassroomColors.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // File-type icon badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(attachmentTypeColor(attachment.fileType)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = attachmentTypeIcon(attachment.fileType),
                    contentDescription = null,
                    tint = attachmentTypeIconTint(attachment.fileType),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Title + meta
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = attachment.title ?: attachment.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MainText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = attachment.fileType.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = attachmentTypeIconTint(attachment.fileType),
                        fontWeight = FontWeight.Medium
                    )
                    if (sizeLabel != null) {
                        Text("•", style = MaterialTheme.typography.labelSmall, color = ClassroomColors.TextMuted)
                        Text(
                            text = sizeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = ClassroomColors.TextMuted
                        )
                    }
                    if (durationLabel != null) {
                        Text("•", style = MaterialTheme.typography.labelSmall, color = ClassroomColors.TextMuted)
                        Text(
                            text = durationLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = ClassroomColors.TextMuted
                        )
                    }
                }
            }

            // Role indicator + delete button (teacher only)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                val roleIcon = if (attachment.role == LessonAttachmentRole.INLINE)
                    Icons.Default.PlayArrow else Icons.Default.Download
                Icon(
                    imageVector = roleIcon,
                    contentDescription = null,
                    tint = ClassroomColors.BrandPrimaryStrong,
                    modifier = Modifier.size(18.dp)
                )
                if (isTeacher) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = ClassroomColors.Danger,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // ── In-app IMAGE viewer ───────────────────────────────────────────────────
    if (showImageDialog) {
        Dialog(
            onDismissRequest = { showImageDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .clickable { showImageDialog = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = attachment.fileUrl,
                    contentDescription = attachment.title ?: attachment.fileName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                Text(
                    text = "Nhấn bất kỳ đâu để đóng",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }

    // ── In-app VIDEO player ───────────────────────────────────────────────────
    if (showVideoDialog) {
        Dialog(
            onDismissRequest = { showVideoDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            val mc = MediaController(ctx)
                            mc.setAnchorView(this)
                            setMediaController(mc)
                            setVideoURI(Uri.parse(attachment.fileUrl))
                            setOnPreparedListener { start() }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )
                // Close button overlay
                IconButton(
                    onClick = { showVideoDialog = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.White
                    )
                }
            }
        }
    }

    // ── In-app AUDIO player ───────────────────────────────────────────────────
    if (showAudioDialog) {
        InAppAudioDialog(
            title    = attachment.title ?: attachment.fileName,
            audioUrl = attachment.fileUrl,
            onDismiss = { showAudioDialog = false }
        )
    }
}

/** Lightweight in-app audio player dialog using [MediaPlayer]. */
@Composable
private fun InAppAudioDialog(title: String, audioUrl: String, onDismiss: () -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }
    var isPrepared by remember { mutableStateOf(false) }
    val mediaPlayer = remember {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            runCatching {
                setDataSource(audioUrl)
                prepareAsync()
            }
            setOnPreparedListener { isPrepared = true }
            setOnCompletionListener { isPlaying = false }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isPrepared) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = ClassroomColors.BrandPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    IconButton(
                        onClick = {
                            if (isPlaying) { mediaPlayer.pause(); isPlaying = false }
                            else { mediaPlayer.start(); isPlaying = true }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = ClassroomColors.BrandSoftSurface,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                            tint = ClassroomColors.BrandPrimaryStrong,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

// ─── AddAttachmentBottomSheet ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAttachmentBottomSheet(
    state: LessonDetailState,
    currentInlineCount: Int,
    currentTotalCount: Int,
    onPickFile: () -> Unit,
    onTitleChange: (String) -> Unit,
    onRoleChange: (LessonAttachmentRole) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val brandFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ClassroomColors.BrandPrimary,
        focusedLabelColor  = ClassroomColors.BrandPrimaryStrong,
        cursorColor        = ClassroomColors.BrandPrimary
    )

    val totalFull      = currentTotalCount >= MAX_TOTAL_ATTACHMENTS
    val inlineFull     = currentInlineCount >= MAX_INLINE_ATTACHMENTS
    val roleWouldBreach = state.attachmentFileUrl.isNotEmpty() &&
        state.attachmentRole == LessonAttachmentRole.INLINE && inlineFull
    val canSave = state.attachmentFileUrl.isNotBlank() &&
        !state.isUploadingAttachment &&
        !totalFull && !roleWouldBreach

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(ClassroomColors.NeutralBorder)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Thêm tài liệu đính kèm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MainText
                    )
                    // Quota badge: e.g. "3 / 20"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (totalFull) ClassroomColors.DangerSoft else ClassroomColors.BrandSoftSurface
                    ) {
                        Text(
                            text = "$currentTotalCount / $MAX_TOTAL_ATTACHMENTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (totalFull) ClassroomColors.Danger else ClassroomColors.BrandPrimaryStrong,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Divider(color = ClassroomColors.NeutralBorder)
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── TOTAL LIMIT WARNING ──────────────────────────────────────────
            if (totalFull) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ClassroomColors.DangerSoft,
                        border = BorderStroke(1.dp, ClassroomColors.Danger.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = ClassroomColors.Danger,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Bài học đã đạt giới hạn $MAX_TOTAL_ATTACHMENTS tài liệu.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ClassroomColors.Danger
                            )
                        }
                    }
                }
            }

            // ── PICK FILE BUTTON ─────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Chọn file",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ClassroomColors.TextSecondary
                    )
                    OutlinedButton(
                        onClick = onPickFile,
                        enabled = !state.isUploadingAttachment && !totalFull,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ClassroomColors.BrandPrimaryStrong
                        ),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = if (state.attachmentFileUrl.isNotEmpty())
                                ClassroomColors.BrandPrimary
                            else ClassroomColors.NeutralBorder
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (state.attachmentFileName.isEmpty()) "Chọn file từ thiết bị"
                                   else state.attachmentFileName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── UPLOADING STATE ──────────────────────────────────────────────
            if (state.isUploadingAttachment) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ClassroomColors.BrandSoftSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = ClassroomColors.BrandPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Column {
                                Text(
                                    "Đang upload lên cloud...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ClassroomColors.BrandPrimaryStrong
                                )
                                Text(
                                    "Vui lòng chờ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClassroomColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            } else if (state.attachmentFileUrl.isNotEmpty()) {

                // ── UPLOAD SUCCESS CARD ──────────────────────────────────────
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ClassroomColors.BrandSoftSurface,
                        border = BorderStroke(1.dp, ClassroomColors.BrandPrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(attachmentTypeColor(state.attachmentFileType)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = attachmentTypeIcon(state.attachmentFileType),
                                    contentDescription = null,
                                    tint = attachmentTypeIconTint(state.attachmentFileType),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = state.attachmentFileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MainText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val meta = buildList {
                                    add(state.attachmentFileType.displayName)
                                    state.attachmentFileSizeBytes?.let { bytes ->
                                        add(when {
                                            bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / 1024.0 / 1024.0)
                                            bytes >= 1024        -> "%.0f KB".format(bytes / 1024.0)
                                            else                 -> "$bytes B"
                                        })
                                    }
                                }.joinToString(" • ")
                                Text(
                                    text = meta,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClassroomColors.TextMuted
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ClassroomColors.BrandPrimaryStrong,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // ── TITLE FIELD ──────────────────────────────────────────────
                item {
                    OutlinedTextField(
                        value = state.attachmentTitle,
                        onValueChange = onTitleChange,
                        label = { Text("Tiêu đề hiển thị (tùy chọn)") },
                        placeholder = { Text("Để trống → dùng tên file") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = brandFieldColors
                    )
                }

                // ── ROLE SELECTOR ────────────────────────────────────────────
                item {
                    // PDF / Document are always DOWNLOAD — they can't be played in-app
                    val typeLockedToDownload = state.attachmentFileType == LessonAttachmentType.PDF ||
                        state.attachmentFileType == LessonAttachmentType.DOCUMENT ||
                        state.attachmentFileType == LessonAttachmentType.OTHER

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Cách học sinh truy cập",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ClassroomColors.TextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LessonAttachmentRole.entries.forEach { role ->
                                val isInlineRole     = role == LessonAttachmentRole.INLINE
                                // INLINE option blocked when: total inline is full AND this file isn't already INLINE
                                // OR the file type can't be played in-app
                                val inlineCountBlocked = isInlineRole && inlineFull && state.attachmentRole != role
                                val typeBlocked      = isInlineRole && typeLockedToDownload
                                val isBlocked        = inlineCountBlocked || typeBlocked
                                val selected         = state.attachmentRole == role

                                val roleLabel = if (isInlineRole) "▶ Xem trong app" else "⬇ Tải về"
                                val roleDesc  = when {
                                    typeBlocked  && isInlineRole ->
                                        "Không hỗ trợ phát trong app"
                                    isInlineRole ->
                                        "Video / Audio / Ảnh mở trong app ($currentInlineCount/$MAX_INLINE_ATTACHMENTS)"
                                    else ->
                                        "PDF, tài liệu, file khác học sinh tải về"
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(enabled = !isBlocked) { onRoleChange(role) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isBlocked -> ClassroomColors.NeutralSurface
                                        selected  -> ClassroomColors.BrandSoftSurface
                                        else      -> Color.White
                                    },
                                    border = BorderStroke(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = when {
                                            isBlocked -> ClassroomColors.NeutralBorder
                                            selected  -> ClassroomColors.BrandPrimary
                                            else      -> ClassroomColors.NeutralBorder
                                        }
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = roleLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBlocked) ClassroomColors.TextMuted
                                                    else if (selected) ClassroomColors.BrandPrimaryStrong
                                                    else MainText
                                        )
                                        Text(
                                            text = roleDesc,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isBlocked) ClassroomColors.TextMuted
                                                    else ClassroomColors.TextSecondary,
                                            lineHeight = 16.sp
                                        )
                                        if (inlineCountBlocked) {
                                            Text(
                                                text = "Đã đạt giới hạn $MAX_INLINE_ATTACHMENTS",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ClassroomColors.Danger,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── SAVE BUTTON ──────────────────────────────────────────────
                item {
                    Button(
                        onClick = onSave,
                        enabled = canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ClassroomColors.BrandPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = ClassroomColors.NeutralBorder,
                            disabledContentColor = ClassroomColors.TextMuted
                        )
                    ) {
                        Text(
                            "Thêm tài liệu",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── TIPS (shown before file is picked) ───────────────────────────
            if (state.attachmentFileUrl.isEmpty() && !state.isUploadingAttachment) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ClassroomColors.NeutralSurface)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Giới hạn file",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ClassroomColors.TextSecondary
                        )
                        val limits = listOf(
                            "🎬 Video"          to "500 MB",
                            "🔊 Audio"          to "50 MB",
                            "📄 PDF / Tài liệu" to "20 MB",
                            "🖼️ Ảnh"            to "5 MB"
                        )
                        limits.forEach { (type, limit) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(type, style = MaterialTheme.typography.bodySmall, color = ClassroomColors.TextSecondary)
                                Text(limit, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MainText)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = ClassroomColors.NeutralBorder)
                        Text(
                            "Mỗi bài học tối đa $MAX_TOTAL_ATTACHMENTS tài liệu, trong đó $MAX_INLINE_ATTACHMENTS phát trực tiếp (video/audio).",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClassroomColors.TextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
