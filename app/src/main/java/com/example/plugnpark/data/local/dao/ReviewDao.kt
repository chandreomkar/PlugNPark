package com.example.plugnpark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.plugnpark.data.local.entity.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE listingId = :listingId ORDER BY createdAt DESC")
    fun getReviewsForListing(listingId: String): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE bookingId = :bookingId")
    fun getReviewByBookingId(bookingId: String): Flow<Review?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    @Query("SELECT COUNT(*) FROM reviews")
    suspend fun getReviewCount(): Int
}
