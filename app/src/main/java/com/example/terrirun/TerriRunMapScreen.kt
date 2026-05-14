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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Whatshot

@SuppressLint("MissingPermission", "UnrememberedMutableState")
@Composable
fun TerriRunMapScreen(
    permissionGranted: Boolean,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    uiState: GameUiState,
    onRefreshData: () -> Unit,
    language: String
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
    var showAttackAlert by remember { mutableStateOf(false) }
    var previousTerritories by remember { mutableStateOf<List<Territory>>(emptyList()) }
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
    var calories by remember { mutableStateOf(0f) }

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

                            calories = (totalDistanceMeters / 1000f) * 70f
                        }
                        routePoints.add(newPoint)
                    }

                    cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation, 17f)
                }
            }
        }
    }
    val myTerritories = territories
        .filter { it.ownerId == uiState.currentUserId }
        .sortedBy { it.control }
    val capitalTerritory = territories.firstOrNull {
        it.ownerId == uiState.currentUserId && it.type == SettlementType.CASTLE
    }

    val playerProfile = uiState.playerProfile

    var isActivityExpanded by remember { mutableStateOf(false) }
    var isTerritoriesExpanded by remember { mutableStateOf(false) }

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

// detectar si han atacado tus territorios
        if (previousTerritories.isNotEmpty()) {
            territories.forEach { newTerritory ->

                val old = previousTerritories.find {
                    it.id == newTerritory.id && it.ownerId == newTerritory.ownerId
                }

                if (old != null &&
                    newTerritory.ownerId == myUid &&
                    newTerritory.control < old.control
                ) {
                    showAttackAlert = true
                }
            }
        }

// guardar estado nuevo
        previousTerritories = territories.map { it.copy() }
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
            title = appText("activity", language),
            icon = Icons.Default.Terrain,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 16.dp)
                .width(180.dp)
                .clickable {
                    isActivityExpanded = !isActivityExpanded
                }
        ) {
            if (isActivityExpanded) {
                ActivityStatRow(
                    icon = Icons.Default.Schedule,
                    label = appText("time", language),
                    value = formatTime(elapsedTimeSeconds)
                )
                ActivityStatRow(
                    icon = Icons.Default.LocationOn,
                    label = appText("distance", language),
                    value = "${"%.2f".format(totalDistanceMeters / 1000)} km"
                )
                ActivityStatRow(
                    icon = Icons.Default.Shield,
                    label = appText("capital_reinforcement", language),
                    value = capitalReinforcementPoints.toString()
                )
                ActivityStatRow(
                    icon = Icons.Default.Whatshot,
                    label = "Calorías",
                    value = "${calories.toInt()} kcal"
                )
            } else {
                Text(text = appText("tap_details", language))
            }
        }

        PrimaryFloatingButton(
            text = if (isTracking)
                appText("stop_activity", language)
            else
                appText("start_activity", language),
            icon = Icons.Default.PlayArrow,
            onClick = {
                if (isTracking) {
                    isTracking = false

                    val startPoint = routePoints.firstOrNull()
                    val endPoint = routePoints.lastOrNull()

                    val distanceToStart = if (startPoint != null && endPoint != null) {
                        distanceInMeters(startPoint, endPoint)
                    } else 9999f

                    val patrolledTerritory = findPatrolledTerritory(routePoints, territories)
                    val attackedTerritory = findAttackedEnemyTerritory(
                        routePoints = routePoints,
                        territories = territories,
                        currentUserId = uiState.currentUserId
                    )

                    val meetsActivityRequirements =
                        totalDistanceMeters >= 200f && elapsedTimeSeconds >= 60

                    val canCreateTerritory =
                        routePoints.size >= 10 &&
                                totalDistanceMeters >= 300f &&
                                elapsedTimeSeconds >= 60 &&
                                distanceToStart <= 80f

                    if (canCreateTerritory) {
                        val territoryPoints = routePoints.toList()
                        val territoryCenter = calculateCenter(territoryPoints)

                        val settlementType = if (territories.none { it.ownerId == uiState.currentUserId }) {
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

                        val uid = authManager.getCurrentUserId() ?: return@PrimaryFloatingButton

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

                        territoryRepository.saveTerritory(
                            newTerritory.toDto()
                        ) { success, error ->
                            if (!success) {
                                println("Error guardando territorio: $error")
                            } else {
                                onRefreshData()
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

                    } else if (patrolledTerritory != null &&
                        patrolledTerritory.ownerId == uiState.currentUserId &&
                        meetsActivityRequirements
                    ) {
                        val updatedControl = minOf(patrolledTerritory.control + 5, 100)

                        territories.replaceAll {
                            if (it.id == patrolledTerritory.id && it.ownerId == patrolledTerritory.ownerId) {
                                it.copy(control = updatedControl)
                            } else it
                        }

                        syncTerritoryControl(
                            patrolledTerritory.ownerId,
                            patrolledTerritory.id,
                            updatedControl
                        )

                        summaryTitle = "Actividad finalizada"
                        summaryMessage = """
                    Has patrullado un territorio existente.
                    
                    Territorio: ${patrolledTerritory.name}
                    Control actual: $updatedControl%
                """.trimIndent()

                    } else if (attackedTerritory != null && meetsActivityRequirements) {
                        val attackDamage = calculateAttackDamage(
                            elapsedTimeSeconds,
                            totalDistanceMeters,
                            attackedTerritory.type
                        )

                        val newControl = maxOf(attackedTerritory.control - attackDamage, 0)
                        val notification = GameNotification(
                            userId = attackedTerritory.ownerId,
                            title = "⚔️ Ataque recibido",
                            message = "Han atacado tu territorio ${attackedTerritory.name}"
                        )

                        userRepository.createNotification(notification) { _, _ -> }
                        val notificationRepository = NotificationRepository()

                        notificationRepository.sendNotification(
                            userId = attackedTerritory.ownerId,
                            title = "⚔️ Tu territorio está siendo atacado",
                            message = "Han atacado ${attackedTerritory.name} y ha perdido $attackDamage% de control."
                        )

                        territories.replaceAll {
                            if (it.id == attackedTerritory.id && it.ownerId == attackedTerritory.ownerId) {
                                it.copy(control = newControl)
                            } else it
                        }

                        syncTerritoryControl(
                            attackedTerritory.ownerId,
                            attackedTerritory.id,
                            newControl
                        )

                        summaryTitle = "Actividad finalizada"
                        summaryMessage = """
                    Has atacado un territorio enemigo.
                    
                    Territorio: ${attackedTerritory.name}
                    Daño: -$attackDamage
                    Control restante: $newControl%
                """.trimIndent()

                    } else {
                        val activityNearCapital =
                            isActivityCoveringCapital(routePoints, capitalTerritory)

                        val reinforcementGained = if (activityNearCapital) {
                            (totalDistanceMeters / 100).toInt()
                        } else 0

                        capitalReinforcementPoints += reinforcementGained

                        summaryTitle = "Actividad finalizada"
                        summaryMessage = """
                    Actividad completada.
                    
                    Tiempo: ${formatTime(elapsedTimeSeconds)}
                    Distancia: ${"%.2f".format(totalDistanceMeters / 1000)} km
                    
                    ${
                            if (activityNearCapital)
                                "Refuerzo generado: +$reinforcementGained"
                            else
                                "No se generó refuerzo"
                        }
                """.trimIndent()
                    }

                    showSummaryDialog = true

                } else {
                    routePoints.clear()
                    elapsedTimeSeconds = 0
                    totalDistanceMeters = 0f
                    calories = 0f
                    isTracking = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .height(62.dp)
        )
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

        SectionCard(
            title = appText("my_territories", language),
            icon = Icons.Default.Castle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 10.dp, top = 16.dp)
                .width(190.dp)
                .clickable {
                    isTerritoriesExpanded = !isTerritoriesExpanded
                }
        ) {
            Text(
                text = if (isTerritoriesExpanded) {
                    appText("hide_territories", language)
                } else {
                    appText("show_territories", language)
                }
            )

            if (isTerritoriesExpanded) {
                if (myTerritories.isEmpty()) {
                    Text(
                        appText("no_territories", language),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .heightIn(max = 220.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        myTerritories.forEach { territory ->
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

                            TerritoryMiniCard(
                                title = if (territory.type == SettlementType.CASTLE) {
                                    "⭐ ${territory.name}"
                                } else {
                                    territory.name
                                },
                                subtitle = territoryType,
                                control = territory.control,
                                status = territoryStatus,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clickable {
                                        selectedTerritory = territory
                                        cameraPositionState.position =
                                            CameraPosition.fromLatLngZoom(territory.center, 17f)
                                    }
                            )
                        }
                    }
                }
            }
        }
        SecondaryFloatingButton(
            text = appText("go_capital", language),
            icon = Icons.Default.Castle,
            enabled = capitalTerritory != null,
            onClick = {
                capitalTerritory?.let {
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(it.center, 17f)
                    selectedTerritory = it
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 95.dp)
        )
        if (showAttackAlert) {
            AlertDialog(
                onDismissRequest = { showAttackAlert = false },
                confirmButton = {
                    TextButton(onClick = { showAttackAlert = false }) {
                        Text("OK")
                    }
                },
                title = { Text("⚔️ Ataque recibido") },
                text = { Text("¡Han atacado uno de tus territorios!") }
            )
        }


    }


}

