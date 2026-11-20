package com.royalcoffee.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.royalcoffee.pos.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY customer_name COLLATE NOCASE ASC")
    fun observeAllCustomers(): Flow<List<CustomerEntity>>

    @Query(
        """
        SELECT * FROM customers 
        WHERE customer_name LIKE '%' || :query || '%' 
           OR phone_number LIKE '%' || :query || '%' 
        ORDER BY customer_name COLLATE NOCASE ASC
        """
    )
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE customer_id = :customerId")
    suspend fun getCustomerById(customerId: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE phone_number = :phoneNumber")
    suspend fun getCustomerByPhone(phoneNumber: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)
}
