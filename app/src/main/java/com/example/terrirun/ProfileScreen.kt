package com.example.terrirun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    uiState: GameUiState,
    modifier: Modifier = Modifier,
    language: String
) {
    val userName = uiState.playerProfile.name
    val userColor = uiState.playerProfile.territoryColor
    val reinforcementPoints = uiState.reinforcementPoints

    val myTerritories = uiState.territories.filter {
        it.ownerId == uiState.currentUserId
    }

    val capital = myTerritories.find { it.type == SettlementType.CASTLE }
    val villageCount = myTerritories.count { it.type == SettlementType.VILLAGE }
    val totalControl = myTerritories.sumOf { it.control }
    val profileImage = uiState.playerProfile.profileImage
    val avatar = uiState.playerProfile.avatar
    val maxControl = 1000 // ajustable
    val progress = (totalControl.toFloat() / maxControl).coerceIn(0f, 1f)
    val notificationRepository = remember { NotificationRepository() }

    var notifications by remember {
        mutableStateOf<List<NotificationData>>(emptyList())
    }

    LaunchedEffect(Unit) {
        notificationRepository.getNotifications(
            uiState.currentUserId
        ) {
            notifications = it
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionCard(
            title = "Perfil",
            modifier = Modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {

                if (profileImage.isNotBlank()) {
                    AsyncImage(
                        model = profileImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape),
                        color = Color.LightGray
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (avatar) {
                                    "avatar_1" -> "⚔️"
                                    "avatar_2" -> "🛡️"
                                    "avatar_3" -> "🏰"
                                    "avatar_4" -> "🐎"
                                    "avatar_5" -> "👑"
                                    "avatar_6" -> "🛖"
                                    else -> "👤"
                                },
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(text = userName, style = MaterialTheme.typography.headlineSmall)
                    Text(text = "${appText("reinforcement", language)}: $reinforcementPoints")
                }
            }
            Text(
                text = appText("profile_progress", language),
                modifier = Modifier.padding(top = 12.dp)
            )

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = userColor
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                StatCard(appText("territories", language), myTerritories.size.toString())
                StatCard(appText("villages", language), villageCount.toString())
                StatCard(appText("control", language), "$totalControl%")
            }
        }
        var notificationsEnabled by remember { mutableStateOf(true) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(appText("notifications", language))

            Switch(
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it

                    val uid = uiState.currentUserId
                    UserRepository().updateNotifications(uid, it) { _, _ -> }
                }
            )
        }
        SectionCard(
            title = "Notificaciones",
            modifier = Modifier.padding(top = 20.dp)
        ) {

            if (notifications.isEmpty()) {

                Text("No tienes notificaciones.")

            } else {

                notifications.take(5).forEach { notification ->

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp
                    ) {

                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            Text(
                                text = notification.title,
                                style = MaterialTheme.typography.titleSmall
                            )

                            Text(
                                text = notification.message,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        Button(
            onClick = onLogout,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(appText("logout", language))
        }
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 3.dp,
        modifier = Modifier
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}