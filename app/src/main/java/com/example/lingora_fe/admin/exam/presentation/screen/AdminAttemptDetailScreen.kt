package com.example.lingora_fe.admin.exam.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.util.DateFormatHelper
import com.example.lingora_fe.user.exam.data.remote.dto.AttemptSectionDto
import com.example.lingora_fe.user.exam.data.remote.dto.AttemptScoreSummaryDto
import com.example.lingora_fe.admin.exam.presentation.AdminExamViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAttemptDetailScreen(
    onNavigateBack: () -> Unit,
    attemptId: Int,
    viewModel: AdminExamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(attemptId) { viewModel.loadAttemptDetail(attemptId) }
    
    // Track which skill is expanded
    var expandedSkill by remember { mutableStateOf<String?>(null) }

    Scaffold {
        val bottomPadding = it.calculateBottomPadding()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading && state.attemptDetail == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = GradientStart
                    )
                }
            } else if (state.attemptDetail != null) {
                val d = state.attemptDetail!!
                val sectionLabel =
                    if (d.attempt.mode != "FULL")
                        d.scoreSummary?.sections?.values?.firstOrNull()?.sectionType
                    else null

                // Exam Info Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = d.exam?.title ?: "Bài thi #${d.attempt.id}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MainText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = listOfNotNull(
                                        d.exam?.examType ?: "IELTS",
                                        d.attempt.mode ?: "FULL",
                                        sectionLabel,
                                        "User: ${d.user?.username}"
                                    ).joinToString(" • "),
                                    fontSize = 13.sp,
                                    color = NavBarText
                                )
                            }
                        }
                        
                        Divider(color = Color(0xFFE5E7EB))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val startFmt = d.attempt.startedAt?.let { DateFormatHelper.formatChatTimestamp(it, includeDate = true) } ?: "N/A"
                            val submitFmt = d.attempt.submittedAt?.let { DateFormatHelper.formatChatTimestamp(it, includeDate = true) } ?: "N/A"
                            
                            InfoColumn(icon = Icons.Default.PlayArrow, label = "Bắt đầu", value = startFmt)
                            InfoColumn(icon = Icons.Default.Done, label = "Nộp bài", value = submitFmt)
                        }
                    }
                }
                
                // Overall Score Card
                d.scoreSummary?.let { summary ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Điểm tổng",
                                fontSize = 14.sp,
                                color = NavBarText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Overall band score
                            val overallBand = summary.bands?.overall ?: summary.overallScore ?: 0.0
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(GradientStart, GradientEnd)
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%.1f", overallBand),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Totals row
                            summary.totals?.let { totals ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatMini(
                                        value = "${totals.totalCorrect ?: 0}",
                                        label = "Đúng",
                                        color = Color(0xFF10B981)
                                    )
                                    StatMini(
                                        value = "${totals.totalQuestions ?: 0}",
                                        label = "Tổng",
                                        color = Color(0xFF3B82F6)
                                    )
                                    StatMini(
                                        value = String.format("%.0f", totals.totalScore ?: 0.0),
                                        label = "Điểm",
                                        color = Color(0xFF9333EA)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Skills section - Expandable cards
                Text(
                    text = "Kết quả theo kỹ năng",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                
                d.sections?.let { sections ->
                    val sectionsBySkill = sections.groupBy { it.sectionType }
                    val skillOrder = listOf("LISTENING", "READING", "WRITING", "SPEAKING")
                    
                    skillOrder.forEach { skill ->
                        val secList = sectionsBySkill[skill] ?: emptyList()
                        if (secList.isNotEmpty()) {
                            val isExpanded = expandedSkill == skill
                            
                            SkillExpandableCard(
                                skill = skill,
                                sections = secList,
                                scoreSummary = d.scoreSummary,
                                isExpanded = isExpanded,
                                onToggle = {
                                    expandedSkill = if (isExpanded) null else skill
                                }
                            )
                        }
                    }
                }
            } else if (state.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                ) {
                    Text(
                        text = state.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFDC2626)
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillExpandableCard(
    skill: String,
    sections: List<AttemptSectionDto>,
    scoreSummary: AttemptScoreSummaryDto?,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val skillColor = when (skill) {
        "LISTENING" -> Color(0xFF3B82F6)
        "READING" -> Color(0xFF10B981)
        "WRITING" -> Color(0xFF9333EA)
        else -> Color(0xFFF59E0B)
    }
    val skillIcon = when (skill) {
        "LISTENING" -> Icons.Default.Headset
        "READING" -> Icons.Default.MenuBook
        "WRITING" -> Icons.Default.Edit
        else -> Icons.Default.Mic
    }
    
    // Calculate total questions and correct for this skill
    var totalQuestions = 0
    var correctCount = 0
    sections.forEach { section ->
        section.groups?.forEach { group ->
            group.questionGroups?.forEach { qg ->
                qg.questions?.forEach { q ->
                    totalQuestions++
                    if (q.isCorrect == true) correctCount++
                }
            }
        }
    }
    
    // Get band from scoreSummary
    val sectionScores = scoreSummary?.sections?.values?.filter { 
        it.sectionType?.uppercase() == skill 
    }
    val band = sectionScores?.firstOrNull()?.band ?: scoreSummary?.bands?.let {
        when (skill) {
            "LISTENING" -> it.listening
            "READING" -> it.reading
            "WRITING" -> it.writing
            "SPEAKING" -> it.speaking
            else -> null
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header - Always visible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Skill icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(skillColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = skillIcon,
                        contentDescription = null,
                        tint = skillColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Skill name and stats
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = skill,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainText
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$correctCount/$totalQuestions câu đúng",
                            fontSize = 13.sp,
                            color = NavBarText
                        )
                        if (band != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = skillColor.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "Band ${String.format("%.1f", band)}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = skillColor
                                )
                            }
                        }
                    }
                }
                
                // Expand indicator
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = NavBarText
                )
            }
            
            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sections.forEach { section ->
                        section.groups?.forEach { group ->
                            // Group header
                            Text(
                                text = group.title ?: "Part ${group.id}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = skillColor
                            )
                            
                            group.questionGroups?.forEach { qg ->
                                // Question group title
                                if (!qg.title.isNullOrBlank()) {
                                    Text(
                                        text = qg.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MainText
                                    )
                                }
                                
                                // Questions
                                qg.questions?.forEachIndexed { index, q ->
                                    val isCorrect = q.isCorrect
                                    val statusColor = when (isCorrect) {
                                        true -> Color(0xFF10B981)
                                        false -> Color(0xFFEF4444)
                                        null -> Color(0xFF6B7280)
                                    }
                                    
                                    // Check if this is an ESSAY or SPEAKING_PROMPT question
                                    val questionTypeUpper = q.questionType?.uppercase() ?: ""
                                    val isEssayOrSpeaking = questionTypeUpper == "ESSAY" || 
                                            questionTypeUpper == "SPEAKING_PROMPT" ||
                                            skill == "WRITING" || skill == "SPEAKING"
                                    
                                    if (isEssayOrSpeaking) {
                                        // Essay/Speaking-style card
                                        EssayQuestionCard(
                                            questionId = q.questionId ?: (index + 1),
                                            prompt = q.prompt,
                                            userAnswer = q.userAnswer?.toString() ?: "",
                                            score = q.score,
                                            aiFeedback = q.aiFeedback,
                                            skillColor = skillColor,
                                            isCorrect = isCorrect,
                                            isSpeaking = skill == "SPEAKING" || questionTypeUpper == "SPEAKING_PROMPT"
                                        )
                                    } else {
                                        // Regular question card
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = when (isCorrect) {
                                                    true -> Color(0xFFF0FDF4)
                                                    false -> Color(0xFFFEF2F2)
                                                    null -> Color.White
                                                }
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                // Question number
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(statusColor.copy(alpha = 0.15f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${q.questionId ?: (index + 1)}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = statusColor
                                                    )
                                                }
                                                
                                                // Answer info
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    // User answer
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "Trả lời:",
                                                            fontSize = 12.sp,
                                                            color = NavBarText
                                                        )
                                                        Text(
                                                            text = formatAnswer(q.userAnswer),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = statusColor
                                                        )
                                                    }
                                                    
                                                    // Correct answer (only if wrong)
                                                    if (isCorrect == false && q.correctAnswer != null) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text(
                                                                text = "Đáp án:",
                                                                fontSize = 12.sp,
                                                                color = NavBarText
                                                            )
                                                            Text(
                                                                text = formatAnswer(q.correctAnswer),
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = Color(0xFF10B981)
                                                            )
                                                        }
                                                    }
                                                }
                                                
                                                // Status icon
                                                Icon(
                                                    imageVector = when (isCorrect) {
                                                        true -> Icons.Default.CheckCircle
                                                        false -> Icons.Default.Cancel
                                                        null -> Icons.Default.HelpOutline
                                                    },
                                                    contentDescription = null,
                                                    tint = statusColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EssayQuestionCard(
    questionId: Int,
    prompt: String?,
    userAnswer: String,
    score: Double?,
    aiFeedback: Any?,
    skillColor: Color,
    isCorrect: Boolean?,
    isSpeaking: Boolean = false
) {
    var showCorrectedVersion by remember { mutableStateOf(false) }
    var showTranscript by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // ExoPlayer for audio playback
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    // Cast aiFeedback to Map
    @Suppress("UNCHECKED_CAST")
    val feedbackMap = aiFeedback as? Map<String, Any>
    val feedback = feedbackMap?.get("feedback") as? String
    val correctedVersion = feedbackMap?.get("correctedVersion") as? String
    val transcript = feedbackMap?.get("transcript") as? String
    
    // Check if userAnswer is a URL (for speaking)
    val isAudioUrl = userAnswer.startsWith("http") && 
            (userAnswer.contains("cloudinary") || userAnswer.contains(".mp4") || 
             userAnswer.contains(".m4a") || userAnswer.contains(".mp3"))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with question ID and score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(skillColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$questionId",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = if (isSpeaking) "Speaking Task" else "Task",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MainText
                    )
                }
                
                // Score badge
                if (score != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = skillColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Band ${String.format("%.1f", score)}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = skillColor
                        )
                    }
                }
            }
            
            // Question prompt
            if (!prompt.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Đề bài",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavBarText
                        )
                        Text(
                            text = prompt,
                            fontSize = 13.sp,
                            color = MainText,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            // Audio Player for Speaking (if URL)
            if (isSpeaking && isAudioUrl) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = skillColor.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = skillColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Bài nói của bạn",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = skillColor
                            )
                        }
                        
                        // Audio play button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledIconButton(
                                onClick = {
                                    if (isPlaying) {
                                        exoPlayer.pause()
                                        isPlaying = false
                                    } else {
                                        if (exoPlayer.playbackState == Player.STATE_IDLE || 
                                            exoPlayer.playbackState == Player.STATE_ENDED) {
                                            exoPlayer.setMediaItem(MediaItem.fromUri(userAnswer))
                                            exoPlayer.prepare()
                                        }
                                        exoPlayer.play()
                                        isPlaying = true
                                    }
                                },
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = skillColor
                                )
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPlaying) "Đang phát..." else "Nhấn để nghe bài nói",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MainText
                                )
                                Text(
                                    text = "Audio đã tải lên Cloudinary",
                                    fontSize = 11.sp,
                                    color = NavBarText
                                )
                            }
                        }
                    }
                }
            } else if (!isSpeaking && userAnswer.isNotBlank()) {
                // Text answer for Writing
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = skillColor.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bài làm của bạn",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = skillColor
                            )
                            Text(
                                text = "${userAnswer.split(" ").filter { it.isNotBlank() }.size} từ",
                                fontSize = 11.sp,
                                color = NavBarText
                            )
                        }
                        Divider(color = skillColor.copy(alpha = 0.2f))
                        Text(
                            text = userAnswer,
                            fontSize = 13.sp,
                            color = MainText,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
            
            // Transcript for Speaking (from AI feedback)
            if (isSpeaking && !transcript.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TextFields,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Phiên âm (Transcript)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1D4ED8)
                                )
                            }
                            
                            TextButton(
                                onClick = { showTranscript = !showTranscript },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = if (showTranscript) "Thu gọn" else "Mở rộng",
                                    fontSize = 11.sp,
                                    color = Color(0xFF3B82F6)
                                )
                            }
                        }
                        
                        AnimatedVisibility(visible = showTranscript) {
                            Column {
                                Divider(color = Color(0xFF3B82F6).copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = transcript,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E40AF),
                                    lineHeight = 22.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
            
            // AI Feedback
            if (!feedback.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LightbulbCircle,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Nhận xét từ AI",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD97706)
                            )
                        }
                        Text(
                            text = feedback,
                            fontSize = 13.sp,
                            color = Color(0xFF92400E),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            // Corrected version toggle
            if (!correctedVersion.isNullOrBlank()) {
                OutlinedButton(
                    onClick = { showCorrectedVersion = !showCorrectedVersion },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF10B981)
                    )
                ) {
                    Icon(
                        imageVector = if (showCorrectedVersion) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showCorrectedVersion) "Ẩn bài sửa" else "Xem bài sửa gợi ý",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                AnimatedVisibility(visible = showCorrectedVersion) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isSpeaking) "Bài nói gợi ý" else "Bài sửa gợi ý",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF059669)
                                )
                            }
                            Divider(color = Color(0xFF10B981).copy(alpha = 0.2f))
                            Text(
                                text = correctedVersion,
                                fontSize = 13.sp,
                                color = Color(0xFF065F46),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatAnswer(answer: Any?): String {
    return when (answer) {
        is List<*> -> answer.joinToString(", ")
        null -> "—"
        else -> answer.toString()
    }
}

@Composable
private fun InfoColumn(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GradientStart,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = NavBarText)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MainText)
    }
}

@Composable
private fun StatMini(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = NavBarText
        )
    }
}
