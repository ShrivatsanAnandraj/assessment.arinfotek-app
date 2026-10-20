package com.example.myapplication.data

import kotlinx.serialization.Serializable

/**
 * Data models mirroring the PostgreSQL schema used by the AR INFOTEK
 * assessment web app (tests, questions, attempts, tab_flags,
 * question_papers, surveys, survey_responses).
 */

@Serializable
data class Test(
    val id: Int,
    val title: String,
    val subject: String = "General",
    val course: String = "",
    val level: String = "",
    val topics: List<String> = emptyList(),
    val testCode: String,
    val durationMinutes: Int = 30
)

@Serializable
data class Question(
    val id: Int,
    val testId: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: Int
)

@Serializable
data class Attempt(
    val id: Int,
    val testCode: String,
    val studentName: String,
    val studentRegisterId: String,
    val score: Int,
    val total: Int,
    val answers: Map<String, Int> = emptyMap(),
    val submittedAt: Long = System.currentTimeMillis()
)

@Serializable
data class TabFlag(
    val id: Int,
    val testCode: String,
    val studentRegisterId: String,
    val studentName: String,
    val reason: String = "Tabs changing found",
    val status: String = "flagged",
    val violationCount: Int = 1,
    val violations: List<Long> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

@Serializable
data class Survey(
    val id: Int,
    val testCode: String,
    val course: String,
    val trainee: String,
    val noOfDays: Int,
    val templateName: String,
    val questions: List<SurveyQuestion>
)

@Serializable
data class SurveyQuestion(
    val text: String,
    val options: List<String>? = null,
    val nps: Boolean = false
)

@Serializable
data class SurveyResponse(
    val id: Int,
    val surveyId: Int,
    val studentName: String,
    val studentRegisterId: String,
    val testCode: String,
    val answers: Map<String, String> = emptyMap(),
    val questions: List<SurveyQuestion> = emptyList(),
    val submittedAt: Long = System.currentTimeMillis(),
    val course: String = "",
    val trainee: String = "",
    val templateName: String = ""
)

@Serializable
data class AppData(
    val tests: List<Test> = emptyList(),
    val questions: List<Question> = emptyList(),
    val attempts: List<Attempt> = emptyList(),
    val tabFlags: List<TabFlag> = emptyList(),
    val surveys: List<Survey> = emptyList(),
    val surveyResponses: List<SurveyResponse> = emptyList()
)

/** Runtime holder for a generated paper (mirrors question_papers table). */
@Serializable
data class GeneratedPaper(
    val paperId: Int,
    val testCode: String,
    val studentName: String,
    val studentRegisterId: String,
    val questions: List<PaperQuestion>,
    val answerKey: List<Int>
)

@Serializable
data class PaperQuestion(
    val id: Int,
    val questionText: String,
    val options: List<String>
)

/** Result of submitting an attempt. */
@Serializable
data class TestResult(
    val score: Int,
    val total: Int,
    val percentage: Int,
    val passed: Boolean,
    val review: List<ReviewItem>
)

@Serializable
data class ReviewItem(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val selectedAnswer: Int? = null
)
