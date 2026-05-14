package com.example.terrirun

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotificationsScreen(
    language: String,
    currentUserId: String,
    onBack: () -> Unit
) {
    var notifications by remember { mutableStateOf<List<GameNotification>>(emptyList()) }

    LaunchedEffect(currentUserId) {
        UserRepository().getNotifications(currentUserId) {
            notifications = it.sortedByDescending { n -> n.timestamp }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (language == "en") "Notifications" else "Notificaciones",
            style = MaterialTheme.typography.headlineMedium
        )

        if (notifications.isEmpty()) {
            Text(
                text = if (language == "en") "No notifications yet." else "Aún no tienes notificaciones.",
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            notifications.forEach { notification ->
                SectionCard(
                    title = notification.title,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(notification.message)
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(if (language == "en") "Back" else "Volver")
        }
    }
}