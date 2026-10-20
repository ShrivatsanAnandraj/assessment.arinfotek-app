package com.example.myapplication.data

import android.content.Context
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Local, on-device persistence for the assessment app.
 *
 * The original web app used Neon PostgreSQL + serverless API routes.
 * This Android port keeps all of that data on the device as JSON so the
 * app runs fully offline inside Android Studio / an emulator.
 */
class AppRepository(context: Context) {

    private val dataDir = File(context.filesDir, "arinfotek").apply { if (!exists()) mkdirs() }

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val dataFile = File(dataDir, "appdata.json")
    private val papersFile = File(dataDir, "papers.json")

    fun load(): AppData {
        if (!dataFile.exists()) {
            val seeded = seed()
            save(seeded)
            return seeded
        }
        return runCatching {
            json.decodeFromString<AppData>(dataFile.readText())
        }.getOrElse { seed() }
    }

    fun save(data: AppData) {
        runCatching { dataFile.writeText(json.encodeToString(AppData.serializer(), data)) }
    }

    fun loadPapers(): List<GeneratedPaper> {
        if (!papersFile.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<GeneratedPaper>>(papersFile.readText())
        }.getOrElse { emptyList() }
    }

    fun savePapers(papers: List<GeneratedPaper>) {
        runCatching { papersFile.writeText(json.encodeToString(ListSerializer(GeneratedPaper.serializer()), papers)) }
    }

    /** Seeds a couple of demo tests and a survey so the app is usable immediately. */
    private fun seed(): AppData {
        val demoQuestions = listOf(
            Triple(
                "What is the output of print(2 ** 3) in Python?",
                listOf("6", "8", "9", "5"), 1
            ),
            Triple(
                "Which keyword is used to define a function in Python?",
                listOf("func", "define", "def", "function"), 2
            ),
            Triple(
                "What data type is the result of 10 / 2 in Python 3?",
                listOf("int", "float", "string", "bool"), 1
            ),
            Triple(
                "Which operator checks equality in Python?",
                listOf("=", "==", "!=", "==="), 1
            ),
            Triple(
                "What does len('hello') return?",
                listOf("4", "5", "6", "error"), 1
            )
        )

        var qId = 1
        val questions = demoQuestions.map {
            Question(
                id = qId,
                testId = 1,
                questionText = it.first,
                options = it.second,
                correctAnswer = it.third
            ).also { _ -> qId++ }
        }

        val test = Test(
            id = 1,
            title = "Python Basics Assessment",
            subject = "Python",
            course = "Python",
            level = "basic",
            topics = listOf("variables", "data types", "operators", "functions"),
            testCode = "APT01",
            durationMinutes = 30
        )

        val survey = Survey(
            id = 1,
            testCode = "APT01",
            course = "Python Programming",
            trainee = "Batch 2025",
            noOfDays = 5,
            templateName = "Template 3",
            questions = SurveyTemplates.templates["Template 3"]!!.questions
        )

        return AppData(
            tests = listOf(test),
            questions = questions,
            surveys = listOf(survey)
        )
    }
}
