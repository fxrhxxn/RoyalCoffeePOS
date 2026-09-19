package com.royalcoffee.pos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.entity.BusinessEntity
import com.royalcoffee.pos.data.local.entity.UserEntity
import com.royalcoffee.pos.data.local.repository.BusinessRepository
import com.royalcoffee.pos.data.local.repository.UserRepository
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.util.HashUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val userRepository: UserRepository,
    private val businessRepository: BusinessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun register(
        fullName: String,
        username: String,
        pin: String,
        businessName: String,
        address: String,
        phone: String,
        brn: String,
        vat: String,
        country: String,
        email: String
    ) {
        viewModelScope.launch {
            _uiState.value = RegistrationUiState.Loading
            try {
                // 1. Create Business Info (Strictly trimmed)
                val business = BusinessEntity(
                    businessName = businessName.trim(),
                    address = address.trim(),
                    phoneNumber = phone.trim(),
                    brn = brn.trim(),
                    vat = vat.trim(),
                    country = country.trim(),
                    email = email.trim()
                )
                businessRepository.updateBusinessInfo(business)

                // 2. Create Admin User (Strictly trimmed)
                val admin = UserEntity(
                    fullName = fullName.trim(),
                    username = username.lowercase().trim(),
                    hashedPin = HashUtils.sha256(pin.trim()),
                    role = UserRole.ADMIN
                )
                userRepository.insertUser(admin)

                _uiState.value = RegistrationUiState.Success
            } catch (e: Exception) {
                _uiState.value = RegistrationUiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegistrationUiState.Idle
    }
}

sealed class RegistrationUiState {
    object Idle : RegistrationUiState()
    object Loading : RegistrationUiState()
    object Success : RegistrationUiState()
    data class Error(val message: String) : RegistrationUiState()
}

class RegistrationViewModelFactory(
    private val userRepository: UserRepository,
    private val businessRepository: BusinessRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            return RegistrationViewModel(userRepository, businessRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
