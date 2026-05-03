package com.example.terrirun


import androidx.compose.ui.graphics.Color

data class GameUiState(
    val currentUserId: String = "",
    val playerProfile: PlayerProfile = PlayerProfile(
        name = "Jugador 1",
        territoryColor = Color(0xFF34A853)
    ),
    val reinforcementPoints: Int = 0,
    val territories: List<Territory> = emptyList(),
    val ownerNames: Map<String, String> = emptyMap(),
    val ownerColors: Map<String, Color> = emptyMap(),
    val ownerAvatars: Map<String, String> = emptyMap(),
    val ownerImages: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true
)