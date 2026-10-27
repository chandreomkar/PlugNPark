package com.example.plugnpark

import android.app.Application
import com.example.plugnpark.data.local.db.DatabaseInitializer
import com.example.plugnpark.data.local.db.PlugNParkDatabase
import com.example.plugnpark.data.repository.PlugNParkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration

class PlugNParkApplication : Application() {

    lateinit var database: PlugNParkDatabase
        private set

    lateinit var repository: PlugNParkRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Osmdroid configuration
        Configuration.getInstance().userAgentValue = packageName

        database = PlugNParkDatabase.getDatabase(this)
        repository = PlugNParkRepository(
            userDao = database.userDao(),
            listingDao = database.listingDao(),
            bookingDao = database.bookingDao(),
            reviewDao = database.reviewDao()
        )

        // Seed initial data in background
        CoroutineScope(Dispatchers.IO).launch {
            DatabaseInitializer.seedDatabase(database)
        }
    }
}
