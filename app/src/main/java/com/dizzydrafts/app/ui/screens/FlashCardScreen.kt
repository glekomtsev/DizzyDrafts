package com.dizzydrafts.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dizzydrafts.app.data.FlashCard
import com.dizzydrafts.app.data.SheetParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

@Composable
fun FlashCardScreen(sheetUrl: String, onReset: () -> Unit) {
    var cards by remember { mutableStateOf<List<FlashCard>>(emptyList()) }
    var currentCard by remember { mutableStateOf<FlashCard?>(null) }
    var showAnswer by remember { mutableStateOf(false) }
    var showQuestionSide by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var errorState by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sheetUrl) {
        isLoading = true
        errorState = null
        try {
            val parsed = withContext(Dispatchers.IO) {
                SheetParser.parse(sheetUrl)
            }
            if (parsed.isEmpty()) {
                errorState = "Таблица пуста или не содержит данных"
            } else {
                cards = parsed
                pickNext(parsed, null) { card, showQ ->
                    currentCard = card
                    showQuestionSide = showQ
                    showAnswer = false
                }
            }
        } catch (e: Exception) {
            errorState = "Ошибка: ${e.message}"
        }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Тема: ${currentCard?.topic ?: "—"}",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onReset) {
                    Text("Сбросить")
                }
            }
        }

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorState != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorState!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onReset) {
                            Text("Изменить таблицу")
                        }
                    }
                }
            }
            currentCard != null -> {
                FlashCardContent(
                    card = currentCard!!,
                    showQuestionSide = showQuestionSide,
                    showAnswer = showAnswer,
                    onReveal = { showAnswer = true },
                    onNext = {
                        pickNext(cards, currentCard) { card, showQ ->
                            currentCard = card
                            showQuestionSide = showQ
                            showAnswer = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FlashCardContent(
    card: FlashCard,
    showQuestionSide: Boolean,
    showAnswer: Boolean,
    onReveal: () -> Unit,
    onNext: () -> Unit
) {
    val topText = if (showQuestionSide) card.question else card.answer
    val bottomText = if (showQuestionSide) card.answer else card.question
    val topLabel = if (showQuestionSide) "Вопрос" else "Ответ"
    val bottomLabel = if (showQuestionSide) "Ответ" else "Вопрос"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = topLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = topText,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(
                    if (!showAnswer) Modifier.clickable { onReveal() }
                    else Modifier
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (showAnswer)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showAnswer) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = bottomLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = bottomText,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = "Нажмите, чтобы показать $bottomLabel",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Дальше",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

private fun pickNext(
    cards: List<FlashCard>,
    current: FlashCard?,
    onResult: (FlashCard, Boolean) -> Unit
) {
    val filtered = if (current != null && cards.size > 1) {
        cards.filter { it != current }
    } else {
        cards
    }
    val card = filtered.random()
    val showQ = Random.nextBoolean()
    onResult(card, showQ)
}
