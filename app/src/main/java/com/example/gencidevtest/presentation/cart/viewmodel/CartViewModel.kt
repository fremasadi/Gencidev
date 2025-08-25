// app/src/main/java/com/example/gencidevtest/presentation/cart/viewmodel/CartViewModel.kt
package com.example.gencidevtest.presentation.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gencidevtest.data.util.NetworkConnectionObserver
import com.example.gencidevtest.domain.model.Cart
import com.example.gencidevtest.domain.usecase.AddToCartUseCase
import com.example.gencidevtest.domain.usecase.GetCartsUseCase
import com.example.gencidevtest.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val carts: List<Cart> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAddingToCart: Boolean = false,
    val addToCartMessage: String? = null,
    val isRefreshing: Boolean = false,  // Added for pull-to-refresh
    val currentUserId: Int? = null,     // Track current user
    val isOffline: Boolean = false      // Added offline state
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartsUseCase: GetCartsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val networkConnectionObserver: NetworkConnectionObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
        observeNetworkConnection()
        loadCarts()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update { currentState ->
                    val newUserId = user?.id
                    if (currentState.currentUserId != newUserId) {
                        // User changed, reload carts
                        if (newUserId != null) {
                            loadCarts()
                        }
                        currentState.copy(currentUserId = newUserId)
                    } else {
                        currentState.copy(currentUserId = newUserId)
                    }
                }
            }
        }
    }

    private fun observeNetworkConnection() {
        viewModelScope.launch {
            networkConnectionObserver.observeNetworkConnection()
                .collect { isConnected ->
                    _uiState.update { it.copy(isOffline = !isConnected) }

//                    // Auto-refresh when coming back online if we have cached data
//                    if (isConnected && _uiState.value.carts.isNotEmpty()) {
//                        // Don't auto-refresh to avoid interrupting user, let them pull-to-refresh
//                        // But you can uncomment below if you want auto-refresh
//                        // refreshCarts()
//                    }
                }
        }
    }

    fun loadCarts() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !it.isRefreshing, // Don't show loading if refreshing
                    errorMessage = null
                )
            }

            getCartsUseCase()
                .onSuccess { carts ->
                    _uiState.update {
                        it.copy(
                            carts = carts,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = exception.message ?: "Failed to load carts"
                        )
                    }
                }
        }
    }

    fun refreshCarts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            loadCarts()
        }
    }

    fun addToCart(productId: Int, quantity: Int = 1) {
        viewModelScope.launch {
            // Check if offline first
            if (_uiState.value.isOffline) {
                _uiState.update {
                    it.copy(
                        addToCartMessage = "Cannot add to cart while offline. Please connect to internet."
                    )
                }
                // Clear error message after 5 seconds
                kotlinx.coroutines.delay(5000)
                clearMessages()
                return@launch
            }

            _uiState.update {
                it.copy(isAddingToCart = true, addToCartMessage = null)
            }

            // Use current user ID if available, otherwise default to 1
            val userId = _uiState.value.currentUserId ?: 1

            addToCartUseCase(userId, productId, quantity)
                .onSuccess { cart ->
                    _uiState.update {
                        it.copy(
                            isAddingToCart = false,
                            addToCartMessage = "Product added to cart successfully! Cart ID: ${cart.id}"
                        )
                    }

                    loadCarts()

                    // Clear success message after 3 seconds
                    kotlinx.coroutines.delay(3000)
                    clearMessages()
                }
                .onFailure { exception ->
                    val errorMsg = when {
                        exception.message?.contains("400") == true ->
                            "Invalid product ID or request format"
                        exception.message?.contains("404") == true ->
                            "Product not found"
                        exception.message?.contains("network", ignoreCase = true) == true ->
                            "Network error. Check your connection."
                        else ->
                            "Failed to add product to cart: ${exception.message}"
                    }

                    _uiState.update {
                        it.copy(
                            isAddingToCart = false,
                            addToCartMessage = errorMsg
                        )
                    }

                    // Clear error message after 5 seconds
                    kotlinx.coroutines.delay(5000)
                    clearMessages()
                }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(errorMessage = null, addToCartMessage = null)
        }
    }

    fun getTotalCartValue(): Double {
        return _uiState.value.carts.sumOf { it.discountedTotal }
    }

    fun getTotalItems(): Int {
        return _uiState.value.carts.sumOf { it.totalQuantity }
    }

    fun getTotalCarts(): Int {
        return _uiState.value.carts.size
    }

    // Helper function to get carts for current user only
    fun getCurrentUserCarts(): List<Cart> {
        val currentUserId = _uiState.value.currentUserId ?: return emptyList()
        return _uiState.value.carts.filter { it.userId == currentUserId }
    }

    // Helper function to get totals for current user only
    fun getCurrentUserTotalValue(): Double {
        return getCurrentUserCarts().sumOf { it.discountedTotal }
    }

    fun getCurrentUserTotalItems(): Int {
        return getCurrentUserCarts().sumOf { it.totalQuantity }
    }
}