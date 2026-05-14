package com.example.terrirun

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    language: String,
    onLanguageChange: (String) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenNotifications: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = appText("language", language),
            modifier = Modifier.padding(top = 24.dp)
        )

        Row(modifier = Modifier.padding(top = 8.dp)) {
            FilterChip(
                selected = language == "es",
                onClick = { onLanguageChange("es") },
                label = { Text("Español") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilterChip(
                selected = language == "en",
                onClick = { onLanguageChange("en") },
                label = { Text("English") }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(appText("notifications", language))

            Switch(
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsChange
            )
        }

        SectionCard(
            title = appText("privacy", language),
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                if (language == "en")
                    "TerriRun uses your location to record routes and create territories."
                else
                    "TerriRun usa tu ubicación para registrar rutas y crear territorios."
            )

            Text(
                if (language == "en")
                    "Your profile, territories and ranking data are stored in Firebase."
                else
                    "Tus datos de perfil, territorios y ranking se guardan en Firebase."
            )
        }
        Button(
            onClick = onOpenPrivacy,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(if (language == "en") "Privacy and data" else "Privacidad y datos")
        }

        Button(
            onClick = onBack,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(appText("back", language))
        }
        Button(
            onClick = onOpenNotifications,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(if (language == "en") "Notifications" else "Notificaciones")
        }
        Button(
            onClick = onLogout,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(appText("logout", language))
        }

    }
}