package com.example.myapplication.data

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Talks to the live AR INFOTEK assessment API and maps the JSON DTOs
 * into the app's domain models.
 */
class ApiRepository(private val client: ApiClient = ApiClient()) {

    private val json = client.serializer

    // ---- Tests ----------------------------------------------------------

    /** Fetches every test (with its questions) from the admin endpoint. */
    suspend fun fetchTestsWithQuestions(): List<Pair<Test, List<Question>>> {
        val resp = client.get("/admin", ApiAdminResponse.serializer())
        return resp.tests.map { dto ->
            val test = dto.toTest()
            test to dto.questions.map { it.toQuestion(test.id) }
        }
    }

    /** Fetches every test (flat list) from the admin endpoint. */
    suspend fun fetchTests(): List<Test> = fetchTestsWithQuestions().map { it.first }

    /** Fetches a test (and questions) by its test code. */
    suspend fun fetchTestByCode(code: String): Pair<Test, List<Question>>? {
        val resp = client.get("/tests", ApiTestResponse.serializer(), mapOf("code" to code))
        val dto = resp.test ?: return null
        val test = dto.toTest()
        val questions = resp.questions.map { it.toQuestion(test.id) }
        return test to questions
    }

    suspend fun fetchAttempts(): List<Attempt> {
        val resp = client.get("/scores", ApiScoresResponse.serializer())
        return resp.attempts.map { it.toAttempt() }
    }

    suspend fun fetchTestsScores(): List<Test> {
        val resp = client.get("/scores", ApiScoresResponse.serializer())
        return resp.tests.map { it.toTest() }
    }

    // ---- Attempts / scoring --------------------------------------------

    suspend fun submitAttempt(
        testId: Int,
        studentName: String,
        studentRegisterId: String,
        answers: Map<String, Int>
    ): TestResult {
        val payload = buildJsonObject {
            put("testId", testId)
            put("studentName", studentName)
            put("studentRegisterId", studentRegisterId)
            put(
                "answers",
                buildJsonObject {
                    answers.forEach { (k, v) -> put(k, v) }
                }
            )
        }
        val resp = client.post("/submit", ApiSubmitResponse.serializer(), payload.toString())
        return resp.toResult()
    }

    // ---- Tab flags ------------------------------------------------------

    suspend fun fetchFlags(): List<TabFlag> {
        val resp = client.get("/tabflags", ApiFlagsResponse.serializer(), mapOf("action" to "list"))
        return resp.flags.map { it.toTabFlag() }
    }

    suspend fun createFlag(
        testCode: String,
        studentName: String,
        studentRegisterId: String,
        violations: List<Long>
    ) {
        val payload = buildJsonObject {
            put("test_code", testCode)
            put("student_name", studentName)
            put("student_register_id", studentRegisterId)
            put("reason", "Tabs changing found")
            put(
                "violations",
                buildJsonArray { violations.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } }
            )
        }
        runCatching { client.post("/tabflags", ApiFlagsResponse.serializer(), payload.toString()) }
    }

    suspend fun resolveFlag(
        testCode: String,
        studentRegisterId: String,
        status: String
    ) {
        val payload = buildJsonObject {
            put("test_code", testCode)
            put("student_register_id", studentRegisterId)
            put("status", status)
        }
        client.post("/tabflags?action=update", ApiFlagsResponse.serializer(), payload.toString())
    }

    suspend fun flagStatus(testCode: String, studentRegisterId: String): String {
        return runCatching {
            client.get(
                "/tabflags",
                ApiFlagStatusResponse.serializer(),
                mapOf("action" to "status", "test_code" to testCode, "student_register_id" to studentRegisterId)
            ).status
        }.getOrDefault("none")
    }

    // ---- Surveys --------------------------------------------------------

    suspend fun fetchSurveys(): List<Survey> {
        val resp = client.get("/surveys", ApiSurveysResponse.serializer())
        return resp.surveys.map { it.toSurvey() }
    }

    suspend fun fetchSurveyResponses(): List<SurveyResponse> {
        val resp = client.get("/surveys", ApiSurveyResponsesResponse.serializer(), mapOf("action" to "responses"))
        return resp.responses.map { it.toSurveyResponse() }
    }

    suspend fun createSurvey(
        testCode: String,
        course: String,
        trainee: String,
        noOfDays: Int,
        templateName: String
    ): Survey? {
        val payload = buildJsonObject {
            put("action", "create")
            put("test_code", testCode)
            put("course", course)
            put("trainee", trainee)
            put("no_of_days", noOfDays)
            put("template_name", templateName)
        }
        val resp = client.post("/surveys", ApiSurveyCreateResponse.serializer(), payload.toString())
        return resp.survey?.toSurvey()
    }

    suspend fun submitSurveyResponse(
        surveyId: Int,
        studentName: String,
        studentRegisterId: String,
        testCode: String,
        answers: Map<String, String>
    ) {
        val payload = buildJsonObject {
            put("action", "submit")
            put("survey_id", surveyId)
            put("student_name", studentName)
            put("student_register_id", studentRegisterId)
            put("test_code", testCode)
            put(
                "answers",
                buildJsonObject { answers.forEach { (k, v) -> put(k, v) } }
            )
        }
        client.post("/surveys", ApiSurveyCreateResponse.serializer(), payload.toString())
    }

    suspend fun fetchTemplateDetails(): Map<String, List<SurveyQuestion>> {
        val resp = client.get(
            "/surveys",
            ApiTemplateDetailsResponse.serializer(),
            mapOf("action" to "template_details")
        )
        return resp.templates.mapValues { (_, detail) -> detail.questions.map { it.toSurveyQuestion() } }
    }

    suspend fun fetchTemplates(): List<String> {
        val resp = client.get("/surveys", ApiSurveysResponse.serializer())
        return resp.templates
    }
}

// ---- DTO -> domain mappers ---------------------------------------------

private fun ApiTest.toTest() = Test(
    id = id,
    title = title,
    subject = subject,
    course = course,
    level = level,
    topics = topics,
    testCode = testCode,
    durationMinutes = durationMinutes
)

private fun ApiAdminTest.toTest() = Test(
    id = id,
    title = title,
    subject = subject,
    course = course,
    level = level,
    topics = topics,
    testCode = testCode,
    durationMinutes = durationMinutes
)

private fun ApiQuestion.toQuestion(testId: Int) = Question(
    id = id,
    testId = testId,
    questionText = questionText,
    options = options,
    correctAnswer = correctAnswer ?: 0
)

private fun ApiAttempt.toAttempt() = Attempt(
    id = id,
    testCode = testCode,
    studentName = studentName,
    studentRegisterId = studentRegisterId,
    score = score,
    total = total,
    submittedAt = parseTime(submittedAt)
)

private fun ApiTabFlag.toTabFlag() = TabFlag(
    id = id,
    testCode = testCode,
    studentRegisterId = studentRegisterId,
    studentName = studentName,
    reason = reason,
    status = status,
    violationCount = violationCount,
    violations = violations.map { parseTime(it) },
    createdAt = parseTime(createdAt),
    resolvedAt = resolvedAt?.let { parseTime(it) }
)

private fun ApiSurveyQuestion.toSurveyQuestion() = SurveyQuestion(
    text = text,
    options = options,
    nps = nps
)

private fun ApiSurvey.toSurvey() = Survey(
    id = id,
    testCode = testCode,
    course = course,
    trainee = trainee,
    noOfDays = noOfDays,
    templateName = templateName,
    questions = questions.map { it.toSurveyQuestion() }
)

private fun ApiSurveyResponseItem.toSurveyResponse() = SurveyResponse(
    id = id,
    surveyId = surveyId,
    studentName = studentName,
    studentRegisterId = studentRegisterId,
    testCode = testCode,
    answers = answers,
    questions = questions.map { it.toSurveyQuestion() },
    submittedAt = parseTime(submittedAt),
    course = course,
    trainee = trainee,
    templateName = templateName
)

private fun ApiSubmitResponse.toResult() = TestResult(
    score = score,
    total = total,
    percentage = percentage,
    passed = passed,
    review = review.map {
        ReviewItem(
            id = it.id,
            question = it.question,
            options = it.options,
            correctAnswer = it.correctAnswer,
            selectedAnswer = it.selectedAnswer
        )
    }
)

/** Attempts to parse an ISO-8601 timestamp; falls back to "now" on failure. */
internal fun parseTime(value: String): Long {
    if (value.isBlank()) return System.currentTimeMillis()
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    for (pattern in patterns) {
        val parsed = runCatching {
            java.text.SimpleDateFormat(pattern, java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.parse(value)?.time
        }.getOrNull()
        if (parsed != null) return parsed
    }
    return System.currentTimeMillis()
}
