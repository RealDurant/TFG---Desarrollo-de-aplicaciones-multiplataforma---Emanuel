package com.example.terrirun

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.String

@Composable
fun RankingScreen(
    uiState: GameUiState,
    language: String,
    modifier: Modifier = Modifier,
    onCenterMap: (Territory) -> Unit
) {
    val ranking = uiState.territories
        .groupBy { it.ownerId }
        .map { (ownerId, playerTerritories) ->
            PlayerRanking(
                ownerId = ownerId,
                playerName = uiState.ownerNames[ownerId] ?: "Jugador",
                territoryCount = playerTerritories.size,
                totalControl = playerTerritories.sumOf { it.control },
                color = uiState.ownerColors[ownerId] ?: Color.Gray,
                avatar = uiState.ownerAvatars[ownerId] ?: "avatar_1",
                profileImage = uiState.ownerImages[ownerId] ?: ""
            )
        }
        .sortedWith(
            compareByDescending<PlayerRanking> { it.totalControl }
                .thenByDescending { it.territoryCount }
        )

    // Estado para jugador seleccionado (para abrir carta)
    var selectedPlayer by remember { mutableStateOf<PlayerRanking?>(null) }
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = appText("ranking_title", language),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = appText("ranking_subtitle", language),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
// Mostrar podium solo si hay al menos 3 jugadores
        if (ranking.size >= 3) {
            PodiumSection(
                topPlayers = ranking.take(3),
                currentUserId = uiState.currentUserId,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
        var searchQuery by remember { mutableStateOf("") }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(if (language == "en") "Search player" else "Buscar jugador") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
        val filteredRanking = ranking.filter {
            it.playerName.contains(searchQuery, ignoreCase = true)
        }
        SectionCard(
            title = appText("general_ranking", language),
            modifier = Modifier.padding(top = 24.dp)
        ) {
            filteredRanking.forEachIndexed { index, player ->
                RankingRow(
                    position = index + 1,
                    player = player,
                    isCurrentUser = player.ownerId == uiState.currentUserId,
                    language = language,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPlayer = player }
                )
            }
        }
    }

    // -------------------
    // DIALOGO DE JUGADOR
    // -------------------
    // ─── Carta del jugador ───
    selectedPlayer?.let { player ->
        val playerTerritories = uiState.territories.filter { it.ownerId == player.ownerId }

        AlertDialog(
            onDismissRequest = { selectedPlayer = null },
            title = { Text(if (player.ownerId == uiState.currentUserId) "Tú" else player.playerName) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    playerTerritories.forEach { territory ->
                        Text(
                            text = "${territory.name} - ${territory.type} - ${territory.control}%",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCenterMap(territory) // Solo aquí centramos el mapa
                                    selectedPlayer = null // Cerramos la carta
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPlayer = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}


@Composable
fun PodiumSection(
    topPlayers: List<PlayerRanking>,
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    if (topPlayers.size < 3) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        PodiumItem(
            player = topPlayers[1],
            position = 2,
            height = 105.dp,
            isCurrentUser = topPlayers[1].ownerId == currentUserId
        )

        PodiumItem(
            player = topPlayers[0],
            position = 1,
            height = 145.dp,
            isCurrentUser = topPlayers[0].ownerId == currentUserId
        )

        PodiumItem(
            player = topPlayers[2],
            position = 3,
            height = 85.dp,
            isCurrentUser = topPlayers[2].ownerId == currentUserId
        )
    }
}

@Composable
fun PodiumItem(
    player: PlayerRanking,
    position: Int,
    height: Dp,
    isCurrentUser: Boolean
) {
    val medal = when (position) {
        1 -> "🥇"
        2 -> "🥈"
        else -> "🥉"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Text(
            text = medal,
            style = MaterialTheme.typography.headlineMedium
        )

        RankingAvatar(
            player = player,
            size = 58.dp
        )

        Text(
            text = if (isCurrentUser) "${player.playerName} (Tú)" else player.playerName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = "${player.totalControl}%",
            style = MaterialTheme.typography.titleMedium,
            color = player.color
        )

        Surface(
            modifier = Modifier
                .padding(top = 8.dp)
                .width(70.dp)
                .height(height),
            color = player.color.copy(alpha = 0.75f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "#$position",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
fun RankingRow(
    position: Int,
    player: PlayerRanking,
    isCurrentUser: Boolean,
    language: String,
    modifier: Modifier = Modifier


) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isCurrentUser) player.color.copy(alpha = 0.16f) else Color(0xFFF7F7F7),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$position",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(42.dp)
            )

            RankingAvatar(
                player = player,
                size = 46.dp
            )

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = if (isCurrentUser) "${player.playerName} (Tú)" else player.playerName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${player.territoryCount} ${appText("territories", language)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${player.totalControl}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = player.color
                )
                Text(
                    text = appText("control", language),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun RankingAvatar(
    player: PlayerRanking,
    size: Dp
) {
    if (player.profileImage.isNotBlank()) {
        AsyncImage(
            model = player.profileImage,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        Surface(
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            color = player.color.copy(alpha = 0.25f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = avatarToEmoji(player.avatar),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

fun avatarToEmoji(avatar: String): String {
    return when (avatar) {
        "avatar_1" -> "⚔️"
        "avatar_2" -> "🛡️"
        "avatar_3" -> "🏰"
        "avatar_4" -> "🐎"
        "avatar_5" -> "👑"
        "avatar_6" -> "🛖"
        else -> "👤"
    }
}