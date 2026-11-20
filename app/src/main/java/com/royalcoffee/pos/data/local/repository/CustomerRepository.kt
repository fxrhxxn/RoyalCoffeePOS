package com.royalcoffee.pos.data.local.repository

import com.royalcoffee.pos.data.local.dao.CustomerDao
import com.royalcoffee.pos.data.local.entity.CustomerEntity
import com.royalcoffee.pos.util.CustomerPhoneUtils
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for customer business logic and database access.
 */
class CustomerRepository(
    private val customerDao: CustomerDao
) {

    /**
     * Reactive list of all customers ordered by name.
     */
    val allCustomers: Flow<List<CustomerEntity>> =
        customerDao.observeAllCustomers()

    /**
     * Search customers by name or phone fragment.
     */
    fun searchCustomers(query: String): Flow<List<CustomerEntity>> {
        val trimmed = query.trim()
        return if (trimmed.isBlank()) {
            customerDao.observeAllCustomers()
        } else {
            customerDao.searchCustomers(trimmed)
        }
    }

    suspend fun getCustomerById(customerId: Long): CustomerEntity? {
        return customerDao.getCustomerById(customerId)
    }

    suspend fun getCustomerByPhone(phoneNumber: String): CustomerEntity? {
        val normalized = CustomerPhoneUtils.normalizeCustomerPhone(phoneNumber) ?: return null
        return customerDao.getCustomerByPhone(normalized)
    }

    /**
     * Adds a new customer after normalizing the phone number and validating uniqueness.
     */
    suspend fun addCustomer(name: String, phoneNumber: String): Result<Long> {
        val cleanedName = name.trim()
        if (cleanedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Customer name cannot be empty."))
        }

        val normalizedPhone = CustomerPhoneUtils.normalizeCustomerPhone(phoneNumber)
            ?: return Result.failure(IllegalArgumentException("Enter a valid phone number."))

        if (customerDao.getCustomerByPhone(normalizedPhone) != null) {
            return Result.failure(IllegalArgumentException("A customer with this phone number already exists."))
        }

        return try {
            val customer = CustomerEntity(
                customerName = cleanedName,
                phoneNumber = normalizedPhone
            )
            val id = customerDao.insertCustomer(customer)
            Result.success(id)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    /**
     * Updates an existing customer, checking phone uniqueness if changed.
     */
    suspend fun updateCustomer(
        customerId: Long,
        name: String,
        phoneNumber: String
    ): Result<Unit> {
        val cleanedName = name.trim()
        if (cleanedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Customer name cannot be empty."))
        }

        val normalizedPhone = CustomerPhoneUtils.normalizeCustomerPhone(phoneNumber)
            ?: return Result.failure(IllegalArgumentException("Enter a valid phone number."))

        val existing = customerDao.getCustomerById(customerId)
            ?: return Result.failure(IllegalArgumentException("Customer not found."))

        if (existing.phoneNumber != normalizedPhone) {
            val duplicate = customerDao.getCustomerByPhone(normalizedPhone)
            if (duplicate != null && duplicate.customerId != customerId) {
                return Result.failure(IllegalArgumentException("A customer with this phone number already exists."))
            }
        }

        return try {
            val updated = existing.copy(
                customerName = cleanedName,
                phoneNumber = normalizedPhone
            )
            customerDao.updateCustomer(updated)
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    /**
     * Deletes a customer.
     */
    suspend fun deleteCustomer(customer: CustomerEntity): Result<Unit> {
        return try {
            customerDao.deleteCustomer(customer)
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
