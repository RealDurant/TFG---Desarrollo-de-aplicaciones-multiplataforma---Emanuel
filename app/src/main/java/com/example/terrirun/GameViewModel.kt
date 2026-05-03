package com.example.terrirun

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

    private val authManager = AuthManager()
    private val userRepository = UserRepository()
    private val territoryRepository = TerritoryRepository()

    var uiState by mutableStateOf(GameUiState())
        private set

    fun loadInitialData() {
        val uid = authManager.getCurrentUserId() ?: return

        uiState = uiState.copy(currentUserId = uid)

        userRepository.getUserProfile(uid) { profile, _ ->
            if (profile != null) {
                val playerProfile = PlayerProfile(
                    name = profile.name,
                    territoryColor = hexToComposeColor(profile.colorHex),
                    avatar = profile.avatar,
                    profileImage = profile.profileImage
                )

                uiState = uiState.copy(
                    playerProfile = playerProfile,
                    reinforcementPoints = profile.reinforcementPoints
                )
            }

            territoryRepository.loadAllTerritories { loadedTerritories, _ ->
                val territories = loadedTerritories.map { it.toDomain() }

                uiState = uiState.copy(
                    territories = territories,
                    isLoading = false
                )

                loadOwnerData(territories)
            }
        }
    }

    private fun loadOwnerData(territories: List<Territory>) {
        val ownerNames = uiState.ownerNames.toMutableMap()
        val ownerColors = uiState.ownerColors.toMutableMap()
        val ownerAvatars = uiState.ownerAvatars.toMutableMap()
        val ownerImages = uiState.ownerImages.toMutableMap()

        territories.map { it.ownerId }
            .distinct()
            .filter { it.isNotBlank() }
            .forEach { ownerId ->
                if (!ownerNames.containsKey(ownerId)) {
                    userRepository.getUserProfile(ownerId) { profile, _ ->
                        if (profile != null) {
                            ownerNames[ownerId] = profile.name
                            ownerColors[ownerId] = hexToComposeColor(profile.colorHex)
                            ownerAvatars[ownerId] = profile.avatar
                            ownerImages[ownerId] = profile.profileImage

                            uiState = uiState.copy(
                                ownerNames = ownerNames.toMap(),
                                ownerColors = ownerColors.toMap(),
                                ownerAvatars = ownerAvatars.toMap(),
                                ownerImages = ownerImages.toMap()
                            )
                        }
                    }
                }
            }
    }

    fun logout() {
        authManager.logout()
    }

    fun getCurrentUserId(): String? = authManager.getCurrentUserId()

    fun refreshTerritories() {
        territoryRepository.loadAllTerritories { loadedTerritories, _ ->
            val territories = loadedTerritories.map { it.toDomain() }
            uiState = uiState.copy(territories = territories)
            loadOwnerData(territories)
        }
    }
}