package com.royalcoffee.pos.ui.customer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.entity.CustomerEntity
import com.royalcoffee.pos.data.local.repository.CustomerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * CustomerViewModel manages UI state for customer directory and search.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CustomerViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    val customers: StateFlow<List<CustomerEntity>> = _searchText
        .flatMapLatest { query ->
            customerRepository.searchCustomers(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    var customerError by mutableStateOf<String?>(null)
        private set

    fun updateSearch(query: String) {
        _searchText.value = query
    }

    fun clearError() {
        customerError = null
    }

    fun addCustomer(
        name: String,
        phoneNumber: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = customerRepository.addCustomer(name, phoneNumber)
            if (result.isSuccess) {
                customerError = null
                onSuccess()
            } else {
                customerError = result.exceptionOrNull()?.message ?: "Failed to add customer."
            }
        }
    }

    fun updateCustomer(
        customerId: Long,
        name: String,
        phoneNumber: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = customerRepository.updateCustomer(customerId, name, phoneNumber)
            if (result.isSuccess) {
                customerError = null
                onSuccess()
            } else {
                customerError = result.exceptionOrNull()?.message ?: "Failed to update customer."
            }
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            val result = customerRepository.deleteCustomer(customer)
            if (result.isFailure) {
                customerError = result.exceptionOrNull()?.message ?: "Failed to delete customer."
            }
        }
    }
}

/**
 * CustomerViewModelFactory creates CustomerViewModel instances.
 */
class CustomerViewModelFactory(
    private val customerRepository: CustomerRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerViewModel::class.java)) {
            return CustomerViewModel(customerRepository = customerRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
