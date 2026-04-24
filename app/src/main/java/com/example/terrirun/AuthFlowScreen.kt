package com.example.terrirun

import androidx.compose.runtime.*

@Composable
fun AuthFlowScreen(
    authManager: AuthManager,
    userRepository: UserRepository,
    onAuthSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var pendingEmail by remember { mutableStateOf<String?>(null) }

    if (pendingEmail != null) {
        CompleteProfileScreen(
            email = pendingEmail!!,
            onSaveProfile = { name, colorHex, avatar, imageUri ->
                val uid = authManager.getCurrentUserId()

                if (uid != null) {
                    val profile = UserProfile(
                        uid = uid,
                        email = pendingEmail!!,
                        name = name,
                        colorHex = colorHex,
                        avatar = avatar,
                        profileImage = imageUri,
                        reinforcementPoints = 0
                    )

                    userRepository.saveUserProfile(profile) { success, _ ->
                        if (success) {
                            onAuthSuccess()
                        }
                    }
                }
            }
        )
    } else {
        if (isLoginMode) {
            LoginScreen(
                authManager = authManager,
                onLoginSuccess = onAuthSuccess,
                onGoToRegister = { isLoginMode = false }
            )
        } else {
            RegisterScreen(
                authManager = authManager,
                userRepository = userRepository,
                onRegisterSuccess = { email ->
                    pendingEmail = email
                },
                onGoToLogin = { isLoginMode = true }
            )
        }
    }
}