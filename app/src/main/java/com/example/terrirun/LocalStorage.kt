package com.example.terrirun


import android.content.Context
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject

fun saveGameState(
    context: Context,
    playerProfile: PlayerProfile,
    capitalReinforcementPoints: Int,
    nextTerritoryId: Int,
    territories: List<Territory>
) {
    val prefs = context.getSharedPreferences("terrirun_prefs", Context.MODE_PRIVATE)

    val territoriesJson = JSONArray()

    territories.forEach { territory ->
        val pointsJson = JSONArray()

        territory.points.forEach { point ->
            val pointJson = JSONObject()
            pointJson.put("lat", point.latitude)
            pointJson.put("lng", point.longitude)
            pointsJson.put(pointJson)
        }

        val territoryJson = JSONObject()
        territoryJson.put("id", territory.id)
        territoryJson.put("name", territory.name)
        territoryJson.put("type", territory.type.name)
        territoryJson.put("control", territory.control)
        territoryJson.put("centerLat", territory.center.latitude)
        territoryJson.put("centerLng", territory.center.longitude)
        territoryJson.put("points", pointsJson)

        territoriesJson.put(territoryJson)
    }

    prefs.edit()
        .putString("player_name", playerProfile.name)
        .putLong("player_color", playerProfile.territoryColor.value.toLong())
        .putInt("capital_reinforcement_points", capitalReinforcementPoints)
        .putInt("next_territory_id", nextTerritoryId)
        .putString("territories_json", territoriesJson.toString())
        .apply()
}

fun loadPlayerProfile(context: Context): PlayerProfile {
    val prefs = context.getSharedPreferences("terrirun_prefs", Context.MODE_PRIVATE)

    val name = prefs.getString("player_name", "Jugador 1") ?: "Jugador 1"
    val colorLong = prefs.getLong("player_color", Color(0xFF34A853).value.toLong())

    return PlayerProfile(
        name = name,
        territoryColor = Color(colorLong.toULong())
    )
}

fun loadCapitalReinforcementPoints(context: Context): Int {
    val prefs = context.getSharedPreferences("terrirun_prefs", Context.MODE_PRIVATE)
    return prefs.getInt("capital_reinforcement_points", 0)
}

fun loadNextTerritoryId(context: Context): Int {
    val prefs = context.getSharedPreferences("terrirun_prefs", Context.MODE_PRIVATE)
    return prefs.getInt("next_territory_id", 1)
}

fun loadTerritories(context: Context): List<Territory> {
    val prefs = context.getSharedPreferences("terrirun_prefs", Context.MODE_PRIVATE)
    val jsonString = prefs.getString("territories_json", null) ?: return emptyList()

    val territories = mutableListOf<Territory>()
    val territoriesJson = JSONArray(jsonString)

    for (i in 0 until territoriesJson.length()) {
        val territoryJson = territoriesJson.getJSONObject(i)
        val pointsJson = territoryJson.getJSONArray("points")

        val points = mutableListOf<LatLng>()
        for (j in 0 until pointsJson.length()) {
            val pointJson = pointsJson.getJSONObject(j)
            points.add(
                LatLng(
                    pointJson.getDouble("lat"),
                    pointJson.getDouble("lng")
                )
            )
        }

        val territory = Territory(
            id = territoryJson.getInt("id"),
            ownerId = territoryJson.getString("ownerId"),
            name = territoryJson.getString("name"),
            points = points,
            center = LatLng(
                territoryJson.getDouble("centerLat"),
                territoryJson.getDouble("centerLng")
            ),
            type = SettlementType.valueOf(territoryJson.getString("type")),
            control = territoryJson.getInt("control")
        )

        territories.add(territory)
    }

    return territories
}
fun clearGameState(context: Context) {
    val prefs = context.getSharedPreferences("terrirun_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
}