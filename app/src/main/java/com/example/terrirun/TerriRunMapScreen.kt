package com.example.terrirun

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("MissingPermission", "UnrememberedMutableState")
@Composable
fun TerriRunMapScreen(
    permissionGranted: Boolean,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    uiState: GameUiState,
    onRefreshData: () -> Unit
) {

    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val defaultLocation = LatLng(40.4168, -3.7038)
    var userLocation by remember { mutableStateOf(defaultLocation) }
    var isTracking by remember { mutableStateOf(false) }
    val routePoints = remember { mutableStateListOf<LatLng>() }
    var elapsedTimeSeconds by remember { mutableStateOf(0) }
    var totalDistanceMeters by remember { mutableStateOf(0f) }
    var showSummaryDialog by remember { mutableStateOf(false) }
    var summaryTitle by remember { mutableStateOf("") }
    var summaryMessage by remember { mutableStateOf("") }
    var selectedTerritory by remember { mutableStateOf<Territory?>(null) }
    val territories = remember { mutableStateListOf<Territory>() }
    var capitalReinforcementPoints by remember {
        mutableStateOf(uiState.reinforcementPoints)
    }
    val ownerNames = uiState.ownerNames
    val ownerColors = uiState.ownerColors
    var nextTerritoryId by remember { mutableStateOf(1) }

    val authManager = remember { AuthManager() }


    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(2000).build()
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val newPoint = LatLng(location.latitude, location.longitude)
                    userLocation = newPoint

                    if (isTracking) {
                        if (routePoints.isNotEmpty()) {
                            val lastPoint = routePoints.last()
                            totalDistanceMeters += distanceInMeters(lastPoint, newPoint)
                        }
                        routePoints.add(newPoint)
                    }

                    cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation, 17f)
                }
            }
        }
    }
    val capitalTerritory = getCapitalTerritory(territories)


    val playerProfile = uiState.playerProfile



    val territoryRepository = remember { TerritoryRepository() }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation, 17f)
                }
            }
        }
    }
    LaunchedEffect(uiState.territories) {
        territories.clear()
        territories.addAll(uiState.territories)

        val myUid = uiState.currentUserId
        val myTerritories = territories.filter { it.ownerId == myUid }
        val maxId = myTerritories.maxOfOrNull { it.id } ?: 0
        nextTerritoryId = maxId + 1
    }
    LaunchedEffect(isTracking) {
        while (isTracking) {
            kotlinx.coroutines.delay(1000)
            elapsedTimeSeconds++
        }
    }

    DisposableEffect(isTracking, permissionGranted) {
        if (permissionGranted && isTracking) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, context.mainLooper
            )
        } else {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }



    val userRepository = remember { UserRepository() }

    LaunchedEffect(capitalReinforcementPoints) {
        val uid = authManager.getCurrentUserId()

        if (uid != null) {
            userRepository.updateReinforcementPoints(
                uid = uid,
                reinforcementPoints = capitalReinforcementPoints
            ) { _, _ -> }
        }
    }
    fun syncTerritoryControl(ownerId: String, territoryId: Int, newControl: Int) {
        territoryRepository.updateTerritoryControl(
            ownerId = ownerId,
            territoryId = territoryId,
            control = newControl
        ) { _, _ -> }
    }


    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = permissionGranted
            )
        ) {
            Marker(
                state = MarkerState(position = userLocation), title = "Tu posición"
            )

            if (routePoints.isNotEmpty()) {

                Polyline(
                    points = routePoints.toList()
                )
            }
            territories.forEach { territory ->

                val myUid = uiState.currentUserId
                val isMine = territory.ownerId == myUid
                val ownerColor = ownerColors[territory.ownerId] ?: Color.Red

                val territoryFillColor = when {
                    territory.control == 0 -> Color.Gray.copy(alpha = 0.35f)
                    isMine -> playerProfile.territoryColor.copy(alpha = 0.35f)
                    else -> ownerColor.copy(alpha = 0.35f)
                }

                val territoryStrokeColor = when {
                    territory.control == 0 -> Color.DarkGray
                    isMine -> playerProfile.territoryColor
                    else -> ownerColor
                }


                val ownerName = when {
                    territory.control == 0 -> "Sin dueño"
                    isMine -> playerProfile.name
                    else -> ownerNames[territory.ownerId] ?: "Cargando..."
                }


                val strokeWidth = when (territory.type) {
                    SettlementType.CASTLE -> 8f
                    SettlementType.VILLAGE -> 4f
                }

                Polygon(
                    points = territory.points,
                    fillColor = territoryFillColor,
                    strokeColor = territoryStrokeColor,
                    strokeWidth = strokeWidth
                )


                val baseTitle = when (territory.type) {
                    SettlementType.CASTLE -> "⭐ ${territory.name}"
                    SettlementType.VILLAGE -> territory.name
                }

                val markerTitle = if (territory.control > 0) {
                    baseTitle
                } else {
                    "$baseTitle (Neutralizado)"
                }

                val territoryTypeLabel = when (territory.type) {
                    SettlementType.CASTLE -> "Capital"
                    SettlementType.VILLAGE -> "Poblado"
                }

                val markerSnippet = "$territoryTypeLabel · $ownerName · Control: ${territory.control}%"

                Marker(
                    state = MarkerState(position = territory.center),
                    title = markerTitle,
                    snippet = markerSnippet,
                    onClick = {
                        selectedTerritory = territory
                        true
                    }
                )
            }
        }

        SectionCard(
            title = "Actividad",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Text(text = "Tiempo: ${formatTime(elapsedTimeSeconds)}")
            Text(text = "Distancia: ${"%.2f".format(totalDistanceMeters / 1000)} km")
            Text(text = "Refuerzo capital: $capitalReinforcementPoints")
        }

        Button(
            onClick = {
                if (isTracking) {
                    isTracking = false

                    if (routePoints.size >= 10 && totalDistanceMeters >= 300f && elapsedTimeSeconds >= 60) {
                        val startPoint = routePoints.first()
                        val endPoint = routePoints.last()
                        val distanceToStart = distanceInMeters(startPoint, endPoint)

                        if (distanceToStart <= 80f) {
                            val territoryPoints = routePoints.toList()
                            val territoryCenter = calculateCenter(territoryPoints)

                            val settlementType = if (territories.isEmpty()) {
                                SettlementType.CASTLE
                            } else {
                                SettlementType.VILLAGE
                            }
                            val existingNames = territories.map { it.name }

                            val territoryName = if (settlementType == SettlementType.CASTLE) {
                                generateCapitalName()
                            } else {
                                generateVillageName(existingNames)
                            }

                            val uid = authManager.getCurrentUserId() ?: return@Button

                            val newTerritory = Territory(
                                id = nextTerritoryId,
                                ownerId = uid,
                                name = territoryName,
                                points = territoryPoints,
                                center = territoryCenter,
                                type = settlementType,
                                control = 20
                            )

                            territories.add(newTerritory)

                            if (uid != null) {
                                territoryRepository.saveTerritory(
                                    newTerritory.toDto()
                                ) { success, error ->
                                    if (!success) {
                                        println("Error guardando territorio: $error")
                                    } else {
                                        onRefreshData()
                                    }
                                }
                            }

                            nextTerritoryId++

                            val settlementName = when (settlementType) {
                                SettlementType.CASTLE -> "Capital"
                                SettlementType.VILLAGE -> "Poblado"
                            }

                            summaryTitle = "Actividad finalizada"
                            summaryMessage = """
                            Has completado un circuito válido.
                            
                            Territorio creado: Sí
                            Nombre: $territoryName
                            Tipo de asentamiento: $settlementName
                            Control inicial: 20%
                            Tiempo: ${formatTime(elapsedTimeSeconds)}
                            Distancia: ${"%.2f".format(totalDistanceMeters / 1000)} km
                            """.trimIndent()
                            showSummaryDialog = true
                        } else {

                            val patrolledTerritory = findPatrolledTerritory(routePoints, territories)

                            if (patrolledTerritory != null && totalDistanceMeters >= 200f && elapsedTimeSeconds >= 60) {
                                val updatedControl = minOf(patrolledTerritory.control + 5, 100)

                                territories.replaceAll { territory ->
                                    if (territory.id == patrolledTerritory.id) {
                                        territory.copy(control = updatedControl)
                                    } else {
                                        territory
                                    }
                                }

                                if (selectedTerritory?.id == patrolledTerritory.id) {
                                    selectedTerritory = patrolledTerritory.copy(control = updatedControl)
                                }
                                syncTerritoryControl(patrolledTerritory.ownerId, patrolledTerritory.id, updatedControl)
                                summaryTitle = "Actividad finalizada"
                                summaryMessage = """
                                No se ha cerrado el circuito, pero has patrullado un territorio existente.
                                
                                Territorio patrullado: ${patrolledTerritory.name}
                                Control actual: $updatedControl%
                                Tiempo: ${formatTime(elapsedTimeSeconds)}
                                Distancia: ${"%.2f".format(totalDistanceMeters / 1000)} km
                                """.trimIndent()
                                showSummaryDialog = true
                            } else {
                                val activityNearCapital = isActivityCoveringCapital(routePoints, capitalTerritory)
                                val reinforcementGained = if (activityNearCapital) {
                                    (totalDistanceMeters / 100).toInt()
                                } else {
                                    0
                                }

                                capitalReinforcementPoints += reinforcementGained

                                summaryTitle = "Actividad finalizada"
                                summaryMessage = """
                                No se ha cerrado el circuito.
                                
                                Distancia al punto inicial: ${distanceToStart.toInt()} m
                                Tiempo: ${formatTime(elapsedTimeSeconds)}
                                Distancia: ${"%.2f".format(totalDistanceMeters / 1000)} km
                                
                                ${if (activityNearCapital)
                                    "Refuerzo generado para la capital: +$reinforcementGained\nRefuerzo disponible: $capitalReinforcementPoints"
                                else
                                    "La actividad no se ha realizado cerca de la capital, por lo que no ha generado refuerzo."}
                                """.trimIndent()
                                showSummaryDialog = true
                            }
                        }
                    } else {

                        val patrolledTerritory = findPatrolledTerritory(routePoints, territories)

                        if (patrolledTerritory != null && totalDistanceMeters >= 200f && elapsedTimeSeconds >= 60) {
                            val updatedControl = minOf(patrolledTerritory.control + 5, 100)

                            territories.replaceAll { territory ->
                                if (territory.id == patrolledTerritory.id) {
                                    territory.copy(control = updatedControl)
                                } else {
                                    territory
                                }

                            }

                            if (selectedTerritory?.id == patrolledTerritory.id) {
                                selectedTerritory = patrolledTerritory.copy(control = updatedControl)
                            }
                            syncTerritoryControl(patrolledTerritory.ownerId, patrolledTerritory.id, updatedControl)

                            summaryTitle = "Actividad finalizada"
                            summaryMessage = """
                            Has patrullado un territorio existente.
                            
                            Territorio patrullado: ${patrolledTerritory.name}
                            Control actual: $updatedControl%
                            Tiempo: ${formatTime(elapsedTimeSeconds)}
                            Distancia: ${"%.2f".format(totalDistanceMeters / 1000)} km
                            """.trimIndent()
                            showSummaryDialog = true
                        } else {
                            val activityNearCapital = isActivityCoveringCapital(routePoints, capitalTerritory)
                            val reinforcementGained = if (activityNearCapital) {
                                (totalDistanceMeters / 100).toInt()
                            } else {
                                0
                            }

                            capitalReinforcementPoints += reinforcementGained

                            summaryTitle = "Actividad finalizada"
                            summaryMessage = """
                            La actividad no cumple los requisitos para crear territorio ni patrullar una zona.
                            
                            Tiempo: ${formatTime(elapsedTimeSeconds)}
                            Distancia: ${"%.2f".format(totalDistanceMeters / 1000)} km
                            
                            ${if (activityNearCapital)
                                "Refuerzo generado para la capital: +$reinforcementGained\nRefuerzo disponible: $capitalReinforcementPoints"
                            else
                                "La actividad no ha transcurrido lo suficiente dentro o alrededor de la capital, por lo que no ha generado refuerzo."}
                            """.trimIndent()
                            showSummaryDialog = true
                        }
                    }

                } else {
                    routePoints.clear()
                    elapsedTimeSeconds = 0
                    totalDistanceMeters = 0f
                    isTracking = true
                }
            }, modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .height(60.dp)
        ) {
            Text(if (isTracking) "Detener actividad" else "Iniciar actividad")
        }
        if (showSummaryDialog) {
            AlertDialog(
                onDismissRequest = { showSummaryDialog = false },
                confirmButton = {
                    TextButton(onClick = { showSummaryDialog = false }) {
                        Text("Aceptar")
                    }
                },
                title = {
                    Text(summaryTitle)
                },
                text = {
                    Text(summaryMessage)
                }
            )
        }
        if (selectedTerritory != null) {
            val territory = selectedTerritory!!
            val currentUserId = uiState.currentUserId
            val isMine = territory.ownerId == currentUserId
            val isNeutralized = territory.control == 0
            val isCapital = territory.type == SettlementType.CASTLE

            val canAttack = !isMine && !isNeutralized
            val canClaim = isNeutralized
            val canRemoteReinforce = isMine &&
                    !isCapital &&
                    territory.control in 1..99 &&
                    capitalReinforcementPoints >= 10

            SectionCard(
                title = territory.name,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 110.dp)
            ) {
                Text(
                    text = "Tipo: ${
                        when (territory.type) {
                            SettlementType.CASTLE -> "Capital del Reino"
                            SettlementType.VILLAGE -> "Poblado"
                        }
                    }"
                )
                Text(text = "Control: ${territory.control}%")

                Text(
                    text = when {
                        isMine -> "Propietario: ${playerProfile.name}"
                        isNeutralized -> "Propietario: Sin dueño"
                        else -> "Propietario: ${ownerNames[territory.ownerId] ?: "Cargando..."}"
                    }
                )

                Text(
                    text = when {
                        isMine && !isNeutralized -> "Este territorio pertenece a tu reino."
                        !isMine && !isNeutralized -> "Este territorio pertenece a otro jugador y puede ser atacado."
                        isNeutralized -> "Este territorio no tiene dueño y puede ser reclamado."
                        else -> ""
                    }
                )


                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Button(
                        onClick = {
                            if (canAttack) {
                                val attackDamage = when (territory.type) {
                                    SettlementType.CASTLE -> 5
                                    SettlementType.VILLAGE -> 10
                                }

                                val newControl = maxOf(territory.control - attackDamage, 0)

                                territories.replaceAll { current ->
                                    if (current.ownerId == territory.ownerId && current.id == territory.id) {
                                        current.copy(control = newControl)
                                    } else {
                                        current
                                    }
                                }

                                selectedTerritory = territory.copy(control = newControl)
                                syncTerritoryControl(territory.ownerId, territory.id, newControl)
                            }
                        },
                        enabled = canAttack
                    ) {
                        Text("Atacar")
                    }

                    Button(
                        onClick = {
                            if (canClaim && currentUserId.isNotBlank()) {
                                val previousOwnerId = territory.ownerId

                                val reclaimedTerritory = territory.copy(
                                    ownerId = currentUserId,
                                    control = 50
                                )

                                territories.replaceAll { current ->
                                    if (current.ownerId == territory.ownerId && current.id == territory.id) {
                                        reclaimedTerritory
                                    } else {
                                        current
                                    }
                                }

                                selectedTerritory = reclaimedTerritory

                                if (previousOwnerId.isNotBlank() && previousOwnerId != currentUserId) {
                                    territoryRepository.deleteTerritory(
                                        ownerId = previousOwnerId,
                                        territoryId = territory.id
                                    ) { _, _ ->
                                        territoryRepository.saveTerritory(
                                            reclaimedTerritory.toDto()
                                        ) { _, _ ->  onRefreshData()}
                                    }
                                } else {
                                    territoryRepository.saveTerritory(
                                        reclaimedTerritory.toDto()
                                    ) { _, _ ->  onRefreshData()}
                                }
                            }
                        },
                        enabled = canClaim,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text("Reclamar")
                    }

                    Button(
                        onClick = {
                            if (canRemoteReinforce) {
                                val newControl = minOf(territory.control + 5, 100)

                                territories.replaceAll { current ->
                                    if (current.ownerId == territory.ownerId && current.id == territory.id) {
                                        current.copy(control = newControl)
                                    } else {
                                        current
                                    }
                                }

                                capitalReinforcementPoints -= 10
                                selectedTerritory = territory.copy(control = newControl)
                                syncTerritoryControl(territory.ownerId, territory.id, newControl)
                            }
                        },
                        enabled = canRemoteReinforce,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text("Reforzar")
                    }

                    Button(
                        onClick = { selectedTerritory = null },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
        Button(
            onClick = {
                territories.replaceAll { territory ->
                    if (territory.control > 0) {
                        val decayAmount = when (territory.type) {
                            SettlementType.CASTLE -> 2
                            SettlementType.VILLAGE -> 5
                        }

                        val newControl = maxOf(territory.control - decayAmount, 0)
                        syncTerritoryControl(territory.ownerId, territory.id, newControl)
                        territory.copy(control = newControl)
                    } else {
                        territory
                    }
                }

                if (selectedTerritory != null) {
                    val updated = territories.find { it.id == selectedTerritory!!.id }
                    selectedTerritory = updated
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text("Pasar tiempo")
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .background(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Territorios del reino")

            if (territories.isEmpty()) {
                Text(text = "Aún no has conquistado territorios.")
            } else {
                territories.sortedBy { it.control }.forEach { territory ->
                    val territoryType = when (territory.type) {
                        SettlementType.CASTLE -> "Capital"
                        SettlementType.VILLAGE -> "Poblado"
                    }

                    val territoryStatus = when {
                        territory.control == 0 -> "Neutralizado"
                        territory.control in 1..20 -> "Muy débil"
                        territory.control in 21..79 -> "En consolidación"
                        territory.control in 80..100 -> "Estable"
                        else -> "Desconocido"
                    }

                    Column(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                selectedTerritory = territory
                                cameraPositionState.position =
                                    CameraPosition.fromLatLngZoom(territory.center, 17f)
                            }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (territory.type == SettlementType.CASTLE) {
                                "⭐ ${territory.name}"
                            } else {
                                territory.name
                            }
                        )
                        Text(text = "Tipo: $territoryType")
                        Text(text = "Control: ${territory.control}%")
                        Text(text = "Estado: $territoryStatus")
                    }
                }
            }
        }
        Button(
            onClick = {
                capitalTerritory?.let {
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(it.center, 17f)
                    selectedTerritory = it
                }
            },
            enabled = capitalTerritory != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        ) {
            Text("Ir a la capital")
        }


    }


}

