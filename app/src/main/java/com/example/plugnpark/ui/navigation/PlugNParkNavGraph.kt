package com.example.plugnpark.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.plugnpark.ui.screens.BookingsScreen
import com.example.plugnpark.ui.screens.ConfirmationScreen
import com.example.plugnpark.ui.screens.ExploreScreen
import com.example.plugnpark.ui.screens.HostScreen
import com.example.plugnpark.ui.screens.ListingDetailScreen
import com.example.plugnpark.ui.screens.MockLoginScreen
import com.example.plugnpark.ui.screens.MockPaymentScreen
import com.example.plugnpark.ui.screens.NotificationsScreen
import com.example.plugnpark.ui.screens.OnboardingScreen
import com.example.plugnpark.ui.screens.ProfileScreen
import com.example.plugnpark.ui.screens.RoleChoiceScreen
import com.example.plugnpark.ui.screens.SplashScreen
import com.example.plugnpark.ui.viewmodel.BookingViewModel
import com.example.plugnpark.ui.viewmodel.BookingsViewModel
import com.example.plugnpark.ui.viewmodel.ExploreViewModel
import com.example.plugnpark.ui.viewmodel.HostViewModel
import com.example.plugnpark.ui.viewmodel.NotificationsViewModel
import com.example.plugnpark.ui.viewmodel.ProfileViewModel
import com.example.plugnpark.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun PlugNParkNavGraph(
    repository: PlugNParkRepository,
    sessionManager: UserSessionManager
) {
    val rootNavController = rememberNavController()
    val session by sessionManager.userSessionFlow.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = rootNavController,
        startDestination = NavRoutes.Splash.route
    ) {
        composable(NavRoutes.Splash.route) {
            val currentSession = session ?: UserSession()
            SplashScreen(
                session = currentSession,
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
                        val user = repository.getOrCreateUserByPhone(phone, role)
                        sessionManager.saveLoginSession(userId = user.id, phone = phone, role = role)
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
    val factory = ViewModelFactory(repository, sessionManager)
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    // Android 13+ POST_NOTIFICATIONS Runtime Permission Request
    var showNotificationRationaleDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        showNotificationRationaleDialog = false
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotifPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasNotifPermission) {
                showNotificationRationaleDialog = true
            }
        }
    }

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
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Explore") },
                        label = { Text("Explore") },
                        selected = currentRoute == NavRoutes.Explore.route,
                        onClick = {
                            if (currentRoute != NavRoutes.Explore.route) {
                                bottomNavController.navigate(NavRoutes.Explore.route) {
                                    popUpTo(NavRoutes.Explore.route) { inclusive = true }
                                }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Bookings") },
                        label = { Text("Bookings") },
                        selected = currentRoute == NavRoutes.Bookings.route,
                        onClick = {
                            if (currentRoute != NavRoutes.Bookings.route) {
                                bottomNavController.navigate(NavRoutes.Bookings.route) {
                                    popUpTo(NavRoutes.Explore.route)
                                }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.EvStation, contentDescription = "Host") },
                        label = { Text("Host") },
                        selected = currentRoute == NavRoutes.Host.route,
                        onClick = {
                            if (currentRoute != NavRoutes.Host.route) {
                                bottomNavController.navigate(NavRoutes.Host.route) {
                                    popUpTo(NavRoutes.Explore.route)
                                }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = currentRoute == NavRoutes.Profile.route,
                        onClick = {
                            if (currentRoute != NavRoutes.Profile.route) {
                                bottomNavController.navigate(NavRoutes.Profile.route) {
                                    popUpTo(NavRoutes.Explore.route)
                                }
                            }
                        }
                    )
                }
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
                    },
                    onOpenNotifications = {
                        bottomNavController.navigate(NavRoutes.Notifications.route)
                    }
                )
            }

            composable(NavRoutes.Notifications.route) {
                val viewModel: NotificationsViewModel = viewModel(factory = factory)
                NotificationsScreen(
                    viewModel = viewModel,
                    onBack = { bottomNavController.popBackStack() }
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
            ) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
                val viewModel: BookingViewModel = viewModel(factory = factory)

                ListingDetailScreen(
                    listingId = listingId,
                    viewModel = viewModel,
                    onBack = { bottomNavController.popBackStack() },
                    onProceedToBooking = {
                        bottomNavController.navigate(NavRoutes.MockPayment.createRoute(listingId))
                    }
                )
            }

            composable(
                route = NavRoutes.MockPayment.route,
                arguments = listOf(navArgument("listingId") { type = NavType.StringType })
            ) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
                val viewModel: BookingViewModel = viewModel(factory = factory)

                MockPaymentScreen(
                    viewModel = viewModel,
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
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val viewModel: BookingViewModel = viewModel(factory = factory)

                ConfirmationScreen(
                    bookingId = bookingId,
                    viewModel = viewModel,
                    onNavigateToBookings = {
                        bottomNavController.navigate(NavRoutes.Explore.route) {
                            popUpTo(NavRoutes.Explore.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        // POST_NOTIFICATIONS Permission Explanation Dialog (Android 13+)
        if (showNotificationRationaleDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationRationaleDialog = false },
                title = { Text("Enable Notifications", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Stay updated on your EV charger and parking reservations! PlugNPark needs notification permission to send instant booking confirmations, 15-minute start reminders, and host approval updates.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                showNotificationRationaleDialog = false
                            }
                        }
                    ) {
                        Text("Allow Notifications")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNotificationRationaleDialog = false }) {
                        Text("Not Now")
                    }
                }
            )
        }
    }
}
