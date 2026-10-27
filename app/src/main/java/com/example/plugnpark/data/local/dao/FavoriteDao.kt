package com.example.plugnpark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.plugnpark.data.local.entity.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesByUser(userId: String): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE userId = :userId AND listingId = :listingId")
    suspend fun deleteFavorite(userId: String, listingId: String)
}
