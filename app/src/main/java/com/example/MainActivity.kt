package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SupabaseSqlViewModel
import com.example.ui.components.CredentialsCard
import com.example.ui.components.QueryHistoryDialog
import com.example.ui.components.QueryResultCard
import com.example.ui.components.SnippetsDialog
import com.example.ui.components.SqlEditorCard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SupabaseGreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SupabaseSqlApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseSqlApp(viewModel: SupabaseSqlViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.notificationMessage) {
        uiState.notificationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissNotification()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("app_title")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SupabaseGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DataObject,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Supabase SQL",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (uiState.projectRef.isNotBlank()) "Ref: ${uiState.projectRef}" else "Management API",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleCredentialsExpanded() },
                        modifier = Modifier.testTag("toggle_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Connection info",
                            tint = if (uiState.apiToken.isNotBlank() && uiState.projectRef.isNotBlank()) {
                                SupabaseGreen
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 700.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Supabase API Token & Project Ref Input Section
                CredentialsCard(
                    apiToken = uiState.apiToken,
                    projectRef = uiState.projectRef,
                    isTokenVisible = uiState.isTokenVisible,
                    isExpanded = uiState.isCredentialsExpanded,
                    onApiTokenChanged = viewModel::onApiTokenChanged,
                    onProjectRefChanged = viewModel::onProjectRefChanged,
                    onToggleTokenVisibility = viewModel::toggleTokenVisibility,
                    onToggleExpanded = viewModel::toggleCredentialsExpanded
                )

                Spacer(modifier = Modifier.height(14.dp))

                // SQL Input Box and Run Button
                SqlEditorCard(
                    sqlQuery = uiState.sqlQuery,
                    isLoading = uiState.isLoading,
                    onSqlQueryChanged = viewModel::onSqlQueryChanged,
                    onClearQuery = viewModel::clearQuery,
                    onOpenTemplates = { viewModel.setShowSnippetsDialog(true) },
                    onOpenHistory = { viewModel.setShowHistoryDialog(true) },
                    onRunQuery = viewModel::runQuery
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Query Execution Results (Table / JSON / Error)
                QueryResultCard(
                    result = uiState.executionResult,
                    viewMode = uiState.viewMode,
                    onViewModeChanged = viewModel::setViewMode
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // History Dialog
    if (uiState.showHistoryDialog) {
        QueryHistoryDialog(
            history = uiState.history,
            onSelectQuery = viewModel::selectHistoryItem,
            onClearHistory = viewModel::clearHistory,
            onDismiss = { viewModel.setShowHistoryDialog(false) }
        )
    }

    // Snippets Dialog
    if (uiState.showSnippetsDialog) {
        SnippetsDialog(
            onSelectSnippet = viewModel::insertSnippet,
            onDismiss = { viewModel.setShowSnippetsDialog(false) }
        )
    }
}
