package com.royalcoffee.pos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.repository.UserRepository
import com.royalcoffee.pos.model.AuthenticatedUser
import com.royalcoffee.pos.util.HashUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, pin: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            val cleanUsername = username.lowercase().trim()
            val cleanPin = pin.trim()
            
            val user = userRepository.getUserByUsername(cleanUsername)
            
            if (user != null && user.hashedPin == HashUtils.sha256(cleanPin)) {
                _uiState.value = LoginUiState.Success(AuthenticatedUser(user.id, user.fullName, user.role))
            } else {
                _uiState.value = LoginUiState.Error("Invalid username or PIN")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: AuthenticatedUser) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModelFactory(
    private val userRepository: UserRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

