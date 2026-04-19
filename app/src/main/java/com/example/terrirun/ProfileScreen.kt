package com.example.terrirun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val userName = uiState.playerProfile.name
    val userColor = uiState.playerProfile.territoryColor
    val reinforcementPoints = uiState.reinforcementPoints

    val myTerritories = uiState.territories.filter {
        it.ownerId == uiState.currentUserId
    }

    val capital = myTerritories.find { it.type == SettlementType.CASTLE }
    val villageCount = myTerritories.count { it.type == SettlementType.VILLAGE }
    val totalControl = myTerritories.sumOf { it.control }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionCard(
            title = "Perfil",
            modifier = Modifier
        ) {
            Text(text = "Nombre: $userName")
            Text(text = "Refuerzo disponible: $reinforcementPoints")
            Text(text = "Color del reino", color = userColor)
            Text(text = "Territorios: ${myTerritories.size}")
            Text(text = "Poblados: $villageCount")
            Text(text = "Capital: ${capital?.name ?: "No creada"}")
            Text(text = "Control total: $totalControl%")
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Cerrar sesión")
        }
    }
}