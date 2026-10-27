package com.example.plugnpark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.plugnpark.data.local.entity.ListingPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingPhotoDao {
    @Query("SELECT * FROM listing_photos WHERE listingId = :listingId ORDER BY sortOrder ASC")
    fun getPhotosForListing(listingId: String): Flow<List<ListingPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: ListingPhoto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<ListingPhoto>)

    @Query("DELETE FROM listing_photos WHERE listingId = :listingId")
    suspend fun deletePhotosByListing(listingId: String)
}
