package com.example.gencidevtest.presentation.splash.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gencidevtest.presentation.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onSplashCompleted: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    var isMinimumTimeElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(3000)
        isMinimumTimeElapsed = true
        onSplashCompleted()
    }

    // Navigate setelah minimum time dan status login sudah dicek
    LaunchedEffect(isMinimumTimeElapsed, uiState.isInitializing, uiState.isLoggedIn) {
        if (isMinimumTimeElapsed && !uiState.isInitializing) {
            if (uiState.isLoggedIn) {
                onNavigateToHome()
            } else {
                onNavigateToLogin()
            }
        }
    }

    // Splash Screen UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Text(
                text = "🛍️",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App Name
            Text(
                text = "GenciDev Test",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "With Api DummyJson",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

        }
    }
}