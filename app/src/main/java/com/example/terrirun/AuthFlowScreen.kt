package com.example.terrirun


import androidx.compose.runtime.*

@Composable
fun AuthFlowScreen(
    authManager: AuthManager,
    userRepository: UserRepository,
    onAuthSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }

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
            onRegisterSuccess = onAuthSuccess,
            onGoToLogin = { isLoginMode = true }
        )
    }
}