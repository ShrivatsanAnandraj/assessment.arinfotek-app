package com.example.myapplication

import com.example.myapplication.data.ExcelQuestions
import org.dhatim.fastexcel.Workbook
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ExcelRoundTripTest {

    @Test
    fun writesRealSheetThenParses() {
        val out = ByteArrayOutputStream()
        Workbook(out, "test", "1.0").use { wb ->
            val ws = wb.newWorksheet("Questions")
            ws.value(0, 0, "question")
            ws.value(0, 1, "optiona")
            ws.value(0, 2, "optionb")
            ws.value(0, 3, "optionc")
            ws.value(0, 4, "optiond")
            ws.value(0, 5, "correctoption")
            ws.value(1, 0, "What is 2+2?")
            ws.value(1, 1, "1")
            ws.value(1, 2, "2")
            ws.value(1, 3, "4")
            ws.value(1, 4, "5")
            ws.value(1, 5, "C")
            ws.value(2, 0, "Capital of France?")
            ws.value(2, 1, "Paris")
            ws.value(2, 2, "Rome")
            ws.value(2, 3, "Berlin")
            ws.value(2, 4, "Madrid")
            ws.value(2, 5, "A")
        }

        val questions = ExcelQuestions.parse(ByteArrayInputStream(out.toByteArray()))
        assertEquals(2, questions.size)
        assertEquals("What is 2+2?", questions[0].questionText)
        assertEquals(listOf("1", "2", "4", "5"), questions[0].options)
        assertEquals(2, questions[0].correctAnswer)
        assertEquals("Capital of France?", questions[1].questionText)
        assertEquals(0, questions[1].correctAnswer)
    }
}
