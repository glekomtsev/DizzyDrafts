package com.dizzydrafts.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun UrlInputScreen(
    onUrlValidated: (String) -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    onErrorDismiss: () -> Unit = {}
) {
    var url by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DizzyDrafts",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Введите ссылку на Google Таблицу",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = url,
            onValueChange = {
                url = it
                validationError = null
                onErrorDismiss()
            },
            label = { Text("Ссылка на таблицу") },
            placeholder = { Text("https://docs.google.com/spreadsheets/d/...") },
            modifier = Modifier.fillMaxWidth(),
            isError = validationError != null || error != null,
            supportingText = {
                val msg = validationError ?: error
                if (msg != null) Text(msg)
            },
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (url.isBlank() || !url.contains("docs.google.com/spreadsheets")) {
                    validationError = "Введите корректную ссылку на Google Таблицу"
                } else {
                    onUrlValidated(url)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.height(24.dp)
                )
            } else {
                Text("Загрузить", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Таблица должна быть доступна по ссылке\nКолонки: Тема, Вопрос, Ответ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("Назад")
        }
    }
}
