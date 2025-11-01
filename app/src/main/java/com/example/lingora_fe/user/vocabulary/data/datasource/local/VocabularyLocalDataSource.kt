package com.example.lingora_fe.user.vocabulary.data.datasource.local

import com.example.lingora_fe.user.vocabulary.data.local.VocabularyDatabase
import com.example.lingora_fe.user.vocabulary.data.local.entity.CategoryEntity
import com.example.lingora_fe.user.vocabulary.data.local.entity.CategoryTopicEntity
import com.example.lingora_fe.user.vocabulary.data.local.entity.TopicEntity
import com.example.lingora_fe.user.vocabulary.data.local.entity.UserCategoryProgressEntity
import com.example.lingora_fe.user.vocabulary.data.local.entity.UserTopicProgressEntity
import com.example.lingora_fe.user.vocabulary.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VocabularyLocalDataSource @Inject constructor(
    private val database: VocabularyDatabase
) {
    // Category operations
    fun observeCategories(): Flow<List<CategoryEntity>> {
        return database.categoryDao().observeAll()
    }

    suspend fun getCategories(): List<CategoryEntity> {
        return database.categoryDao().getAll()
    }

    suspend fun getCategoryById(categoryId: Int): CategoryEntity? {
        return database.categoryDao().getById(categoryId)
    }

    suspend fun insertCategory(category: CategoryEntity) {
        database.categoryDao().insert(category)
    }

    suspend fun insertCategories(categories: List<CategoryEntity>) {
        database.categoryDao().insertAll(categories)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        database.categoryDao().update(category)
    }

    suspend fun deleteCategory(categoryId: Int) {
        database.categoryDao().deleteById(categoryId)
    }

    // Topic operations
    fun observeTopicsByCategory(categoryId: Int): Flow<List<TopicEntity>> {
        return database.topicDao().observeByCategory(categoryId)
    }

    suspend fun getTopics(): List<TopicEntity> {
        return database.topicDao().getAll()
    }

    suspend fun getTopicById(topicId: Int): TopicEntity? {
        return database.topicDao().getById(topicId)
    }

    suspend fun getTopicsByCategory(categoryId: Int): List<TopicEntity> {
        return database.topicDao().getByCategory(categoryId)
    }

    suspend fun insertTopic(topic: TopicEntity) {
        database.topicDao().insert(topic)
    }

    suspend fun insertTopics(topics: List<TopicEntity>) {
        database.topicDao().insertAll(topics)
    }

    suspend fun updateTopic(topic: TopicEntity) {
        database.topicDao().update(topic)
    }

    suspend fun deleteTopic(topicId: Int) {
        database.topicDao().deleteById(topicId)
    }

    // Word operations
    fun observeWordsByTopic(topicId: Int): Flow<List<WordEntity>> {
        return database.wordDao().observeByTopic(topicId)
    }

    suspend fun getWordsByTopic(topicId: Int): List<WordEntity> {
        return database.wordDao().getByTopic(topicId)
    }

    suspend fun getWordById(wordId: Int): WordEntity? {
        return database.wordDao().getById(wordId)
    }

    suspend fun insertWord(word: WordEntity) {
        database.wordDao().insert(word)
    }

    suspend fun insertWords(words: List<WordEntity>) {
        database.wordDao().insertAll(words)
    }

    suspend fun updateWord(word: WordEntity) {
        database.wordDao().update(word)
    }

    // CategoryTopic operations
    suspend fun insertCategoryTopics(categoryTopics: List<CategoryTopicEntity>) {
        database.categoryTopicDao().insertAll(categoryTopics)
    }

    // User progress operations
    fun observeCategoryProgress(userId: Int): Flow<List<UserCategoryProgressEntity>> {
        return database.userCategoryProgressDao().observeByUser(userId)
    }

    suspend fun getCategoryProgress(userId: Int): List<UserCategoryProgressEntity> {
        return database.userCategoryProgressDao().getByUser(userId)
    }

    suspend fun getCategoryProgressById(userId: Int, categoryId: Int): UserCategoryProgressEntity? {
        return database.userCategoryProgressDao().getByUserAndCategory(userId, categoryId)
    }

    suspend fun insertCategoryProgress(progress: UserCategoryProgressEntity) {
        database.userCategoryProgressDao().insert(progress)
    }

    suspend fun insertCategoryProgressList(progressList: List<UserCategoryProgressEntity>) {
        database.userCategoryProgressDao().insertAll(progressList)
    }

    suspend fun updateCategoryProgress(progress: UserCategoryProgressEntity) {
        database.userCategoryProgressDao().update(progress)
    }

    fun observeTopicProgress(userId: Int, topicId: Int): Flow<UserTopicProgressEntity?> {
        return database.userTopicProgressDao().observeByUserAndTopic(userId, topicId)
    }

    suspend fun getTopicProgress(userId: Int, topicId: Int): UserTopicProgressEntity? {
        return database.userTopicProgressDao().getByUserAndTopic(userId, topicId)
    }

    suspend fun insertTopicProgress(progress: UserTopicProgressEntity) {
        database.userTopicProgressDao().insert(progress)
    }

    suspend fun updateTopicProgress(progress: UserTopicProgressEntity) {
        database.userTopicProgressDao().update(progress)
    }
}

