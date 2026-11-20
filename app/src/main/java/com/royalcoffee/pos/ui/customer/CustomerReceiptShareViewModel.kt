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
 * ViewModel responsible solely for customer selection and quick-saving during WhatsApp receipt sharing.
 * Maintains independent search state from CustomerManagementScreen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CustomerReceiptShareViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val customers: StateFlow<List<CustomerEntity>> = _searchQuery
        .flatMapLatest { query ->
            customerRepository.searchCustomers(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    var shareError by mutableStateOf<String?>(null)
        private set

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun reset() {
        _searchQuery.value = ""
        shareError = null
    }

    fun clearError() {
        shareError = null
    }

    fun saveCustomerAndShare(
        name: String,
        phoneNumber: String,
        onSuccess: (CustomerEntity) -> Unit
    ) {
        viewModelScope.launch {
            val result = customerRepository.addCustomer(name, phoneNumber)
            if (result.isSuccess) {
                val createdId = result.getOrNull()
                val createdCustomer = createdId?.let { customerRepository.getCustomerById(it) }
                if (createdCustomer != null) {
                    shareError = null
                    onSuccess(createdCustomer)
                } else {
                    shareError = "Customer saved, but details could not be loaded."
                }
            } else {
                shareError = result.exceptionOrNull()?.message ?: "Failed to save customer."
            }
        }
    }
}

class CustomerReceiptShareViewModelFactory(
    private val customerRepository: CustomerRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerReceiptShareViewModel::class.java)) {
            return CustomerReceiptShareViewModel(customerRepository = customerRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
