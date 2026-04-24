package com.example.terrirun

import androidx.compose.ui.graphics.Color

data class PlayerProfile(
    val name: String,
    val territoryColor: Color,
    val avatar: String = "avatar_1",
    val profileImage: String = ""
)