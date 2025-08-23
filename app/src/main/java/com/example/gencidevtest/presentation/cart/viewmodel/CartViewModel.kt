package com.example.gencidevtest.presentation.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val addToCartMessage: String? = null
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartsUseCase: GetCartsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        loadCarts()
    }

    fun loadCarts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            getCartsUseCase()
                .onSuccess { carts ->
                    _uiState.update {
                        it.copy(
                            carts = carts,
                            isLoading = false
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to load carts"
                        )
                    }
                }
        }
    }

    fun addToCart(productId: Int, quantity: Int = 1) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isAddingToCart = true, addToCartMessage = null)
            }

            // Use a default user ID of 1 since DummyJSON accepts any user ID
            // In a real app, you would get this from the current user session
            val userId = 1 // Default user ID for testing

            addToCartUseCase(userId, productId, quantity)
                .onSuccess { cart ->
                    _uiState.update {
                        it.copy(
                            isAddingToCart = false,
                            addToCartMessage = "Product added to cart successfully! Cart ID: ${cart.id}"
                        )
                    }
                    // Clear message after 3 seconds
                    kotlinx.coroutines.delay(3000)
                    clearMessages()
                }
                .onFailure { exception ->
                    val errorMsg = when {
                        exception.message?.contains("400") == true ->
                            "Invalid product ID or request format"
                        exception.message?.contains("404") == true ->
                            "Product not found"
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
}