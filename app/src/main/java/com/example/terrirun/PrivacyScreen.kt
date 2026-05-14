package com.example.terrirun

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyScreen(
    language: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = if (language == "en") "Privacy and data" else "Privacidad y datos",
            style = MaterialTheme.typography.headlineMedium
        )

        SectionCard(
            title = if (language == "en") "Location" else "Ubicación",
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(
                if (language == "en")
                    "TerriRun uses your location to record routes, detect activity zones and create territories."
                else
                    "TerriRun usa tu ubicación para registrar rutas, detectar zonas de actividad y crear territorios."
            )
        }

        SectionCard(
            title = if (language == "en") "Data stored" else "Datos guardados",
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                if (language == "en")
                    "Your profile, territories, ranking data and game notifications are stored in Firebase."
                else
                    "Tu perfil, territorios, ranking y notificaciones del juego se guardan en Firebase."
            )
        }

        SectionCard(
            title = if (language == "en") "Profile image" else "Imagen de perfil",
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                if (language == "en")
                    "If you choose a profile image, it is used only to display your profile in the app."
                else
                    "Si eliges una imagen de perfil, se usa solo para mostrar tu perfil dentro de la app."
            )
        }

        Button(
            onClick = onBack,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(if (language == "en") "Back" else "Volver")
        }
    }
}