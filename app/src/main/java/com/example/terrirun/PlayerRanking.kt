package com.example.terrirun

import androidx.compose.ui.graphics.Color

data class PlayerRanking(
    val ownerId: String,
    val playerName: String,
    val territoryCount: Int,
    val totalControl: Int,
    val color: Color = Color.Gray,
    val avatar: String = "avatar_1",
    val profileImage: String = ""
)