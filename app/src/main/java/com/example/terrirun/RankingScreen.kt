package com.example.terrirun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RankingScreen(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val ranking = uiState.territories
        .groupBy { it.ownerId }
        .map { (ownerId, playerTerritories) ->
            PlayerRanking(
                ownerId = ownerId,
                playerName = uiState.ownerNames[ownerId] ?: "Cargando...",
                territoryCount = playerTerritories.size,
                totalControl = playerTerritories.sumOf { it.control }
            )
        }
        .sortedWith(
            compareByDescending<PlayerRanking> { it.territoryCount }
                .thenByDescending { it.totalControl }
        )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Ranking")

        if (ranking.isEmpty()) {
            Text(
                text = "Aún no hay jugadores en el ranking.",
                modifier = Modifier.padding(top = 12.dp)
            )
        } else {
            ranking.forEachIndexed { index, item ->
                val isMe = item.ownerId == uiState.currentUserId
                val displayName = if (isMe) "${item.playerName} (Tú)" else item.playerName

                SectionCard(
                    title = "${index + 1}. $displayName",
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(text = "Territorios: ${item.territoryCount}")
                    Text(text = "Control total: ${item.totalControl}%")
                }
            }
        }
    }
}