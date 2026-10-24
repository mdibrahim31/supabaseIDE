package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.QueryHistoryItem
import org.json.JSONArray
import org.json.JSONObject

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "supabase_sql_runner_prefs",
        Context.MODE_PRIVATE
    )

    fun getApiToken(): String {
        return prefs.getString(KEY_API_TOKEN, "") ?: ""
    }

    fun saveApiToken(token: String) {
        prefs.edit().putString(KEY_API_TOKEN, token).apply()
    }

    fun getProjectRef(): String {
        return prefs.getString(KEY_PROJECT_REF, "") ?: ""
    }

    fun saveProjectRef(ref: String) {
        prefs.edit().putString(KEY_PROJECT_REF, ref).apply()
    }

    fun getLastQuery(): String {
        return prefs.getString(
            KEY_LAST_QUERY,
            "SELECT table_name \nFROM information_schema.tables \nWHERE table_schema = 'public'\nLIMIT 10;"
        ) ?: ""
    }

    fun saveLastQuery(query: String) {
        prefs.edit().putString(KEY_LAST_QUERY, query).apply()
    }

    fun getQueryHistory(): List<QueryHistoryItem> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<QueryHistoryItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    QueryHistoryItem(
                        id = obj.optString("id", ""),
                        timestamp = obj.optLong("timestamp", 0L),
                        query = obj.optString("query", ""),
                        isSuccess = obj.optBoolean("isSuccess", false),
                        rowCount = obj.optInt("rowCount", 0),
                        durationMs = obj.optLong("durationMs", 0L)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addQueryHistory(item: QueryHistoryItem) {
        val current = getQueryHistory().toMutableList()
        // Remove duplicate if same query was run recently
        current.removeAll { it.query.trim() == item.query.trim() }
        current.add(0, item)
        // Keep up to 50 recent items
        val trimmed = if (current.size > 50) current.subList(0, 50) else current

        val array = JSONArray()
        for (h in trimmed) {
            val obj = JSONObject().apply {
                put("id", h.id)
                put("timestamp", h.timestamp)
                put("query", h.query)
                put("isSuccess", h.isSuccess)
                put("rowCount", h.rowCount)
                put("durationMs", h.durationMs)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun clearQueryHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    fun clearCredentials() {
        prefs.edit().remove(KEY_API_TOKEN).remove(KEY_PROJECT_REF).apply()
    }

    companion object {
        private const val KEY_API_TOKEN = "api_token"
        private const val KEY_PROJECT_REF = "project_ref"
        private const val KEY_LAST_QUERY = "last_query"
        private const val KEY_HISTORY = "query_history"
    }
}
