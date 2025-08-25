package com.example.gencidevtest.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
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
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val navController = rememberNavController()

    if (uiState.isLoggedIn) {
        // Main App with Bottom Navigation
        Scaffold(
            bottomBar = {
                // Only show bottom navigation for main screens (not search or product detail)
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
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = modifier.padding(paddingValues)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onProductClick = { product ->
                            navController.navigate(Screen.ProductDetail.createRoute(product.id))
                        },
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onProductClick = { product ->
                            navController.navigate(Screen.ProductDetail.createRoute(product.id))
                        },

                    )
                }

                composable(Screen.Cart.route) {
                    CartScreen()
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onLogout = {
                            // Navigation will be handled by AuthViewModel state change
                        }
                    )
                }

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
    } else {
        // Login Screen
        LoginScreen(
            onLoginSuccess = {
                // Navigation will be handled by AuthViewModel state change
            }
        )
    }
}