package com.dizzydrafts.app.data

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class FlashCard(
    val topic: String,
    val question: String,
    val answer: String
)

object SheetParser {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    data class ParseResult(val cards: List<FlashCard>, val title: String)

    fun parse(sheetUrl: String): ParseResult {
        val sheetId = extractSheetId(sheetUrl)
            ?: throw IllegalArgumentException("Не удалось извлечь ID таблицы из ссылки")

        val csvUrl = "https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv"
        val csv = fetchCsv(csvUrl)
        val cards = parseCsv(csv)
        val title = fetchTitle(sheetUrl)
        return ParseResult(cards, title)
    }

    private fun extractSheetId(url: String): String? {
        val regex = Regex("/spreadsheets/d/([a-zA-Z0-9_-]+)")
        return regex.find(url)?.groupValues?.getOrNull(1)
    }

    private fun fetchCsv(url: String): String {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code} ${response.message}")
        }
        return response.body?.string() ?: throw Exception("Пустой ответ от сервера")
    }

    private fun fetchTitle(sheetUrl: String): String {
        val id = extractSheetId(sheetUrl) ?: return "Таблица"
        try {
            val request = Request.Builder()
                .url("https://docs.google.com/spreadsheets/d/$id/")
                .build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return id
            val titleMatch = Regex("<title>(.*?)</title>").find(html)
            val raw = titleMatch?.groupValues?.getOrNull(1) ?: return id
            return raw.removeSuffix(" - Google Sheets").trim()
        } catch (_: Exception) {
            return id
        }
    }

    private fun parseCsv(csv: String): List<FlashCard> {
        val lines = csv.lines().filter { it.isNotBlank() }
        if (lines.size < 2) return emptyList()

        return lines.drop(1).mapNotNull { line ->
            val parsed = parseCsvLine(line)
            if (parsed.size >= 3) {
                FlashCard(
                    topic = parsed[0].trim().removeSurrounding("\""),
                    question = parsed[1].trim().removeSurrounding("\""),
                    answer = parsed[2].trim().removeSurrounding("\"")
                )
            } else null
        }.filter { it.question.isNotBlank() && it.answer.isNotBlank() }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())
        return result
    }
}
