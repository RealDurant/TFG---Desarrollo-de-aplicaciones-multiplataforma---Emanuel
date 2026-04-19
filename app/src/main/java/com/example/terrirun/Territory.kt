package com.example.terrirun

import com.google.android.gms.maps.model.LatLng

enum class SettlementType {
    CASTLE,
    VILLAGE
}

data class Territory(
    val id: Int,
    val ownerId: String,
    val name: String,
    val points: List<LatLng>,
    val center: LatLng,
    val type: SettlementType,
    var control: Int
)