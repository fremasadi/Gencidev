// app/src/main/java/com/example/gencidevtest/presentation/home/screen/HomeScreen.kt
package com.example.gencidevtest.presentation.home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gencidevtest.data.util.NetworkConnectionObserver
import com.example.gencidevtest.domain.model.Product
import com.example.gencidevtest.presentation.cart.viewmodel.CartViewModel
import com.example.gencidevtest.presentation.common.components.CategoryCard
import com.example.gencidevtest.presentation.common.components.ProductCard
import com.example.gencidevtest.presentation.home.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
    productViewModel: ProductViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val productUiState by productViewModel.uiState.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Get network observer from Hilt
    val context = androidx.compose.ui.platform.LocalContext.current
    val networkObserver = remember { NetworkConnectionObserver(context) }

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    // Pull to refresh state using the older but more compatible SwipeRefresh
    val pullRefreshState = rememberPullRefreshState(
        refreshing = productUiState.isRefreshing,
        onRefresh = { productViewModel.refreshData() }
    )

    // Observe network connectivity changes
    val isOnline by networkObserver.observeNetworkConnection().collectAsState(initial = networkObserver.isNetworkAvailable())

    // Update network state in ViewModel
    LaunchedEffect(isOnline) {
        productViewModel.updateNetworkState(isOnline)
    }

    // Show online status snackbar when coming back online
    LaunchedEffect(isOnline) {
        if (isOnline && productUiState.isOffline) {
            snackbarHostState.showSnackbar(
                message = "✅ Back online! Data refreshed",
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
        }
    }

    // Show add to cart messages
    LaunchedEffect(cartUiState.addToCartMessage) {
        cartUiState.addToCartMessage?.let {
            // You can show snackbar here if needed
        }
    }

    // Sync search query with state
    LaunchedEffect(productUiState.searchQuery) {
        if (searchQuery != productUiState.searchQuery) {
            searchQuery = productUiState.searchQuery
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Produk",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Offline mode banner
            if (productUiState.isOffline && productUiState.products.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Menampilkan produk yang disimpan dalam cache • Tarik ke bawah untuk menyegarkan saat online",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newQuery ->
                    searchQuery = newQuery
                    productViewModel.searchProducts(newQuery)
                },
                label = { Text("Search products...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                enabled = !productUiState.isLoading
            )

            // Categories Section
            if (productUiState.categories.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kategori",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (productUiState.isLoadingCategories) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    // "All" category chip
                    item {
                        FilterChip(
                            onClick = { productViewModel.selectCategory(null) },
                            label = {
                                Text(
                                    text = "Semua",
                                    fontWeight = if (productUiState.selectedCategory == null) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = productUiState.selectedCategory == null,
                            modifier = Modifier.padding(end = 8.dp),
                            enabled = !productUiState.isLoading
                        )
                    }

                    // Category chips
                    items(productUiState.categories) { category ->
                        CategoryCard(
                            category = category,
                            isSelected = productUiState.selectedCategory?.slug == category.slug,
                            onCategoryClick = { productViewModel.selectCategory(it) }
                        )
                    }
                }
            }

            // Add to Cart Message
            cartUiState.addToCartMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("success", ignoreCase = true)) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = if (message.contains("success", ignoreCase = true)) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    productUiState.isLoading -> {
                        // Loading State
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (productUiState.isOffline) "Loading cached produk..." else "Loading produk...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    productUiState.errorMessage != null -> {
                        // Error State
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = productUiState.errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { productViewModel.retryLastOperation() }
                            ) {
                                Text("Coba Lagi")
                            }
                        }
                    }

                    productUiState.products.isEmpty() -> {
                        // Empty State
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (productUiState.isOffline) {
                                    "Tidak ada produk yang di-cache tersedia"
                                } else {
                                    "Tidak ada produk yang ditemukan"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (productUiState.searchQuery.isNotEmpty() || productUiState.selectedCategory != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Coba sesuaikan filter pencarian atau kategori Anda",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    else -> {
                        // Products Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = productUiState.products,
                                key = { product -> product.id }
                            ) { product ->
                                ProductCard(
                                    product = product,
                                    onProductClick = onProductClick
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pull refresh indicator
        PullRefreshIndicator(
            refreshing = productUiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Snackbar host for showing online status
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
                actionColor = Color.White
            )
        }
    }
}