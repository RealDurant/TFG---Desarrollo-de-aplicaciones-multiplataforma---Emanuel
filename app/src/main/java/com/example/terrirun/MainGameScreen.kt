package com.example.terrirun

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

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
    var showNotificationDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    var language by remember { mutableStateOf(settingsManager.getLanguage()) }

    LaunchedEffect(uiState.playerProfile) {
        if (uiState.playerProfile.name.isNotBlank()) {
            showNotificationDialog = true
        }
    }
    LaunchedEffect(Unit) {
        gameViewModel.loadInitialData()
    }

    val title = when (currentScreen) {
        AppScreen.MAP -> "TerriRun"
        AppScreen.RANKING -> appText("ranking", language)
        AppScreen.PROFILE -> appText("profile", language)
        AppScreen.SETTINGS -> appText("settings", language)
        AppScreen.PRIVACY -> appText("privacy", language)
        AppScreen.NOTIFICATIONS -> appText("notifications", language) // 👈 AÑADE ESTA
    }
    var notifications by remember { mutableStateOf<List<GameNotification>>(emptyList()) }
    LaunchedEffect(Unit) {
        val uid = gameViewModel.getCurrentUserId()
        if (uid != null) {
            UserRepository().getNotifications(uid) {
                notifications = it
            }
        }
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
                    label = { Text(appText("map", language)) },
                    icon = { Icon(Icons.Default.Map, null) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.RANKING,
                    onClick = { currentScreen = AppScreen.RANKING },
                    label = { Text(appText("ranking", language)) },
                    icon = { Icon(Icons.Default.Leaderboard, null) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.PROFILE,
                    onClick = { currentScreen = AppScreen.PROFILE },
                    label = { Text(appText("profile", language)) },
                    icon = { Icon(Icons.Default.Person, null) }                )
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
                    onRefreshData = { gameViewModel.refreshTerritories() },
                    language = language
                )
            }
            AppScreen.RANKING -> {
                RankingScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    language = language
                )
            }
            AppScreen.PROFILE -> {
                ProfileScreen(
                    onLogout = {
                        gameViewModel.logout()
                        onLogout()
                    },
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    language = language
                )
            }
            AppScreen.SETTINGS -> {
                var notificationsEnabled by remember { mutableStateOf(true) }

                SettingsScreen(
                    language = language,
                    onLanguageChange = { newLanguage ->
                        language = newLanguage
                        settingsManager.setLanguage(newLanguage)
                    },
                    notificationsEnabled = notificationsEnabled,
                    onNotificationsChange = { enabled ->
                        notificationsEnabled = enabled

                        val uid = gameViewModel.getCurrentUserId()
                        if (uid != null) {
                            UserRepository().updateNotifications(uid, enabled) { _, _ -> }
                        }
                    },
                    onBack = { currentScreen = AppScreen.MAP },
                    onLogout = {
                        gameViewModel.logout()
                        onLogout()
                    },
                    onOpenPrivacy = {
                        currentScreen = AppScreen.PRIVACY
                    },
                    onOpenNotifications = {
                        currentScreen = AppScreen.NOTIFICATIONS
                    }
                )
            }
            AppScreen.PRIVACY -> {
                PrivacyScreen(
                    language = language,
                    onBack = { currentScreen = AppScreen.SETTINGS }
                )
            }
            AppScreen.NOTIFICATIONS -> {
                NotificationsScreen(
                    language = language,
                    currentUserId = uiState.currentUserId,
                    onBack = { currentScreen = AppScreen.SETTINGS }
                )
            }
        }

        if (showNotificationDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationDialog = false },
                title = {
                    Text(appText("activate_notifications", language))
                        },
                text = {
                    Text(appText("activate_notifications_text", language))
                },
                confirmButton = {
                    TextButton(onClick = {
                        val uid = gameViewModel.getCurrentUserId()
                        if (uid != null) {
                            UserRepository().updateNotifications(uid, true) { _, _ -> }
                        }
                        showNotificationDialog = false
                    }) {
                        Text(appText("yes", language))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        val uid = gameViewModel.getCurrentUserId()
                        if (uid != null) {
                            UserRepository().updateNotifications(uid, false) { _, _ -> }
                        }
                        showNotificationDialog = false
                    }) {
                        Text(appText("no", language))
                    }
                }
            )
            if (notifications.isNotEmpty()) {
                val latest = notifications.last()

                AlertDialog(
                    onDismissRequest = { notifications = emptyList() },
                    confirmButton = {
                        TextButton(onClick = { notifications = emptyList() }) {
                            Text("OK")
                        }
                    },
                    title = { Text(latest.title) },
                    text = { Text(latest.message) }
                )
            }
        }
    }
}