package com.example.plugnpark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.plugnpark.data.local.entity.ParkingSpec
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingSpecDao {
    @Query("SELECT * FROM parking_specs WHERE listingId = :listingId")
    fun getParkingSpec(listingId: String): Flow<ParkingSpec?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingSpec(parkingSpec: ParkingSpec)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingSpecs(specs: List<ParkingSpec>)
}
