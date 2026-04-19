package com.example.terrirun

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.compareTo
import androidx.compose.ui.graphics.Color



fun distanceInMeters(point1: LatLng, point2: LatLng): Float {
    val results = FloatArray(1)
    Location.distanceBetween(
        point1.latitude, point1.longitude, point2.latitude, point2.longitude, results
    )
    return results[0]
}

fun formatTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun calculateCenter(points: List<LatLng>): LatLng {
    val avgLat = points.map { it.latitude }.average()
    val avgLng = points.map { it.longitude }.average()
    return LatLng(avgLat, avgLng)
}

fun getCapitalTerritory(territories: List<Territory>): Territory? {
    return territories.find { it.type == SettlementType.CASTLE }
}
fun isActivityCoveringCapital(
    routePoints: List<LatLng>,
    capital: Territory?,
    maxDistanceMeters: Float = 200f,
    requiredCoverage: Float = 0.4f
): Boolean {
    if (routePoints.isEmpty() || capital == null) return false

    val pointsNearCapital = routePoints.count { point ->
        distanceInMeters(point, capital.center) <= maxDistanceMeters
    }

    val coverage = pointsNearCapital.toFloat() / routePoints.size.toFloat()
    return coverage >= requiredCoverage
}
fun findPatrolledTerritory(
    routePoints: List<LatLng>,
    territories: List<Territory>,
    maxDistanceMeters: Float = 200f,
    requiredCoverage: Float = 0.4f
): Territory? {
    if (routePoints.isEmpty() || territories.isEmpty()) return null

    return territories.firstOrNull { territory ->
        val pointsNearTerritory = routePoints.count { point ->
            distanceInMeters(point, territory.center) <= maxDistanceMeters
        }

        val coverage = pointsNearTerritory.toFloat() / routePoints.size.toFloat()
        coverage >= requiredCoverage
    }
}
fun generateCapitalName(): String {
    return "Capital del Reino"
}

fun generateVillageName(existingNames: List<String>): String {
    val baseNames = listOf(
        "Villa del Río",
        "Bastión Norte",
        "Loma Verde",
        "Poblado del Valle",
        "Aldea del Roble",
        "Paso del Este",
        "Villa del Camino",
        "Refugio del Sur",
        "Colina del Alba",
        "Bastión del Bosque"
    )

    val availableNames = baseNames.filter { it !in existingNames }

    return if (availableNames.isNotEmpty()) {
        availableNames.random()
    } else {
        "Poblado ${existingNames.size + 1}"
    }
}
fun hexToComposeColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF34A853)
    }
}

fun Territory.toDto(): TerritoryDto {
    return TerritoryDto(
        id = id,
        ownerId = ownerId,
        name = name,
        type = type.name,
        control = control,
        centerLat = center.latitude,
        centerLng = center.longitude,
        points = points.map { LatLngDto(it.latitude, it.longitude) }
    )
}

fun TerritoryDto.toDomain(): Territory {
    return Territory(
        id = id,
        ownerId = ownerId,
        name = name,
        points = points.map { LatLng(it.lat, it.lng) },
        center = LatLng(centerLat, centerLng),
        type = SettlementType.valueOf(type),
        control = control
    )
}