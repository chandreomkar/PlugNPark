package com.example.plugnpark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.plugnpark.data.local.datastore.UserSessionManager
import com.example.plugnpark.ui.navigation.PlugNParkNavGraph
import com.example.plugnpark.ui.theme.PlugNParkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as PlugNParkApplication
        val repository = app.repository
        val sessionManager = UserSessionManager(this)

        setContent {
            PlugNParkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val rootNavController = rememberNavController()
                    PlugNParkNavGraph(
                        rootNavController = rootNavController,
                        repository = repository,
                        sessionManager = sessionManager
                    )
                }
            }
        }
    }
}
