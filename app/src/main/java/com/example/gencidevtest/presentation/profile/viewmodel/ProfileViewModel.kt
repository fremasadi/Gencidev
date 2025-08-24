// app/src/main/java/com/example/gencidevtest/presentation/profile/viewmodel/ProfileViewModel.kt
package com.example.gencidevtest.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gencidevtest.domain.model.User
import com.example.gencidevtest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ProfileUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastLoginTime: String? = null,
    val gender: String? = null,
    val allUsers: List<User> = emptyList() // For showing login history if needed
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { user ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isLoading = false
                    )
                }

                // Load additional user details if user is available
                if (user != null) {
                    loadUserDetails(user.id)
                }
            }
        }
    }

    fun loadCurrentUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val currentUser = userRepository.getCurrentUserSync()
                if (currentUser != null) {
                    loadUserDetails(currentUser.id)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No current user found. Please login again."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load profile: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadUserDetails(userId: Int) {
        try {
            userRepository.getUserById(userId).fold(
                onSuccess = { user ->
                    if (user != null) {
                        // Load additional details from Room database
                        // This would include gender, last login time, etc.
                        loadAdditionalUserInfo(userId)
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            errorMessage = "Failed to load user details: ${exception.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = "Error loading user details: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadAdditionalUserInfo(userId: Int) {
        try {
            // Here you could load additional info from Room
            // For now, we'll simulate this with the current user data
            userRepository.getUserById(userId).fold(
                onSuccess = { user ->
                    // Format current date as last login time for demo
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                    val lastLoginFormatted = dateFormat.format(Date())

                    _uiState.update {
                        it.copy(
                            lastLoginTime = lastLoginFormatted,
                            gender = "Not specified", // This would come from Room
                            isLoading = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load additional info: ${exception.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Error loading additional user info: ${e.message}"
                )
            }
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { users ->
                _uiState.update {
                    it.copy(allUsers = users)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun refreshProfile() {
        loadCurrentUserProfile()
    }
}