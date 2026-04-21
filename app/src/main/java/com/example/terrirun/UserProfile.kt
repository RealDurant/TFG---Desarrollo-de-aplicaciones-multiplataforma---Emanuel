package com.example.terrirun

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "Jugador 1",
    val colorHex: String = "#34A853",
    val avatar: String = "avatar_1",
    val createdAt: Long = System.currentTimeMillis(),
    val reinforcementPoints: Int = 0
)