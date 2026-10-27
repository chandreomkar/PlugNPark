package com.example.plugnpark.ui.navigation

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    object Onboarding : NavRoutes("onboarding")
    object Login : NavRoutes("login")
    object RoleChoice : NavRoutes("role_choice")
    object Main : NavRoutes("main")

    // Bottom Bar Sub-routes
    object Explore : NavRoutes("explore")
    object Bookings : NavRoutes("bookings")
    object Host : NavRoutes("host")
    object Profile : NavRoutes("profile")

    // Detail, Wizard & Booking Flow Routes
    object AddEditListing : NavRoutes("add_edit_listing")
    object ListingDetail : NavRoutes("listing_detail/{listingId}") {
        fun createRoute(listingId: String) = "listing_detail/$listingId"
    }
    object BookingConfigure : NavRoutes("booking_configure/{listingId}") {
        fun createRoute(listingId: String) = "booking_configure/$listingId"
    }
    object MockPayment : NavRoutes("mock_payment/{listingId}") {
        fun createRoute(listingId: String) = "mock_payment/$listingId"
    }
    object Confirmation : NavRoutes("confirmation/{bookingId}/{listingId}") {
        fun createRoute(bookingId: String, listingId: String) = "confirmation/$bookingId/$listingId"
    }
}
