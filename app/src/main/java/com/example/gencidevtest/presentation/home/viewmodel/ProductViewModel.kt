package com.example.gencidevtest.presentation.home.viewmodel

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
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedCategory: Category? = null
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadProducts()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true) }

            getCategoriesUseCase()
                .onSuccess { categories ->
                    _uiState.update {
                        it.copy(
                            categories = categories,
                            isLoadingCategories = false
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoadingCategories = false,
                            errorMessage = exception.message ?: "Failed to load categories"
                        )
                    }
                }
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            getProductsUseCase()
                .onSuccess { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false,
                            selectedCategory = null,
                            searchQuery = ""
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to load products"
                        )
                    }
                }
        }
    }

    fun searchProducts(query: String) {
        _uiState.update { it.copy(searchQuery = query, selectedCategory = null) }

        if (query.isEmpty()) {
            loadProducts()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            searchProductsUseCase(query)
                .onSuccess { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Search failed"
                        )
                    }
                }
        }
    }

    fun selectCategory(category: Category?) {
        val currentSelected = _uiState.value.selectedCategory

        // If clicking the same category, deselect it
        if (currentSelected?.slug == category?.slug) {
            _uiState.update { it.copy(selectedCategory = null, searchQuery = "") }
            loadProducts()
            return
        }

        _uiState.update { it.copy(selectedCategory = category, searchQuery = "") }

        if (category == null) {
            loadProducts()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            getProductsByCategoryUseCase(category.slug)
                .onSuccess { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to load category products"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}