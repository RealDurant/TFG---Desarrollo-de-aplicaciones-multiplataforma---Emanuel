package com.example.terrirun


data class PlayerRanking(
    val ownerId: String,
    val playerName: String,
    val territoryCount: Int,
    val totalControl: Int
)