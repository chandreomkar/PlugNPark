package com.example.plugnpark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.plugnpark.data.local.entity.ChargerSpec
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargerSpecDao {
    @Query("SELECT * FROM charger_specs WHERE listingId = :listingId")
    fun getChargerSpec(listingId: String): Flow<ChargerSpec?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChargerSpec(chargerSpec: ChargerSpec)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChargerSpecs(specs: List<ChargerSpec>)
}
