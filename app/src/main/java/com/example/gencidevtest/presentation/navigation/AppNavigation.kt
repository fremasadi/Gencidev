package com.example.gencidevtest.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gencidevtest.presentation.auth.screen.LoginScreen
import com.example.gencidevtest.presentation.auth.viewmodel.AuthViewModel
import com.example.gencidevtest.presentation.cart.screen.CartScreen
import com.example.gencidevtest.presentation.home.screen.HomeScreen
import com.example.gencidevtest.presentation.profile.screen.ProfileScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object Products : Screen("products")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
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
            bottomBar = { BottomNavigation(navController) }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Products.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Products.route) {
                    HomeScreen(
                        onProductClick = { product ->
                            // Handle product click - navigate to detail screen
                        }
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
            }
        }
    } else {
        // Login Screen
        LoginScreen(
            onLoginSuccess = {

            }
        )
    }
}