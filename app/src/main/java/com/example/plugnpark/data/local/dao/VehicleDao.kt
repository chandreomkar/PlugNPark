package com.example.plugnpark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.plugnpark.data.local.entity.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE ownerId = :ownerId")
    fun getVehiclesByOwner(ownerId: String): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    fun getVehicleById(id: String): Flow<Vehicle?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<Vehicle>)

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicleById(id: String)

    @Query("UPDATE vehicles SET isDefault = 0 WHERE ownerId = :ownerId")
    suspend fun clearDefaultVehicle(ownerId: String)
}
