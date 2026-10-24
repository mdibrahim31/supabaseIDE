package com.example.data.model

data class ExecutionResponse(
    val isSuccess: Boolean,
    val httpStatusCode: Int,
    val rawJson: String,
    val formattedJson: String,
    val columns: List<String> = emptyList(),
    val rows: List<List<String>> = emptyList(),
    val rowCount: Int = 0,
    val executionTimeMs: Long = 0L,
    val errorMessage: String? = null
)

data class QueryHistoryItem(
    val id: String,
    val timestamp: Long,
    val query: String,
    val isSuccess: Boolean,
    val rowCount: Int,
    val durationMs: Long
)
