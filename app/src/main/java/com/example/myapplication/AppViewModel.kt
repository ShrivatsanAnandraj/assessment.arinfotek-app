package com.example.myapplication

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.*
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

enum class Screen { JOIN, TEST, RESULT, SURVEY, ADMIN }

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(app)
    private val api = ApiRepository()

    /** Local cache used as an offline fallback. */
    private var data: AppData = repo.load()

    private var papers: MutableList<GeneratedPaper> = repo.loadPapers().toMutableList()

    // The questions for the test currently being taken (from the live API).
    private var activeQuestions: List<Question> = emptyList()

    // ---- Navigation / session ----
    var screen by mutableStateOf(Screen.JOIN)
        private set
    var studentName by mutableStateOf("")
        private set
    var studentRegisterId by mutableStateOf("")
        private set

    // ---- Verified test info ----
    var verifiedTest by mutableStateOf<Test?>(null)
        private set
    var verifying by mutableStateOf(false)
        private set
    var joinError by mutableStateOf("")
        internal set

    // ---- Network status ----
    var loading by mutableStateOf(false)
        private set
    var online by mutableStateOf(true)
        private set

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
    var adminTests = mutableStateListOf<Test>()
        private set
    var adminAttempts = mutableStateListOf<Attempt>()
        private set
    var adminSurveyResponses = mutableStateListOf<SurveyResponse>()
        private set

    var templates by mutableStateOf(SurveyTemplates.templates.keys.toList())
        private set

    init {
        refreshAll()
    }

    /** Pulls the latest tests, attempts, surveys and flags from the API. */
    fun refreshAll() {
        viewModelScope.launch { refreshAllSuspending() }
    }

    private suspend fun refreshAllSuspending() {
        loading = true
        val ok = runCatching { loadTests() }.isSuccess
        runCatching { loadAttempts() }
        runCatching { loadFlags() }
        runCatching { loadSurveyResponses() }
        runCatching { loadTemplates() }
        online = ok
        loading = false
    }

    private suspend fun loadTests() {
        val remote = api.fetchTestsWithQuestions()
        if (remote.isNotEmpty()) {
            val tests = remote.map { it.first }
            val questions = remote.flatMap { it.second }
            adminTests.clear()
            adminTests.addAll(tests)
            data = data.copy(tests = tests, questions = questions)
            repo.save(data)
        }
    }

    private suspend fun loadAttempts() {
        val remote = api.fetchAttempts()
        adminAttempts.clear()
        adminAttempts.addAll(remote)
        data = data.copy(attempts = remote)
        repo.save(data)
    }

    private suspend fun loadFlags() {
        val remote = api.fetchFlags()
        adminFlags.clear()
        adminFlags.addAll(remote.sortedByDescending { it.id })
        data = data.copy(tabFlags = remote)
        repo.save(data)
    }

    private suspend fun loadSurveyResponses() {
        val remote = api.fetchSurveyResponses()
        adminSurveyResponses.clear()
        adminSurveyResponses.addAll(remote)
        data = data.copy(surveyResponses = remote)
        repo.save(data)
    }

    private suspend fun loadTemplates() {
        val remote = runCatching { api.fetchTemplates() }.getOrNull()
        if (!remote.isNullOrEmpty()) templates = remote
    }

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
        val upper = code.trim().uppercase()
        viewModelScope.launch {
            verifying = true
            val remote = runCatching { api.fetchTestByCode(upper) }.getOrNull()
            if (remote != null) {
                val (test, questions) = remote
                activeQuestions = questions
                finishVerify(name, regId, test, onVerified)
                return@launch
            }
            // Offline / not found remotely: fall back to the local cache.
            val cached = data.tests.firstOrNull { it.testCode.equals(upper, ignoreCase = true) }
            if (cached != null) {
                activeQuestions = data.questions.filter { it.testId == cached.id }
                finishVerify(name, regId, cached, onVerified)
            } else {
                verifying = false
                joinError = if (online) "Invalid test code" else "You're offline and this code isn't cached."
                onVerified(false)
            }
        }
    }

    private fun finishVerify(name: String, regId: String, test: Test, onVerified: (Boolean) -> Unit) {
        studentName = name.trim()
        studentRegisterId = regId.trim()
        verifiedTest = test
        verifying = false
        onVerified(true)
    }

    fun resetVerification() {
        verifiedTest = null
        activeQuestions = emptyList()
        joinError = ""
    }

    // ---------------------------------------------------------------
    // Test taking
    // ---------------------------------------------------------------
    fun startTest() {
        val test = verifiedTest ?: return
        val qs = activeQuestions.ifEmpty { data.questions.filter { it.testId == test.id } }
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
        val test = verifiedTest
        // Map paper (array-index) answers to DB question ids for the API.
        val dbAnswers: Map<String, Int> = answers.entries.associate { (idx, choice) ->
            val qid = activeQuestions.getOrNull(idx)?.id ?: idx
            qid.toString() to choice
        }
        viewModelScope.launch {
            loading = true
            val remote = if (test != null) {
                runCatching {
                    api.submitAttempt(
                        testId = test.id,
                        studentName = studentName,
                        studentRegisterId = studentRegisterId,
                        answers = dbAnswers
                    )
                }.getOrNull()
            } else null

            result = remote ?: localScore(paper, answers)
            loading = false
            screen = Screen.RESULT
            runCatching { loadAttempts() }
        }
    }

    /** Scores a paper locally (offline fallback). */
    private fun localScore(paper: GeneratedPaper, answers: Map<Int, Int>): TestResult {
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
        return TestResult(score, total, percentage, passed, review)
    }

    fun flagTabViolation() {
        val test = verifiedTest ?: return
        val now: Long = System.currentTimeMillis()
        viewModelScope.launch {
            runCatching {
                api.createFlag(
                    testCode = test.testCode,
                    studentName = studentName,
                    studentRegisterId = studentRegisterId,
                    violations = listOf(now)
                )
            }
            runCatching { loadFlags() }
        }
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
    fun openSurveyForCurrentTest(onResult: (Boolean) -> Unit) {
        val test = verifiedTest ?: run { onResult(false); return }
        viewModelScope.launch {
            loading = true
            val remote = runCatching { api.fetchSurveys() }.getOrNull()
            if (!remote.isNullOrEmpty()) {
                data = data.copy(surveys = remote)
                repo.save(data)
            }
            val survey = (remote ?: data.surveys).firstOrNull {
                it.testCode.equals(test.testCode, ignoreCase = true)
            }
            loading = false
            if (survey == null) {
                onResult(false)
            } else {
                activeSurvey = survey
                screen = Screen.SURVEY
                onResult(true)
            }
        }
    }

    fun submitSurvey(answers: Map<Int, String>, onDone: () -> Unit) {
        val survey = activeSurvey ?: run { onDone(); return }
        viewModelScope.launch {
            loading = true
            runCatching {
                api.submitSurveyResponse(
                    surveyId = survey.id,
                    studentName = studentName,
                    studentRegisterId = studentRegisterId,
                    testCode = survey.testCode,
                    answers = answers.mapKeys { it.key.toString() }
                )
            }
            loading = false
            runCatching { loadSurveyResponses() }
            onDone()
        }
    }

    fun finishSession() {
        activeSurvey = null
        activePaper = null
        result = null
        verifiedTest = null
        activeQuestions = emptyList()
        joinError = ""
        screen = Screen.JOIN
    }

    fun openAdmin() {
        screen = Screen.ADMIN
        refreshAll()
    }

    // ---------------------------------------------------------------
    // Admin: tests & questions
    // ---------------------------------------------------------------
    fun allTests(): List<Test> = adminTests.ifEmpty { data.tests }

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
        if (allTests().any { it.testCode.equals(upper, ignoreCase = true) }) {
            return "Test code already exists"
        }
        val newId = (allTests().maxOfOrNull { it.id } ?: 0) + 1
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
        // Persist locally so the app is immediately usable even if the
        // remote admin endpoint is read-only.
        data = data.copy(tests = data.tests + newTest, questions = data.questions + questions)
        repo.save(data)
        adminTests.add(newTest)
        return null
    }

    fun deleteTest(testCode: String) {
        val test = allTests().firstOrNull { it.testCode.equals(testCode, ignoreCase = true) } ?: return
        data = data.copy(
            tests = data.tests.filter { it.id != test.id },
            questions = data.questions.filter { it.testId != test.id },
            attempts = data.attempts.filter { !it.testCode.equals(testCode, ignoreCase = true) },
            surveys = data.surveys.filter { !it.testCode.equals(testCode, ignoreCase = true) },
            surveyResponses = data.surveyResponses.filter { !it.testCode.equals(testCode, ignoreCase = true) }
        )
        repo.save(data)
        adminTests.removeAll { it.id == test.id }
    }

    // ---------------------------------------------------------------
    // Admin: scores / attempts
    // ---------------------------------------------------------------
    fun allAttempts(): List<Attempt> =
        (adminAttempts.ifEmpty { data.attempts }).sortedByDescending { it.submittedAt }

    fun deleteAttempt(id: Int) {
        data = data.copy(attempts = data.attempts.filter { it.id != id })
        repo.save(data)
        adminAttempts.removeAll { it.id == id }
    }

    fun deleteAllAttempts() {
        data = data.copy(attempts = emptyList())
        repo.save(data)
        adminAttempts.clear()
    }

    // ---------------------------------------------------------------
    // Admin: tab flags
    // ---------------------------------------------------------------
    fun refreshFlags() {
        viewModelScope.launch { runCatching { loadFlags() } }
    }

    fun approveFlag(id: Int) {
        val flag = data.tabFlags.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            runCatching {
                api.resolveFlag(flag.testCode, flag.studentRegisterId, "approved")
            }
            data = data.copy(
                tabFlags = data.tabFlags.map {
                    if (it.id == id) it.copy(status = "approved", resolvedAt = System.currentTimeMillis()) else it
                }
            )
            repo.save(data)
            adminFlags.removeAll { it.id == id }
            runCatching { loadFlags() }
        }
    }

    // ---------------------------------------------------------------
    // Admin: surveys
    // ---------------------------------------------------------------
    fun createSurvey(
        testCode: String,
        course: String,
        trainee: String,
        noOfDays: Int,
        templateName: String,
        onResult: (String?) -> Unit
    ) {
        if (testCode.isBlank() || course.isBlank() || trainee.isBlank() || noOfDays <= 0) {
            onResult("All fields are required.")
            return
        }
        viewModelScope.launch {
            loading = true
            val remote = runCatching {
                api.createSurvey(testCode.trim().uppercase(), course.trim(), trainee.trim(), noOfDays, templateName)
            }.getOrNull()
            loading = false
            if (remote != null) {
                data = data.copy(surveys = data.surveys + remote)
                repo.save(data)
                onResult(null)
                return@launch
            }
            // Offline fallback: build the survey from a bundled template.
            val template = SurveyTemplates.templates[templateName]
            if (template == null) {
                onResult("Please select a valid template.")
                return@launch
            }
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
            onResult(null)
        }
    }

    fun surveyResponses(): List<SurveyResponse> =
        (adminSurveyResponses.ifEmpty { data.surveyResponses }).sortedByDescending { it.submittedAt }

    fun deleteSurveyResponse(id: Int) {
        data = data.copy(surveyResponses = data.surveyResponses.filter { it.id != id })
        repo.save(data)
        adminSurveyResponses.removeAll { it.id == id }
    }

    fun deleteSurveyResponsesByTest(testCode: String) {
        data = data.copy(surveyResponses = data.surveyResponses.filter {
            !it.testCode.equals(testCode, ignoreCase = true)
        })
        repo.save(data)
        adminSurveyResponses.removeAll { it.testCode.equals(testCode, ignoreCase = true) }
    }

    fun deleteAllSurveyResponses() {
        data = data.copy(surveyResponses = emptyList())
        repo.save(data)
        adminSurveyResponses.clear()
    }

    fun templateQuestions(templateName: String): List<SurveyQuestion> =
        SurveyTemplates.templates[templateName]?.questions ?: emptyList()

    // ---------------------------------------------------------------
    // Admin: feedback data analysis
    // ---------------------------------------------------------------
    fun analyze(testCode: String): DataAnalysis {
        val responses = surveyResponses().filter {
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
