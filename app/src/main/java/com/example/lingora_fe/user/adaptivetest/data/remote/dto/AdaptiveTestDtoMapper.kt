package com.example.lingora_fe.user.adaptivetest.data.remote.dto

import com.example.lingora_fe.user.adaptivetest.domain.model.*

fun PublicAdaptiveQuestionDto.toDomainModel(): PublicAdaptiveQuestion {
    return PublicAdaptiveQuestion(
        id = id,
        skill = skill,
        text = text,
        options = options,
        proficiency = ProficiencyLevel.fromString(proficiency) ?: ProficiencyLevel.BEGINNER
    )
}

fun QuestionBankMetaData.toDomainModel(): QuestionBank {
    return QuestionBank(
        beginner = beginner.map { it.toDomainModel() },
        intermediate = intermediate.map { it.toDomainModel() },
        advanced = advanced.map { it.toDomainModel() }
    )
}

fun AnswerEvaluationDto.toDomainModel(): AnswerEvaluation {
    return AnswerEvaluation(
        questionId = questionId,
        isCorrect = isCorrect,
        proficiency = ProficiencyLevel.fromString(proficiency) ?: ProficiencyLevel.BEGINNER
    )
}

fun NextQuestionMetaData.toDomainModel(): NextQuestionResult {
    return NextQuestionResult(
        currentProficiency = ProficiencyLevel.fromString(currentProficiency) ?: ProficiencyLevel.BEGINNER,
        answeredCount = answeredCount,
        answerEvaluations = answerEvaluations.map { it.toDomainModel() },
        isCompleted = isCompleted,
        nextQuestion = nextQuestion?.toDomainModel(),
        proficiency = proficiency?.let { ProficiencyLevel.fromString(it) }
    )
}

fun AdaptiveTestAnswer.toDto(): AdaptiveTestAnswerDto {
    return AdaptiveTestAnswerDto(
        questionId = questionId,
        answer = answer
    )
}

