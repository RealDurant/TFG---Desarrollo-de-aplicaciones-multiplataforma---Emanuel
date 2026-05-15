package com.example.terrirun

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
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
        SettingsButton(
            text = if (language == "en") "Privacy and data" else "Privacidad y datos",
            onClick = onOpenPrivacy
        )

        SettingsButton(
            text = appText("back", language),
            onClick = onBack
        )

        SettingsButton(
            text = if (language == "en") "Notifications" else "Notificaciones",
            onClick = onOpenNotifications
        )

        SettingsButton(
            text = appText("logout", language),
            onClick = onLogout
        )

    }
}
@Composable
fun SettingsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.primary, // color uniforme para los botones
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary, // texto blanco sobre azul
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
