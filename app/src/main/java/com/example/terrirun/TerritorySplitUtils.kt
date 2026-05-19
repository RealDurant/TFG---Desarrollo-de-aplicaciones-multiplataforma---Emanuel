package com.example.terrirun

import com.google.android.gms.maps.model.LatLng

data class SplitTerritoryResult(
    val conqueredPart: Territory,
    val remainingPart: Territory?
)
fun cleanTerritoryName(name: String): String {
    var clean = name

    repeat(10) {
        clean = clean
            .replace("Zona conquistada de ", "")
            .replace("Restos de ", "")
            .replace("Conquista de ", "")
    }

    return clean.trim()
}
fun splitTerritoryAfterClaim(
    original: Territory,
    conquerRoute: List<LatLng>,
    newOwnerId: String
): SplitTerritoryResult {
    if (conquerRoute.size < 3) {
        return SplitTerritoryResult(
            conqueredPart = original.copy(
                id = (100000..999999).random(),
                ownerId = newOwnerId,
                name = "Conquista de ${cleanTerritoryName(original.name)}",
                control = 20
            ),
            remainingPart = null
        )
    }
    val cleanConquerRoute = simplifyRoutePoints(conquerRoute)
    if (cleanConquerRoute.size < 3) {
        return SplitTerritoryResult(
            conqueredPart = original.copy(
                id = (100000..999999).random(),
                ownerId = newOwnerId,
                name = "Conquista de ${cleanTerritoryName(original.name)}",
                control = 20
            ),
            remainingPart = null
        )
    }

    val fullConquest = routeContainsTerritory(
        routePoints = conquerRoute,
        territoryPoints = original.points
    )

    val conquered = original.copy(
        id = (100000..999999).random(),
        ownerId = newOwnerId,
        name = if (fullConquest)
            "Conquista de ${cleanTerritoryName(original.name)}"
        else
            "Zona conquistada de ${cleanTerritoryName(original.name)}",
        points = if (fullConquest) original.points else cleanConquerRoute,
        center = if (fullConquest) original.center else calculateCenter(cleanConquerRoute),
        control = 20
    )

    val remaining =
        if (fullConquest) {
            null
        } else {
            original.copy(
                name = "Restos de ${cleanTerritoryName(original.name)}",
                points = original.points,
                center = original.center,
                control = 20
            )
        }

    return SplitTerritoryResult(
        conqueredPart = conquered,
        remainingPart = remaining
    )
}
fun routeContainsTerritory(
    routePoints: List<LatLng>,
    territoryPoints: List<LatLng>
): Boolean {
    val routeBounds = getBounds(routePoints)
    val territoryBounds = getBounds(territoryPoints)

    val routeContainsBounds =
        routeBounds.left <= territoryBounds.left &&
                routeBounds.top <= territoryBounds.top &&
                routeBounds.right >= territoryBounds.right &&
                routeBounds.bottom >= territoryBounds.bottom

    val territoryPointsInsideRouteBounds =
        territoryPoints.count { point ->
            point.longitude.toFloat() >= routeBounds.left &&
                    point.longitude.toFloat() <= routeBounds.right &&
                    point.latitude.toFloat() >= routeBounds.top &&
                    point.latitude.toFloat() <= routeBounds.bottom
        }

    val enoughTerritoryInside =
        territoryPointsInsideRouteBounds >= territoryPoints.size * 0.8

    return routeContainsBounds && enoughTerritoryInside && isClosedRoute(routePoints)
}
fun isPointNearRoute(
    point: LatLng,
    route: List<LatLng>,
    maxDistanceMeters: Float = 20f
): Boolean {
    return route.any { routePoint ->
        distanceInMeters(point, routePoint) <= maxDistanceMeters
    }
}
fun simplifyRoutePoints(
    points: List<LatLng>,
    minDistanceMeters: Float = 10f
): List<LatLng> {
    if (points.isEmpty()) return emptyList()

    val simplified = mutableListOf<LatLng>()
    var lastPoint = points.first()
    simplified.add(lastPoint)

    points.drop(1).forEach { point ->
        if (distanceInMeters(lastPoint, point) >= minDistanceMeters) {
            simplified.add(point)
            lastPoint = point
        }
    }

    return simplified
}