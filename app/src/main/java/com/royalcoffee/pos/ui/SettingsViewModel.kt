package com.royalcoffee.pos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.entity.BusinessEntity
import com.royalcoffee.pos.data.local.entity.UserEntity
import com.royalcoffee.pos.data.local.repository.BusinessRepository
import com.royalcoffee.pos.data.local.repository.UserRepository
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.util.HashUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val businessRepository: BusinessRepository
) : ViewModel() {

    val allUsers: StateFlow<List<UserEntity>> = userRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessInfo: StateFlow<BusinessEntity?> = businessRepository.getBusinessInfo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun createUser(fullName: String, username: String, pin: String, role: UserRole) {
        viewModelScope.launch {
            val user = UserEntity(
                fullName = fullName,
                username = username.lowercase().trim(),
                hashedPin = HashUtils.sha256(pin),
                role = role
            )
            userRepository.insertUser(user)
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            userRepository.deleteUser(user)
        }
    }

    fun updateBusinessInfo(business: BusinessEntity) {
        viewModelScope.launch {
            businessRepository.updateBusinessInfo(business)
        }
    }
}

class SettingsViewModelFactory(
    private val userRepository: UserRepository,
    private val businessRepository: BusinessRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(userRepository, businessRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
