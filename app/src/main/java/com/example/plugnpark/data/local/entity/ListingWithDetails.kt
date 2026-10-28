package com.example.plugnpark.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.plugnpark.domain.model.ConnectorType

data class ListingWithDetails(
    @Embedded val listing: Listing,
    @Relation(
        parentColumn = "id",
        entityColumn = "listingId"
    )
    val chargerSpec: ChargerSpec?,
    @Relation(
        parentColumn = "id",
        entityColumn = "listingId"
    )
    val parkingSpec: ParkingSpec?,
    @Relation(
        parentColumn = "id",
        entityColumn = "listingId"
    )
    val photos: List<ListingPhoto>
) {
    val connectorTypes: List<ConnectorType> get() = chargerSpec?.connectorTypes ?: emptyList()
    val powerKw: Double get() = chargerSpec?.powerKw ?: 0.0
    val isCovered: Boolean get() = parkingSpec?.isCovered ?: false
    val hasCCTV: Boolean get() = parkingSpec?.hasCctv ?: false
    val vehicleType: String get() = parkingSpec?.vehicleTypes ?: "4-Wheeler"
    val imageUrl: String get() = photos.firstOrNull()?.url ?: ""

    fun toDomainListing(): Listing {
        return listing.copy(
            connectorTypes = connectorTypes,
            powerKw = powerKw,
            isCovered = isCovered,
            hasCCTV = hasCCTV,
            vehicleType = vehicleType,
            imageUrl = imageUrl
        )
    }
}
