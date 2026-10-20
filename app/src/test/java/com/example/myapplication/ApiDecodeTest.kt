package com.example.myapplication

import com.example.myapplication.data.ApiTestResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ApiDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun decodesTestByCodeResponse() {
        val body = """{"test":{"id":10,"title":"Assessment - Data visualization using PowerBI ","subject":"General","course":"","level":"","topics":[],"test_code":"ARPB248","duration_minutes":30},"questions":[{"id":24,"question_text":"What is the main purpose of Power BI in a data analytics project?","options":["To design only Excel spreadsheets","To write only Python programs","To connect, transform, model and visualise data","To manage computer hardware"],"correct_answer":2}]}"""
        val resp = json.decodeFromString(ApiTestResponse.serializer(), body)
        assertNotNull(resp.test)
        assertEquals("ARPB248", resp.test!!.testCode)
        assertEquals(1, resp.questions.size)
        assertEquals(2, resp.questions[0].correctAnswer)
    }

    @Test
    fun decodesCode123WithOneQuestion() {
        val body = """{"test":{"id":12,"title":"test","subject":"General","course":"","level":"","topics":[],"test_code":"123","duration_minutes":30},"questions":[{"id":50,"question_text":"Placeholder","options":["A","B","C","D"]}]}"""
        val resp = json.decodeFromString(ApiTestResponse.serializer(), body)
        assertEquals("123", resp.test!!.testCode)
        assertEquals(1, resp.questions.size)
    }
}
