package com.example

import com.example.data.SupabaseApiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SupabaseApiServiceTest {

    private val apiService = SupabaseApiService()

    @Test
    fun executeSql_emptyToken_returnsError() = runBlocking {
        val result = apiService.executeSql(
            apiToken = "",
            projectRef = "myprojectref",
            sqlQuery = "SELECT 1;"
        )
        assertFalse(result.isSuccess)
        assertEquals("Supabase Management API Token is required.", result.errorMessage)
    }

    @Test
    fun executeSql_emptyRef_returnsError() = runBlocking {
        val result = apiService.executeSql(
            apiToken = "sbp_validtoken",
            projectRef = "",
            sqlQuery = "SELECT 1;"
        )
        assertFalse(result.isSuccess)
        assertEquals("Project Ref is required.", result.errorMessage)
    }

    @Test
    fun executeSql_emptyQuery_returnsError() = runBlocking {
        val result = apiService.executeSql(
            apiToken = "sbp_validtoken",
            projectRef = "myprojectref",
            sqlQuery = "   "
        )
        assertFalse(result.isSuccess)
        assertEquals("SQL query cannot be empty.", result.errorMessage)
    }
}
