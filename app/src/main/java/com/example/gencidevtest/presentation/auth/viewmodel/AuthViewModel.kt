package com.example.gencidevtest.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gencidevtest.domain.model.User
import com.example.gencidevtest.domain.usecase.IsLoggedInUseCase
import com.example.gencidevtest.domain.usecase.LoginUseCase
import com.example.gencidevtest.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            isLoggedInUseCase().collect { isLoggedIn ->
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            loginUseCase(username, password)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            isLoggedIn = true
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Login gagal"
                        )
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}