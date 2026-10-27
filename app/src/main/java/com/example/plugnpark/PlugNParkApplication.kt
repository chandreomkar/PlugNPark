package com.example.plugnpark

import android.app.Application
import androidx.preference.PreferenceManager
import com.example.plugnpark.data.local.db.DatabaseInitializer
import com.example.plugnpark.data.local.db.PlugNParkDatabase
import com.example.plugnpark.data.repository.PlugNParkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import java.io.File

class PlugNParkApplication : Application() {

    lateinit var database: PlugNParkDatabase
        private set

    lateinit var repository: PlugNParkRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize Osmdroid configuration
        val config = Configuration.getInstance()
        config.load(this, PreferenceManager.getDefaultSharedPreferences(this))
        config.userAgentValue = "PlugNParkApp/1.0 (student project)"

        val osmBase = File(cacheDir, "osmdroid")
        val osmTiles = File(cacheDir, "osmdroid/tiles")
        if (!osmBase.exists()) osmBase.mkdirs()
        if (!osmTiles.exists()) osmTiles.mkdirs()

        config.osmdroidBasePath = osmBase
        config.osmdroidTileCache = osmTiles

        database = PlugNParkDatabase.getDatabase(this)
        repository = PlugNParkRepository(database)

        // Seed initial data in background safely
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DatabaseInitializer.seedDatabase(database)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
