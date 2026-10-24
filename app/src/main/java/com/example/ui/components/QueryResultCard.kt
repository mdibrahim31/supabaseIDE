package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionResponse
import com.example.ui.ResultViewMode
import com.example.ui.theme.AccentError
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.CodeEditorBgDark
import com.example.ui.theme.SupabaseGreen

@Composable
fun QueryResultCard(
    result: ExecutionResponse?,
    viewMode: ResultViewMode,
    onViewModeChanged: (ResultViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("query_result_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            if (result == null) {
                // Placeholder state before running
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = "Results placeholder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Results will appear here",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Write a query above and tap 'Run Query'",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                // Results Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        if (result.isSuccess) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = AccentSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Success",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AccentSuccess
                                )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = AccentError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Query Error",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AccentError
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Duration badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Execution time",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${result.executionTimeMs} ms",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (result.isSuccess && result.rowCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${result.rowCount} row${if (result.rowCount != 1) "s" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Copy action button
                    IconButton(
                        onClick = {
                            val textToCopy = if (result.isSuccess) {
                                result.formattedJson
                            } else {
                                result.errorMessage ?: result.rawJson
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("SQL Result", textToCopy)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("copy_result_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy result",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // If success and has rows, show view mode toggle chips
                if (result.isSuccess && result.rows.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        FilterChip(
                            selected = viewMode == ResultViewMode.TABLE,
                            onClick = { onViewModeChanged(ResultViewMode.TABLE) },
                            label = { Text("Table View") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = "Table",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SupabaseGreen.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("table_view_chip")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilterChip(
                            selected = viewMode == ResultViewMode.JSON,
                            onClick = { onViewModeChanged(ResultViewMode.JSON) },
                            label = { Text("Raw JSON") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DataObject,
                                    contentDescription = "JSON",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SupabaseGreen.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("json_view_chip")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Content View
                if (!result.isSuccess) {
                    // Error Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentError.copy(alpha = 0.1f))
                            .border(1.dp, AccentError.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        SelectionContainer {
                            Column {
                                Text(
                                    text = result.errorMessage ?: "Unknown error executing SQL query",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        color = AccentError
                                    )
                                )
                            }
                        }
                    }
                } else if (result.rows.isEmpty()) {
                    // Empty rows / statement executed (e.g. CREATE, UPDATE, or empty SELECT)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Query executed successfully",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No rows returned (0 rows)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (viewMode == ResultViewMode.TABLE) {
                    // Table Layout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        val horizontalScrollState = rememberScrollState()
                        val verticalScrollState = rememberScrollState()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                                .verticalScroll(verticalScrollState)
                                .horizontalScroll(horizontalScrollState)
                        ) {
                            Column {
                                // Table Header
                                Row(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(vertical = 8.dp)
                                ) {
                                    result.columns.forEach { col ->
                                        Box(
                                            modifier = Modifier
                                                .widthIn(min = 100.dp, max = 220.dp)
                                                .padding(horizontal = 10.dp)
                                        ) {
                                            Text(
                                                text = col,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                ),
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                // Table Rows
                                result.rows.forEachIndexed { index, rowValues ->
                                    val rowBg = if (index % 2 == 0) {
                                        Color.Transparent
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .background(rowBg)
                                            .border(
                                                0.5.dp,
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                            )
                                            .padding(vertical = 8.dp)
                                    ) {
                                        rowValues.forEach { cellValue ->
                                            Box(
                                                modifier = Modifier
                                                    .widthIn(min = 100.dp, max = 220.dp)
                                                    .padding(horizontal = 10.dp)
                                            ) {
                                                SelectionContainer {
                                                    Text(
                                                        text = cellValue,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 12.sp
                                                        ),
                                                        color = if (cellValue == "NULL") {
                                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Raw JSON View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CodeEditorBgDark)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        val scroll = rememberScrollState()
                        SelectionContainer {
                            Text(
                                text = result.formattedJson,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 380.dp)
                                    .verticalScroll(scroll)
                            )
                        }
                    }
                }
            }
        }
    }
}
