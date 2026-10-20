package com.example.myapplication.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Minimal HTTP client for the live AR INFOTEK assessment API.
 *
 * Uses [HttpURLConnection] so no third-party networking dependency is needed,
 * and kotlinx.serialization to (de)serialize JSON.
 */
class ApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    val serializer: Json get() = json

    private val baseUrl = "https://arinfotek-assesment.vercel.app/api"

    suspend fun <T> get(
        path: String,
        deserializer: KSerializer<T>,
        query: Map<String, String> = emptyMap()
    ): T = request("GET", path, query, null, deserializer)

    suspend fun <T> post(
        path: String,
        deserializer: KSerializer<T>,
        body: String
    ): T = request("POST", path, emptyMap(), body, deserializer)

    private suspend fun <T> request(
        method: String,
        path: String,
        query: Map<String, String>,
        body: String?,
        deserializer: KSerializer<T>
    ): T = withContext(Dispatchers.IO) {
        val url = buildUrl(path, query)
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = 30_000
        conn.readTimeout = 90_000
        conn.setRequestProperty("Accept", "application/json")
        try {
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            if (code !in 200..299) {
                error("HTTP $code: ${text.take(300)}")
            }
            json.decodeFromString(deserializer, text)
        } finally {
            conn.disconnect()
        }
    }

    private fun buildUrl(path: String, query: Map<String, String>): String {
        val url = StringBuilder(baseUrl).append(path)
        if (query.isNotEmpty()) {
            url.append('?')
            url.append(
                query.entries.joinToString("&") { (k, v) ->
                    "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
                }
            )
        }
        return url.toString()
    }
}
