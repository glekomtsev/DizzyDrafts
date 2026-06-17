package com.dizzydrafts.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.dizzydrafts.app.data.FlashCard
import com.dizzydrafts.app.data.SavedTable
import com.dizzydrafts.app.data.SheetParser
import com.dizzydrafts.app.data.TableStorage
import com.dizzydrafts.app.ui.screens.FlashCardScreen
import com.dizzydrafts.app.ui.screens.TableListScreen
import com.dizzydrafts.app.ui.screens.UrlInputScreen
import com.dizzydrafts.app.ui.theme.DizzyDraftsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed class Screen {
    data object TableList : Screen()
    data object UrlInput : Screen()
    data class Study(val table: SavedTable, val cards: List<FlashCard>) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DizzyDraftsTheme {
                val scope = rememberCoroutineScope()
                var tables by remember { mutableStateOf(TableStorage.loadTables(this@MainActivity)) }
                var screen by remember { mutableStateOf<Screen>(Screen.TableList) }
                var loading by remember { mutableStateOf(false) }
                var loadingError by remember { mutableStateOf<String?>(null) }
                var refreshError by remember { mutableStateOf<String?>(null) }

                if (tables.isEmpty()) screen = Screen.UrlInput

                when (val current = screen) {
                    is Screen.TableList -> {
                        TableListScreen(
                            tables = tables,
                            onTableTap = { table ->
                                refreshError = null
                                scope.launch {
                                    loading = true
                                    loadingError = null
                                    try {
                                        val cards = withContext(Dispatchers.IO) {
                                            TableStorage.loadCards(this@MainActivity, table.id)
                                        }
                                        screen = Screen.Study(table, cards)
                                    } catch (e: Exception) {
                                        loadingError = e.message
                                    }
                                    loading = false
                                }
                            },
                            onAddTable = { screen = Screen.UrlInput },
                            onDeleteTable = { table ->
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        TableStorage.deleteTable(this@MainActivity, table.id)
                                    }
                                    tables = TableStorage.loadTables(this@MainActivity)
                                }
                            },
                            onRefreshTable = { table ->
                                scope.launch {
                                    try {
                                        val result = withContext(Dispatchers.IO) {
                                            SheetParser.parse(table.url)
                                        }
                                        val updatedTable = table.copy(name = result.title)
                                        withContext(Dispatchers.IO) {
                                            TableStorage.saveTable(this@MainActivity, updatedTable, result.cards)
                                        }
                                        tables = TableStorage.loadTables(this@MainActivity)
                                        refreshError = null
                                    } catch (_: Exception) {
                                        refreshError = "Нет сети"
                                    }
                                }
                            },
                            refreshError = refreshError
                        )
                    }

                    is Screen.UrlInput -> {
                        UrlInputScreen(
                            onUrlValidated = { url ->
                                scope.launch {
                                    loading = true
                                    loadingError = null
                                    try {
                                        val result = withContext(Dispatchers.IO) {
                                            SheetParser.parse(url)
                                        }
                                        if (result.cards.isEmpty()) {
                                            loadingError = "Таблица пуста"
                                            return@launch
                                        }
                                        val table = SavedTable(
                                            id = TableStorage.generateId(),
                                            name = result.title,
                                            url = url
                                        )
                                        withContext(Dispatchers.IO) {
                                            TableStorage.saveTable(this@MainActivity, table, result.cards)
                                        }
                                        tables = TableStorage.loadTables(this@MainActivity)
                                        screen = Screen.Study(table, result.cards)
                                    } catch (e: Exception) {
                                        loadingError = "Ошибка: ${e.message}"
                                    }
                                    loading = false
                                }
                            },
                            onBack = {
                                tables = TableStorage.loadTables(this@MainActivity)
                                screen = Screen.TableList
                            },
                            isLoading = loading,
                            error = loadingError,
                            onErrorDismiss = { loadingError = null }
                        )
                    }

                    is Screen.Study -> {
                        FlashCardScreen(
                            cards = current.cards,
                            name = current.table.name,
                            onBack = {
                                tables = TableStorage.loadTables(this@MainActivity)
                                screen = Screen.TableList
                            }
                        )
                    }
                }
            }
        }
    }
}
