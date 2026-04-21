package com.example.terrirun

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
@Composable
fun CompleteProfileScreen(
    email: String,
    onSaveProfile: (name: String, colorHex: String, avatar: String) -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var red by remember { mutableStateOf(52f) }
    var green by remember { mutableStateOf(168f) }
    var blue by remember { mutableStateOf(83f) }
    var brightness by remember { mutableStateOf(1f) }   // 0.5f - 1.5f
    var alpha by remember { mutableStateOf(1f) }        // 0f - 1f
    val baseColor = Color(
        red = red / 255f,
        green = green / 255f,
        blue = blue / 255f
    )

    val selectedColor = Color(
        red = (baseColor.red * brightness).coerceIn(0f, 1f),
        green = (baseColor.green * brightness).coerceIn(0f, 1f),
        blue = (baseColor.blue * brightness).coerceIn(0f, 1f),
        alpha = alpha
    )

    var selectedAvatar by remember { mutableStateOf("avatar_1") }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    val availableAvatars = listOf(
        "avatar_1" to "⚔️",
        "avatar_2" to "🛡️",
        "avatar_3" to "🏰",
        "avatar_4" to "🐎",
        "avatar_5" to "👑",
        "avatar_6" to "🛖",
        "avatar_7" to "🪖",
        "avatar_8" to "🦀",
        "avatar_9" to "🦍",
        "avatar_10" to "🦣",

    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Completa tu perfil",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = email,
            modifier = Modifier.padding(top = 8.dp)
        )

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it.take(20) },
            label = { Text("Nombre de usuario") },
            singleLine = true,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = "Color del reino",
            modifier = Modifier.padding(top = 16.dp)
        )

        Surface(
            modifier = Modifier
                .padding(top = 10.dp)
                .size(72.dp)
                .clip(CircleShape),
            color = selectedColor,
            shadowElevation = 6.dp
        ) {}

        Text(
            text = "Rojo: ${red.toInt()}",
            modifier = Modifier.padding(top = 16.dp)
        )

        Slider(
            value = red,
            onValueChange = { red = it },
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "Verde: ${green.toInt()}")

        Slider(
            value = green,
            onValueChange = { green = it },
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "Azul: ${blue.toInt()}")

        Slider(
            value = blue,
            onValueChange = { blue = it },
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Brillo: ${"%.2f".format(brightness)}",
            modifier = Modifier.padding(top = 12.dp)
        )

        Slider(
            value = brightness,
            onValueChange = { brightness = it },
            valueRange = 0.5f..1.5f,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Transparencia: ${"%.2f".format(alpha)}",
            modifier = Modifier.padding(top = 12.dp)
        )

        Slider(
            value = alpha,
            onValueChange = { alpha = it },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Avatar",
            modifier = Modifier.padding(top = 16.dp)
        )

        val firstRowAvatars = availableAvatars.take(4)
        val secondRowAvatars = availableAvatars.drop(4)

        Row(modifier = Modifier.padding(top = 8.dp)) {
            firstRowAvatars.forEach { (avatarId, emoji) ->
                Surface(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedAvatar = avatarId },
                    color = if (selectedAvatar == avatarId) {
                        Color(0xFFEDE7F6)
                    } else {
                        Color(0xFFF5F5F5)
                    }
                ) {
                    Text(
                        text = emoji,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Row(modifier = Modifier.padding(top = 8.dp)) {
            secondRowAvatars.forEach { (avatarId, emoji) ->
                Surface(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedAvatar = avatarId },
                    color = if (selectedAvatar == avatarId) {
                        Color(0xFFEDE7F6)
                    } else {
                        Color(0xFFF5F5F5)
                    }
                ) {
                    Text(
                        text = emoji,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Button(
            onClick = {
                if (userName.isBlank()) {
                    errorMessage = "Introduce un nombre de usuario"
                    return@Button
                }

                val solidColor = selectedColor.copy(alpha = 1f)

                val colorHex = String.format(
                    "#%06X",
                    0xFFFFFF and solidColor.toArgb()
                )

                onSaveProfile(userName, colorHex, selectedAvatar)
            },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text("Guardar perfil")
        }
    }
}