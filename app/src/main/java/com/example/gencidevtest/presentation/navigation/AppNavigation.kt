package com.example.gencidevtest.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gencidevtest.presentation.auth.screen.LoginScreen
import com.example.gencidevtest.presentation.auth.viewmodel.AuthViewModel
import com.example.gencidevtest.presentation.cart.screen.CartScreen
import com.example.gencidevtest.presentation.home.screen.HomeScreen
import com.example.gencidevtest.presentation.home.screen.ProductDetailScreen
import com.example.gencidevtest.presentation.profile.screen.ProfileScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object Home : Screen("home")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
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
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = modifier
        ) {
            composable(Screen.Home.route) {
                Scaffold(
                    bottomBar = { BottomNavigation(navController) }
                ) { paddingValues ->
                    HomeScreen(
                        onProductClick = { product ->
                            navController.navigate(Screen.ProductDetail.createRoute(product.id))
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }

            composable(Screen.Cart.route) {
                Scaffold(
                    bottomBar = { BottomNavigation(navController) }
                ) { paddingValues ->
                    CartScreen(
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }

            composable(Screen.Profile.route) {
                Scaffold(
                    bottomBar = { BottomNavigation(navController) }
                ) { paddingValues ->
                    ProfileScreen(
                        onLogout = {
                            // Navigation will be handled by AuthViewModel state change
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
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
    } else {
        // Login Screen
        LoginScreen(
            onLoginSuccess = {
                // Navigation will be handled by AuthViewModel state change
            }
        )
    }
}
