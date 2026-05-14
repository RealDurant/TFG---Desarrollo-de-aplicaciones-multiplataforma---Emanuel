package com.example.terrirun

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlinx.coroutines.*

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SplashScreen()
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(2500)

            startActivity(
                Intent(this@SplashActivity, MainActivity::class.java)
            )

            finish()
        }
    }
}