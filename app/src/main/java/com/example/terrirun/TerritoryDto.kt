package com.example.terrirun


data class LatLngDto(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

data class TerritoryDto(
    val id: Int = 0,
    val ownerId: String = "",
    val name: String = "",
    val type: String = "",
    val control: Int = 0,
    val centerLat: Double = 0.0,
    val centerLng: Double = 0.0,
    val points: List<LatLngDto> = emptyList()
)