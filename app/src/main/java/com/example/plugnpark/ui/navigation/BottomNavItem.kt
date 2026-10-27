package com.example.plugnpark.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Explore : BottomNavItem(
        route = NavRoutes.Explore.route,
        title = "Explore",
        icon = Icons.Default.EvStation
    )

    object Bookings : BottomNavItem(
        route = NavRoutes.Bookings.route,
        title = "Bookings",
        icon = Icons.Default.BookOnline
    )

    object Host : BottomNavItem(
        route = NavRoutes.Host.route,
        title = "Host",
        icon = Icons.Default.Storefront
    )

    object Profile : BottomNavItem(
        route = NavRoutes.Profile.route,
        title = "Profile",
        icon = Icons.Default.Person
    )
}
