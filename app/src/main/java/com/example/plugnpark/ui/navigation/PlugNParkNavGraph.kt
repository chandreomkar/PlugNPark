package com.example.plugnpark.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.plugnpark.data.local.datastore.UserSession
import com.example.plugnpark.data.local.datastore.UserSessionManager
import com.example.plugnpark.data.repository.PlugNParkRepository
import com.example.plugnpark.ui.screens.AddEditListingScreen
import com.example.plugnpark.ui.screens.BookingScreen
import com.example.plugnpark.ui.screens.BookingsScreen
import com.example.plugnpark.ui.screens.ConfirmationScreen
import com.example.plugnpark.ui.screens.ExploreScreen
import com.example.plugnpark.ui.screens.HostScreen
import com.example.plugnpark.ui.screens.ListingDetailScreen
import com.example.plugnpark.ui.screens.MockLoginScreen
import com.example.plugnpark.ui.screens.MockPaymentScreen
import com.example.plugnpark.ui.screens.OnboardingScreen
import com.example.plugnpark.ui.screens.ProfileScreen
import com.example.plugnpark.ui.screens.RoleChoiceScreen
import com.example.plugnpark.ui.screens.SplashScreen
import com.example.plugnpark.ui.viewmodel.BookingViewModel
import com.example.plugnpark.ui.viewmodel.BookingsViewModel
import com.example.plugnpark.ui.viewmodel.ExploreViewModel
import com.example.plugnpark.ui.viewmodel.HostViewModel
import com.example.plugnpark.ui.viewmodel.ProfileViewModel
import com.example.plugnpark.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun PlugNParkNavGraph(
    rootNavController: NavHostController,
    repository: PlugNParkRepository,
    sessionManager: UserSessionManager,
    modifier: Modifier = Modifier
) {
    val session by sessionManager.userSessionFlow.collectAsState(initial = UserSession())
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = rootNavController,
        startDestination = NavRoutes.Splash.route,
        modifier = modifier
    ) {
        composable(NavRoutes.Splash.route) {
            SplashScreen(
                session = session,
                onNavigateNext = { targetRoute ->
                    rootNavController.navigate(targetRoute) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    coroutineScope.launch {
                        sessionManager.setOnboarded(true)
                        rootNavController.navigate(NavRoutes.Login.route) {
                            popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(NavRoutes.Login.route) {
            MockLoginScreen(
                onLoginSuccess = { phone ->
                    rootNavController.navigate("${NavRoutes.RoleChoice.route}/$phone")
                }
            )
        }

        composable(
            route = "${NavRoutes.RoleChoice.route}/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            RoleChoiceScreen(
                phoneNumber = phone,
                onRoleSelected = { role ->
                    coroutineScope.launch {
                        sessionManager.saveLoginSession(phone, role)
                        rootNavController.navigate(NavRoutes.Main.route) {
                            popUpTo(NavRoutes.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(NavRoutes.Main.route) {
            MainFlowScreen(
                repository = repository,
                sessionManager = sessionManager,
                onLogout = {
                    coroutineScope.launch {
                        sessionManager.logout()
                        rootNavController.navigate(NavRoutes.Login.route) {
                            popUpTo(NavRoutes.Main.route) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MainFlowScreen(
    repository: PlugNParkRepository,
    sessionManager: UserSessionManager,
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val factory = ViewModelFactory(repository)
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on primary 4 tabs
    val showBottomBar = currentRoute in listOf(
        NavRoutes.Explore.route,
        NavRoutes.Bookings.route,
        NavRoutes.Host.route,
        NavRoutes.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = bottomNavController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = NavRoutes.Explore.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Explore.route) {
                val viewModel: ExploreViewModel = viewModel(factory = factory)
                ExploreScreen(
                    viewModel = viewModel,
                    onListingClick = { listingId ->
                        bottomNavController.navigate(NavRoutes.ListingDetail.createRoute(listingId))
                    }
                )
            }

            composable(NavRoutes.Bookings.route) {
                val viewModel: BookingsViewModel = viewModel(factory = factory)
                BookingsScreen(viewModel = viewModel)
            }

            composable(NavRoutes.Host.route) {
                val viewModel: HostViewModel = viewModel(factory = factory)
                HostScreen(
                    viewModel = viewModel,
                    onNavigateToAddListing = {
                        bottomNavController.navigate(NavRoutes.AddEditListing.route)
                    }
                )
            }

            composable(NavRoutes.Profile.route) {
                val viewModel: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = onLogout
                )
            }

            composable(NavRoutes.AddEditListing.route) {
                val viewModel: HostViewModel = viewModel(factory = factory)
                AddEditListingScreen(
                    viewModel = viewModel,
                    onBack = { bottomNavController.popBackStack() }
                )
            }

            // Detail & Booking Flow Screens
            composable(
                route = NavRoutes.ListingDetail.route,
                arguments = listOf(navArgument("listingId") { type = NavType.StringType })
            ) { backStack ->
                val listingId = backStack.arguments?.getString("listingId") ?: ""
                val bookingViewModel: BookingViewModel = viewModel(factory = factory)
                ListingDetailScreen(
                    listingId = listingId,
                    viewModel = bookingViewModel,
                    onBack = { bottomNavController.popBackStack() },
                    onProceedToBooking = {
                        bottomNavController.navigate(NavRoutes.BookingConfigure.createRoute(listingId))
                    }
                )
            }

            composable(
                route = NavRoutes.BookingConfigure.route,
                arguments = listOf(navArgument("listingId") { type = NavType.StringType })
            ) { backStack ->
                val listingId = backStack.arguments?.getString("listingId") ?: ""
                val bookingViewModel: BookingViewModel = viewModel(factory = factory)
                BookingScreen(
                    viewModel = bookingViewModel,
                    onBack = { bottomNavController.popBackStack() },
                    onProceedToPayment = {
                        bottomNavController.navigate(NavRoutes.MockPayment.createRoute(listingId))
                    }
                )
            }

            composable(
                route = NavRoutes.MockPayment.route,
                arguments = listOf(navArgument("listingId") { type = NavType.StringType })
            ) { backStack ->
                val listingId = backStack.arguments?.getString("listingId") ?: ""
                val bookingViewModel: BookingViewModel = viewModel(factory = factory)
                MockPaymentScreen(
                    viewModel = bookingViewModel,
                    onBack = { bottomNavController.popBackStack() },
                    onPaymentSuccess = { bookingId ->
                        bottomNavController.navigate(NavRoutes.Confirmation.createRoute(bookingId, listingId)) {
                            popUpTo(NavRoutes.Explore.route)
                        }
                    }
                )
            }

            composable(
                route = NavRoutes.Confirmation.route,
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.StringType },
                    navArgument("listingId") { type = NavType.StringType }
                )
            ) { backStack ->
                val bookingId = backStack.arguments?.getString("bookingId") ?: ""
                val bookingViewModel: BookingViewModel = viewModel(factory = factory)
                ConfirmationScreen(
                    bookingId = bookingId,
                    viewModel = bookingViewModel,
                    onNavigateToBookings = {
                        bottomNavController.navigate(NavRoutes.Bookings.route) {
                            popUpTo(NavRoutes.Explore.route)
                        }
                    }
                )
            }
        }
    }
}
