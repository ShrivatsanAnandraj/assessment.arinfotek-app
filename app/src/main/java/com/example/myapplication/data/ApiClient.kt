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

    private val baseUrl = "https://assessment.arinfotek.co.in/api"

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
        var currentUrl = buildUrl(path, query)
        var redirects = 0
        while (true) {
            val conn = URL(currentUrl).openConnection() as HttpURLConnection
            conn.instanceFollowRedirects = false
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
                if (code in 300..399) {
                    val location = conn.getHeaderField("Location")
                    if (location.isNullOrBlank() || redirects++ >= 5) {
                        throw ApiException(code, "Too many redirects")
                    }
                    // 307/308 preserve method + body; re-send on the new URL.
                    currentUrl = location
                    continue
                }
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
                if (code !in 200..299) {
                    throw ApiException(code, text)
                }
                return@withContext json.decodeFromString(deserializer, text)
            } finally {
                conn.disconnect()
            }
        }
        @Suppress("UNREACHABLE_CODE")
        error("unreachable")
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

/** Thrown when the API returns a non-2xx status. */
class ApiException(val statusCode: Int, val body: String) :
    Exception("HTTP $statusCode: ${body.take(300)}") {
    val isNotFound: Boolean get() = statusCode == 404
}
