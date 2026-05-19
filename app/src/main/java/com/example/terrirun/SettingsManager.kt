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

    fun hasSeenNotificationDialog(): Boolean {
        return prefs.getBoolean(
            "notification_dialog_seen",
            false
        )
    }

    fun setNotificationDialogSeen(seen: Boolean) {
        prefs.edit()
            .putBoolean(
                "notification_dialog_seen",
                seen
            )
            .apply()
    }
}
