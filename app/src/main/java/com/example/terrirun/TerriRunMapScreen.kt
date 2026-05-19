package com.example.terrirun

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission", "UnrememberedMutableState")
@Composable
fun TerriRunMapScreen(
    permissionGranted: Boolean,
    modifier: Modifier = Modifier,
    uiState: GameUiState,
    language: String,
    selectedTerritoryFromRanking: Territory?,
    onRefreshData: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0,0.0), 10f)
    }

    var selectedTerritory by remember { mutableStateOf<Territory?>(null) }
    var showClaimDialog by remember { mutableStateOf(false) }
    LaunchedEffect(selectedTerritoryFromRanking) {
        selectedTerritoryFromRanking?.let { territoryFromRanking ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(territoryFromRanking.center, 17f)
            )

            selectedTerritory = territoryFromRanking
        }
    }


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
    var lastAttackData by remember {
        mutableStateOf<Pair<Int, List<LatLng>>?>(null)
    }
    val territories = remember { mutableStateListOf<Territory>() }
    var capitalReinforcementPoints by remember {
        mutableStateOf(uiState.reinforcementPoints)
    }
    val ownerNames = uiState.ownerNames
    val ownerColors = uiState.ownerColors
    var nextTerritoryId by remember { mutableStateOf(1) }

    val authManager = remember { AuthManager() }
    var calories by remember { mutableStateOf(0f) }


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
        .filter {
            it.ownerId == uiState.currentUserId &&
                    it.control > 0
        }
        .sortedBy { it.control }
    val capitalTerritory = territories.firstOrNull {
        it.ownerId == uiState.currentUserId &&
                it.type == SettlementType.CASTLE &&
                it.control > 0
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
                val isNeutralizedTerritory = territory.control == 0

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
                    territory.ownerId.isBlank() || territory.control == 0 -> "Sin dueño"
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

                val markerTitle = when {
                    territory.control == 0 -> "⚪ $baseTitle (Reclamable)"
                    territory.control in 1..20 -> "🔴 $baseTitle"
                    territory.control in 21..60 -> "🟡 $baseTitle"
                    else -> "🟢 $baseTitle"
                }

                val territoryTypeLabel = when (territory.type) {
                    SettlementType.CASTLE -> "Capital"
                    SettlementType.VILLAGE -> "Poblado"
                }

                val markerSnippet =
                    "$territoryTypeLabel · $ownerName · Estado: ${territory.control}%"

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

                    val enemyAttackResult = findEnemyTerritoryAffectedByRoute(
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

                    if (enemyAttackResult != null && meetsActivityRequirements) {
                        val attackedTerritory = enemyAttackResult.territory
                        val attackDamage = calculateRouteAttackDamage(
                            elapsedTimeSeconds = elapsedTimeSeconds,
                            totalDistanceMeters = totalDistanceMeters,
                            attackType = enemyAttackResult.attackType,
                            territoryType = enemyAttackResult.territory.type
                        )
                        val newControl = maxOf(attackedTerritory.control - attackDamage, 0)
                        lastAttackData =
                            attackedTerritory.id to routePoints.toList()

                        territories.replaceAll {
                            if (it.id == attackedTerritory.id &&
                                it.ownerId == attackedTerritory.ownerId
                            ) {

                                if (newControl == 0) {
                                    it.copy(
                                        control = 0,
                                        ownerId = "",
                                        type = SettlementType.VILLAGE
                                    )
                                } else {
                                    it.copy(control = newControl)
                                }

                            } else {
                                it
                            }
                        }

                        if (newControl == 0) {
                            territoryRepository.deleteTerritory(
                                ownerId = attackedTerritory.ownerId,
                                territoryId = attackedTerritory.id
                            ) { _, _ ->

                                territoryRepository.saveTerritory(
                                    attackedTerritory.copy(
                                        ownerId = "",
                                        name = "Territorio neutralizado",
                                        type = SettlementType.VILLAGE,
                                        control = 0
                                    ).toDto()
                                ) { _, _ ->
                                    onRefreshData()
                                }
                            }
                        } else {
                            syncTerritoryControl(
                                attackedTerritory.ownerId,
                                attackedTerritory.id,
                                newControl
                            )
                        }
                        selectedTerritory =
                            if (newControl == 0) {
                                attackedTerritory.copy(
                                    control = 0,
                                    ownerId = "",
                                    name = "Territorio neutralizado",
                                    type = SettlementType.VILLAGE
                                )
                            } else {
                                attackedTerritory.copy(control = newControl)
                            }

                        val notification = GameNotification(
                            userId = attackedTerritory.ownerId,
                            title = "⚔️ Ataque recibido",
                            message = "Han atacado tu territorio ${attackedTerritory.name}. Control restante: $newControl%."
                        )

                        userRepository.createNotification(notification) { _, _ -> }

                        NotificationRepository().sendNotification(
                            userId = attackedTerritory.ownerId,
                            title = "⚔️ Tu territorio está siendo atacado",
                            message = "Han atacado ${attackedTerritory.name} y ahora queda al $newControl% de control."
                        )

                        val attackText = when (enemyAttackResult.attackType) {
                            AttackType.FULL_SURROUND ->
                                "Has rodeado completamente un territorio enemigo. El daño aplicado es mayor."

                            AttackType.PARTIAL ->
                                "Has realizado una incursión parcial sobre un territorio enemigo."
                        }

                        summaryTitle = "Ataque finalizado"
                        summaryMessage = """
                            $attackText
                            
                            Territorio atacado: ${attackedTerritory.name}
                            Daño causado: -$attackDamage%
                            Tipo de ataque: ${
                                                    if (enemyAttackResult.attackType == AttackType.FULL_SURROUND)
                                                        "Rodeo completo"
                                                    else
                                                        "Ataque parcial"
                                                }
                            Control restante: $newControl%
                            
                            ${
                                                    if (newControl == 0)
                                                        "El territorio ha quedado neutralizado. Ahora puede ser reclamado."
                                                    else
                                                        "Necesitarás más ataques para neutralizarlo."
                                                }
                        """.trimIndent()

                        showSummaryDialog = true

                    } else if (patrolledTerritory != null &&
                        patrolledTerritory.ownerId == uiState.currentUserId &&
                        meetsActivityRequirements &&
                        !routeExpandsOwnTerritory(routePoints, patrolledTerritory)
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
                        summaryTitle = "Patrulla completada"
                        summaryMessage = """
                            Has reforzado un territorio de tu reino.
                            
                            Territorio: ${patrolledTerritory.name}
                            Control actual: $updatedControl%
                            
                            Patrullar territorios propios aumenta su control y los hace más resistentes.
                        """.trimIndent()

                    }else if (canCreateTerritory && enemyAttackResult == null) {
                        // AQUÍ DEJAS TU BLOQUE ACTUAL DE CREAR / FUSIONAR TERRITORIO
                        val territoryPoints = routePoints.toList()

                        if (territoryPoints.size < 3) {
                            summaryTitle = "Actividad no válida"
                            summaryMessage = "No se ha podido crear un territorio porque la ruta no tiene suficientes puntos."
                            showSummaryDialog = true
                            return@PrimaryFloatingButton
                        }

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

// Fusiona si hay territorio existente
                        val mergeResult = mergeOrCreateTerritory(newTerritory, territories, uid)
                        val finalTerritory = mergeResult.territory
                        val wasMerged = mergeResult.wasMerged
                        selectedTerritory = finalTerritory

                        territoryRepository.saveTerritory(
                            finalTerritory.toDto()
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

                        summaryTitle = if (wasMerged) "Territorio ampliado y reforzado" else "Territorio creado"

                        summaryMessage = if (wasMerged) {
                            """
                                Has ampliado un territorio existente.
                                
                                Territorio: ${finalTerritory.name}
                                Control actual: ${finalTerritory.control}%
                                Tiempo: ${formatTime(elapsedTimeSeconds)}
                                Distancia: ${"%.2f".format(totalDistanceMeters / 1000)} km
                            """.trimIndent()
                                                } else {
                                                    """
                                Has completado un circuito válido.
                                
                                Territorio creado: Sí
                                Nombre: ${finalTerritory.name}
                                Tipo de asentamiento: $settlementName
                                Control inicial: ${finalTerritory.control}%
                                Tiempo: ${formatTime(elapsedTimeSeconds)}
                                Distancia: ${"%.2f".format(totalDistanceMeters / 1000)} km
                            """.trimIndent()
                                                }

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
                    routePoints.clear()
                    elapsedTimeSeconds = 0
                    totalDistanceMeters = 0f
                    calories = 0f
                    showSummaryDialog = true

                } else {
                    routePoints.clear()
                    elapsedTimeSeconds = 0
                    totalDistanceMeters = 0f
                    calories = 0f
                    lastAttackData = null
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
                    .padding(start = 12.dp, end = 12.dp, bottom = 105.dp)
                    .heightIn(max = 280.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
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
                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { territory.control / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = when {
                            territory.control == 0 -> Color.Gray
                            territory.control in 1..20 -> Color.Red
                            territory.control in 21..60 -> Color(0xFFFFC107)
                            else -> Color(0xFF2E7D32)
                        },
                        trackColor = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Estado: ${territoryStatusText(territory.control)}"
                    )
                Text(
                    text = when {
                        isMine -> "Propietario: ${playerProfile.name}"
                        isNeutralized || territory.ownerId.isBlank() -> "Propietario: Sin dueño"
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


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                    if (canClaim) {
                        Button(
                            onClick = {
                                if (currentUserId.isNotBlank()) {
                                    val previousOwnerId = territory.ownerId
                                    val wasNeutralTerritory = previousOwnerId.isBlank()

                                    val attackRoute = lastAttackData?.second ?: territory.points

                                    val splitResult =
                                        if (wasNeutralTerritory) {
                                            SplitTerritoryResult(
                                                conqueredPart = territory.copy(
                                                    ownerId = currentUserId,
                                                    name = "Territorio reclamado",
                                                    type = SettlementType.VILLAGE,
                                                    control = 20
                                                ),
                                                remainingPart = null
                                            )
                                        } else {
                                            splitTerritoryAfterClaim(
                                                original = territory,
                                                conquerRoute = attackRoute,
                                                newOwnerId = currentUserId
                                            )
                                        }

                                    val conqueredTerritory = splitResult.conqueredPart
                                    val remainingTerritory = splitResult.remainingPart

                                    territories.removeAll {
                                        it.ownerId == territory.ownerId && it.id == territory.id
                                    }

                                    if (remainingTerritory != null && previousOwnerId.isNotBlank()) {
                                        territories.add(remainingTerritory)
                                    }

                                    territories.add(conqueredTerritory)

                                    selectedTerritory = conqueredTerritory
                                    lastAttackData = null
                                    routePoints.clear()
                                    elapsedTimeSeconds = 0
                                    totalDistanceMeters = 0f
                                    calories = 0f

                                    if (previousOwnerId.isNotBlank()) {
                                        val conquestNotification = GameNotification(
                                            userId = previousOwnerId,
                                            title = "🏳️ Territorio conquistado",
                                            message = "Han reclamado una zona de tu territorio ${territory.name}."
                                        )

                                        userRepository.createNotification(conquestNotification) { _, _ -> }

                                        NotificationRepository().sendNotification(
                                            userId = previousOwnerId,
                                            title = "🏳️ Has perdido territorio",
                                            message = "Un rival ha reclamado una zona de ${territory.name}."
                                        )
                                    }

                                    summaryTitle = "Territorio reclamado"
                                    summaryMessage = if (remainingTerritory == null) {
                                        """
                                            Has conquistado completamente el territorio.
                                            
                                            Nuevo territorio: ${conqueredTerritory.name}
                                            Control inicial: ${conqueredTerritory.control}%
                                        """.trimIndent()
                                    } else {
                                        """
                                            Has reclamado una zona conquistada.
                                            
                                            Nuevo territorio: ${conqueredTerritory.name}
                                            Control inicial: ${conqueredTerritory.control}%
                                            
                                            La parte restante del territorio anterior queda debilitada al 20%.
                                        """.trimIndent()
                                    }

                                    showSummaryDialog = true

                                    if (!wasNeutralTerritory) {
                                        territoryRepository.deleteTerritory(
                                            ownerId = previousOwnerId,
                                            territoryId = territory.id
                                        ) { _, _ ->

                                        if (remainingTerritory != null && previousOwnerId.isNotBlank()) {
                                            territoryRepository.saveTerritory(
                                                remainingTerritory.toDto()
                                            ) { _, _ ->

                                                territoryRepository.saveTerritory(
                                                    conqueredTerritory.toDto()
                                                ) { _, _ ->
                                                    onRefreshData()
                                                }
                                            }
                                        } else {
                                            territoryRepository.saveTerritory(
                                                conqueredTerritory.toDto()
                                            ) { _, _ ->
                                                onRefreshData()
                                            }
                                        }
                                    }} else {
                                        territoryRepository.saveTerritory(
                                            conqueredTerritory.toDto()
                                        ) { _, _ ->
                                            onRefreshData()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp
                            )
                        ) {
                            Text("Reclamar")
                        }
                    }
                    if (isNeutralized && lastAttackData?.first != territory.id) {
                        Text(
                            text = "Este territorio está neutralizado, pero necesitas atacarlo antes para reclamarlo.",
                            modifier = Modifier.padding(top = 8.dp)
                        )
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp
                        )
                    ) {
                        Text("Reforzar")
                    }

                        Button(
                            onClick = { selectedTerritory = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp
                            )
                        ) {
                            Text("Cerrar")
                        }
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

                            val territoryStatus = territoryStatusText(territory.control)

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

                                        kotlinx.coroutines.CoroutineScope(
                                            kotlinx.coroutines.Dispatchers.Main
                                        ).launch {

                                            cameraPositionState.animate(
                                                update = CameraUpdateFactory.newLatLngZoom(
                                                    territory.center,
                                                    17f
                                                ),
                                                durationMs = 1200
                                            )
                                        }
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

                    selectedTerritory = it

                    kotlinx.coroutines.CoroutineScope(
                        kotlinx.coroutines.Dispatchers.Main
                    ).launch {

                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(
                                it.center,
                                17f
                            ),
                            durationMs = 1200
                        )
                    }
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
const val MIN_ROUTE_POINTS_FOR_ATTACK = 5
const val MIN_ROUTE_POINTS_INSIDE_TERRITORY = 5
const val CLOSED_ROUTE_MAX_DISTANCE_METERS = 80f
const val MIN_ROUTE_POINTS_FOR_TERRITORY = 3
const val MIN_REMAINING_POINTS_AFTER_CLAIM = 5
data class MergeTerritoryResult(
    val territory: Territory,
    val wasMerged: Boolean
)
fun mergeOrCreateTerritory(
    newTerritory: Territory,
    existingTerritories: MutableList<Territory>,
    currentUserId: String
): MergeTerritoryResult {
    val overlap = existingTerritories.find {
        it.ownerId == currentUserId &&
                it.control > 0 &&
                polygonsOverlap(it.points, newTerritory.points)
    }
    return if (overlap != null) {
        // Fusionar los puntos para que abarque el rango más grande
        val mergedPoints = mergePolygons(overlap.points, newTerritory.points)
        val mergedCenter = calculateCenter(mergedPoints)
        val mergedControl = minOf(overlap.control + 20, 100) // sube control

        val mergedTerritory = overlap.copy(
            points = mergedPoints,
            center = mergedCenter,
            control = mergedControl
        )

        // Reemplaza el territorio antiguo por el fusionado
        existingTerritories.replaceAll {
            if (it.id == overlap.id && it.ownerId == currentUserId) mergedTerritory else it
        }

        MergeTerritoryResult(
            territory = mergedTerritory,
            wasMerged = true
        )
    } else {
        // No hay superposición, se crea uno nuevo
        existingTerritories.add(newTerritory)
        MergeTerritoryResult(
            territory = newTerritory,
            wasMerged = false
        )
    }
}
// Verifica si dos polígonos se superponen (aproximación simple)
fun polygonsOverlap(p1: List<LatLng>, p2: List<LatLng>): Boolean {
    val p1Bounds = getBounds(p1)
    val p2Bounds = getBounds(p2)
    return p1Bounds.intersect(p2Bounds)
}

// Calcula bounds de un polígono
fun getBounds(points: List<LatLng>): android.graphics.RectF {
    val lats = points.map { it.latitude }
    val lngs = points.map { it.longitude }
    return android.graphics.RectF(
        lngs.minOrNull()?.toFloat() ?: 0f,
        lats.minOrNull()?.toFloat() ?: 0f,
        lngs.maxOrNull()?.toFloat() ?: 0f,
        lats.maxOrNull()?.toFloat() ?: 0f
    )
}
fun countRoutePointsInsideBounds(
    routePoints: List<LatLng>,
    territoryPoints: List<LatLng>
): Int {
    val bounds = getBounds(territoryPoints)

    return routePoints.count { point ->
        point.longitude.toFloat() >= bounds.left &&
                point.longitude.toFloat() <= bounds.right &&
                point.latitude.toFloat() >= bounds.top &&
                point.latitude.toFloat() <= bounds.bottom
    }
}
fun isClosedRoute(routePoints: List<LatLng>): Boolean {
    val start = routePoints.firstOrNull()
    val end = routePoints.lastOrNull()

    if (start == null || end == null) return false

    return distanceInMeters(start, end) <= CLOSED_ROUTE_MAX_DISTANCE_METERS
}
fun calculateRouteAttackDamage(
    elapsedTimeSeconds: Int,
    totalDistanceMeters: Float,
    attackType: AttackType,
    territoryType: SettlementType
): Int {
    val baseDamage = when (attackType) {
        AttackType.FULL_SURROUND -> 25
        AttackType.PARTIAL -> 10
    }

    val distanceBonus = (totalDistanceMeters / 500f).toInt() * 5
    val timeBonus = (elapsedTimeSeconds / 300) * 5

    val territoryPenalty = when (territoryType) {
        SettlementType.CASTLE -> -5
        SettlementType.VILLAGE -> 0
    }

    val rawDamage = baseDamage + distanceBonus + timeBonus + territoryPenalty

    return when (territoryType) {
        SettlementType.CASTLE -> rawDamage.coerceIn(5, 25)
        SettlementType.VILLAGE -> rawDamage.coerceIn(5, 45)
    }
}
// Fusiona dos polígonos (simplemente concatena y elimina duplicados)
fun mergePolygons(p1: List<LatLng>, p2: List<LatLng>): List<LatLng> {
    val points = (p1 + p2)
        .distinctBy { it.latitude to it.longitude }

    val center = calculateCenter(points)

    return points.sortedBy { point ->
        kotlin.math.atan2(
            point.latitude - center.latitude,
            point.longitude - center.longitude
        )
    }
}

enum class AttackType {
    PARTIAL,
    FULL_SURROUND
}

data class TerritoryAttackResult(
    val territory: Territory,
    val attackType: AttackType
)

fun findEnemyTerritoryAffectedByRoute(
    routePoints: List<LatLng>,
    territories: List<Territory>,
    currentUserId: String
): TerritoryAttackResult? {
    if (routePoints.size < MIN_ROUTE_POINTS_FOR_ATTACK) return null

    val enemyTerritories = territories.filter {
        it.ownerId.isNotBlank() &&
                it.ownerId != currentUserId &&
                it.control > 0 &&
                it.points.size >= 3
    }

    return enemyTerritories.mapNotNull { territory ->
        val routeBounds = getBounds(routePoints)
        val territoryBounds = getBounds(territory.points)

        val routeContainsTerritory =
            routeBounds.left <= territoryBounds.left &&
                    routeBounds.top <= territoryBounds.top &&
                    routeBounds.right >= territoryBounds.right &&
                    routeBounds.bottom >= territoryBounds.bottom

        val territoryPointsInsideRouteBounds =
            territory.points.count { point ->
                point.longitude.toFloat() >= routeBounds.left &&
                        point.longitude.toFloat() <= routeBounds.right &&
                        point.latitude.toFloat() >= routeBounds.top &&
                        point.latitude.toFloat() <= routeBounds.bottom
            }

        val enoughTerritoryInside =
            territoryPointsInsideRouteBounds >= territory.points.size * 0.8

        val isClosed = isClosedRoute(routePoints)
        val overlaps = polygonsOverlap(routePoints, territory.points)
        val pointsInside = countRoutePointsInsideBounds(routePoints, territory.points)
        val enoughRouteInside = pointsInside >= MIN_ROUTE_POINTS_INSIDE_TERRITORY
        when {
            routeContainsTerritory && enoughTerritoryInside && isClosed -> {
                TerritoryAttackResult(
                    territory = territory,
                    attackType = AttackType.FULL_SURROUND
                )
            }

            overlaps && enoughRouteInside -> {
                TerritoryAttackResult(
                    territory = territory,
                    attackType = AttackType.PARTIAL
                )
            }

            else -> null
        }
    }.maxByOrNull { result ->
        countRoutePointsInsideBounds(
            routePoints = routePoints,
            territoryPoints = result.territory.points
        )
    }
}
fun territoryStatusText(control: Int): String {
    return when {
        control == 0 -> "Reclamable"
        control in 1..20 -> "Muy débil"
        control in 21..60 -> "En disputa"
        else -> "Fuerte"
    }
}
fun routeExpandsOwnTerritory(
    routePoints: List<LatLng>,
    ownTerritory: Territory
): Boolean {
    val routeBounds = getBounds(routePoints)
    val territoryBounds = getBounds(ownTerritory.points)

    val routeGoesOutsideTerritory =
        routeBounds.left < territoryBounds.left ||
                routeBounds.top < territoryBounds.top ||
                routeBounds.right > territoryBounds.right ||
                routeBounds.bottom > territoryBounds.bottom

    return routeGoesOutsideTerritory
}