// app/src/main/java/com/example/gencidevtest/presentation/home/viewmodel/ProductViewModel.kt
package com.example.gencidevtest.presentation.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gencidevtest.domain.model.Category
import com.example.gencidevtest.domain.model.Product
import com.example.gencidevtest.domain.usecase.GetCategoriesUseCase
import com.example.gencidevtest.domain.usecase.GetProductsByCategoryUseCase
import com.example.gencidevtest.domain.usecase.GetProductsUseCase
import com.example.gencidevtest.domain.usecase.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingCategories: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val isOffline: Boolean = false,
    val cacheInfo: String? = null
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "ProductViewModel"
    }

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "ProductViewModel initialized, loading initial data")
        loadInitialData()
    }

    private fun loadInitialData() {
        Log.d(TAG, "Loading initial data (categories and products)")
        loadCategories()
        loadProducts()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            Log.d(TAG, "Starting to load categories")
            _uiState.update { it.copy(isLoadingCategories = true) }

            try {
                getCategoriesUseCase()
                    .onSuccess { categories ->
                        Log.d(TAG, "Successfully loaded ${categories.size} categories")
                        _uiState.update {
                            it.copy(
                                categories = categories,
                                isLoadingCategories = false,
                                errorMessage = null
                            )
                        }
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Failed to load categories", exception)
                        _uiState.update {
                            it.copy(
                                isLoadingCategories = false,
                                errorMessage = "Failed to load categories: ${exception.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading categories", e)
                _uiState.update {
                    it.copy(
                        isLoadingCategories = false,
                        errorMessage = "An error occurred while loading categories"
                    )
                }
            }
        }
    }

    fun loadProducts(showLoading: Boolean = true) {
        viewModelScope.launch {
            Log.d(TAG, "Starting to load products, showLoading: $showLoading")

            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
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
                                selectedCategory = null,
                                searchQuery = "",
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

    fun refreshData() {
        Log.d(TAG, "Refreshing all data")
        _uiState.update { it.copy(isRefreshing = true) }

        // Refresh both categories and products
        loadCategories()
        loadProducts(showLoading = false)
    }

    fun searchProducts(query: String) {
        Log.d(TAG, "Searching products with query: '$query'")
        _uiState.update {
            it.copy(
                searchQuery = query,
                selectedCategory = null,
                errorMessage = null
            )
        }

        if (query.isEmpty()) {
            Log.d(TAG, "Empty search query, loading all products")
            loadProducts()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                searchProductsUseCase(query)
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

    fun selectCategory(category: Category?) {
        val currentSelected = _uiState.value.selectedCategory

        Log.d(TAG, "Selecting category: ${category?.name ?: "All"}")

        // If clicking the same category, deselect it
        if (currentSelected?.slug == category?.slug) {
            Log.d(TAG, "Deselecting current category, loading all products")
            _uiState.update {
                it.copy(
                    selectedCategory = null,
                    searchQuery = "",
                    errorMessage = null
                )
            }
            loadProducts()
            return
        }

        _uiState.update {
            it.copy(
                selectedCategory = category,
                searchQuery = "",
                errorMessage = null
            )
        }

        if (category == null) {
            Log.d(TAG, "No category selected, loading all products")
            loadProducts()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getProductsByCategoryUseCase(category.slug)
                    .onSuccess { products ->
                        Log.d(TAG, "Successfully loaded ${products.size} products for category: ${category.name}")
                        _uiState.update {
                            it.copy(
                                products = products,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Failed to load products for category: ${category.name}", exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to load products for ${category.name}: ${exception.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading products for category: ${category.name}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "An error occurred while loading category products"
                    )
                }
            }
        }
    }

    fun clearError() {
        Log.d(TAG, "Clearing error message")
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun retryLastOperation() {
        Log.d(TAG, "Retrying last operation")
        val currentState = _uiState.value

        clearError()

        when {
            currentState.searchQuery.isNotEmpty() -> {
                Log.d(TAG, "Retrying search: ${currentState.searchQuery}")
                searchProducts(currentState.searchQuery)
            }
            currentState.selectedCategory != null -> {
                Log.d(TAG, "Retrying category filter: ${currentState.selectedCategory.name}")
                selectCategory(currentState.selectedCategory)
            }
            else -> {
                Log.d(TAG, "Retrying load all products")
                loadProducts()
            }
        }
    }

    // Method to handle network state changes
    fun updateNetworkState(isOnline: Boolean) {
        val wasOffline = _uiState.value.isOffline
        val isNowOnline = !isOnline

        _uiState.update { it.copy(isOffline = !isOnline) }

        // If device came back online, refresh data
        if (wasOffline && isOnline) {
            Log.d(TAG, "Device came back online, refreshing data")
            refreshData()
        }
    }



    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ProductViewModel cleared")
    }
}