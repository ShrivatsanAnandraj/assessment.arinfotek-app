package com.example.myapplication

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.myapplication.data.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

enum class Screen { JOIN, TEST, RESULT, SURVEY, ADMIN }

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(app)

    private var data: AppData = repo.load()
    private var papers: MutableList<GeneratedPaper> = repo.loadPapers().toMutableList()

    // ---- Navigation / session ----
    var screen by mutableStateOf(Screen.JOIN)
        private set
    var studentName by mutableStateOf("")
        private set
    var studentRegisterId by mutableStateOf("")
        private set

    // ---- Verified test info (sync: local, async: remote) ----
    var verifiedTest by mutableStateOf<Test?>(null)
        private set
    var verifying by mutableStateOf(false)
        private set
    var joinError by mutableStateOf("")
        internal set

    // ---- Active test ----
    var activePaper by mutableStateOf<GeneratedPaper?>(null)
        private set
    var generatedTestTitle by mutableStateOf("")
        private set

    // ---- Result ----
    var result by mutableStateOf<TestResult?>(null)
        private set

    // ---- Active survey ----
    var activeSurvey by mutableStateOf<Survey?>(null)
        private set

    // ---- Admin ----
    var adminFlags = mutableStateListOf<TabFlag>()
        private set

    val templates: List<String> = SurveyTemplates.templates.keys.toList()

    // ---------------------------------------------------------------
    // Join / verify
    // ---------------------------------------------------------------
    fun verifyTestCode(name: String, regId: String, code: String, onVerified: (Boolean) -> Unit) {
        joinError = ""
        if (name.isBlank() || regId.isBlank() || code.isBlank()) {
            joinError = "Please fill in all fields."
            onVerified(false)
            return
        }
        verifying = true
        val upper = code.trim().uppercase()
        val found = data.tests.firstOrNull { it.testCode.equals(upper, ignoreCase = true) }
        verifying = false
        if (found == null) {
            joinError = "Invalid test code"
            onVerified(false)
            return
        }
        studentName = name.trim()
        studentRegisterId = regId.trim()
        verifiedTest = found
        onVerified(true)
    }

    fun resetVerification() {
        verifiedTest = null
        joinError = ""
    }

    // ---------------------------------------------------------------
    // Test taking
    // ---------------------------------------------------------------
    fun startTest() {
        val test = verifiedTest ?: return
        val qs = data.questions.filter { it.testId == test.id }
        val paperQuestions = qs.mapIndexed { index, q ->
            PaperQuestion(id = index, questionText = q.questionText, options = q.options)
        }
        val key = qs.map { it.correctAnswer }
        val paperId = (papers.maxOfOrNull { it.paperId } ?: 0) + 1
        val paper = GeneratedPaper(
            paperId = paperId,
            testCode = test.testCode,
            studentName = studentName,
            studentRegisterId = studentRegisterId,
            questions = paperQuestions,
            answerKey = key
        )
        papers.add(paper)
        repo.savePapers(papers)
        activePaper = paper
        generatedTestTitle = test.title
        screen = Screen.TEST
    }

    fun submitTest(answers: Map<Int, Int>) {
        val paper = activePaper ?: return
        var score = 0
        val review = paper.questions.mapIndexed { i, q ->
            val correct = paper.answerKey.getOrElse(i) { 0 }
            val selected = answers[q.id]
            if (selected != null && selected == correct) score++
            ReviewItem(
                id = q.id,
                question = q.questionText,
                options = q.options,
                correctAnswer = correct,
                selectedAnswer = selected
            )
        }
        val total = paper.questions.size
        val percentage: Int = if (total > 0) (score * 100f / total).roundToInt() else 0
        val passed = percentage >= 40

        val attempt = Attempt(
            id = (data.attempts.maxOfOrNull { it.id } ?: 0) + 1,
            testCode = paper.testCode,
            studentName = studentName,
            studentRegisterId = studentRegisterId,
            score = score,
            total = total,
            answers = answers.mapKeys { it.key.toString() }
        )
        data = data.copy(attempts = data.attempts + attempt)
        repo.save(data)

        result = TestResult(score, total, percentage, passed, review)
        screen = Screen.RESULT
    }

    fun flagTabViolation() {
        val test = verifiedTest ?: return
        val now: Long = System.currentTimeMillis()
        val existing = data.tabFlags.firstOrNull {
            it.testCode == test.testCode &&
                it.studentRegisterId == studentRegisterId &&
                it.status == "flagged"
        }
        data = if (existing != null) {
            val updated = existing.copy(
                violationCount = existing.violationCount + 1,
                violations = existing.violations + now
            )
            data.copy(tabFlags = data.tabFlags.map { if (it.id == existing.id) updated else it })
        } else {
            val flag = TabFlag(
                id = (data.tabFlags.maxOfOrNull { it.id } ?: 0) + 1,
                testCode = test.testCode,
                studentRegisterId = studentRegisterId,
                studentName = studentName,
                violations = listOf(now)
            )
            data.copy(tabFlags = data.tabFlags + flag)
        }
        repo.save(data)
        refreshFlags()
    }

    fun flagStatusForCurrentStudent(): String {
        val test = verifiedTest ?: return "none"
        return data.tabFlags.firstOrNull {
            it.testCode == test.testCode && it.studentRegisterId == studentRegisterId
        }?.status ?: "none"
    }

    // ---------------------------------------------------------------
    // Survey
    // ---------------------------------------------------------------
    fun openSurveyForCurrentTest(): Boolean {
        val test = verifiedTest ?: return false
        val survey = data.surveys.firstOrNull {
            it.testCode.equals(test.testCode, ignoreCase = true)
        }
        if (survey == null) return false
        activeSurvey = survey
        screen = Screen.SURVEY
        return true
    }

    fun submitSurvey(answers: Map<Int, String>) {
        val survey = activeSurvey ?: return
        val response = SurveyResponse(
            id = (data.surveyResponses.maxOfOrNull { it.id } ?: 0) + 1,
            surveyId = survey.id,
            studentName = studentName,
            studentRegisterId = studentRegisterId,
            testCode = survey.testCode,
            answers = answers.mapKeys { it.key.toString() },
            questions = survey.questions,
            course = survey.course,
            trainee = survey.trainee,
            templateName = survey.templateName
        )
        data = data.copy(surveyResponses = data.surveyResponses + response)
        repo.save(data)
    }

    fun finishSession() {
        activeSurvey = null
        activePaper = null
        result = null
        verifiedTest = null
        joinError = ""
        screen = Screen.JOIN
    }

    fun openAdmin() {
        refreshFlags()
        screen = Screen.ADMIN
    }

    // ---------------------------------------------------------------
    // Admin: tests & questions
    // ---------------------------------------------------------------
    fun allTests(): List<Test> = data.tests

    fun questionsForTest(testId: Int): List<Question> =
        data.questions.filter { it.testId == testId }

    fun createTest(
        title: String,
        testCode: String,
        duration: Int,
        subject: String,
        course: String,
        level: String,
        topics: String,
        questions: List<Question>
    ): String? {
        if (title.isBlank() || testCode.isBlank()) return "Test title and code are required."
        val upper = testCode.trim().uppercase()
        if (data.tests.any { it.testCode.equals(upper, ignoreCase = true) }) {
            return "Test code already exists"
        }
        val newId = (data.tests.maxOfOrNull { it.id } ?: 0) + 1
        val newTest = Test(
            id = newId,
            title = title.trim(),
            subject = subject.ifBlank { course.ifBlank { "General" } },
            course = course.trim(),
            level = level.trim(),
            topics = topics.split(",").map { it.trim() }.filter { it.isNotBlank() },
            testCode = upper,
            durationMinutes = duration
        )
        var qId = (data.questions.maxOfOrNull { it.id } ?: 0) + 1
        val newQuestions = questions.map {
            Question(
                id = qId++,
                testId = newId,
                questionText = it.questionText,
                options = it.options,
                correctAnswer = it.correctAnswer
            )
        }
        data = data.copy(tests = data.tests + newTest, questions = data.questions + newQuestions)
        repo.save(data)
        return null
    }

    fun deleteTest(testCode: String) {
        val test = data.tests.firstOrNull { it.testCode.equals(testCode, ignoreCase = true) } ?: return
        data = data.copy(
            tests = data.tests.filter { it.id != test.id },
            questions = data.questions.filter { it.testId != test.id },
            attempts = data.attempts.filter { !it.testCode.equals(testCode, ignoreCase = true) },
            surveys = data.surveys.filter { !it.testCode.equals(testCode, ignoreCase = true) },
            surveyResponses = data.surveyResponses.filter { !it.testCode.equals(testCode, ignoreCase = true) }
        )
        repo.save(data)
    }

    // ---------------------------------------------------------------
    // Admin: scores / attempts
    // ---------------------------------------------------------------
    fun allAttempts(): List<Attempt> = data.attempts.sortedByDescending { it.submittedAt }

    fun deleteAttempt(id: Int) {
        data = data.copy(attempts = data.attempts.filter { it.id != id })
        repo.save(data)
    }

    fun deleteAllAttempts() {
        data = data.copy(attempts = emptyList())
        repo.save(data)
    }

    // ---------------------------------------------------------------
    // Admin: tab flags
    // ---------------------------------------------------------------
    fun refreshFlags() {
        adminFlags.clear()
        adminFlags.addAll(data.tabFlags.filter { it.status == "flagged" }.sortedByDescending { it.id })
    }

    fun approveFlag(id: Int) {
        data = data.copy(
            tabFlags = data.tabFlags.map {
                if (it.id == id) it.copy(status = "approved", resolvedAt = System.currentTimeMillis()) else it
            }
        )
        repo.save(data)
        refreshFlags()
    }

    // ---------------------------------------------------------------
    // Admin: surveys
    // ---------------------------------------------------------------
    fun createSurvey(
        testCode: String,
        course: String,
        trainee: String,
        noOfDays: Int,
        templateName: String
    ): String? {
        if (testCode.isBlank() || course.isBlank() || trainee.isBlank() || noOfDays <= 0) {
            return "All fields are required."
        }
        val template = SurveyTemplates.templates[templateName]
            ?: return "Please select a valid template."
        val survey = Survey(
            id = (data.surveys.maxOfOrNull { it.id } ?: 0) + 1,
            testCode = testCode.trim().uppercase(),
            course = course.trim(),
            trainee = trainee.trim(),
            noOfDays = noOfDays,
            templateName = templateName,
            questions = template.questions
        )
        data = data.copy(surveys = data.surveys + survey)
        repo.save(data)
        return null
    }

    fun surveyResponses(): List<SurveyResponse> =
        data.surveyResponses.sortedByDescending { it.submittedAt }

    fun deleteSurveyResponse(id: Int) {
        data = data.copy(surveyResponses = data.surveyResponses.filter { it.id != id })
        repo.save(data)
    }

    fun deleteSurveyResponsesByTest(testCode: String) {
        data = data.copy(surveyResponses = data.surveyResponses.filter {
            !it.testCode.equals(testCode, ignoreCase = true)
        })
        repo.save(data)
    }

    fun deleteAllSurveyResponses() {
        data = data.copy(surveyResponses = emptyList())
        repo.save(data)
    }

    fun templateQuestions(templateName: String): List<SurveyQuestion> =
        SurveyTemplates.templates[templateName]?.questions ?: emptyList()

    // ---------------------------------------------------------------
    // Admin: feedback data analysis
    // ---------------------------------------------------------------
    fun analyze(testCode: String): DataAnalysis {
        val responses = data.surveyResponses.filter {
            it.testCode.equals(testCode, ignoreCase = true)
        }
        if (responses.isEmpty()) return DataAnalysis(error = "No responses found.")

        val questions = responses.firstOrNull()?.questions ?: emptyList()
        val positives = mutableListOf<AnalysisBucket>()
        val improvements = mutableListOf<AnalysisBucket>()
        val negatives = mutableListOf<AnalysisBucket>()

        questions.forEachIndexed { i, q ->
            val options = q.options ?: listOf("Very Poor", "Poor", "Average", "Good", "Excellent")
            val ratings = responses.mapNotNull { it.answers[i.toString()] }
            val counts = options.associateWith { opt -> ratings.count { it == opt } }
            val total: Int = ratings.size
            val positiveOpts = options.drop(ceil(options.size * 0.6).toInt())
            val negativeOpts = options.take(floor(options.size * 0.4).toInt())
            val posCount = positiveOpts.sumOf { counts[it] ?: 0 }
            val negCount = negativeOpts.sumOf { counts[it] ?: 0 }
            val posPct: Int = if (total > 0) (posCount * 100f / total).roundToInt() else 0
            val negPct: Int = if (total > 0) (negCount * 100f / total).roundToInt() else 0
            when {
                posPct >= 60 -> positives.add(AnalysisBucket(q.text, posPct, negPct))
                negPct >= 40 -> negatives.add(AnalysisBucket(q.text, posPct, negPct))
                else -> improvements.add(AnalysisBucket(q.text, posPct, negPct))
            }
        }

        val sentiment = when {
            positives.size > negatives.size -> "Mostly Positive"
            positives.size < negatives.size -> "Needs Improvement"
            else -> "Mixed"
        }
        return DataAnalysis(
            testCode = testCode,
            totalResponses = responses.size,
            positives = positives,
            negatives = negatives,
            improvements = improvements,
            overallSentiment = sentiment
        )
    }
}

data class AnalysisBucket(val question: String, val positivePct: Int, val negativePct: Int)

data class DataAnalysis(
    val testCode: String = "",
    val totalResponses: Int = 0,
    val positives: List<AnalysisBucket> = emptyList(),
    val negatives: List<AnalysisBucket> = emptyList(),
    val improvements: List<AnalysisBucket> = emptyList(),
    val overallSentiment: String = "",
    val error: String? = null
)


