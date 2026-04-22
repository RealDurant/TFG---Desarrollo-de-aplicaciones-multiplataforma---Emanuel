package com.example.terrirun

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit

) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }

    var isDarkMode by remember {
        mutableStateOf(settingsManager.isDarkMode())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Modo oscuro")

            Switch(
                checked = isDarkMode,
                onCheckedChange = {
                    isDarkMode = it
                    settingsManager.setDarkMode(it)
                }
            )
        }

        Button(
            onClick = onBack,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text("Volver")
        }
    }

}