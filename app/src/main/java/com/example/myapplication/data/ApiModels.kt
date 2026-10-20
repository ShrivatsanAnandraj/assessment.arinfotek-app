package com.example.myapplication.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Objects matching the JSON returned by the live
 * AR INFOTEK assessment API (https://arinfotek-assesment.vercel.app/api/...).
 */

@Serializable
data class ApiTest(
    val id: Int,
    val title: String = "",
    val subject: String = "General",
    val course: String = "",
    val level: String = "",
    val topics: List<String> = emptyList(),
    @SerialName("test_code") val testCode: String = "",
    @SerialName("duration_minutes") val durationMinutes: Int = 30
)

@Serializable
data class ApiQuestion(
    val id: Int,
    @SerialName("question_text") val questionText: String = "",
    val options: List<String> = emptyList(),
    @SerialName("correct_answer") val correctAnswer: Int? = null
)

@Serializable
data class ApiTestResponse(
    val test: ApiTest? = null,
    val questions: List<ApiQuestion> = emptyList(),
    @SerialName("paperId") val paperId: Int? = null,
    val error: String? = null
)

@Serializable
data class ApiAttempt(
    val id: Int,
    @SerialName("student_name") val studentName: String = "",
    @SerialName("student_register_id") val studentRegisterId: String = "",
    val score: Int = 0,
    val total: Int = 0,
    @SerialName("submitted_at") val submittedAt: String = "",
    @SerialName("test_title") val testTitle: String = "",
    @SerialName("test_code") val testCode: String = ""
)

@Serializable
data class ApiScoresResponse(
    val tests: List<ApiTest> = emptyList(),
    val attempts: List<ApiAttempt> = emptyList()
)

@Serializable
data class ApiTabFlag(
    val id: Int,
    @SerialName("test_code") val testCode: String = "",
    @SerialName("student_register_id") val studentRegisterId: String = "",
    @SerialName("student_name") val studentName: String = "",
    val reason: String = "Tabs changing found",
    val status: String = "flagged",
    @SerialName("violation_count") val violationCount: Int = 1,
    val violations: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("resolved_at") val resolvedAt: String? = null
)

@Serializable
data class ApiFlagsResponse(
    val flags: List<ApiTabFlag> = emptyList(),
    val flag: ApiTabFlag? = null
)

@Serializable
data class ApiFlagStatusResponse(val status: String = "none")

@Serializable
data class ApiSurveyQuestion(
    val text: String,
    val options: List<String>? = null,
    val nps: Boolean = false
)

@Serializable
data class ApiSurvey(
    val id: Int,
    @SerialName("test_code") val testCode: String = "",
    val course: String = "",
    val trainee: String = "",
    @SerialName("no_of_days") val noOfDays: Int = 0,
    @SerialName("template_name") val templateName: String = "",
    val questions: List<ApiSurveyQuestion> = emptyList(),
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class ApiSurveysResponse(
    val surveys: List<ApiSurvey> = emptyList(),
    val templates: List<String> = emptyList(),
    val error: String? = null
)

@Serializable
data class ApiSurveyCreateResponse(
    val survey: ApiSurvey? = null,
    val response: ApiSurveyResponseItem? = null,
    val error: String? = null
)

@Serializable
data class ApiSurveyResponseItem(
    val id: Int,
    @SerialName("survey_id") val surveyId: Int = 0,
    @SerialName("student_name") val studentName: String = "",
    @SerialName("student_register_id") val studentRegisterId: String = "",
    @SerialName("test_code") val testCode: String = "",
    val answers: Map<String, String> = emptyMap(),
    @SerialName("submitted_at") val submittedAt: String = "",
    val course: String = "",
    val trainee: String = "",
    @SerialName("template_name") val templateName: String = "",
    val questions: List<ApiSurveyQuestion> = emptyList()
)

@Serializable
data class ApiSurveyResponsesResponse(
    val responses: List<ApiSurveyResponseItem> = emptyList()
)

@Serializable
data class ApiAdminResponse(
    val tests: List<ApiAdminTest> = emptyList(),
    val error: String? = null
)

@Serializable
data class ApiAdminTest(
    val id: Int,
    val title: String = "",
    val subject: String = "General",
    val course: String = "",
    val level: String = "",
    val topics: List<String> = emptyList(),
    @SerialName("test_code") val testCode: String = "",
    @SerialName("duration_minutes") val durationMinutes: Int = 30,
    val questions: List<ApiQuestion> = emptyList()
)

@Serializable
data class ApiSubmitResponse(
    val score: Int = 0,
    val total: Int = 0,
    val percentage: Int = 0,
    val passed: Boolean = false,
    val review: List<ApiReviewItem> = emptyList(),
    val error: String? = null
)

@Serializable
data class ApiReviewItem(
    val id: Int,
    val question: String = "",
    val options: List<String> = emptyList(),
    @SerialName("correctAnswer") val correctAnswer: Int = 0,
    @SerialName("selectedAnswer") val selectedAnswer: Int? = null
)

@Serializable
data class ApiTemplateDetailsResponse(
    val templates: Map<String, ApiTemplateDetail> = emptyMap()
)

@Serializable
data class ApiTemplateDetail(
    val name: String = "",
    val questions: List<ApiSurveyQuestion> = emptyList()
)
