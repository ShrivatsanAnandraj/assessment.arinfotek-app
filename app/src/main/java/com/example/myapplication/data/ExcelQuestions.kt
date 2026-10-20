package com.example.myapplication.data

import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.reader.ReadableWorkbook
import java.io.InputStream
import java.io.OutputStream

/**
 * Port of the website's "Import Questions from Excel" feature
 * (SheetJS `sheet_to_json` in the admin panel).
 *
 * Required columns (case-insensitive):
 *   question, optiona, optionb, optionc, optiond, correctoption
 * `correctoption` is one of A/B/C/D and maps to index 0..3.
 */
object ExcelQuestions {

    private val REQUIRED = listOf(
        "question", "optiona", "optionb", "optionc", "optiond", "correctoption"
    )

    class ImportException(message: String) : Exception(message)

    /** Parse an .xlsx stream into a list of (text, options, correctIndex). */
    fun parse(input: InputStream): List<Question> {
        val rows: List<List<String>> = ReadableWorkbook(input).use { wb ->
            val sheet = wb.firstSheet
                ?: throw ImportException("Error: Excel file is empty.")
            val collected = ArrayList<List<String>>()
            sheet.openStream().use { stream ->
                val it = stream.iterator()
                while (it.hasNext()) {
                    val row = it.next()
                    collected.add((0 until row.cellCount).map { c ->
                        row.getCell(c)?.asString()?.trim() ?: ""
                    })
                }
            }
            collected
        }

        val nonEmpty = rows.filter { r -> r.any { it.isNotBlank() } }
        if (nonEmpty.isEmpty()) throw ImportException("Error: Excel file is empty.")

        val header = nonEmpty.first().map { it.lowercase().trim() }
        REQUIRED.forEach { col ->
            if (col !in header) {
                throw ImportException(
                    "Error: Wrong format. Required columns: question, optiona, optionb, optionc, optiond, correctoption"
                )
            }
        }
        val idx = REQUIRED.associateWith { header.indexOf(it) }

        val questions = nonEmpty.drop(1).map { row ->
            fun cell(col: String): String = row.getOrElse(idx.getValue(col)) { "" }.trim()
            val opts = listOf("optiona", "optionb", "optionc", "optiond").map { cell(it) }
            val answer = cell("correctoption").uppercase()
            val correct = listOf("A", "B", "C", "D").indexOf(answer).let { if (it >= 0) it else 0 }
            Question(
                id = 0,
                testId = 0,
                questionText = cell("question"),
                options = opts,
                correctAnswer = correct
            )
        }.filter { it.questionText.isNotBlank() }

        if (questions.isEmpty()) throw ImportException("Error: Excel file is empty.")
        return questions
    }

    /** Write the same template the website downloads: a header row only. */
    fun writeTemplate(output: OutputStream) {
        Workbook(output, "AR INFOTEK", "1.0").use { wb ->
            val ws = wb.newWorksheet("Questions")
            REQUIRED.forEachIndexed { c, name ->
                ws.value(0, c, name)
                ws.width(c, if (c == 0) 40.0 else if (name == "correctoption") 15.0 else 20.0)
            }
        }
    }
}
