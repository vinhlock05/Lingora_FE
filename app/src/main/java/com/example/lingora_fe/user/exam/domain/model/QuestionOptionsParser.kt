package com.example.lingora_fe.user.exam.domain.model

/**
 * Helper data class for MATCHING question options
 */
data class MatchingOption(
    val key: String,
    val value: String
)

/**
 * Helper object to parse question options based on question type
 */
object QuestionOptionsParser {
    
    /**
     * Parse options for MATCHING questions
     * Input: List<Map<String, String>> with keys "key" and "value"
     * Output: List<MatchingOption>
     */
    fun parseMatchingOptions(options: Any?): List<MatchingOption> {
        if (options == null) return emptyList()
        
        return try {
            @Suppress("UNCHECKED_CAST")
            val optionsList = options as? List<Map<String, Any>> ?: return emptyList()
            
            optionsList.mapNotNull { map ->
                val key = map["key"] as? String
                val value = map["value"] as? String
                if (key != null && value != null) {
                    MatchingOption(key, value)
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Parse options for MULTIPLE_CHOICE, YES_NO_NOT_GIVEN, etc.
     * Input: List<String>
     * Output: List<String>
     */
    fun parseSimpleOptions(options: Any?): List<String> {
        if (options == null) return emptyList()
        
        return try {
            @Suppress("UNCHECKED_CAST")
            options as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get metadata value with type safety
     */
    inline fun <reified T> getMetadataValue(metadata: Map<String, Any>?, key: String): T? {
        return metadata?.get(key) as? T
    }
    
    /**
     * Check if question allows multiple selection
     */
    fun allowsMultipleSelection(metadata: Map<String, Any>?): Boolean {
        return getMetadataValue<Boolean>(metadata, "allowMultiple") == true
    }
    
    /**
     * Get max selection count for multiple choice questions
     */
    fun getMaxSelection(metadata: Map<String, Any>?): Int? {
        return getMetadataValue<Int>(metadata, "maxSelection")
            ?: getMetadataValue<Double>(metadata, "maxSelection")?.toInt()
    }
    
    /**
     * Get word limit for fill-in-the-blank or note completion questions
     */
    fun getWordLimit(metadata: Map<String, Any>?): Int? {
        return getMetadataValue<Int>(metadata, "wordLimit")
            ?: getMetadataValue<Int>(metadata, "maxLength")
            ?: getMetadataValue<Double>(metadata, "wordLimit")?.toInt()
            ?: getMetadataValue<Double>(metadata, "maxLength")?.toInt()
    }
    
    /**
     * Get image URL from metadata
     */
    fun getImageUrl(metadata: Map<String, Any>?): String? {
        return getMetadataValue<String>(metadata, "imageUrl")
    }
    
    /**
     * Get minimum word count for essay questions
     */
    fun getMinWordCount(metadata: Map<String, Any>?): Int? {
        return getMetadataValue<Int>(metadata, "word_count_minimum")
            ?: getMetadataValue<Double>(metadata, "word_count_minimum")?.toInt()
    }
    
    /**
     * Validate answer based on question type and metadata
     */
    fun validateAnswer(
        questionType: ExamQuestionType,
        answer: Any?,
        metadata: Map<String, Any>?
    ): ValidationResult {
        return when (questionType) {
            ExamQuestionType.FILL_IN_THE_BLANK,
            ExamQuestionType.NOTE_COMPLETION -> {
                val text = answer as? String ?: return ValidationResult.Invalid("Answer cannot be empty")
                val wordLimit = getWordLimit(metadata)
                
                if (wordLimit != null) {
                    val wordCount = text.trim().split("\\s+".toRegex()).size
                    if (wordCount > wordLimit) {
                        return ValidationResult.Invalid("Maximum $wordLimit words allowed")
                    }
                }
                ValidationResult.Valid
            }
            
            ExamQuestionType.ESSAY -> {
                val text = answer as? String ?: return ValidationResult.Invalid("Essay cannot be empty")
                val minWordCount = getMinWordCount(metadata)
                
                if (minWordCount != null) {
                    val wordCount = text.trim().split("\\s+".toRegex()).size
                    if (wordCount < minWordCount) {
                        return ValidationResult.Invalid("Minimum $minWordCount words required")
                    }
                }
                ValidationResult.Valid
            }
            
            ExamQuestionType.MULTIPLE_CHOICE -> {
                if (allowsMultipleSelection(metadata)) {
                    val selected = answer as? List<*> ?: return ValidationResult.Invalid("Please select at least one option")
                    val maxSelection = getMaxSelection(metadata)
                    
                    if (maxSelection != null && selected.size > maxSelection) {
                        return ValidationResult.Invalid("Maximum $maxSelection options allowed")
                    }
                }
                ValidationResult.Valid
            }
            
            else -> ValidationResult.Valid
        }
    }
}

/**
 * Result of answer validation
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}
