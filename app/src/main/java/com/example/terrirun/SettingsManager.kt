package com.example.terrirun

import android.content.Context

class SettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun getLanguage(): String {
        return prefs.getString("language", "es") ?: "es"
    }

    fun setLanguage(language: String) {
        prefs.edit().putString("language", language).apply()
    }
}