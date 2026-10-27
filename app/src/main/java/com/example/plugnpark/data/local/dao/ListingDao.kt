package com.example.plugnpark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.local.entity.ListingWithDetails
import com.example.plugnpark.domain.model.ListingType
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Transaction
    @Query("SELECT * FROM listings WHERE isActive = 1")
    fun getAllActiveListingsWithDetails(): Flow<List<ListingWithDetails>>

    @Transaction
    @Query("SELECT * FROM listings WHERE id = :id")
    fun getListingWithDetailsById(id: String): Flow<ListingWithDetails?>

    @Transaction
    @Query("SELECT * FROM listings WHERE type = :type AND isActive = 1")
    fun getListingsWithDetailsByType(type: ListingType): Flow<List<ListingWithDetails>>

    @Transaction
    @Query("SELECT * FROM listings WHERE city = :city AND isActive = 1")
    fun getListingsWithDetailsByCity(city: String): Flow<List<ListingWithDetails>>

    @Transaction
    @Query("SELECT * FROM listings WHERE hostId = :hostId")
    fun getListingsWithDetailsByHostId(hostId: String): Flow<List<ListingWithDetails>>

    @Query("SELECT * FROM listings WHERE isActive = 1")
    fun getAllActiveListings(): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE id = :id")
    fun getListingById(id: String): Flow<Listing?>

    @Query("SELECT * FROM listings WHERE type = :type AND isActive = 1")
    fun getListingsByType(type: ListingType): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE city = :city AND isActive = 1")
    fun getListingsByCity(city: String): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE hostId = :hostId")
    fun getListingsByHostId(hostId: String): Flow<List<Listing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: Listing)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<Listing>)

    @Update
    suspend fun updateListing(listing: Listing)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListingById(id: String)

    @Query("SELECT COUNT(*) FROM listings")
    suspend fun getListingCount(): Int
}
