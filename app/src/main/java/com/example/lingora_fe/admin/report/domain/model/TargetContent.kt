package com.example.lingora_fe.admin.report.domain.model

import com.example.lingora_fe.user.forum.domain.model.Post
import com.example.lingora_fe.user.studyset.domain.model.StudySet
import com.example.lingora_fe.user.forum.domain.model.Comment

// Sealed class for target content
sealed class TargetContent {
    data class PostContent(val post: Post) : TargetContent()
    data class StudySetContent(val studySet: StudySet) : TargetContent()
    data class CommentContent(val comment: Comment) : TargetContent()
}
