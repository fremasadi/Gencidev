// app/src/main/java/com/example/gencidevtest/presentation/home/viewmodel/ProductViewModel.kt
package com.example.gencidevtest.presentation.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gencidevtest.domain.model.Product
import com.example.gencidevtest.domain.usecase.GetProductsByCategoryUseCase
import com.example.gencidevtest.domain.usecase.GetProductsUseCase
import com.example.gencidevtest.domain.usecase.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val isOffline: Boolean = false,
    val cacheInfo: String? = null,
    val currentFilter: ProductFilter = ProductFilter.All
)

sealed class ProductFilter {
    object All : ProductFilter()
    data class Search(val query: String) : ProductFilter()
    data class Category(val categorySlug: String) : ProductFilter()
}

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "ProductViewModel"
    }

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "ProductViewModel initialized")
        loadProducts()
    }

    fun loadProducts(showLoading: Boolean = true) {
        viewModelScope.launch {
            Log.d(TAG, "Starting to load all products, showLoading: $showLoading")

            if (showLoading) {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null,
                        currentFilter = ProductFilter.All,
                        searchQuery = ""
                    )
                }
            }

            try {
                getProductsUseCase()
                    .onSuccess { products ->
                        Log.d(TAG, "Successfully loaded ${products.size} products")
                        _uiState.update {
                            it.copy(
                                products = products,
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = null
                            )
                        }
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Failed to load products", exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = "Failed to load products: ${exception.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading products", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "An error occurred while loading products"
                    )
                }
            }
        }
    }

    fun searchProducts(query: String) {
        Log.d(TAG, "Searching products with query: '$query'")

        if (query.trim().isEmpty()) {
            Log.d(TAG, "Empty search query, loading all products")
            loadProducts()
            return
        }

        _uiState.update {
            it.copy(
                searchQuery = query.trim(),
                currentFilter = ProductFilter.Search(query.trim()),
                errorMessage = null
            )
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                searchProductsUseCase(query.trim())
                    .onSuccess { products ->
                        Log.d(TAG, "Search returned ${products.size} products for query: '$query'")
                        _uiState.update {
                            it.copy(
                                products = products,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Search failed for query: '$query'", exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Search failed: ${exception.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during search for query: '$query'", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "An error occurred during search"
                    )
                }
            }
        }
    }

    fun loadProductsByCategory(categorySlug: String) {
        Log.d(TAG, "Loading products for category: $categorySlug")

        _uiState.update {
            it.copy(
                currentFilter = ProductFilter.Category(categorySlug),
                searchQuery = "",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getProductsByCategoryUseCase(categorySlug)
                    .onSuccess { products ->
                        Log.d(TAG, "Successfully loaded ${products.size} products for category: $categorySlug")
                        _uiState.update {
                            it.copy(
                                products = products,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Failed to load products for category: $categorySlug", exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to load products for category: ${exception.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading products for category: $categorySlug", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "An error occurred while loading category products"
                    )
                }
            }
        }
    }

    fun refreshData() {
        Log.d(TAG, "Refreshing products data")
        _uiState.update { it.copy(isRefreshing = true) }

        // Refresh based on current filter
        when (val currentFilter = _uiState.value.currentFilter) {
            is ProductFilter.All -> loadProducts(showLoading = false)
            is ProductFilter.Search -> searchProducts(currentFilter.query)
            is ProductFilter.Category -> loadProductsByCategory(currentFilter.categorySlug)
        }
    }

    fun clearSearch() {
        Log.d(TAG, "Clearing search")
        _uiState.update {
            it.copy(
                searchQuery = "",
                currentFilter = ProductFilter.All
            )
        }
        loadProducts()
    }

    fun clearError() {
        Log.d(TAG, "Clearing error message")
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun retryLastOperation() {
        Log.d(TAG, "Retrying last operation")
        clearError()

        when (val currentFilter = _uiState.value.currentFilter) {
            is ProductFilter.All -> {
                Log.d(TAG, "Retrying load all products")
                loadProducts()
            }
            is ProductFilter.Search -> {
                Log.d(TAG, "Retrying search: ${currentFilter.query}")
                searchProducts(currentFilter.query)
            }
            is ProductFilter.Category -> {
                Log.d(TAG, "Retrying category filter: ${currentFilter.categorySlug}")
                loadProductsByCategory(currentFilter.categorySlug)
            }
        }
    }

    // Method to handle network state changes
    fun updateNetworkState(isOnline: Boolean) {
        val wasOffline = _uiState.value.isOffline

        _uiState.update { it.copy(isOffline = !isOnline) }

        // If device came back online, refresh data
        if (wasOffline && isOnline) {
            Log.d(TAG, "Device came back online, refreshing data")
            refreshData()
        }
    }

    // Helper methods
    fun isSearchActive(): Boolean {
        return _uiState.value.currentFilter is ProductFilter.Search
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ProductViewModel cleared")
    }
}