package com.example.terrirun

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun RegisterScreen(
    authManager: AuthManager,
    userRepository: UserRepository,
    onRegisterSuccess: (String) -> Unit,
    onGoToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        // EMAIL
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
                generalError = null
            },
            label = { Text("Correo electrónico") },
            isError = emailError != null,
            singleLine = true,
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
        )

        if (emailError != null) {
            Text(
                text = emailError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // PASSWORD
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
                generalError = null
            },
            label = { Text("Contraseña") },
            isError = passwordError != null,
            singleLine = true,
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = {
                    passwordVisible = !passwordVisible
                }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Default.VisibilityOff
                        else
                            Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        )

        if (passwordError != null) {
            Text(
                text = passwordError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Text(
                text = "Mínimo 6 caracteres y al menos un número",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // ERROR GENERAL
        if (generalError != null) {
            Text(
                text = generalError!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // BOTÓN REGISTRO
        Button(
            onClick = {

                emailError = null
                passwordError = null
                generalError = null

                var isValid = true

                if (!isValidEmail(email)) {
                    emailError = "Correo no válido (gmail, outlook, etc.)"
                    isValid = false
                }

                if (!isValidPassword(password)) {
                    passwordError = "Mínimo 6 caracteres y un número"
                    isValid = false
                }

                if (!isValid) return@Button

                isLoading = true

                authManager.register(email, password) { success, error ->

                    isLoading = false

                    if (success) {
                        val uid = authManager.getCurrentUserId()

                        if (uid == null) {
                            generalError = "Error interno"
                            return@register
                        }

                        onRegisterSuccess(email)

                    } else {
                        generalError = error ?: "Error al registrar usuario"
                    }
                }
            },
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
        ) {
            Text("Crear cuenta")
        }

        // BOTÓN LOGIN
        TextButton(
            onClick = onGoToLogin,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
fun isValidEmail(email: String): Boolean {
    val validDomains = listOf(
        "gmail.com",
        "outlook.com",
        "hotmail.com",
        "yahoo.com"
    )

    val parts = email.split("@")
    return parts.size == 2 && parts[1] in validDomains
}

fun isValidPassword(password: String): Boolean {
    return password.length >= 6 && password.any { it.isDigit() }
}