package com.example.gencidevtest.presentation.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gencidevtest.domain.model.Category
import com.example.gencidevtest.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "CategoryViewModel"
    }

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    // Expose selected category as separate flow for easy observation
    val selectedCategory: StateFlow<Category?> = _uiState
        .map { it.selectedCategory }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        Log.d(TAG, "CategoryViewModel initialized")
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            Log.d(TAG, "Starting to load categories")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                getCategoriesUseCase()
                    .onSuccess { categories ->
                        Log.d(TAG, "Successfully loaded ${categories.size} categories")
                        _uiState.update {
                            it.copy(
                                categories = categories,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Failed to load categories", exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to load categories: ${exception.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading categories", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "An error occurred while loading categories"
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
            Log.d(TAG, "Deselecting current category")
            _uiState.update {
                it.copy(selectedCategory = null)
            }
            return
        }

        _uiState.update {
            it.copy(selectedCategory = category)
        }
    }

    fun clearSelection() {
        Log.d(TAG, "Clearing category selection")
        _uiState.update { it.copy(selectedCategory = null) }
    }

    fun refreshCategories() {
        Log.d(TAG, "Refreshing categories")
        loadCategories()
    }

    fun retryLoadCategories() {
        Log.d(TAG, "Retrying load categories")
        clearError()
        loadCategories()
    }

    fun clearError() {
        Log.d(TAG, "Clearing error message")
        _uiState.update { it.copy(errorMessage = null) }
    }

    // Helper method to get category by slug
    fun getCategoryBySlug(slug: String): Category? {
        return _uiState.value.categories.find { it.slug == slug }
    }

    // Helper method to check if category is selected
    fun isCategorySelected(category: Category): Boolean {
        return _uiState.value.selectedCategory?.slug == category.slug
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "CategoryViewModel cleared")
    }
}