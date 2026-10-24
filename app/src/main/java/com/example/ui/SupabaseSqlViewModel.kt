package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesRepository
import com.example.data.SupabaseApiService
import com.example.data.model.ExecutionResponse
import com.example.data.model.QueryHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class ResultViewMode {
    TABLE,
    JSON
}

data class SqlUiState(
    val apiToken: String = "",
    val projectRef: String = "",
    val isTokenVisible: Boolean = false,
    val isCredentialsExpanded: Boolean = true,
    val sqlQuery: String = "",
    val isLoading: Boolean = false,
    val executionResult: ExecutionResponse? = null,
    val viewMode: ResultViewMode = ResultViewMode.TABLE,
    val history: List<QueryHistoryItem> = emptyList(),
    val showHistoryDialog: Boolean = false,
    val showSnippetsDialog: Boolean = false,
    val notificationMessage: String? = null
)

class SupabaseSqlViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesRepository = PreferencesRepository(application)
    private val apiService = SupabaseApiService()

    private val _uiState = MutableStateFlow(SqlUiState())
    val uiState: StateFlow<SqlUiState> = _uiState.asStateFlow()

    init {
        val savedToken = preferencesRepository.getApiToken()
        val savedRef = preferencesRepository.getProjectRef()
        val savedQuery = preferencesRepository.getLastQuery()
        val savedHistory = preferencesRepository.getQueryHistory()

        _uiState.update {
            it.copy(
                apiToken = savedToken,
                projectRef = savedRef,
                sqlQuery = savedQuery,
                history = savedHistory,
                // Collapse credentials card if both are already filled in to give maximum room to SQL editor
                isCredentialsExpanded = savedToken.isEmpty() || savedRef.isEmpty()
            )
        }
    }

    fun onApiTokenChanged(token: String) {
        _uiState.update { it.copy(apiToken = token) }
        preferencesRepository.saveApiToken(token)
    }

    fun onProjectRefChanged(ref: String) {
        _uiState.update { it.copy(projectRef = ref) }
        preferencesRepository.saveProjectRef(ref)
    }

    fun toggleTokenVisibility() {
        _uiState.update { it.copy(isTokenVisible = !it.isTokenVisible) }
    }

    fun toggleCredentialsExpanded() {
        _uiState.update { it.copy(isCredentialsExpanded = !it.isCredentialsExpanded) }
    }

    fun onSqlQueryChanged(query: String) {
        _uiState.update { it.copy(sqlQuery = query) }
        preferencesRepository.saveLastQuery(query)
    }

    fun clearQuery() {
        _uiState.update { it.copy(sqlQuery = "") }
        preferencesRepository.saveLastQuery("")
    }

    fun setViewMode(mode: ResultViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun setShowHistoryDialog(show: Boolean) {
        _uiState.update { it.copy(showHistoryDialog = show) }
    }

    fun setShowSnippetsDialog(show: Boolean) {
        _uiState.update { it.copy(showSnippetsDialog = show) }
    }

    fun selectHistoryItem(item: QueryHistoryItem) {
        onSqlQueryChanged(item.query)
        _uiState.update { it.copy(showHistoryDialog = false) }
    }

    fun clearHistory() {
        preferencesRepository.clearQueryHistory()
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun insertSnippet(sql: String) {
        onSqlQueryChanged(sql)
        _uiState.update { it.copy(showSnippetsDialog = false) }
    }

    fun dismissNotification() {
        _uiState.update { it.copy(notificationMessage = null) }
    }

    fun runQuery() {
        val currentState = _uiState.value
        val token = currentState.apiToken.trim()
        val ref = currentState.projectRef.trim()
        val query = currentState.sqlQuery.trim()

        if (token.isEmpty()) {
            _uiState.update {
                it.copy(
                    isCredentialsExpanded = true,
                    notificationMessage = "Please enter your Supabase Management API Token."
                )
            }
            return
        }

        if (ref.isEmpty()) {
            _uiState.update {
                it.copy(
                    isCredentialsExpanded = true,
                    notificationMessage = "Please enter your Supabase Project Ref."
                )
            }
            return
        }

        if (query.isEmpty()) {
            _uiState.update {
                it.copy(notificationMessage = "Please write or paste a SQL query to execute.")
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, executionResult = null) }

        viewModelScope.launch {
            val result = apiService.executeSql(
                apiToken = token,
                projectRef = ref,
                sqlQuery = query
            )

            // Save to history
            val historyItem = QueryHistoryItem(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                query = query,
                isSuccess = result.isSuccess,
                rowCount = result.rowCount,
                durationMs = result.executionTimeMs
            )
            preferencesRepository.addQueryHistory(historyItem)
            val updatedHistory = preferencesRepository.getQueryHistory()

            // If table has no columns/rows, auto switch to JSON view for cleaner display
            val preferredMode = if (result.isSuccess && result.rows.isNotEmpty()) {
                ResultViewMode.TABLE
            } else {
                ResultViewMode.JSON
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    executionResult = result,
                    history = updatedHistory,
                    viewMode = preferredMode
                )
            }
        }
    }
}
