package com.example.data

import com.example.data.model.ExecutionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SupabaseApiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {

    suspend fun executeSql(
        apiToken: String,
        projectRef: String,
        sqlQuery: String
    ): ExecutionResponse = withContext(Dispatchers.IO) {
        val cleanToken = apiToken.trim()
        val cleanRef = projectRef.trim()
        val cleanQuery = sqlQuery.trim()

        if (cleanToken.isEmpty()) {
            return@withContext ExecutionResponse(
                isSuccess = false,
                httpStatusCode = 0,
                rawJson = "",
                formattedJson = "",
                errorMessage = "Supabase Management API Token is required."
            )
        }

        if (cleanRef.isEmpty()) {
            return@withContext ExecutionResponse(
                isSuccess = false,
                httpStatusCode = 0,
                rawJson = "",
                formattedJson = "",
                errorMessage = "Project Ref is required."
            )
        }

        if (cleanQuery.isEmpty()) {
            return@withContext ExecutionResponse(
                isSuccess = false,
                httpStatusCode = 0,
                rawJson = "",
                formattedJson = "",
                errorMessage = "SQL query cannot be empty."
            )
        }

        val url = "https://api.supabase.com/v1/projects/$cleanRef/database/query"
        val jsonPayload = JSONObject().apply {
            put("query", cleanQuery)
        }.toString()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonPayload.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $cleanToken")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val startTime = System.currentTimeMillis()

        try {
            client.newCall(request).execute().use { response ->
                val duration = System.currentTimeMillis() - startTime
                val responseBody = response.body?.string() ?: ""
                val statusCode = response.code

                if (response.isSuccessful) {
                    parseSuccessResponse(responseBody, statusCode, duration)
                } else {
                    parseErrorResponse(responseBody, statusCode, duration)
                }
            }
        } catch (e: IOException) {
            val duration = System.currentTimeMillis() - startTime
            ExecutionResponse(
                isSuccess = false,
                httpStatusCode = 0,
                rawJson = "",
                formattedJson = "",
                executionTimeMs = duration,
                errorMessage = "Network error: ${e.localizedMessage ?: e.message ?: "Failed to connect to Supabase API"}"
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            ExecutionResponse(
                isSuccess = false,
                httpStatusCode = 0,
                rawJson = "",
                formattedJson = "",
                executionTimeMs = duration,
                errorMessage = "Unexpected error: ${e.localizedMessage ?: e.message ?: "An unknown error occurred"}"
            )
        }
    }

    private fun parseSuccessResponse(
        body: String,
        statusCode: Int,
        duration: Long
    ): ExecutionResponse {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) {
            return ExecutionResponse(
                isSuccess = true,
                httpStatusCode = statusCode,
                rawJson = "[]",
                formattedJson = "[] (Query executed successfully with no output rows)",
                rowCount = 0,
                executionTimeMs = duration
            )
        }

        return try {
            if (trimmed.startsWith("[")) {
                val jsonArray = JSONArray(trimmed)
                val rowCount = jsonArray.length()
                val columns = mutableListOf<String>()
                val rows = mutableListOf<List<String>>()

                // Gather columns from first row if objects
                if (rowCount > 0) {
                    val first = jsonArray.optJSONObject(0)
                    if (first != null) {
                        val keys = first.keys()
                        while (keys.hasNext()) {
                            columns.add(keys.next())
                        }

                        for (i in 0 until rowCount) {
                            val rowObj = jsonArray.optJSONObject(i)
                            if (rowObj != null) {
                                val rowValues = columns.map { col ->
                                    if (rowObj.isNull(col)) "NULL" else rowObj.optString(col, "")
                                }
                                rows.add(rowValues)
                            } else {
                                rows.add(listOf(jsonArray.opt(i)?.toString() ?: ""))
                            }
                        }
                    } else {
                        columns.add("Value")
                        for (i in 0 until rowCount) {
                            rows.add(listOf(jsonArray.opt(i)?.toString() ?: ""))
                        }
                    }
                }

                val formatted = jsonArray.toString(2)

                ExecutionResponse(
                    isSuccess = true,
                    httpStatusCode = statusCode,
                    rawJson = trimmed,
                    formattedJson = formatted,
                    columns = columns,
                    rows = rows,
                    rowCount = rowCount,
                    executionTimeMs = duration
                )
            } else if (trimmed.startsWith("{")) {
                val jsonObject = JSONObject(trimmed)
                val columns = mutableListOf<String>()
                val rowValues = mutableListOf<String>()

                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    columns.add(key)
                    rowValues.add(if (jsonObject.isNull(key)) "NULL" else jsonObject.optString(key, ""))
                }

                ExecutionResponse(
                    isSuccess = true,
                    httpStatusCode = statusCode,
                    rawJson = trimmed,
                    formattedJson = jsonObject.toString(2),
                    columns = columns,
                    rows = if (rowValues.isNotEmpty()) listOf(rowValues) else emptyList(),
                    rowCount = if (rowValues.isNotEmpty()) 1 else 0,
                    executionTimeMs = duration
                )
            } else {
                ExecutionResponse(
                    isSuccess = true,
                    httpStatusCode = statusCode,
                    rawJson = trimmed,
                    formattedJson = trimmed,
                    rowCount = 0,
                    executionTimeMs = duration
                )
            }
        } catch (e: Exception) {
            ExecutionResponse(
                isSuccess = true,
                httpStatusCode = statusCode,
                rawJson = trimmed,
                formattedJson = trimmed,
                rowCount = 0,
                executionTimeMs = duration
            )
        }
    }

    private fun parseErrorResponse(
        body: String,
        statusCode: Int,
        duration: Long
    ): ExecutionResponse {
        val trimmed = body.trim()
        var errorMsg: String? = null

        if (trimmed.isNotEmpty()) {
            try {
                if (trimmed.startsWith("{")) {
                    val json = JSONObject(trimmed)
                    errorMsg = when {
                        json.has("message") -> json.optString("message")
                        json.has("error") -> json.optString("error")
                        json.has("msg") -> json.optString("msg")
                        else -> json.toString(2)
                    }
                } else {
                    errorMsg = trimmed
                }
            } catch (_: Exception) {
                errorMsg = trimmed
            }
        }

        val finalMessage = when (statusCode) {
            401 -> "HTTP 401 Unauthorized: Invalid API Token. Verify your Supabase Management API personal access token."
            403 -> "HTTP 403 Forbidden: Token does not have permission to execute database queries on this project."
            404 -> "HTTP 404 Not Found: Project with reference not found. Verify your Project Ref."
            else -> "HTTP $statusCode Error: ${errorMsg ?: "Request failed"}"
        }

        return ExecutionResponse(
            isSuccess = false,
            httpStatusCode = statusCode,
            rawJson = trimmed,
            formattedJson = trimmed,
            executionTimeMs = duration,
            errorMessage = if (errorMsg != null && statusCode != 401 && statusCode != 403 && statusCode != 404) {
                "HTTP $statusCode: $errorMsg"
            } else {
                finalMessage
            }
        )
    }
}
