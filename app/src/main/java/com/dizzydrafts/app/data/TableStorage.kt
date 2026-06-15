package com.dizzydrafts.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class SavedTable(
    val id: String,
    val name: String,
    val url: String
)

object TableStorage {
    private const val PREFS_NAME = "dizzy_tables"
    private const val KEY_TABLES = "saved_tables"

    fun loadTables(context: Context): List<SavedTable> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TABLES, null) ?: return emptyList()
        return parseTableList(json)
    }

    fun saveTable(context: Context, table: SavedTable, cards: List<FlashCard>) {
        val tables = loadTables(context).toMutableList()
        val existing = tables.indexOfFirst { it.id == table.id }
        if (existing >= 0) tables[existing] = table else tables.add(table)
        saveTableList(context, tables)
        saveCards(context, table.id, cards)
    }

    fun deleteTable(context: Context, tableId: String) {
        val tables = loadTables(context).filter { it.id != tableId }
        saveTableList(context, tables)
        val file = File(context.filesDir, "tables/$tableId.json")
        file.delete()
    }

    fun loadCards(context: Context, tableId: String): List<FlashCard> {
        val file = File(context.filesDir, "tables/$tableId.json")
        if (!file.exists()) return emptyList()
        val json = file.readText()
        return parseCards(json)
    }

    fun generateId(): String = UUID.randomUUID().toString()

    private fun saveTableList(context: Context, tables: List<SavedTable>) {
        val arr = JSONArray()
        tables.forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id)
                put("name", t.name)
                put("url", t.url)
            })
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TABLES, arr.toString())
            .apply()
    }

    private fun saveCards(context: Context, tableId: String, cards: List<FlashCard>) {
        val dir = File(context.filesDir, "tables")
        dir.mkdirs()
        val arr = JSONArray()
        cards.forEach { c ->
            arr.put(JSONObject().apply {
                put("topic", c.topic)
                put("question", c.question)
                put("answer", c.answer)
            })
        }
        File(dir, "$tableId.json").writeText(arr.toString())
    }

    private fun parseTableList(json: String): List<SavedTable> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            SavedTable(
                id = obj.getString("id"),
                name = obj.getString("name"),
                url = obj.getString("url")
            )
        }
    }

    private fun parseCards(json: String): List<FlashCard> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            FlashCard(
                topic = obj.getString("topic"),
                question = obj.getString("question"),
                answer = obj.getString("answer")
            )
        }
    }
}
