package com.example.plugnpark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.plugnpark.data.local.entity.AvailabilityRule
import kotlinx.coroutines.flow.Flow

@Dao
interface AvailabilityRuleDao {
    @Query("SELECT * FROM availability_rules WHERE listingId = :listingId")
    fun getRulesForListing(listingId: String): Flow<List<AvailabilityRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AvailabilityRule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<AvailabilityRule>)

    @Query("DELETE FROM availability_rules WHERE listingId = :listingId")
    suspend fun deleteRulesByListing(listingId: String)
}
