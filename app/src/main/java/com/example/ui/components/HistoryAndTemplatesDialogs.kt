package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QueryHistoryItem
import com.example.ui.theme.AccentError
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.SupabaseGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QueryHistoryDialog(
    history: List<QueryHistoryItem>,
    onSelectQuery: (QueryHistoryItem) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = SupabaseGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Query History")
                }
                if (history.isNotEmpty()) {
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No queries in history yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .testTag("history_list")
                ) {
                    items(history) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectQuery(item) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (item.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (item.isSuccess) AccentSuccess else AccentError,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (item.isSuccess) "${item.rowCount} rows • ${item.durationMs}ms" else "Failed",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (item.isSuccess) AccentSuccess else AccentError
                                        )
                                    }
                                    val formattedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                        .format(Date(item.timestamp))
                                    Text(
                                        text = formattedTime,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.query,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

data class SqlSnippet(
    val title: String,
    val description: String,
    val sql: String
)

private val SAMPLE_SNIPPETS = listOf(
    SqlSnippet(
        title = "List Public Tables",
        description = "Shows all user tables in the public schema",
        sql = "SELECT table_name \nFROM information_schema.tables \nWHERE table_schema = 'public' \nORDER BY table_name;"
    ),
    SqlSnippet(
        title = "Table Sizes & Row Counts",
        description = "Check table size and estimated live row count",
        sql = "SELECT relname AS table_name, \n       n_live_tup AS estimated_rows \nFROM pg_stat_user_tables \nORDER BY n_live_tup DESC;"
    ),
    SqlSnippet(
        title = "Database Version & Uptime",
        description = "PostgreSQL engine version and server start time",
        sql = "SELECT version(), pg_postmaster_start_time() AS server_started_at;"
    ),
    SqlSnippet(
        title = "Installed Extensions",
        description = "List all enabled PostgreSQL extensions (e.g. pg_trgm, pgcrypto)",
        sql = "SELECT name, default_version, installed_version, comment \nFROM pg_available_extensions \nWHERE installed_version IS NOT NULL;"
    ),
    SqlSnippet(
        title = "Inspect Columns of a Table",
        description = "List column names, data types, and nullability",
        sql = "SELECT column_name, data_type, is_nullable, column_default \nFROM information_schema.columns \nWHERE table_schema = 'public' \nORDER BY ordinal_position;"
    ),
    SqlSnippet(
        title = "Create Quick Test Table",
        description = "Creates a test table with primary key and timestamps",
        sql = "CREATE TABLE IF NOT EXISTS public.test_items (\n    id BIGSERIAL PRIMARY KEY,\n    title TEXT NOT NULL,\n    created_at TIMESTAMPTZ DEFAULT NOW()\n);\n\nINSERT INTO public.test_items (title) VALUES ('First Supabase item') RETURNING *;"
    )
)

@Composable
fun SnippetsDialog(
    onSelectSnippet: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.IntegrationInstructions,
                    contentDescription = "SQL Snippets",
                    tint = SupabaseGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "SQL Snippets & Templates")
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .testTag("snippets_list")
            ) {
                items(SAMPLE_SNIPPETS) { snippet ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectSnippet(snippet.sql) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = snippet.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = snippet.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = snippet.sql.replace("\n", " "),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp
                            ),
                            maxLines = 2,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
