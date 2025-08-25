package com.example.gencidevtest.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gencidevtest.presentation.auth.screen.LoginScreen
import com.example.gencidevtest.presentation.auth.viewmodel.AuthViewModel
import com.example.gencidevtest.presentation.cart.screen.CartScreen
import com.example.gencidevtest.presentation.home.screen.HomeScreen
import com.example.gencidevtest.presentation.home.screen.ProductDetailScreen
import com.example.gencidevtest.presentation.home.screen.SearchScreen
import com.example.gencidevtest.presentation.profile.screen.ProfileScreen
import com.example.gencidevtest.presentation.splash.screen.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object Search : Screen("search")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()

    // State untuk track apakah splash sudah selesai
    var splashCompleted by remember { mutableStateOf(false) }

    // Observe auth state changes untuk navigation HANYA setelah splash selesai
    LaunchedEffect(splashCompleted, authState.isLoggedIn, authState.isInitializing) {
        if (splashCompleted) {
            val currentRoute = navController.currentDestination?.route

            when {
                // Jika masih initializing, tetap di splash
                authState.isInitializing -> {
                    // Do nothing, stay in current state
                }
                // Jika logged in, navigate ke home (kecuali sudah di main screens)
                authState.isLoggedIn -> {
                    if (currentRoute == Screen.Splash.route || currentRoute == Screen.Login.route) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                // Jika not logged in, navigate ke login
                !authState.isLoggedIn -> {
                    if (currentRoute != Screen.Login.route && currentRoute != Screen.Splash.route) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onSplashCompleted = {
                    splashCompleted = true
                },
                authViewModel = authViewModel
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // Main App Screens dengan Bottom Navigation
        composable(Screen.Home.route) {
            MainAppContent(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Cart.route) {
            MainAppContent(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Profile.route) {
            MainAppContent(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Search Screen (tanpa bottom navigation)
        composable(Screen.Search.route) {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail.createRoute(product.id))
                }
            )
        }

        // Product Detail Screen (tanpa bottom navigation)
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            ProductDetailScreen(
                productId = productId,
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppContent(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val showBottomBar = when (currentRoute) {
                Screen.Home.route,
                Screen.Cart.route,
                Screen.Profile.route -> true
                else -> false
            }

            if (showBottomBar) {
                BottomNavigation(navController)
            }
        }
    ) { paddingValues ->
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentRoute) {
                Screen.Home.route -> {
                    HomeScreen(
                        onProductClick = { product ->
                            navController.navigate(Screen.ProductDetail.createRoute(product.id))
                        },
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        }
                    )
                }
                Screen.Cart.route -> {
                    CartScreen()
                }
                Screen.Profile.route -> {
                    ProfileScreen(
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}