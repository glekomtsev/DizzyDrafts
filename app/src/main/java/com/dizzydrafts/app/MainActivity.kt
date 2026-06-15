package com.dizzydrafts.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.dizzydrafts.app.ui.screens.FlashCardScreen
import com.dizzydrafts.app.ui.screens.UrlInputScreen
import com.dizzydrafts.app.ui.theme.DizzyDraftsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("dizzy", Context.MODE_PRIVATE)
        var savedUrl = prefs.getString("sheet_url", null)

        setContent {
            DizzyDraftsTheme {
                var currentUrl by remember { mutableStateOf(savedUrl) }

                if (currentUrl == null) {
                    UrlInputScreen(onUrlSaved = { url ->
                        prefs.edit().putString("sheet_url", url).apply()
                        currentUrl = url
                    })
                } else {
                    FlashCardScreen(
                        sheetUrl = currentUrl!!,
                        onReset = {
                            prefs.edit().remove("sheet_url").apply()
                            currentUrl = null
                        }
                    )
                }
            }
        }
    }
}
