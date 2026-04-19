package com.example.terrirun


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun RegisterScreen(
    authManager: AuthManager,
    userRepository: UserRepository,
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit
){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Registro",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.padding(top = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            modifier = Modifier.padding(top = 12.dp)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Button(
            onClick = {
                errorMessage = null

                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Completa todos los campos"
                    return@Button
                }

                if (password.length < 6) {
                    errorMessage = "La contraseña debe tener al menos 6 caracteres"
                    return@Button
                }

                isLoading = true

                authManager.register(email, password) { success, error ->
                    if (success) {
                        val uid = authManager.getCurrentUserId()

                        if (uid == null) {
                            isLoading = false
                            errorMessage = "No se pudo obtener el usuario actual"
                            return@register
                        }

                        val profile = UserProfile(
                            uid = uid,
                            email = email
                        )

                        userRepository.saveUserProfile(profile) { saveSuccess, saveError ->
                            isLoading = false
                            if (saveSuccess) {
                                onRegisterSuccess()
                            } else {
                                errorMessage = saveError ?: "Usuario creado, pero no se pudo guardar el perfil"
                            }
                        }
                    } else {
                        isLoading = false
                        errorMessage = error ?: "Error al registrar usuario"
                    }
                }
            },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text("Crear cuenta")
        }
        Button(
            onClick = onGoToLogin,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Ir a inicio de sesión")
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}