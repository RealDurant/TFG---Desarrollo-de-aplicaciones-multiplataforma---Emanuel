package com.example.terrirun

import android.content.Context

class SettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun isDarkMode(): Boolean {
        return prefs.getBoolean("dark_mode", false)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }
}