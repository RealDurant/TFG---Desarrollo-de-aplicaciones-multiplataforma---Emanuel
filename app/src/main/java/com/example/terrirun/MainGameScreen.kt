package com.example.terrirun

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameScreen(
    permissionGranted: Boolean,
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(AppScreen.MAP) }

    val gameViewModel: GameViewModel = viewModel()
    val uiState = gameViewModel.uiState

    LaunchedEffect(Unit) {
        gameViewModel.loadInitialData()
    }

    val title = when (currentScreen) {
        AppScreen.MAP -> "TerriRun"
        AppScreen.RANKING -> "Ranking"
        AppScreen.PROFILE -> "Perfil"
        AppScreen.SETTINGS -> "Ajustes"

    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    IconButton(onClick = { currentScreen = AppScreen.SETTINGS }) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.MAP,
                    onClick = { currentScreen = AppScreen.MAP },
                    label = { Text("Mapa") },
                    icon = {}
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.RANKING,
                    onClick = { currentScreen = AppScreen.RANKING },
                    label = { Text("Ranking") },
                    icon = {}
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.PROFILE,
                    onClick = { currentScreen = AppScreen.PROFILE },
                    label = { Text("Perfil") },
                    icon = {}
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.MAP -> {
                TerriRunMapScreen(
                    permissionGranted = permissionGranted,
                    onLogout = {
                        gameViewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.padding(innerPadding),
                    uiState = uiState,
                    onRefreshData = { gameViewModel.refreshTerritories() }
                )
            }
            AppScreen.RANKING -> {
                RankingScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppScreen.PROFILE -> {
                ProfileScreen(
                    onLogout = {
                        gameViewModel.logout()
                        onLogout()
                    },
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppScreen.SETTINGS -> {
                SettingsScreen(
                    onBack = { currentScreen = AppScreen.MAP }
                )
            }
        }
    }
}